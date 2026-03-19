package ch.epfl.gsn.data;

import ch.epfl.gsn.config.GsnConf;
import ch.epfl.gsn.config.StorageConf;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.C3P0Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.beans.PropertyVetoException;

public class DataStore {

    private static final Logger log = LoggerFactory.getLogger(DataStore.class);
    private final GsnConf gsn;
    private final DataSource dataSource;

    public DataStore(GsnConf gsn) {
        this.gsn = gsn;
        this.dataSource = createDataSource("gsn", gsn.storageConf());
    }

    /**
     * Gets a connection from the pool. 
     * In Java, this replaces the .withSession logic.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private DataSource createDataSource(String name, StorageConf store) {
        // Check if a datasource with this URL already exists in C3P0 registry
        DataSource ds = C3P0Registry.pooledDataSourceByName(store.url());
        if (ds != null) {
            return ds;
        }

        log.debug("Creating a new datasource: {}", store);
        ComboPooledDataSource cpds = new ComboPooledDataSource(name);
        try {
            cpds.setDriverClass(store.driver());
        } catch (PropertyVetoException e) {
            log.error("Invalid JDBC driver: " + store.driver(), e);
            throw new RuntimeException(e);
        }

        cpds.setJdbcUrl(store.url());
        cpds.setUser(store.user());
        cpds.setPassword(store.pass());
        
        // Pool Settings
        cpds.setMinPoolSize(1);
        cpds.setAcquireIncrement(1);
        cpds.setMaxPoolSize(5);

        return cpds;
    }

    public void close() {
        DataSource ds = C3P0Registry.pooledDataSourceByName(gsn.storageConf().url());
        if (ds instanceof ComboPooledDataSource) {
            ((ComboPooledDataSource) ds).close();
            log.info("Closed C3P0 data source: {}", gsn.storageConf());
        }
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
}