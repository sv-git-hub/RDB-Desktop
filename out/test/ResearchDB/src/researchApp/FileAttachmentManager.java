package researchApp;

import dbUtil.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import logging.LoggerWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public class FileAttachmentManager {
    private Connection fConn;
    private String tempFileName;
    private Integer noteID;
    Integer newFileAttachmentID;
    ObservableList<NoteFile> foundFiles = FXCollections.observableArrayList();
    private boolean err;

    FileAttachmentManager(){ }

    public void loadFileIntoDB() throws IOException{
        Path path = getFile();
        if(path != null)
            insertFileInDB(selectedFile(retrieveFile(path)));
    }

    void loadFileIntoDB(Path path) throws IOException{
        insertFileInDB(selectedFile(retrieveFile(path)));
    }

    void loadFileIntoDB(String fName, byte[] file) throws IOException{
        this.tempFileName = fName;
        insertFileInDB(file);
    }

    public void openFile(){
        try{
            String fileName = requestFileName();
            if(!fileName.equals("")) {
                File file = new File(fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(Objects.requireNonNull(retrieveFileFromDB(fileName)));
                fos.close();
                Desktop.getDesktop().open(file);
            }
        }catch(IOException ex){
            ex.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("FileManager.openFile failed");
        }
    }

    void openFile(String fileName, Integer nID){
        this.noteID = nID;
        try{
            if(!fileName.equals("")) {
                File file = new File(GlobalVariables.TEMP + fileName);
                if(!Files.exists(file.toPath())) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(Objects.requireNonNull(retrieveFileFromDB(fileName)));
                    fos.close();
                }
                Desktop.getDesktop().open(file);
            }
        }catch(IOException ex){
            ex.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("FileManager.openFile failed");
        }
    }

    private byte[] retrieveFileFromDB(String fileName){
        Connection conn = null;
        try {
            String file = "SELECT f.FileData FROM File as f " +
            "LEFT JOIN File_By_Note as n ON n.FileID = f.FileID " +
            "WHERE n.NoteID = " + this.noteID + " AND f.FileName = '" + fileName + "'";
            conn = DBConnection.getConnection();
            PreparedStatement ps;
            ResultSet rs;
            if(conn != null) {
                ps = conn.prepareStatement(file);
                rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getBytes("FileData");}
            }
            conn.close();
        }catch (SQLException | NullPointerException ex){
            ex.printStackTrace();
        }finally{
            try{
                if(conn != null) conn.close();
            }catch(SQLException sq){
                sq.printStackTrace();
                LoggerWrapper.getInstance().myLogger.severe("FileAttachmentManager.retrieveFileFromDB failed to close connection.");
            }

        }
        return null;
    }

    public byte[] retrieveFileFromDB(String fileName, Integer nID){
        this.noteID = nID;
        return retrieveFileFromDB(fileName);
    }

    public Path getFile(){
        Path path = null;
        FileChooser openFile = new FileChooser();
        openFile.setTitle("Select File:");
        openFile.setInitialDirectory(new File(GlobalVariables.USER_DESKTOP));
        File file = openFile.showOpenDialog(null);
        if(file != null) {
            path = file.toPath();
            /*if (count(path.getFileName().toString()) > 0) {
                JOptionPane.showMessageDialog(null, "A file exists with the name: " + path.getFileName().toString());
                return null;
            }*/
        }
        return path;
    }

    private FileInputStream retrieveFile(Path path) throws IOException{
        this.tempFileName = path.getFileName().toString();
        return new FileInputStream(new File(path.toString()));
    }

    private byte[] selectedFile(FileInputStream fis) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for(int readNum; (readNum=fis.read(buf))!=-1;)
            bos.write(buf,0,readNum);
        fis.close();
        return bos.toByteArray();
    }

    private void insertFileInDB(byte[] file) throws NullPointerException{
        try {
            String insertFile = "INSERT INTO File(FileName,FileData) VALUES(?,?)";
            if(fConn == null)
                fConn = DBConnection.getConnection();
            PreparedStatement ps;
            ResultSet rs;
            newFileAttachmentID = 0;
            if (fConn != null){
                ps = fConn.prepareStatement(insertFile);
                ps.setString(1, tempFileName);
                ps.setBytes(2, file);
                ps.execute();
                ps = fConn.prepareStatement("SELECT last_insert_rowid()");
                rs = ps.executeQuery();
                if (rs.next()) newFileAttachmentID = rs.getInt(1);
                rs.close();
                ps.close();
            }else{
                System.out.println("The conn is null!");
            }
        }catch (SQLException | NullPointerException ex){
            err = true;
            ex.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("FileAttachmentManager.insertFileInDB failed");
        }
    }

    private Integer count(String fileName){
        int count = 0;
        try {
            String countIDs = "SELECT COUNT(*) FROM File WHERE FileName = '" + fileName +"'";
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps;
            ResultSet rs;
            if(conn != null) {
                ps = conn.prepareStatement(countIDs);
                rs = ps.executeQuery();

                while (rs.next())
                    count = rs.getInt(1);
                rs.close();
                ps.close();
                conn.close();
            }
        }catch (SQLException | NullPointerException ex){
            ex.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("FileManager.count failed");
        }
        return count;
    }

    private String requestFileName(){
        Object ans = JOptionPane.showInputDialog(null, "Enter a new Summary then press OK.", "Edit Table Summary",
                JOptionPane.PLAIN_MESSAGE, null, queryFileNames(), "");
        return ans != null ? ans.toString() : "";
    }

    private String[] queryFileNames(){
        ObservableList<String> str = FXCollections.observableArrayList();
        ObservableList<NoteFile> file = FXCollections.observableArrayList();
        if (!foundFiles.isEmpty()) foundFiles.clear();
        String[] arr = null;
        try {
            String insertFile = "SELECT FileID, FileName FROM File";
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps;
            ResultSet rs;

            if(conn != null) {
                ps = conn.prepareStatement(insertFile);
                rs = ps.executeQuery();
                while (rs.next()) {
                    str.add(rs.getString("FileName"));
                    file.add(new NoteFile(rs.getInt("FileID"), rs.getString("FileName"),""));
                }
                foundFiles = file;
                rs.close();
                ps.close();
                conn.close();

                arr = new String[str.size()];
                int i = 0;
                for (String s : str)
                    arr[i++] = s;
            }

        }catch (SQLException | NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
        return arr;
    }

    boolean getErrors(){
        return this.err;
    }

    void deleteFile(Integer fileID){
        try {
            String deleteFileNoteRef = "DELETE FROM File_By_Note WHERE FileID = " + fileID;
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps;
            if (conn != null) {
                ps = conn.prepareStatement(deleteFileNoteRef);
                ps.execute();
                String deleteFile = "DELETE FROM File WHERE FileID = " + fileID;
                ps = conn.prepareStatement(deleteFile);
                ps.execute();
                ps.close();
            }
        }catch(SQLException se){
            se.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("FileAttachmentManager.deleteFile failed.");
        }
    }

    void passFileConnection(Connection hostConn){
        if(hostConn != null)
            this.fConn = hostConn;
    }
}
