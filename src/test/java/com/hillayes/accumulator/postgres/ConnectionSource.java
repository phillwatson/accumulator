package com.hillayes.accumulator.postgres;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ConnectionSource {
    private static PostgreSQLContainer<?> postgres;
    private static PoolingDataSource<?> dataSource;

    public static void init() {
        if ((postgres == null) || (postgres.isRunning())) {
            postgres = new PostgreSQLContainer<>("postgres:18.0-alpine3.21");
            postgres.start();

            dataSource = initDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        }
    }

    public static void close() {
        if ((postgres != null) && (postgres.isRunning())) {
            try {
                dataSource.close();
            } catch (SQLException ignore) {
            }
            postgres.stop();
            postgres = null;
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void withConnection(Consumer<Connection> action) {
        try (Connection con = getConnection()) {
            action.accept(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static PoolingDataSource<?> initDataSource(String connectURI,
                                                    String username,
                                                    String password) {
        ConnectionFactory connectionFactory =
            new DriverManagerConnectionFactory(connectURI, username, password);

        PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, null);

        ObjectPool<PoolableConnection> connectionPool =
            new GenericObjectPool<>(poolableConnectionFactory);

        poolableConnectionFactory.setPool(connectionPool);

        return new PoolingDataSource<>(connectionPool);
    }
}
