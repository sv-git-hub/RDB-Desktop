package researchApp;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class NoteFile {
    private final SimpleIntegerProperty FileID;
    private final SimpleStringProperty FileName;
    private final SimpleStringProperty FilePath;

    public NoteFile(String fileName, String filePath){
        this(0, fileName, filePath);
    }

    public NoteFile(Integer fileID, String fileName, String filePath) {
        this.FileID = new SimpleIntegerProperty(fileID);
        this.FileName = new SimpleStringProperty(fileName);
        this.FilePath = new SimpleStringProperty(filePath);
    }

    public Integer getFileID() { return FileID.get(); }

    public String getFileName() {
        return FileName.get();
    }

    public String getFilePath() { return FilePath.get(); }

    public void setFileID(String fileID) { this.FileName.set(fileID); }

    public void setFileName(String fileName) {
        this.FileName.set(fileName);
    }

    public void setFilePath(String filePath) {
        this.FilePath.set(filePath);
    }

    public SimpleIntegerProperty fileIDProperty() { return FileID; }

    public SimpleStringProperty fileNameProperty() {
        return FileName;
    }

    public SimpleStringProperty filePathProperty() { return FilePath; }
}
