package dbUtil;

import logging.LoggerWrapper;
import researchApp.GlobalVariables;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class DatabaseSource {
    private static String database;
    public static String getDatabase(){
        try {
            Scanner f = new Scanner(Paths.get(GlobalVariables.RESEARCH_DB_PATH + "user_log/userInfo.txt"));
            while (f.hasNextLine()){
                String l = f.nextLine();
                if(l.contains("database:=")) {
                    database = l.split(":=")[1];
                    break;
                }
            }
            f.close();

            return database;

        }catch ( Exception ep){
            LoggerWrapper.getInstance().myLogger.severe("DatabaseSources.getDatabase failed.");
            JOptionPane.showMessageDialog(null, "DatabaseSources.getDatabase failed. The sample.db will be loaded by default.",
                    "Error: Installation",JOptionPane.ERROR_MESSAGE);
            return GlobalVariables.DEFAULT_DB;
        }
    }
    public static void setDatabase(String database){
        try
        {
            Scanner f = new Scanner(Paths.get(GlobalVariables.RESEARCH_DB_PATH + "user_log/userInfo.txt"));
            String currentDatabase = "database:=" + getDatabase();
            String line;
            String newLine = "";
            while (f.hasNextLine()){
                line = f.nextLine();
                if(line.contains(currentDatabase)){
                    newLine = newLine + line.replace(currentDatabase, "database:=" + database) + "\r\n";
                }else{
                    newLine = newLine + line + "\r\n";
                }
            }
            f.close();
            FileWriter writer = new FileWriter(GlobalVariables.RESEARCH_DB_PATH + "user_log/userInfo.txt");
            writer.write(newLine);
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
