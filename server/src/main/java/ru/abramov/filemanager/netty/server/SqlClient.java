package ru.abramov.filemanager.netty.server;

import java.sql.*;

public class SqlClient {

    private static Connection connection;

    private static Statement statement;


    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/users.db");
            statement = connection.createStatement();
            System.out.println("DB connected");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
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
    synchronized static void setReNickname(String nickname,String login, String password) {
        try {
            String reNickname = String.format("UPDATE users SET'nickname'='%s' where login='%s' and password='%s';",nickname, login, password);
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

    synchronized static boolean getNotExistsClient(String nickname,String login) {
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
}
