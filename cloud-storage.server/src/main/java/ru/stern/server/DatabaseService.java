package ru.stern.server;

import java.sql.*;

public class DatabaseService {
    private static final String URL = "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM 'classpath:scripts/schema.sql'\\;RUNSCRIPT FROM 'classpath:scripts/data.sql'";
    private static final String GET_HASH_PASSWORD = "SELECT login, password FROM users where login = ?";
    private static final String INSERT_NEW_USER = "INSERT INTO users (login, password) VALUES(?, ?)";

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

    public boolean checkPassword(String login, int hashPassword){
        try (Connection connection = getConnection();
             PreparedStatement getHashPassword = connection.prepareStatement(GET_HASH_PASSWORD)){
            getHashPassword.setString(1,login);
            try(ResultSet rs = getHashPassword.executeQuery()){
                if(rs.next()){
                    int dbHashPassord = rs.getInt("password");
                    if (dbHashPassord == hashPassword){
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

    public boolean insertNewUser(String login, int hashPassword){
        try(Connection connection = getConnection();
            PreparedStatement getLogin = connection.prepareStatement(GET_HASH_PASSWORD);
            PreparedStatement insertUser = connection.prepareStatement(INSERT_NEW_USER)){
            Savepoint savepoint = connection.setSavepoint("insertUser");
            getLogin.setString(1, login);
            try(ResultSet rs = getLogin.executeQuery()){
                if(rs.next()){
                    return false;
                }
            }
            insertUser.setString(1, login);
            insertUser.setInt(2, hashPassword);
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
