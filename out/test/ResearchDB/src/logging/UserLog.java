package logging;

import researchApp.GlobalVariables;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class UserLog {

    private static String data;

     public static String getUserInfo(String parameter){
        try {
            Scanner f = new Scanner(Paths.get(GlobalVariables.RESEARCH_DB_PATH + "user_log/userInfo.txt"));
            while (f.hasNextLine()){
                String l = f.nextLine();
                if(l.contains(parameter +":=")) {
                    data = l.split(":=")[1];
                    break;
                }
            }
            f.close();
            return data;

        }catch ( Exception ep){
            ep.printStackTrace();
            return null;
        }
    }
    public static void setUserInfo(String parameter, String data){
        try
        {
            Scanner f = new Scanner(Paths.get(GlobalVariables.RESEARCH_DB_PATH + "user_log/userInfo.txt"));
            String currentUserInfo = parameter + ":=" + getUserInfo(parameter);
            String line;
            String newLine = "";
            while (f.hasNextLine()){
                line = f.nextLine();
                if(line.contains(currentUserInfo)){
                    newLine = newLine + line.replace(currentUserInfo, parameter +":=" + data) + "\r\n";
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
