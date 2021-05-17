package install;

import logging.LoggerWrapper;
import researchApp.GlobalVariables;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor {
    private static String sysFolder = GlobalVariables.RESEARCH_DB_PATH;

    public static void extract(String jarName,String entryFolder, String entryFile, String destFolder)throws IOException {
        String jarEntry = entryFolder + "/" + entryFile;
        if (!destFolder.isEmpty())
            destFolder = destFolder + "/";

        JarFile jar = new JarFile(jarName);
        try {
            JarEntry entry = jar.getJarEntry(jarEntry);
            if (entry != null) {
                InputStream entryStream = jar.getInputStream(entry);
                try {
                    checkFolderPath(destFolder);
                    outputStream(entryStream, entry, destFolder);
                }
                finally {
                    entryStream.close();
                }
            }
            else {
                JOptionPane.showMessageDialog(null, "NotFound: " + jarEntry );
            }
        }
        finally {
            jar.close();
            LoggerWrapper.getInstance().myLogger.config("Extraction completed: " + destFolder  + entryFile);
        }
    }

    private static void outputStream(InputStream input, JarEntry entry, String folder) throws IOException{
        FileOutputStream file = new FileOutputStream(sysFolder + folder  + new File(entry.getName()).getCanonicalFile().getName());
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                file.write(buffer, 0, bytesRead);
            }
        }
        finally {
            file.close();
        }
    }

    private static void checkFolderPath( String folder){
        try {
            Path path = Paths.get(sysFolder + folder);
            if (!Files.exists(path)) Files.createDirectory(path);
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }
}
