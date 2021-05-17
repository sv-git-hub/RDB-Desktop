package dbConnect;

import java.sql.Connection;
import java.sql.SQLException;
import dbUtil.DBConnection;

public class ConnectionModel {
    private Connection connection = null;

    public ConnectionModel(){

        try{
            this.connection = DBConnection.getConnection();
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        if(this.connection == null){
            System.exit(1);
        }
    }

    public boolean isDatabaseConnected(){
        return this.connection != null;
    }

}
