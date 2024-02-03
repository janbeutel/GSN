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
* File: src/ch/epfl/gsn/storage/DataSources.java
*
* @author Timotee Maret
*
*/

package ch.epfl.gsn.storage;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.storage.hibernate.DBConnectionInfo;
import ch.epfl.gsn.utils.jndi.GSNContext;

import org.slf4j.Logger;

import javax.naming.NamingException;

public class DataSources {

    private static final transient Logger logger = LoggerFactory.getLogger(DataSources.class);

    /**
     * Retrieves or creates a BasicDataSource object based on the provided
     * DBConnectionInfo.
     * If a BasicDataSource object already exists for the given DBConnectionInfo, it
     * is retrieved.
     * Otherwise, a new BasicDataSource object is created and configured with the
     * provided DBConnectionInfo.
     * The created BasicDataSource object is then bound to the GSNContext using the
     * hash code of the DBConnectionInfo as the lookup key.
     * 
     * @param dci The DBConnectionInfo object containing the connection details.
     * @return The BasicDataSource object associated with the provided
     *         DBConnectionInfo.
     */
    public static BasicDataSource getDataSource(DBConnectionInfo dci) {
        BasicDataSource ds = null;
        try {
            ds = (BasicDataSource) GSNContext.getMainContext().lookup(Integer.toString(dci.hashCode()));
            if (ds == null) {
                ds = new BasicDataSource();
                ds.setDriverClassName(dci.getDriverClass());
                ds.setUsername(dci.getUserName());
                ds.setPassword(dci.getPassword());
                ds.setUrl(dci.getUrl());
                // ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                // ds.setAccessToUnderlyingConnectionAllowed(true);
                GSNContext.getMainContext().bind(Integer.toString(dci.hashCode()), ds);
                logger.info("Created a DataSource to: " + ds.getUrl());
            }
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }
        return ds;
    }
}
