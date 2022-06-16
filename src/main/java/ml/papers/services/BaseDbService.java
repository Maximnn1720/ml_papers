package ml.papers.services;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDbService {
//    @Property(name = "db.user-name")
//    protected String userName;
//
//    @Property(name = "db.password")
//    protected String password;
//
//    @Property(name = "db.url")
//    protected String url;

    protected Connection connection;

    public BaseDbService(String userName, String password, String url) {
        try {
            connection = DriverManager.getConnection(url, userName, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
