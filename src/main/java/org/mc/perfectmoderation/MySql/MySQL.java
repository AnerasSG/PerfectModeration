package org.mc.perfectmoderation.MySql;

import org.mc.perfectmoderation.configs.MySQLConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private  String host = MySQLConfig.get().getString("host");
    private String port = MySQLConfig.get().getString("port");
    private  String database = MySQLConfig.get().getString("database");
    private  String username = MySQLConfig.get().getString("username");
    private  String password = MySQLConfig.get().getString("password");

    private Connection connection;

    public boolean isConnected(){
        return (connection == null ? false : true);
    }

    public void Connect() throws ClassNotFoundException, SQLException {
        if(!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        }
    }
    public void Disconnect(){
        if(isConnected()){
            try {
                connection.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

    }
    public Connection getConnection() {
        return connection;
    }
}

