package com.cloud.box.common;

import java.sql.*;

public class AuthDB {

    private Connection connection = null;
    private Statement st = null;
    private PreparedStatement select = null;
    private static final String connectionURL = "jdbc:sqlite:usersCloud.db";
    private static final String sqlGetNickName = "SELECT nickname FROM main WHERE login = ? AND password = ?;";
    private static final String sqlGetAllUsers = "SELECT * FROM users";

    //** Открыть БД
    private Connection dbOpen() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection(connectionURL);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    //** Закрыть БД
    private void dbClose() {
        try {
            if (connection != null) {
                if (!connection.isClosed()) connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }

    //** Возвращает nickname пользователя
    public String userLogin(String[] id){

        try {
            connection = dbOpen();
            PreparedStatement select = connection.prepareStatement(sqlGetNickName);

            select.setString(1, id[0]); // login
            select.setString(2, id[1]); // password

            ResultSet resultSet = select.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbClose();
        }
        return null;
    }
}