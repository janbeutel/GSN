/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/ch/epfl/gsn/storage/hibernate/HibernateStorage.java
*
* @author Timotee Maret
*
*/

package ch.epfl.gsn.storage.hibernate;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.storage.DataEnumeratorIF;
import ch.epfl.gsn.utils.GSNRuntimeException;

import org.slf4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.io.Serializable;
import java.util.*;

public class HibernateStorage implements VirtualSensorStorage {

    private static final transient Logger logger = LoggerFactory.getLogger(HibernateStorage.class);

    private SessionFactory sf;

    private String identifier;

    private DataField[] structure;

    private static final int PAGE_SIZE = 1000;

    /**
     * Creates a new instance of HibernateStorage.
     * 
     * @param dbInfo     the DBConnectionInfo object containing the database
     *                   connection information
     * @param identifier the identifier for the HibernateStorage instance
     * @param structure  an array of DataField objects representing the structure of
     *                   the storage
     * @param unique     a boolean value indicating whether the storage should
     *                   enforce uniqueness constraints
     * @return a new instance of HibernateStorage
     */
    public static HibernateStorage newInstance(DBConnectionInfo dbInfo, String identifier, DataField[] structure,
            boolean unique) {
        try {
            return new HibernateStorage(dbInfo, identifier, structure, unique);
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private HibernateStorage(DBConnectionInfo dbInfo, String identifier, DataField[] structure, boolean unique)
            throws RuntimeException {
        String em = generateEntityMapping(identifier, structure, unique);
        this.sf = HibernateUtil.getSessionFactory(dbInfo.getDriverClass(), dbInfo.getUrl(), dbInfo.getUserName(),
                dbInfo.getPassword(), em);
        if (this.sf == null) {
            throw new RuntimeException("Unable to instanciate the Storage for:" + identifier);
        }
        this.identifier = identifier.toLowerCase();
        this.structure = structure;
    }

    public boolean init() {
        return true;
    }

    /**
     * Saves a StreamElement object to the database and returns the generated
     * identifier.
     *
     * @param se the StreamElement object to be saved
     * @return the generated identifier of the saved object
     * @throws GSNRuntimeException if an error occurs while saving the object
     */
    public Serializable saveStreamElement(StreamElement se) throws GSNRuntimeException {
        // Create the dynamic map
        try {
            return storeElement(se2dm(se));
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error occurred on inserting data to the database, an stream element dropped due to: ")
                    .append(e.getMessage())
                    .append(". (Stream element: ")
                    .append(se.toString())
                    .append(")");
            logger.warn(sb.toString());
            throw new GSNRuntimeException(e.getMessage());
        } catch (RuntimeException e) {
            throw new GSNRuntimeException(e.getMessage());
        }
    }

    /**
     * Retrieves a StreamElement from the database based on the provided primary
     * key.
     *
     * @param pk the primary key of the StreamElement to retrieve
     * @return the retrieved StreamElement object
     * @throws GSNRuntimeException if an error occurs during the retrieval process
     */
    public StreamElement getStreamElement(Serializable pk) throws GSNRuntimeException {
        Transaction tx = null;
        try {
            Session session = sf.getCurrentSession();
            tx = session.beginTransaction();
            Map<String, Serializable> dm = (Map<String, Serializable>) session.get(identifier, pk);
            tx.commit();
            return dm2se(dm);
        } catch (RuntimeException e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (RuntimeException ex) {
                logger.error("Couldn't roll back transaction.");
            }
            throw e;
        }
    }

    /**
     * Returns the count of stream elements in the storage.
     *
     * @return The count of stream elements.
     * @throws GSNRuntimeException if an error occurs during the operation.
     */
    public long countStreamElement() throws GSNRuntimeException {
        Transaction tx = null;
        try {
            Session session = sf.getCurrentSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(identifier);
            criteria.setProjection(Projections.rowCount());
            List count = criteria.list();
            tx.commit();
            return (Long) count.get(0);

        } catch (RuntimeException e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (RuntimeException ex) {
                logger.error("Couldn't roll back transaction.");
            }
            throw e;
        }
    }

    /**
     * Retrieves a stream of elements from the storage.
     *
     * @param pageSize   the number of elements to retrieve per page
     * @param order      the order in which the elements should be retrieved
     * @param crits      an array of criteria to filter the elements
     * @param maxResults the maximum number of elements to retrieve
     * @return a DataEnumeratorIF object representing the stream of elements
     * @throws GSNRuntimeException if an error occurs while retrieving the elements
     */
    public DataEnumeratorIF getStreamElements(int pageSize, Order order, Criterion[] crits, int maxResults)
            throws GSNRuntimeException {
        return new PaginatedDataEnumerator(pageSize, order, crits, maxResults);
    }

    /**
     * Retrieves a stream of elements from the storage.
     *
     * @param pageSize the number of elements to retrieve per page
     * @param order    the order in which the elements should be retrieved
     * @param crits    the criteria to filter the elements
     * @return a DataEnumeratorIF object representing the stream of elements
     * @throws GSNRuntimeException if an error occurs during the retrieval process
     */
    public DataEnumeratorIF getStreamElements(int pageSize, Order order, Criterion[] crits) throws GSNRuntimeException {
        return getStreamElements(pageSize, order, crits, -1);
    }

    /**
     * Stores an element in the database using Hibernate.
     *
     * @param dm A Map representing the data to be stored in the database.
     * @return A Serializable representing the primary key of the stored element.
     * @throws RuntimeException If an error occurs during the transaction, and the
     *                          transaction cannot be rolled back.
     */
    private Serializable storeElement(Map dm) {
        Transaction tx = null;
        try {
            Session session = sf.getCurrentSession();
            tx = session.beginTransaction();
            Serializable pk = session.save(identifier, dm);
            tx.commit();
            return pk;
        } catch (RuntimeException e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (RuntimeException ex) {
                logger.error("Couldn't roll back transaction.");
            }
            throw e;
        }
    }

    /**
     * This method is called by the garbage collector when the object is no longer
     * reachable.
     * It is used to perform any necessary cleanup operations before the object is
     * destroyed.
     * In this case, it closes the Hibernate session factory if it is not null.
     * 
     * @throws Throwable if an error occurs during finalization
     */
    protected void finalize() throws Throwable {
        try {
            if (sf != null) {
                HibernateUtil.closeSessionFactory(sf);
            }
        } finally {
            super.finalize();
        }
    }

    //

    /**
     * @param gsnType
     * @return
     */
    public static String convertGSNTypeToLocalType(DataField gsnType) {
        switch (gsnType.getDataTypeID()) {
            case DataTypes.VARCHAR:
            case DataTypes.CHAR:
                return "string";
            case DataTypes.BIGINT:
            case DataTypes.TIME:
                return "long";
            case DataTypes.INTEGER:
                return "integer";
            case DataTypes.SMALLINT:
                return "short";
            case DataTypes.TINYINT:
                return "byte";
            case DataTypes.DOUBLE:
                return "double";
            case DataTypes.FLOAT:
                return "float";
            case DataTypes.BINARY:
                return "binary";
        }
        return null;
    }

    /**
     * Converts a map of data fields to a StreamElement object.
     * 
     * @param dm The map of data fields.
     * @return The converted StreamElement object.
     */
    private StreamElement dm2se(Map<String, Serializable> dm) {
        ArrayList<Serializable> data = new ArrayList<Serializable>();
        long timed = (Long) dm.get("timed");
        for (DataField df : structure) {
            if (!"timed".equalsIgnoreCase(df.getName())) {
                data.add(dm.get(df.getName()));
            }
        }
        return new StreamElement(structure, data.toArray(new Serializable[] { data.size() }), timed);
    }

    /**
     * Converts a StreamElement object to a map of key-value pairs.
     * The "timed" field is included as a key-value pair in the map.
     * All other fields in the StreamElement object are also included as key-value
     * pairs in the map.
     * 
     * @param se The StreamElement object to be converted.
     * @return A map of key-value pairs representing the StreamElement object.
     */
    private Map<String, Serializable> se2dm(StreamElement se) {
        Map<String, Serializable> dm = new HashMap<String, Serializable>();
        dm.put("timed", se.getTimeStamp());
        for (String fieldName : se.getFieldNames()) {
            if (!"timed".equalsIgnoreCase(fieldName)) {
                dm.put(fieldName, se.getData(fieldName));
            }
        }
        return dm;
    }

    /**
     * Create the Hibernate mapping configuration file for the specified virtual
     * sensor, according to the structure.
     * The <code>pk</code> and <code>timed</code> are added by default to the
     * mapping. Moreover, an index on
     * the <code>timed</code> field is created. Finally, an optional
     * <code>UNIQUE</code> clause is added to the
     * <code>timed</code> column, iff the parameter <code>unique</code> is set to
     * <code>true</code>.
     *
     * @param identifier
     * @param structure
     * @param unique
     * @return return a StringBuilder containing the hibernate mapping configuration
     */
    private static String generateEntityMapping(String identifier, DataField[] structure, boolean unique) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
        sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
        sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
        sb.append("<hibernate-mapping>\n");
        sb.append("<class entity-name=\"")
                .append(identifier.toLowerCase())
                .append("\" table=\"")
                .append(identifier.toLowerCase())
                .append("\">\n");
        // sb.append("<cache usage=\"read-only\"/>");
        // PK field
        sb.append("<id type=\"long\" column=\"PK\" name=\"pk\" >\n");
        sb.append("<generator class=\"native\"/>\n");
        sb.append("</id>\n");
        // TIMED field and it index
        sb.append("<property name=\"timed\"  column=\"TIMED\"  type=\"long\"");
        sb.append(" index=\"")
                .append(identifier.toUpperCase())
                .append("_TIMED_INDEX\"");
        sb.append(" not-null=\"true\"");
        if (unique) {
            sb.append(" unique=\"true\"");
        }
        sb.append(" />\n");
        // OTHER DATA FIELDS
        for (DataField df : structure) {
            if (!"timed".equalsIgnoreCase(df.getName())) {
                sb.append("<property name=\"")
                        .append(df.getName())
                        .append("\" column=\"")
                        .append(df.getName().toUpperCase())
                        .append("\" type=\"")
                        .append(convertGSNTypeToLocalType(df))
                        .append("\"/>\n");
            }
        }
        sb.append("</class>\n");
        sb.append("</hibernate-mapping>\n");
        return sb.toString();
    }

    //

    private class PaginatedDataEnumerator implements DataEnumeratorIF {

        /** The global max number of result returned */
        private int maxResults;

        private int currentPage;

        private int pageSize;

        private Order order;

        private Criterion[] crits;

        private Iterator<Map<String, Serializable>> pci;

        private boolean closed;

        private PaginatedDataEnumerator(int pageSize, Order order, Criterion[] crits, int maxResults) {
            this.maxResults = maxResults;
            this.pageSize = pageSize;
            this.order = order;
            this.crits = crits;
            currentPage = 0;
            pci = null; // page content iterator
            if (maxResults == 0) {
                close();
            }
            hasMoreElements();
        }

        /**
         * This method checks if there is one or more
         * {@link ch.epfl.gsn.beans.StreamElement} available in the DataEnumerator.
         * If the current page is empty, it tries to load the next page.
         * 
         * @return
         */
        public boolean hasMoreElements() {

            // Check if the DataEnumerator is closed
            if (closed) {
                return false;
            }

            // Check if there is still data in the current pageContent
            if (pci != null && pci.hasNext()) {
                return true;
            }

            // Compute the next number of elements to fetch
            int offset = currentPage * pageSize;
            int mr = pageSize;
            if (maxResults > 0) {
                int remaining = maxResults - offset;
                mr = remaining > 0 ? remaining >= pageSize ? pageSize : remaining % pageSize : 0;
            }

            // Try to load the next page
            pci = null;
            Transaction tx = null;
            try {
                Session session = sf.getCurrentSession();
                tx = session.beginTransaction();
                //
                Criteria criteria = session.createCriteria(identifier);
                for (Criterion criterion : crits) {
                    criteria.add(criterion);
                }
                criteria.addOrder(order);
                criteria.setCacheable(true);
                criteria.setReadOnly(true);
                criteria.setFirstResult(offset);
                criteria.setMaxResults(mr);
                //
                pci = criteria.list().iterator();
                tx.commit();
                currentPage++;

            } catch (RuntimeException e) {
                try {
                    if (tx != null) {
                        tx.rollback();
                    }
                } catch (RuntimeException ex) {
                    logger.error("Couldn't roll back transaction.");
                }
                throw e;
            }
            if (pci != null && pci.hasNext()) {
                return true;
            } else {
                close();
                return false;
            }
        }

        /**
         * Retrieves the next StreamElement from the DataEnumerator.
         *
         * @return the next StreamElement
         * @throws IndexOutOfBoundsException if there are no more StreamElements or if
         *                                   the DataEnumerator is closed
         */
        public StreamElement nextElement() throws RuntimeException {
            if (hasMoreElements()) {
                return dm2se(pci.next());
            } else {
                throw new IndexOutOfBoundsException("The DataEnumerator has no more StreamElement or is closed.");
            }
        }

        public void close() {
            if (!closed) {
                closed = true;
            }

        }
    }
}
