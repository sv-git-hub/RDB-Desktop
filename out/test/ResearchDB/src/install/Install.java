package install;

import jdk.nashorn.internal.objects.Global;
import logging.LoggerWrapper;
import logging.UserLog;
import researchApp.GlobalVariables;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;

public class Install {

    public Install(){
        String jarPath = new File(Install.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toString();
        if(!ValidateInstall()){
            if(SetPermissions())
                RetrieveResources(jarPath);
            LoggerWrapper.getInstance().myLogger.finest("Install complete");
        }else{
            CheckForUpdatesOrReInstall(jarPath);
            LoggerWrapper.getInstance().myLogger.finest("Install validation complete");
        }
    }

    private boolean ValidateInstall(){
        LoggerWrapper.getInstance().myLogger.finest("Validating install...");
        boolean tf = true;
        File file1 = new File(GlobalVariables.USER_LOG, "userInfo.txt");
        File file2 = new File(GlobalVariables.TEMPLATES, "templates/blank.db");
        File file3 = new File(GlobalVariables.RESEARCH_DB_PATH, "sample.db");
        if(!file1.exists() && !file2.exists() && !file3.exists()) tf = false;
        return tf;
    }

    private void RetrieveResources(String jarPath) {
        LoggerWrapper.getInstance().myLogger.finest("Validating/Retrieving resources...");
        try{
            JarExtractor.extract(jarPath, "logging", "userInfo.txt", "user_log");
            JarExtractor.extract(jarPath, "sqlite","sample.db", "");
            JarExtractor.extract(jarPath, "sqlite", "blank.db", "templates");
            JarExtractor.extract(jarPath, "sqlite","apologetic.db", "");
            JarExtractor.extract(jarPath, "sqlite","RDSI.db", "");
            JarExtractor.extract(jarPath, "appInfo", "ResearchDBManual.pdf", "app_info");
            //JarExtractor.extract(jarPath, "appInfo", "ResearchDB.docx", "app_info");

            Files.createDirectory(Paths.get(GlobalVariables.TEMP));
            Files.createDirectory(Paths.get(GlobalVariables.BACKUP));
            UserLog.setUserInfo("dbPath", GlobalVariables.RESEARCH_DB_PATH + UserLog.getUserInfo("defaultDB"));

        }catch(IOException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Application setup failed. User may not have " +
                    "folder permissions for the home directory:\n" + GlobalVariables.USER_PATH, "Error: Installation",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void CheckForUpdatesOrReInstall(String jarPath){
        try {
            Path varPath;
            for(Field field : GlobalVariables.class.getDeclaredFields()){
                if(field.getName().equals("BACKUP") || field.getName().equals("TEMP")){
                    varPath = new File(field.get(field).toString()).toPath();
                    if (!Files.exists(varPath))
                        Files.createDirectory(varPath);
                }
            }
        }catch(Exception io){
            io.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("Install.CheckForUpdatesOrReInstall: could not iterate field variables");
        }

        UpdateOrReinstall(jarPath, "user_log/userInfo.txt", "logging", "");
        UpdateOrReinstall(jarPath, "templates/blank.db", "sqlite", "");
        UpdateOrReinstall(jarPath, "sample.db", "sqlite", "");
        UpdateOrReinstall(jarPath, "app_info/ResearchDBManual.pdf", "appInfo", "manualPDF");
        //UpdateOrReinstall(jarPath, "app_info/ResearchDB.docx", "appInfo", "manualDOCX");

    }

    private void UpdateOrReinstall(String jarPath, String current, String entryFolder, String userInfo){
        try {
            if (!Files.exists(new File(GlobalVariables.RESEARCH_DB_PATH + current).toPath())) {
                if(current.split("/").length > 1)
                    JarExtractor.extract(jarPath, entryFolder, current.split("/")[1], current.split("/")[0]);
                else
                    JarExtractor.extract(jarPath, entryFolder, current, "");
            }
            if (!userInfo.equals("") && !lastModifiedDateTimeCurrent(jarPath, GlobalVariables.RESEARCH_DB_PATH + current)) {
                Files.delete(new File(GlobalVariables.RESEARCH_DB_PATH + current).toPath());
                JarExtractor.extract(jarPath, entryFolder, current.split("/")[1], current.split("/")[0]);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean lastModifiedDateTimeCurrent(String jarPath, String curr) throws Exception{
        String fName = new File(curr).getName();
        JarFile jarFile;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        Date dt1 = sdf.parse(sdf.format(new File(curr).lastModified()));
        Date dt2;
        try {
           jarFile = new JarFile(jarPath);
            dt2 = sdf.parse(sdf.format(jarFile.getJarEntry("appInfo/" + fName).getTime()));
        }catch(FileNotFoundException f){
            // Development/Debugging code
            dt2 = sdf.parse(sdf.format(new File(jarPath + "/appInfo/" + fName).lastModified()));
        }
        /*String fName = new File(curr).getName();
        JarFile jarFile = new JarFile(jarPath);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        Date dt1 = sdf.parse(sdf.format(new File(curr).lastModified()));
        Date dt2 = sdf.parse(sdf.format(jarFile.getJarEntry("appInfo/" + fName).getTime()));*/
        return dt1.compareTo(dt2) >= 0;
    }

    private boolean SetPermissions(){
        LoggerWrapper.getInstance().myLogger.finest("Validating permissions");
        File file = new File(GlobalVariables.USER_PATH);
        if(!file.canExecute() && !file.canRead() && !file.canWrite()){
            if(!file.setExecutable(true) || !file.setReadable(true)  || !file.setWritable(true)){
                JOptionPane.showMessageDialog(null, "Access Denied: The ResearchDB location could not be created.", "Database Creation Failed", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
        return true;
    }



}