import db.User;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.LinkedList;

public class DBInteraction {
    final String URL;
    final String USER;
    final String PASS;
    Connection connection = null;
    Statement statement = null;

    public DBInteraction(String URL, String user, String password) {
        this.URL = URL;
        this.USER = user;
        this.PASS = password;
        connect();
        createWorkerTable();
        createUserTable();
    }

    public void connect() {
        try {
            System.out.println("Trying to connect the database...");
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("You successfully connected to the database!");
        } catch (SQLException e) {
            System.out.println("Failed to make connection to the database. Try to execute the program again.\n" +
                    e.getMessage());
            System.exit(0);
        }
    }

    public void createWorkerTable() {
        try {
            if (!checkTableExistence("workers")) {
                System.out.println("Trying to create the table for data...");
                statement = connection.createStatement();
                String sql = "CREATE TABLE WORKERS" +
                        "(creator VARCHAR(255), " +
                        "id INTEGER not NULL, " +
                        "name VARCHAR(255), " +
                        "coordinatesX INTEGER not NULL, " +
                        "coordinatesY INTEGER not NULL, " +
                        "creationDate VARCHAR(255), " +
                        "salary FLOAT not NULL, " +
                        "startDate VARCHAR(255), " +
                        "endDate VARCHAR(255), " +
                        "position VARCHAR(255), " +
                        "organizationAnnualTurnover INTEGER not NULL, " +
                        "organizationType VARCHAR(255) not NULL, " +
                        "addressZipCode VARCHAR(255), " +
                        "locationX INTEGER not NULL, " +
                        "locationY INTEGER not NULL, " +
                        "locationName VARCHAR(255))";
                statement.executeUpdate(sql);
                statement.close();
                System.out.println("The table created successfully!");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void createUserTable() {
        try {
            if (!checkTableExistence("users")) {
                System.out.println("Trying to create the table for users...");
                statement = connection.createStatement();
                String sql = "CREATE TABLE USERS" +
                        "(logins VARCHAR(255), " +
                        "passwords VARCHAR(255))";
                statement.executeUpdate(sql);
                statement.close();
                System.out.println("The table created successfully!");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean checkTableExistence(String tableName) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName,
                    new String[]{"TABLE"});
            System.out.println("Checking existence of the " + tableName.toUpperCase() + " table...");

            while (rs.next()) {
                if (rs.getString("TABLE_NAME").equals(tableName)) {
                    System.out.println("The " + tableName.toUpperCase() + " table exists.");
                    return true;
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    public String validateUser(User user) {
        String result = "Sorry, there is no such user. Please, register.";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT logins, passwords from USERS")) {

            if (user.isOldUser()) {
                while (rs.next() && !user.isAuthorized()) {
                    if (user.getUserName().equals(rs.getString("logins"))) {
                        Encoder encoder = new Encoder();
                        byte[] hash = encoder.doHash(user.getPassword());
                        String pwd = new String(hash, StandardCharsets.UTF_8);
                        if (pwd.equals(rs.getString("passwords"))) {
                            user.setAuthorized(true);
                            result = "User " + user.getUserName() + " successfully logged in.";
                        }
                    }
                }
            } else {
                result = register(user);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public void removeTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE WORKERS");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String register(User user) {
        String result = "";
        Encoder encoder = new Encoder();
        byte[] hashPwd = encoder.doHash(user.getPassword());
        String pwd = new String(hashPwd, StandardCharsets.UTF_8);

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT logins, passwords from USERS")) {
            LinkedList<String> userNames = new LinkedList<>();
            while (rs.next()) {
                userNames.add(rs.getString("logins"));
            }

            if (!userNames.contains(user.getUserName())) {
                statement.executeUpdate("INSERT INTO USERS VALUES('" + user.getUserName() + "', '" + pwd + "')");
                result = "New user created and authorized successfully!";
                user.setAuthorized(true);
            } else
                result = "Sorry, such user has already exists.";

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public Connection getConnection() {
        return connection;
    }
}