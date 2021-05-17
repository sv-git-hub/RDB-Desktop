package researchApp;

import dbUtil.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import logging.LoggerWrapper;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.*;

public class Search{

    private Connection sConn;
    private PreparedStatement prepStmt;
    private ResultSet resultSet;
    private boolean err;
    boolean fileUpdate;
    boolean removeTopic;

    private ObservableList<Source> tableData = FXCollections.observableArrayList();
    private ObservableMap<String, Integer> NoteIDResult = FXCollections.observableHashMap();
    private ObservableList<Integer> searchNoteIDs = FXCollections.observableArrayList();
    private ObservableList<Integer> newSourceAuthorIDs = FXCollections.observableArrayList();
    private ObservableList<Integer> newFileAttachmentIDs = FXCollections.observableArrayList();

    ObservableList<Source> SourceList;
    ObservableList<String> SourceData;
    ObservableList<String> SummaryData;
    ObservableList<String> TopicData;
    HashMap<Integer, ArrayList<String>> Sources;

    // Constructor
    public Search(){ }

    /*PUBLIC METHODS:
    ==================================================================================================================*/
    void addToNotesTable(Integer sID, Integer cID, Integer qnID, Integer qtID, Integer tmID, Integer toID){
        try{
            int nID = 0;
            String insertNotes = "INSERT INTO Notes(SourceID, CommentID, QuestionID, QuoteID, TermID, TopicID, Deleted)" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?)";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: addToNotesTable");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(insertNotes);
            prepStmt.setInt(1, sID);
            prepStmt.setInt(2, cID);
            prepStmt.setInt(3, qnID);
            prepStmt.setInt(4, qtID);
            prepStmt.setInt(5, tmID);
            prepStmt.setInt(6, toID);
            prepStmt.setInt(7, 0);
            prepStmt.execute();
            prepStmt = sConn.prepareStatement("SELECT last_insert_rowid()");
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                nID = resultSet.getInt(1);
            }
            prepStmt.close();

            if(!newSourceAuthorIDs.isEmpty()) {
                for (Integer aID : newSourceAuthorIDs) {
                    String insertAuthorBySource = "INSERT INTO Author_By_Source(AuthorID, SourceID)" +
                            "VALUES(" + aID + ", " + sID + ")";
                    prepStmt = sConn.prepareStatement(insertAuthorBySource);
                    prepStmt.execute();
                }
            }

            if(!newFileAttachmentIDs.isEmpty()) {
                for (Integer fID : newFileAttachmentIDs) {
                    String insertFileIntoDB = "INSERT INTO File_By_Note (NoteID, FileID)" +
                            "VALUES(" + nID + ", " + fID + ")";
                    prepStmt = sConn.prepareStatement(insertFileIntoDB);
                    prepStmt.execute();
                }
            }
            resultSet.close();
            prepStmt.close();

        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: addToNotesTable");
        }
    }

    void addAuthors(ObservableList<Author> newAuthors){
        try{
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if (sConn == null) {
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeSourceBox");
                System.exit(0);
            }
            for(Author auth : newAuthors){
                int idAuthor = findAuthor(auth);
                if(idAuthor != 0){
                    newSourceAuthorIDs.add(idAuthor);
                }else{
                    String sourceAuthor = "INSERT INTO Author(FirstName, MiddleName, LastName, Suffix) " +
                            "VALUES" + buildAuthorValues(auth.getFirstName(), auth.getMiddleName(), auth.getLastName(), auth.getSuffix());
                    prepStmt = sConn.prepareStatement(sourceAuthor);
                    prepStmt.execute();
                    prepStmt = sConn.prepareStatement("SELECT last_insert_rowid()");
                    resultSet = prepStmt.executeQuery();
                    if (resultSet.next()) {
                        newSourceAuthorIDs.add(resultSet.getInt(1));
                    }
                }
            }
            prepStmt.close();
            resultSet.close();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: addAuthors");
        }
    }

    void addFiles(ObservableList<NoteFile> newFiles){
        try {
            FileAttachmentManager attachFile = new FileAttachmentManager();
            if(sConn != null) attachFile.passFileConnection(sConn);
            for (NoteFile file : newFiles) {
                if(file.getFilePath().length() > 1000){
                    attachFile.loadFileIntoDB(file.getFileName(), file.getFilePath().getBytes());
                }else {
                    attachFile.loadFileIntoDB(new File(file.getFilePath()).toPath());
                }
                this.newFileAttachmentIDs.add(attachFile.newFileAttachmentID);
            }
        }catch(IOException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: addAuthors");
        }
    }

    void captureSources(){
        try {
            HashMap<Integer, ArrayList<String>> sources = new HashMap<>();
            String sourceQuery = "SELECT SourceID, SourceType, Title FROM Source";

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if (sConn == null) {
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeSourceBox");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(sourceQuery);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {
                String sourceAuthors;
                if (!sources.containsKey(rs.getInt("SourceID"))) {
                    ObservableList<Author> SourceAuthors = captureAuthors(rs.getInt("SourceID")); // Builds SourceAuthors
                    sourceAuthors = concatenateAuthors(SourceAuthors);
                    sources.put(rs.getInt("SourceID"), new ArrayList<>( Arrays.asList(
                            rs.getString("SourceID"), rs.getString("SourceType"), rs.getString("Title"),
                            sourceAuthors)));
                }
            }
            Sources = sources;
            prepStmt.close();
            rs.close();
            sConn.close();
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: captureSources");
        }
    }

    void captureSummaries(){
        ObservableList<String> searchSummary = FXCollections.observableArrayList();
        try {
            String topicQuery = "SELECT Summary FROM Comment";

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if (sConn == null) {
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeSourceBox");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(topicQuery);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {
                if (!searchSummary.contains(rs.getString("Summary"))) {
                    searchSummary.add(rs.getString("Summary"));
                }
            }
            SummaryData = searchSummary.sorted();
            prepStmt.close();
            rs.close();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: captureTopics");
        }
    }

    void captureTopics(){
        ObservableList<String> searchTopic = FXCollections.observableArrayList();
        try {
            String topicQuery = "SELECT Topic FROM Topic";

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if (sConn == null) {
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeSourceBox");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(topicQuery);
            ResultSet rs = prepStmt.executeQuery();

            if(!searchTopic.contains(null))
                searchTopic.add(null);

            while (rs.next()) {
                if (!searchTopic.contains(rs.getString("Topic"))) {
                    searchTopic.add(rs.getString("Topic"));
                }
            }
            TopicData = searchTopic;
            prepStmt.close();
            rs.close();
            sConn.close();
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: captureTopics");
        }
    }

    void completeSearch(boolean containsAll, String strSearch){
        searchNoteIDs.clear();
        tableData.clear();

        searchTable(containsAll, strSearch,"Source", Collections.singletonList("Title"));
        searchTable(containsAll, strSearch,"Comment", Arrays.asList("Summary", "Comment", "Page", "TimeStamp","Hyperlink"));
        searchTable(containsAll, strSearch,"Question", Collections.singletonList("Question"));
        searchTable(containsAll, strSearch,"Quote", Collections.singletonList("Quote"));
        searchTable(containsAll, strSearch,"Term", Collections.singletonList("Term"));
        searchTable(containsAll, strSearch,"Topic", Collections.singletonList("Topic"));
        searchTable(containsAll, strSearch,"File", Collections.singletonList("FileName"));
        searchTable(containsAll, strSearch,"Author", Arrays.asList("FirstName", "MiddleName", "LastName"));

        //noinspection Convert2MethodRef
        searchNoteIDs.forEach((Integer i) -> searchNoteIDs(i));
    }

    void deleteNote(Integer noteID, boolean del, int mark){
        String strDelete;
        try{
            if(del)
                strDelete = "DELETE FROM Notes WHERE NoteID = " + noteID;
            else
                strDelete = "UPDATE Notes SET Deleted = " + mark +" WHERE Notes.NoteID = " + noteID;
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: deleteNote");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(strDelete);
            prepStmt.execute();
            prepStmt.close();

        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: deleteNote");
        }

    }

    void deleteRecord(Integer noteID){
        ObservableList<Integer> AuthorIDs;
        ObservableList<Integer> FileIDs;
        List<String> arrTables  = Arrays.asList("Topic", "Source", "File", "Comment", "Question", "Quote", "Term");
        captureNoteIDs(noteID); // Dumps result into ObservableMap 'NoteIDResults'
        int sourceCount = count("Notes", "SourceID", NoteIDResult.get("SourceID"));
        int topicCount = count("Notes", "TopicID", NoteIDResult.get("TopicID"));
        int fileCount = count("File_By_Note", "NoteID", noteID);
        removeTopic = false;
        try{
            deleteNote(noteID, true, 0);
            for(String tbl : arrTables) {

                if(tbl.equals("Source")) {
                    if (sourceCount == 1 && count("Author_By_Source", "SourceID", NoteIDResult.get("SourceID")) == 1)
                        if (count("Author_By_Source", "AuthorID", findAuthor(NoteIDResult.get("SourceID"))) == 1) {
                            int authorID = findAuthor(NoteIDResult.get("SourceID"));
                            deleteTableRecord("Author_By_Source", "SourceID", NoteIDResult.get("SourceID"));
                            deleteTableRecord("Author", "AuthorID", authorID);
                            deleteTableRecord("Source", "SourceID", NoteIDResult.get("SourceID"));
                        } else {
                            deleteTableRecord("Author_By_Source", "SourceID", NoteIDResult.get("SourceID"));
                            deleteTableRecord("Source", "SourceID", NoteIDResult.get("SourceID"));
                        }
                    else if (sourceCount == 1 && count("Author_By_Source", "SourceID", NoteIDResult.get("SourceID")) > 1){
                        AuthorIDs = findAuthorIDs(NoteIDResult.get("SourceID"));
                        for (Integer i: AuthorIDs) {
                            if (count("Author_By_Source", "AuthorID", i) == 1) {
                                deleteTableRecord("Author_By_Source", "AuthorID", i);
                                deleteTableRecord("Author", "AuthorID", i);
                            }else{
                                deleteTableRecord("Author_By_Source", "AuthorID", i, "SourceID", NoteIDResult.get("SourceID"));
                            }
                        }
                        deleteTableRecord("Source", "SourceID", NoteIDResult.get("SourceID"));
                    }

                }else if (tbl.equals("Topic")) {
                    if (topicCount == 1) {
                        deleteTableRecord("Topic", "TopicID", NoteIDResult.get("TopicID"));
                        removeTopic = true;
                    }

                }else if(tbl.equals("File") && fileCount >= 1){
                    FileIDs = findFileIDs(noteID);
                    for(Integer f : FileIDs){
                        deleteTableRecord("File_By_Note", "FileID", f);
                        deleteTableRecord("File", "FileID", f);
                    }

                }else if (NoteIDResult.containsKey(tbl + "ID") &&  NoteIDResult.get(tbl + "ID") != 0){
                    deleteTableRecord(tbl, tbl + "ID", NoteIDResult.get(tbl + "ID"));
                }


            }

        }catch (Exception ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: deleteNote");
        }

    }

    void displaySelection(Integer noteID){
        try{
            ObservableList<String> searchSelectionResult = FXCollections.observableArrayList();
            String searchSelection = "SELECT qn.Question, qt.Quote, te.Term, s.Year, s.Month, s.Day, " +
            "s.Volume, s.Edition, s.Issue, tp.Topic, c.Hyperlink, c.Comment, c.Page, c.TimeStamp, c.Summary FROM Comment as c " +
            "LEFT JOIN Notes as n ON n.CommentID = c.CommentID " +
            "LEFT JOIN Source as s ON n.SourceID = s.SourceID " +
            "LEFT JOIN Question as qn ON n.QuestionID = qn.QuestionID " +
            "LEFT JOIN Quote as qt ON n.QuoteID = qt.QuoteID " +
            "LEFT JOIN Term as te ON n.TermID = te.TermID " +
            "LEFT JOIN Topic as tp ON n.TopicID = tp.TopicID " +
            "WHERE n.NoteID = '" + noteID + "'";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchSelection);
            resultSet = prepStmt.executeQuery();

            StringBuilder sb = new StringBuilder();

            if(resultSet.next()){
                searchSelectionResult.add(resultSet.getString("Question"));
                searchSelectionResult.add(resultSet.getString("Quote"));
                searchSelectionResult.add(resultSet.getString("Term"));
                if(!resultSet.getString("Month").isEmpty()){
                    sb.append(resultSet.getString("Month"));
                }
                if(!resultSet.getString("Day").isEmpty()){
                    if (!resultSet.getString("Month").isEmpty()){
                        sb.append("/").append(resultSet.getString("Day"));
                    }else{
                        sb.append(resultSet.getString("Day"));
                    }
                }
                if(!resultSet.getString("Year").isEmpty()){
                    if (resultSet.getString("Month").isEmpty() && resultSet.getString("Day").isEmpty() ){
                        sb.append(resultSet.getString("Year"));
                    }else{
                        sb.append("/").append(resultSet.getString("Year"));
                    }
                }
                searchSelectionResult.add(sb.toString());
                searchSelectionResult.add(resultSet.getString("Volume"));
                searchSelectionResult.add(resultSet.getString("Edition"));
                searchSelectionResult.add(resultSet.getString("Issue"));
                searchSelectionResult.add(resultSet.getString("Hyperlink"));
                searchSelectionResult.add(resultSet.getString("Comment"));
                searchSelectionResult.add(resultSet.getString("Page"));
                searchSelectionResult.add(resultSet.getString("TimeStamp"));
                searchSelectionResult.add(resultSet.getString("Summary"));
                searchSelectionResult.add(resultSet.getString("Topic"));
            }
            this.SourceData = searchSelectionResult;
            resultSet.close();
            prepStmt.close();

            captureNoteIDs(noteID);

        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: displaySelection");
        }
    }

    void passSearchConnection(Connection hostConn){
        if(hostConn != null)
            this.sConn = hostConn;
    }

    void searchForMarkedDeleted(){
        try{
            String searchQuestion = "SELECT n.NoteID, s.SourceID, s.SourceType, s.Title, c.Summary FROM Comment as c " +
                    "LEFT JOIN Notes as n ON n.CommentID = c.CommentID " +
                    "LEFT JOIN Source as s ON n.SourceID = s.SourceID " +
                    "WHERE n.Deleted = 1";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchTopic");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchQuestion);
            resultSet = prepStmt.executeQuery();

            while(resultSet.next()) {
                tableData.add(new Source(resultSet.getInt("NoteID"),
                        resultSet.getString("SourceType"),
                        resultSet.getString("Title") + " ~ " + retrieveAllSourceAuthors(resultSet.getInt("SourceID")),
                        resultSet.getString("Summary")));
            }
            this.SourceList = tableData;
            resultSet.close();
            prepStmt.close();


        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: searchTopic");
        }
    }

    void searchQuestion(String strQuestion){
        try{
            String searchQuestion = "SELECT n.NoteID, s.SourceID, s.SourceType, s.Title, c.Summary FROM Comment as c " +
                    "LEFT JOIN Notes as n ON n.CommentID = c.CommentID " +
                    "LEFT JOIN Source as s ON n.SourceID = s.SourceID " +
                    "LEFT JOIN Question as q ON n.QuestionID = q.QuestionID " +
                    "WHERE q.Question = '" + strQuestion.replace("'", "''") + "' AND n.Deleted = 0";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchTopic");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchQuestion);
            resultSet = prepStmt.executeQuery();


            while(resultSet.next()) {
                tableData.add(new Source(resultSet.getInt("NoteID"),
                        resultSet.getString("SourceType"),
                        resultSet.getString("Title") + " ~ " + retrieveAllSourceAuthors(resultSet.getInt("SourceID")),
                        resultSet.getString("Summary")));
            }
            this.SourceList = tableData;
            resultSet.close();
            prepStmt.close();


        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: searchTopic");
        }
    }

    void searchTopic(String strTopicData){
        try{
            String searchTopic = "SELECT n.NoteID, s.SourceID, s.SourceType, s.Title, c.Summary FROM Comment as c " +
                    "LEFT JOIN Notes as n ON n.CommentID = c.CommentID " +
                    "LEFT JOIN Source as s ON n.SourceID = s.SourceID " +
                    "LEFT JOIN Topic as t ON n.TopicID = t.TopicID " +
                    "WHERE t.Topic = '" + strTopicData.replace("'", "''") + "' AND n.Deleted = 0";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchTopic");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchTopic);
            resultSet = prepStmt.executeQuery();

            while(resultSet.next()) {
                tableData.add(new Source(resultSet.getInt("NoteID"),
                        resultSet.getString("SourceType"),
                        resultSet.getString("Title") + " ~ " + retrieveAllSourceAuthors(resultSet.getInt("SourceID")) ,
                        resultSet.getString("Summary")));
            }
            this.SourceList = tableData;
            resultSet.close();
            prepStmt.close();


        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: searchTopic");
        }
    }

    void updateField(Integer noteID, String table, String tableID, String field, String fieldData){

        int currValuePKID = findPKIDonNotes(noteID, tableID);
        int newValuePKID = findDataPKID(table, field, fieldData);
        int intCurrValue = count("Notes", tableID, currValuePKID);

        if(currValuePKID == 0) {
            addNewDataToDatabase(noteID, table, tableID, field, fieldData);
        }else if (currValuePKID > 0){
            if (!table.equals("Topic") && !table.equals("Question")) {
                replaceData(noteID, table, tableID, field, fieldData);
            } // Beyond this point handles "Topic" and "Question" updates

              else if (newValuePKID == 0 && count("Notes", tableID, currValuePKID) == 1) {
                  replaceData(noteID, table, tableID, field, fieldData);

            } else if (newValuePKID == 0 && count("Notes", tableID, currValuePKID) > 1) {
                addNewDataToDatabase(noteID, table, tableID, field, fieldData);

            }else if (newValuePKID > 0) {
                updateNotesTable(noteID, tableID, newValuePKID);
                if(intCurrValue == 1) {
                    deleteTableRecord(table, tableID, currValuePKID);
                }
            }
        }else{
            JOptionPane.showMessageDialog(null, "The " + field + " field in table " + table + " failed to update.");
        }
    }

    void updateFiles(Integer noteID, ObservableList<NoteFile> listA, ObservableList<NoteFile> listB){
        try {
            if (listA != null && listB != null) {
                // Add new files
                for (NoteFile aFile : listA) {
                    boolean found = false;
                    for (NoteFile bFile : listB) {
                        if (bFile.toString().equals(aFile.toString())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        addFileUpdateFileNote(noteID, aFile);
                        fileUpdate = true;
                    }
                }

                // Delete Files
                for (NoteFile bFile : listB) {
                    boolean found = false;
                    for (NoteFile aFile : listA) {
                        if (aFile.toString().equals(bFile.toString())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        FileAttachmentManager attachFile = new FileAttachmentManager();
                        attachFile.deleteFile(bFile.getFileID());
                        fileUpdate = true;
                    }
                }
            } else if (listA.size() > 0) {
                listA.forEach((f) -> addFileUpdateFileNote(noteID, f));
                fileUpdate = true;
            }

        }catch (Exception e){
            err = true;
            e.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("Search.updateFiles failed.");
        }
    }


    /* PUBLIC FUNCTIONS:
    ==================================================================================================================*/
    boolean doesSourceAlreadyExist(String title, ObservableList<Author> list){
        ObservableList<Integer> currSources = FXCollections.observableArrayList();
        boolean sourceExist = false;
        try{
            String grabSourceID = "SELECT SourceID FROM Source WHERE Title = '" + title.replace("'", "''") + "'";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchID");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(grabSourceID);
            resultSet = prepStmt.executeQuery();

            while(resultSet.next()){
                currSources.add(resultSet.getInt("SourceID"));
            }
            prepStmt.close();
            resultSet.close();

            for(Integer i : currSources){
                ObservableList<Author> sourceAuthors = captureAuthors(i);
                for (Author tblAuthor : list) {
                    sourceExist = false;
                    for (Author sourceAuthor : sourceAuthors) {
                        if (sourceAuthor.toString().equals(tblAuthor.toString())) {
                            sourceExist = true;
                            break;
                        }
                    }
                    if (!sourceExist)
                        break;
                }
                if(sourceExist)
                    break;
            }
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(),"Error: doesSourceAlreadyExist");
        }

        return sourceExist;
    }

    boolean getErrors(){
        return this.err;
    }

    ObservableList<NoteFile> searchForAttachments(Integer noteID){
        ObservableList<NoteFile> file = FXCollections.observableArrayList();
        try {
            String selectFiles = "SELECT f.FileId, f.FileName FROM File as f " +
                    "LEFT JOIN File_By_Note as n ON n.FileID = f.FileID " +
                    "WHERE n.NoteID = " + noteID;
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps;
            ResultSet rs;

            if(conn != null) {
                ps = conn.prepareStatement(selectFiles);
                rs = ps.executeQuery();
                while (rs.next()) {
                    file.add(new NoteFile(rs.getInt("FileID"), rs.getString("FileName"),"stored_in_database"));
                }
                file = file.sorted();
                rs.close();
                ps.close();
            }

        }catch (SQLException | NullPointerException ex){
            ex.printStackTrace();
            file.clear();
            return file;
        }
        return file;
    }

    Integer searchID(String strTableName, String strPKColumnName, String strColumnName, String strData){
        int PKID = 0;
        try{
            String searchForID = "SELECT " + strPKColumnName + " FROM " + strTableName + " WHERE " + strColumnName + " = '" + strData.replace("'", "''") + "'";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchID");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchForID);
            resultSet = prepStmt.executeQuery();
            if(resultSet.next()){
                PKID = resultSet.getInt(strPKColumnName);
            }
            resultSet.close();
            prepStmt.close();

        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: searchID");
        }
        return PKID;
    }

    /*Private Methods:
    ==================================================================================================================*/
    private void addFileUpdateFileNote(Integer noteID, NoteFile aFile){
        try {
            FileAttachmentManager attachFile = new FileAttachmentManager();
            attachFile.loadFileIntoDB(new File(aFile.getFilePath()).toPath());
            String insertFileIntoDB = "INSERT INTO File_By_Note (NoteID, FileID)" +
                    "VALUES(" + noteID + ", " + attachFile.newFileAttachmentID + ")";
            if (sConn == null){
                sConn = DBConnection.getConnection();}
            if(sConn != null){
                prepStmt = sConn.prepareStatement(insertFileIntoDB);
                prepStmt.execute();
            }

        } catch (IOException | SQLException ie) {
            err = true;
            LoggerWrapper.getInstance().myLogger.severe("Search.updateFiles failed.");
        }
    }

    private void addNewDataToDatabase(Integer noteID, String table, String tablesID, String fieldName, String tableData){
        int idData = 0;
        try{
            String insertQuote = "INSERT INTO " + table + "(" + fieldName + ") VALUES(?)";
            prepStmt = sConn.prepareStatement(insertQuote);
            prepStmt.setString(1,tableData);
            prepStmt.execute();

            prepStmt = sConn.prepareStatement("SELECT last_insert_rowid()");
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                idData = resultSet.getInt(1);
                resultSet.close();
            }

            updateNotesTable(noteID, tablesID, idData);

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: addNewDataToDatabase");
        }
    }

    private void captureNoteIDs(Integer noteID){
        ObservableMap<String, Integer> noteIDs = FXCollections.observableHashMap();
        try{
            String captureNoteIDs = "SELECT SourceID, CommentID, QuestionID, QuoteID, TermID, TopicID FROM Notes " +
                    "WHERE NoteID = " + noteID;

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: captureNoteIDs");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(captureNoteIDs);
            resultSet = prepStmt.executeQuery();

            while(resultSet.next()){
                noteIDs.put("SourceID", resultSet.getInt("SourceID"));
                noteIDs.put("CommentID", resultSet.getInt("CommentID"));
                noteIDs.put("QuestionID", resultSet.getInt("QuestionID"));
                noteIDs.put("QuoteID", resultSet.getInt("QuoteID"));
                noteIDs.put("TermID", resultSet.getInt("TermID"));
                noteIDs.put("TopicID", resultSet.getInt("TopicID"));
            }
            this.NoteIDResult = noteIDs;

            resultSet.close();
            prepStmt.close();


        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage("Failed to capture Notes NoteID " + noteID + "\n\n" + ex.toString(), "Error: captureNoteIDs");
        }

    }

    private void captureSearchNoteIDs(String table, Integer tableID){
        try{
            String searchString;
            if(table.equals("File"))
                searchString = "SELECT NoteID FROM " + table + "_By_Note WHERE " + table + "ID = " + tableID;
            else if(table.equals("Author"))
                searchString = "SELECT NoteID FROM Notes AS n " +
                        "LEFT JOIN Author_By_Source AS s " +
                        "WHERE " + tableID + " = s.AuthorID AND s.SourceID = n.SourceID";
            else
                searchString = "SELECT NoteID FROM Notes WHERE " + table + "ID = " + tableID;
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: captureSearchNoteIDs");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchString);
            resultSet = prepStmt.executeQuery();

            while (resultSet.next()){
                if(!searchNoteIDs.contains(resultSet.getInt("NoteID")))
                    searchNoteIDs.add(resultSet.getInt("NoteID"));
            }
            resultSet.close();
            prepStmt.close();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: captureSearchNoteIDs");
        }
    }

    private void deleteTableRecord(String table, String field, Integer id){
        try{
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            String deleteSelection = "DELETE FROM " + table + " WHERE " + field + " = " + id;
            prepStmt = sConn.prepareStatement(deleteSelection);
            prepStmt.execute();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: deleteTableRecord");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void deleteTableRecord(String table, String fieldAuth, Integer aID, String fieldSrc, Integer sID ){
        try{
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            String deleteSelection = "DELETE FROM " + table + " WHERE " + fieldAuth + " = " + aID + " AND " + fieldSrc + " = " + sID;
            prepStmt = sConn.prepareStatement(deleteSelection);
            prepStmt.execute();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: deleteTableRecord");
        }
    }

    private void displayFailureMessage(String msg, String title){
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE );
    }

    private void replaceData(Integer noteID, String table, String tableID, String field, String fieldData){
        try{
            String strUpdate = "UPDATE " + table +
                    " SET " + field + " = '" + fieldData.replace("'", "''") + "'" +
                    " WHERE EXISTS (SELECT NoteID FROM Notes" +
                    " WHERE " + table + "." + tableID +  " = Notes." + tableID + " and Notes.NoteID = " + noteID + ")";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: updateField");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(strUpdate);
            prepStmt.execute();
            prepStmt.close();

        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage("The " + field + " field in table " + table + " could not be found.\n\n" + ex.toString(), "Error: updateField");
        }
    }

    private void searchNoteIDs(Integer noteID){
        try{
            String searchTopic = "SELECT n.NoteID, s.SourceID, s.SourceType, s.Title, c.Summary FROM Comment as c " +
                    "LEFT JOIN Notes as n ON n.CommentID = c.CommentID " +
                    "LEFT JOIN Source as s ON n.SourceID = s.SourceID " +
                    "WHERE n.NoteID = " + noteID;
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchTopic");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchTopic);
            resultSet = prepStmt.executeQuery();

            while(resultSet.next()) {
                tableData.add(new Source(resultSet.getInt("NoteID"),
                        resultSet.getString("SourceType"),
                        resultSet.getString("Title")  + " ~ " + retrieveAllSourceAuthors(resultSet.getInt("SourceID")),
                        resultSet.getString("Summary")));
            }
            this.SourceList = tableData;
            resultSet.close();
            prepStmt.close();


        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: searchTopic");
        }
    }

    private void searchTable(boolean containsAll, String criteria, String table, List<String> columnNames ){
        try{
            String searchString = "SELECT " + table + "ID FROM " + table + " WHERE" + searchColumns(containsAll, criteria, columnNames);
            ArrayList<Integer> temp = new ArrayList<>();

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: searchTopic");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchString);
            resultSet = prepStmt.executeQuery();

            while (resultSet.next()){
                temp.add(resultSet.getInt(table + "ID"));
            }
            temp.forEach((Integer i) -> captureSearchNoteIDs(table, i));
            resultSet.close();
            prepStmt.close();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: completeSearch");
        }
    }

    private void updateNotesTable(Integer noteID, String tablesID, Integer idData){
        try {
            String updateNotes = "UPDATE Notes SET " + tablesID + " = " + idData + " WHERE Notes.NoteID = " + noteID;
            prepStmt = sConn.prepareStatement(updateNotes);
            prepStmt.execute();
            prepStmt.close();
        }catch (SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: updateNotesTable");
        }

    }

    /*PRIVATE FUNCTIONS:
    ==================================================================================================================*/
    private String buildAuthorValues(String fName, String mName, String lName, String sfx) {
        return "('" + fName + "', '" + mName + "', '" + lName + "', '" + sfx + "')";
    }

    ObservableList<Author> captureAuthors(Integer idSource){
        ObservableList<Author> authors = FXCollections.observableArrayList();
        try {
            String sourceQuery = "SELECT a.FirstName, a.MiddleName, a.LastName, a.Suffix FROM Author as a " +
                    "LEFT JOIN Author_By_Source as abs ON abs.AuthorID = a.AuthorID " +
                    "WHERE abs.SourceID = " + idSource;

            if(sConn == null)
                sConn = DBConnection.getConnection();
            if (sConn == null) {
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeSourceBox");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(sourceQuery);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()){
                authors.add(new Author(rs.getString("FirstName"), rs.getString("MiddleName"),
                        rs.getString("LastName"), rs.getString("Suffix")));
            }
            prepStmt.close();
            rs.close();

        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage("Failed to capture authors", "Error: captureAuthors");
        }
        return authors;
    }

    private String concatenateAuthors(ObservableList<Author> src){
        StringBuilder str = new StringBuilder();
        for(int i = 0 ; i < src.size(); i++){
            str.append(src.get(i).getFirstName().trim());
            if(!src.get(i).getMiddleName().isEmpty())
                str.append(" ").append(src.get(i).getMiddleName().trim());
            if(!src.get(i).getLastName().isEmpty())
                str.append(" ").append(src.get(i).getLastName().trim());
            if(!src.get(i).getSuffix().isEmpty())
                str.append(" ").append(src.get(i).getSuffix().trim());
            if(src.size()> 1 && i < src.size())
                str.append("; ");
        }
        return str.toString().trim();
    }

    public Integer count(String table, String field, Integer tableID) {
        int count = 0;
        try {
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: captureNoteIDs");
                System.exit(0);
            }
            String countIDs = "SELECT COUNT(*) FROM " + table + " WHERE " + field + " = " + tableID;
            prepStmt = sConn.prepareStatement(countIDs);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next())
                count = resultSet.getInt(1);

        }catch (SQLException ex){
            err = true;
        }
        return count;
    }

    public Integer count(String table, String field, String value) {
        int count = 0;
        try {
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: captureNoteIDs");
                System.exit(0);
            }
            String countValue = "SELECT COUNT(*) FROM " + table + " WHERE " + field + " = '" + value.replace("'", "''") + "'";
            prepStmt = sConn.prepareStatement(countValue);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next())
                count = resultSet.getInt(1);

        }catch (SQLException ex){
            err = true;
        }
        return count;
    }

    private Integer findAuthor(Author auth) {
        try {
            if (sConn == null)
                sConn = DBConnection.getConnection();
            prepStmt = sConn.prepareStatement("SELECT AuthorID FROM Author " +
                    "WHERE FirstName = '" + auth.getFirstName().replace("'", "''") + "' " +
                    "AND MiddleName = '" + auth.getMiddleName().replace("'", "''") + "' AND " +
                    "LastName = '" + auth.getLastName().replace("'", "''") + "' AND " +
                    "Suffix = '"+ auth.getSuffix().replace("'", "''") + "'");
            resultSet = prepStmt.executeQuery();
            if(resultSet.next())
                return resultSet.getInt("AuthorID");
            prepStmt.close();
            resultSet.close();

        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: Search.findAuthor");
        }
        return 0;
    }

    private Integer findAuthor(Integer sourceID) {
        int id = 0;
        try {
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement("SELECT AuthorID FROM Author_By_Source WHERE SourceID =" + sourceID);
            resultSet = prepStmt.executeQuery();
            if(resultSet.next())
                id = resultSet.getInt("AuthorID");
            prepStmt.close();
            resultSet.close();
            return id;

        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: Search.findAuthor");
        }
        return id;
    }

    private ObservableList<Integer> findAuthorIDs(Integer sourceID){
        ObservableList<Integer> authorIDs = FXCollections.observableArrayList();
        try{
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            String authorSearch = "SELECT AuthorID FROM Author_By_Source WHERE SourceID = " + sourceID;
            prepStmt = sConn.prepareStatement(authorSearch);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next())
                authorIDs.add(resultSet.getInt("AuthorID"));
            prepStmt.close();
            resultSet.close();
            authorIDs = authorIDs.sorted();
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: Search.findAuthorIDs");
        }
        return authorIDs;
    }

    private Integer findDataPKID(String table, String field, String data){
        int PKID = 0;
        try{
            String searchKey = "SELECT " + table + "ID FROM " + table + " WHERE " + field + " = '" + data.replace("'","''") + "'";
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: findPKIDonNotes");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchKey);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next()){
                PKID = resultSet.getInt(table + "ID");
            }
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage("The Search.findDataPKID method failed while attempting to return a PKID for the " + table + "ID in the " + table + " table.\n\n" + ex.toString(), "Error: findDataPKID");
        }

        return PKID;
    }

    private Integer findFile(Integer noteID) {
        int id = 0;
        try {
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement("SELECT FileID FROM File_By_Note WHERE NoteID =" + noteID);
            resultSet = prepStmt.executeQuery();
            if(resultSet.next())
                id = resultSet.getInt("FileID");
            prepStmt.close();
            resultSet.close();
            return id;

        } catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: Search.findAuthor");
        }
        return id;
    }

    private ObservableList<Integer> findFileIDs(Integer noteID){
        ObservableList<Integer> fileIDs = FXCollections.observableArrayList();
        try{
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                err = true;
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: displaySelection");
                System.exit(0);
            }
            String fileSearch = "SELECT FileID FROM File_By_Note WHERE NoteID = " + noteID;
            prepStmt = sConn.prepareStatement(fileSearch);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next())
                fileIDs.add(resultSet.getInt("FileID"));
            prepStmt.close();
            resultSet.close();
            fileIDs = fileIDs.sorted();
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: Search.findAuthorIDs");
        }
        return fileIDs;
    }

    Integer findPKIDonNotes(Integer notesID, String tablesID){
        int PKID = -1;
        try{
            String searchKey = "SELECT Notes." + tablesID + " FROM Notes WHERE Notes.NoteID = " + notesID;
            if(sConn == null)
                sConn = DBConnection.getConnection();
            if(sConn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: findPKIDonNotes");
                System.exit(0);
            }
            prepStmt = sConn.prepareStatement(searchKey);
            resultSet = prepStmt.executeQuery();
            while(resultSet.next()){
                PKID = resultSet.getInt(tablesID);
            }
        }catch(SQLException ex){
            err = true;
            ex.printStackTrace();
            displayFailureMessage("The " + tablesID + " foreign " +
                    "key could not be found in the Notes table to perform an update.\n\n" + ex.toString(), "Error: findPKIDonNotes");
        }
        return PKID;
    }

    private String retrieveAllSourceAuthors(Integer sourceID){
        ObservableList<Author> sourceAuthors = captureAuthors(sourceID); // Builds SourceAuthors
        return concatenateAuthors(sourceAuthors);
    }

    private String searchColumns(boolean containsAll, String criteria, List<String> columns){

        StringBuilder sb = new StringBuilder();

        for(String str : columns){
            if(columns.indexOf(str) == 0)
                sb.append(searchWords(containsAll, str, criteria));
            else
                sb.append(" OR ").append(searchWords(containsAll, str, criteria));
        }
        return sb.toString();
    }

    private String searchWords(boolean containsAll, String column, String words){
        StringBuilder sb = new StringBuilder();
        List<String> word = Arrays.asList(words.split(" "));
        String operator = " OR ";
        if(containsAll)
            operator = " AND ";

        for(String str : word){
            if(word.indexOf(str) == 0)
                sb.append(" ").append(column).append(" LIKE '%").append(str.replace("'", "''")).append("%'");
            else
                sb.append(operator).append(column).append(" LIKE '%").append(str.replace("'", "''")).append("%'");
        }
        return sb.toString();
    }

}
