package dbUtil;

import logging.UserLog;
import researchApp.GlobalVariables;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String SQLHome = "jdbc:sqlite:" + GlobalVariables.RESEARCH_DB_PATH;
    private static Connection con;
    public static  Connection getConnection() throws SQLException{
        checkDBExists();
        try{
            Class.forName("org.sqlite.JDBC");
            if(con == null || con.isClosed())
                con = DriverManager.getConnection(SQLHome + DatabaseSource.getDatabase());
            return con;

        }catch(ClassNotFoundException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "DBConnection: " + DatabaseSource.getDatabase() + " could not be found." +
                    ex.toString());
            return null;
        }
    }
    public static void close() throws SQLException{
        if(con !=null) con.close();
    }

    private static void checkDBExists(){
        String check = GlobalVariables.RESEARCH_DB_PATH + UserLog.getUserInfo("database");
        if (!new File(check).exists()) {
            JOptionPane.showMessageDialog(null, UserLog.getUserInfo("database") + " could not be found. The 'sample.db' will be opened as default.",
                    "ResearchController - No database present",JOptionPane.INFORMATION_MESSAGE);
            UserLog.setUserInfo("dbPath", GlobalVariables.RESEARCH_DB_PATH + GlobalVariables.DEFAULT_DB);
            DatabaseSource.setDatabase(GlobalVariables.DEFAULT_DB);

        }
    }
}
