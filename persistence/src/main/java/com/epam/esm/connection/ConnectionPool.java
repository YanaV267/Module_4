package com.epam.esm.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String POOL_PROPERTY_FILE = "pool";
    private static final String POOL_SIZE_PROPERTY = "size";
    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int POOL_SIZE;
    private static BlockingDeque<ProxyConnection> freeConnections;
    private static BlockingDeque<ProxyConnection> takenConnections;
    @Autowired
    private ConnectionPool connectionPool;

    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(POOL_PROPERTY_FILE);
        String poolSize;
        if (resourceBundle.containsKey(POOL_SIZE_PROPERTY)) {
            poolSize = resourceBundle.getString(POOL_SIZE_PROPERTY);
            POOL_SIZE = Integer.parseInt(poolSize);
        } else {
            LOGGER.warn("Error of retrieving pool size value: pool size will be initialised by a default value");
            POOL_SIZE = DEFAULT_POOL_SIZE;
        }
    }

    @PostConstruct
    public void initPool() {
        freeConnections = new LinkedBlockingDeque<>(POOL_SIZE);
        takenConnections = new LinkedBlockingDeque<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                Connection connection = ConnectionFactory.getConnection();
                ProxyConnection proxyConnection = new ProxyConnection(connectionPool, connection);
                freeConnections.add(proxyConnection);
            } catch (SQLException exception) {
                LOGGER.error("Error has occurred while creating connection: " + exception);
            }
        }
        if (freeConnections.isEmpty()) {
            LOGGER.fatal("Error: no connections were created");
            throw new RuntimeException("Error: no connections were created");
        }
        LOGGER.info("{} connections were created", freeConnections.size());
    }

    public Connection getConnection() {
        if (freeConnections == null || takenConnections == null) {
            initPool();
        }
        ProxyConnection connection = null;
        try {
            connection = freeConnections.take();
            takenConnections.put(connection);
        } catch (InterruptedException exception) {
            LOGGER.error("Error has occurred while getting connection: " + exception.getMessage());
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        if (!(connection instanceof ProxyConnection)) {
            return false;
        }
        try {
            if (takenConnections.remove(connection)) {
                freeConnections.put((ProxyConnection) connection);
                return true;
            }
        } catch (InterruptedException exception) {
            LOGGER.error("Error has occurred while releasing connection: " + exception.getMessage());
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public void destroyPool() {
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            try {
                freeConnections.take().reallyClose();
            } catch (InterruptedException | SQLException exception) {
                LOGGER.error("Error has occurred while destroying pool: " + exception.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        deregisterDrivers();
    }

    private void deregisterDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException exception) {
                LOGGER.error("Error has occurred while deregistering drivers: " + exception.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}

