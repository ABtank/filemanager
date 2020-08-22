package ru.abramov.filemanager.netty.server;

import javax.sql.DataSource;
import java.sql.*;

public class SqlClient {

    private static Connection connection;

    private static Statement statement;

    public SqlClient(DataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
        statement = connection.createStatement();
        createTableIfNotExists(connection);
    }


    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getMySQLConnection() throws ClassNotFoundException, SQLException {
        String hostname = "localhost";
        String dbName = "mysql_chat";
        String user = "root";
        String password = "Z4Vesrfd1.";

        return getMySQLConnection(hostname, dbName, user, password);
    }

    private static Connection getMySQLConnection(String hostname, String dbName, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String connectionURL = "jdbc:mysql://" + hostname + ":3306/" + dbName+"?&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
        Connection connection = DriverManager.getConnection(connectionURL, user, password);
        statement = connection.createStatement();
        createTableIfNotExists(connection);
        return connection;
    }


    synchronized static void disConnect() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            // connection close failed.
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращать никнейм по логину и парлю
     */
    synchronized static String getNickname(String login, String password) {
        String query = String.format("select nickname from users where login='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {// получаем запрос в ResaltSet
            if (set.next()) { //из полученного запроса вытаскиваем nickName
                return set.getString(1);// нумерация колонок в Sql начинается с 1.
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Смена никнейма
     */
    synchronized static void setReNickname(String nickname, String login, String password) {
        try {
            String reNickname = String.format("UPDATE users SET'nickname'='%s' where login='%s' and password='%s';", nickname, login, password);
            statement.execute(reNickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Регистрация пользователя
     */
    synchronized static void setNewClient(String nickname, String login, String password) {
        try {
            String register = String.format("INSERT INTO users ('login', 'password','nickname') VALUES ('%s','%s','%s'); ", login, password, nickname);
            statement.execute(register);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static boolean getNotExistsClient(String nickname, String login) {
        String query = String.format("select nickname, login from users where nickname='%s' or login='%s'", nickname, login);
        try (ResultSet set = statement.executeQuery(query)) {// получаем запрос в ResultSet
            if (!set.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static void createTableIfNotExists(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("create table if not exists users (\n" +
                    "\tlogin varchar(25),\n" +
                    "    password  varchar(25),\n" +
                    "     nickname  varchar(25)\n" +
                    ");");
        }
    }
}
