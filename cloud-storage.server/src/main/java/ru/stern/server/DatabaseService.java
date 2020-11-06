package ru.stern.server;

import java.sql.*;

public class DatabaseService {
    private static final String URL = "jdbc:h2:tcp://localhost/~/test";
    private static final String GET_HASH_PASSWORD = "SELECT login, password FROM users WHERE login = ?";
    private static final String INSERT_NEW_USER = "INSERT INTO users VALUES (NULL, ?, ?)";
    public void checkDatabaseStatus(){
        try{
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex){
            Server.logger.error("Database driver not found.");
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        connection.setAutoCommit(false);
        return connection;
    }

    public boolean checkUser(String login){
        try (Connection connection = getConnection();
             PreparedStatement getUser = connection.prepareStatement(GET_HASH_PASSWORD)){
            getUser.setString(1,login);
            try(ResultSet rs = getUser.executeQuery()){
                if(rs.next()){
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException ex) {
            Server.logger.info("PROCESS: Failed checking the user's {} existence.", login);
            return false;
        }
    }

    public boolean checkPassword(String login, String hashPassword){
        try (Connection connection = getConnection();
             PreparedStatement getHashPassword = connection.prepareStatement(GET_HASH_PASSWORD)){
            getHashPassword.setString(1,login);
            try(ResultSet rs = getHashPassword.executeQuery()){
                if(rs.next()){
                    String dbHashPassword = rs.getString("password");
                    if (dbHashPassword.equals(hashPassword)){
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        } catch (SQLException ex) {
            Server.logger.info("PROCESS: Failed authentication by user: {}.", login);
            return false;
        }
    }

    public boolean insertNewUser(String login, String hashPassword){
        try(Connection connection = getConnection();
            PreparedStatement insertUser = connection.prepareStatement(INSERT_NEW_USER)){
            Savepoint savepoint = connection.setSavepoint("insertUser");
            insertUser.setString(1, login);
            insertUser.setString(2, hashPassword);
            try {
                insertUser.executeUpdate();
                connection.commit();
                return true;
            } catch (SQLException ex){
                connection.rollback(savepoint);
                return false;
            }
        } catch (SQLException ex){
            Server.logger.info("PROCESS: Failed insertion new user: {}.", login);
            return false;
        }
    }

}
