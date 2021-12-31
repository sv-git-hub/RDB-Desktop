package researchApp;

import dbUtil.DBConnection;
import dbUtil.DatabaseSource;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import dbConnect.ConnectionModel;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import logging.LoggerWrapper;
import logging.UserLog;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import myAutoCompletion.ComboBoxAutoComplete;
import printer.PrintOut;
import xmlManager.CreateXMLFileDOMParser;
import xmlManager.ReadXMLFileDOMParser;

// Used for JOption message boxes
import javax.swing.*;

public class ResearchController implements Initializable {
    private ConnectionModel connModel = new ConnectionModel();

    // Labels: =================================
    @FXML private Label lblDatabase;
    @FXML private Label lblStatus;
    @FXML private Label editHyperlink;
    @FXML private Label editTable;
    @FXML private Label editTopic;
    @FXML private Label editQuestion;
    @FXML private Label lblRecords;

    // Tables: =================================
    @FXML private TableView<Source> tblSources;
    @FXML private TableColumn<Source, Integer> tblID;
    @FXML private TableColumn<Source, String> tblSourceType;
    @FXML private TableColumn<Source, String> tblTitle;
    @FXML private TableColumn<Source, String> tblSummary;
    @FXML private TableView<Author> tblAuthors;
    @FXML private TableColumn<Author, String> tblFirstName;
    @FXML private TableColumn<Author, String> tblMiddleName;
    @FXML private TableColumn<Author, String> tblLastName;
    @FXML private TableColumn<Author, String> tblSuffix;
    @FXML private TableView<NoteFile> tblFiles;
    @FXML private TableColumn<NoteFile, String> tblFileName;
    @FXML private TableColumn<NoteFile, String> tblFilePath;
    @FXML private TableView<NoteFile> tblSearchFiles;
    @FXML private TableColumn<NoteFile, String> tblSearchFileName;
    @FXML private TableColumn<NoteFile, String> tblSearchFilePath;

    // Author objects ==========================
    @FXML private TextField tbxAuthorFirst1;
    @FXML private TextField tbxAuthorMiddle1;
    @FXML private TextField tbxAuthorLast1;
    @FXML private TextField tbxAuthorSuffix;

    // TextFields: =============================
    @FXML private TextField tbxTitle;
    @FXML private TextField dtYear;
    @FXML private TextField dtMonth;
    @FXML private TextField dtDay;
    @FXML private TextField tbxSearchDate;
    @FXML private TextField tbxVolume;
    @FXML private TextField tbxSearchVolume;
    @FXML private TextField tbxEdition;
    @FXML private TextField tbxSearchEdition;
    @FXML private TextField tbxIssue;
    @FXML private TextField tbxSearchIssue;
    @FXML private TextField tbxPages;
    @FXML private TextField tbxSummary;
    @FXML private TextField tbxSearchPages;
    @FXML private TextField tbxTimeStamp;
    @FXML private TextField tbxSearchTimeStamp;
    @FXML private TextField tbxHyperlink;
    @FXML private TextField tbxCustomSearch;

    // Hyperlinks: ===========================
    @FXML private Hyperlink tbxSearchHyperlink;

    // CheckBoxes: ===========================
    @FXML private CheckBox ckxContainsAll;

    // Buttons: ===========================
    @FXML private Button btnEnter;
    @FXML private Button btnRemove;
    @FXML private Button btnSearchAttachFile;
    @FXML private Button btnSearchRemoveFile;
    @FXML private Button btnOpenFile;

    // TextArea: =============================
    @FXML private TextArea tbxTerm;
    @FXML private TextArea tbxSearchTerm;
    @FXML private TextArea tbxQuote;
    @FXML private TextArea tbxSearchQuote;
    @FXML private TextArea tbxComment;
    @FXML private TextArea tbxSearchComment;

     // ChoiceBox/ComboBox: ===================
    @FXML private ChoiceBox<String> cbxSourceType;
    @FXML private ComboBox<String> cbxSource;
    @FXML private ComboBox<String> cbxAuthors;
    @FXML private ComboBox<String> cbxTopic;
    @FXML private ComboBox<String> cbxSearchTopic;
    @FXML private ComboBox<String> cbxQuestion;
    @FXML private ComboBox<String> cbxSearchQuestion;

     // Menu: =================================
    @FXML private MenuItem menuNewDatabase;
    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuAdd;
    @FXML private MenuItem menuPrint;
    @FXML private MenuItem menuUpdate;
    @FXML private MenuItem menuMarkDelete;
    @FXML private MenuItem menuUnMarkForDelete;
    @FXML private MenuItem menuReviewForDelete;
    @FXML private MenuItem menuPermDelete;
    @FXML private MenuItem menuImport;
    @FXML private MenuItem menuExport;
    @FXML private MenuItem menuExportAll;

    // Tab: ==================================
    @FXML private TabPane paneTab;
    @FXML private Tab tabEntry;
    @FXML private Tab tabSearch;
    @FXML private Tab tabFile;

    // Boolean: ==============================
    private boolean boolClearAuthorPressed;
    private boolean boolSkipSearchData;
    private boolean boolRefresh;
    private boolean firstSession = true;
    private boolean importingData = false;

    // AutoCompletionFindings
    private AutoCompletionBinding autoQuestion;
    private AutoCompletionBinding autoTopic;
    private AutoCompletionBinding autoSummaries;

    private AutoCompletionBinding autoSource;
    private ComboBoxAutoComplete skinSearchTopic;
    private ComboBoxAutoComplete skinSearchQuestion;
    private ComboBoxAutoComplete skinAuthors;

    private Integer CurrentRowPos;
    private Integer CurrentNoteID;
    private String CurrentSourceType;
    private Source CurrentSource;
    private String CurrentSummary;
    private String CurrentTopic;
    private String CurrentQuestion;

    private ObservableList<String> CurrentSelectionData;
    private ObservableList<String> Summaries;
    private ObservableList<NoteFile> CurrentFiles;
    private String[] QuestionList;
    private String[] TopicList;

    private ArrayList<TextArea> arrEntryTextAreas = new ArrayList<>();
    private ArrayList<TextField> arrEntryTextFields = new ArrayList<>();
    private ArrayList<TextArea> arrSearchTextAreas = new ArrayList<>();
    private ArrayList<TextField> arrSearchTextFields = new ArrayList<>();
    private ArrayList<TextField> arrEntrySourceTextFields = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> Sources;

    private String dbPath = GlobalVariables.RESEARCH_DB_PATH;
    private String selectedDBPath;

    /* ===================================== INITIALIZING APP METHODS ================================================

    ==================================================================================================================*/

    public void initialize(URL url, ResourceBundle rb){
        LoggerWrapper.getInstance().myLogger.fine("FXML initialize launch");
        if (this.connModel.isDatabaseConnected()) {
            this.lblStatus.setText("Connected..." + GlobalVariables.RESEARCH_DB_PATH + DatabaseSource.getDatabase());
            this.lblDatabase.setText(DatabaseSource.getDatabase());
        }
        setup();
        loadOnInitializationOnly();
        LoggerWrapper.getInstance().myLogger.config("Initialization / Setup complete");

        if( "y".equals(UserLog.getUserInfo("firstInstall"))) {
            UserLog.setUserInfo("firstInstall", "n");
            about();
        }
    }

    @FXML private void initializeSourceBox(){
        ObservableList<String> sources = FXCollections.observableArrayList();
        Search search = new Search();
        search.captureSources();
        Sources = search.Sources;

        if (!sources.contains(null))
            sources.add(null);
        /*                           0         1         2       3
        Sources array positions: SourceID, SourceType, Title, Author*/
        for(Integer src : Sources.keySet()) {
            String str = Sources.get(src).get(2) + " ~ " + Sources.get(src).get(3);
            if (!sources.contains(str))
                sources.add(str);
        }

        sources = sources.sorted();
        this.cbxSource.setMaxHeight(30);
        this.cbxSource.setPrefWidth(868);
        this.cbxSource.setEditable(true);
        this.cbxSource.setItems(sources);

    }

    @FXML private void initializeTopicBox() {
        // Known defect with a 'null' option. However, this will continue to work as expected
        // https://stackoverflow.com/questions/25877323/no-select-item-on-javafx-combobox

        Search search = new Search();
        search.captureTopics();
        ObservableList<String> Topics = search.TopicData;

        Topics = Topics.sorted();
        int i = 0;
        TopicList = new String[Topics.size()]; // Reset
        for (String s : Topics) {
            TopicList[i++] = s;
        }
        this.cbxTopic.setItems(Topics);
        this.cbxTopic.setEditable(true);
        //this.cbxSearchTopic.setEditable(true);
        this.cbxSearchTopic.getItems().clear();
        this.cbxSearchTopic.getItems().addAll(TopicList);
        this.cbxSearchTopic.setEditable(false);
    }

    @FXML private void initializeQuestionBox(){
        ObservableList<String> questions = FXCollections.observableArrayList();
        Connection conn = null;
        try {
            String questionQuery = "SELECT Question FROM Question";
            conn = DBConnection.getConnection();
            if(conn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeQuestionBox");
                System.exit(0);
            }
            PreparedStatement ps = conn.prepareStatement(questionQuery);
            ResultSet rs = ps.executeQuery();

            if(!questions.contains(null))
                questions.add(null);

            while(rs.next()){
                if(!questions.contains(rs.getString("Question")))
                questions.add(rs.getString("Question"));
            }
            ps.close();
            rs.close();
            conn.close();
            questions = questions.sorted();

            int i = 0;
            QuestionList = new String[questions.size()];    // Reset
            for(String q : questions){
                QuestionList[i++] = q;
            }

            this.cbxQuestion.setItems(questions);
            this.cbxQuestion.setEditable(true);
            this.cbxQuestion.setPrefWidth(600);

            this.cbxSearchQuestion.getItems().clear();
            this.cbxSearchQuestion.getItems().addAll(QuestionList);
            this.cbxSearchQuestion.setEditable(false);

        }catch(SQLException ex){
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: InitializeQuestionBox");
        }finally{
            try{
                if(conn != null) conn.close(); // throws exception here
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                displayFailureMessage(e.toString(), "Error: InitializeQuestionBox");
            }
        }
    }

    @FXML private void initializeAuthorBox(){
        ObservableList<String> authors = FXCollections.observableArrayList();
        String strAuthor;
        Connection conn;
        try {
            String authorQuery = "SELECT a.AuthorID, a.FirstName, a.MiddleName, a.LastName, a.Suffix FROM Author as a";

            conn = DBConnection.getConnection();
            if(conn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeAuthorBox");
                System.exit(0);
            }
            PreparedStatement ps = conn.prepareStatement(authorQuery);
            ResultSet rs = ps.executeQuery();

            if(!authors.contains(null))
                authors.add(null);

            while(rs.next()){
                strAuthor = concatenateAuthor(rs.getString("FirstName").trim(), rs.getString("MiddleName").trim(),
                        rs.getString("LastName").trim(), rs.getString("Suffix"));
                if(!authors.contains(strAuthor + " - id: " + rs.getString("AuthorID"))){
                    authors.add(strAuthor + " - id: " + rs.getString("AuthorID"));
                }
            }
            ps.close();
            rs.close();
            this.cbxAuthors.setEditable(false);
            this.cbxAuthors.setItems(authors);
            this.cbxAuthors.setVisibleRowCount(5);

        }catch(SQLException ex){
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: InitializeAuthorBox");
        }


    }

    @FXML private void about(){
        try{
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("about.fxml"));

            Scene scene = new Scene(root);
            stage.getIcons().add(new Image("images/icon.png"));
            stage.setScene(scene);
            stage.setTitle("About " + GlobalVariables.TITLE);
            stage.setResizable(false);
            stage.setAlwaysOnTop(true);
            stage.show();
        }catch(IOException ex){
            JOptionPane.showMessageDialog(null, "about window failed to open.", "about ResearchDB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeTextBoxes(){
        this.tbxComment.setWrapText(true);
        this.tbxTerm.setWrapText(true);
        this.tbxQuote.setWrapText(true);

        this.tbxSearchComment.setWrapText(true);
        this.tbxSearchTerm.setWrapText(true);
        this.tbxSearchComment.setWrapText(true);
    }

    private void initializeSummaryBox(){
        ObservableList<String> summarys;
        Search search = new Search();
        search.captureSummaries();
        summarys = search.SummaryData;
        Summaries = summarys.sorted();
    }

    private void initializeSearchTab(){
        this.tbxSearchDate.setDisable(true);
        this.tbxSearchEdition.setDisable(true);
        this.tbxSearchHyperlink.setDisable(true);
        this.tbxSearchIssue.setDisable(true);
        this.tbxSearchPages.setDisable(true);
        this.tbxSearchTimeStamp.setDisable(true);
        this.tbxSearchVolume.setDisable(true);
        this.tbxSearchComment.setDisable(true);
        this.tbxSearchQuote.setDisable(true);
        this.tbxSearchTerm.setDisable(true);
        this.tblSearchFiles.setDisable(true);
        this.btnSearchAttachFile.setDisable(true);
        this.btnSearchRemoveFile.setDisable(true);
        this.btnOpenFile.setDisable(true);
        this.tabFile.setText("Files(0)");
    }

    private void initializeTableView(){
        // Sources Table
        tblID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        tblSourceType.setCellValueFactory(new PropertyValueFactory<>("SourceType"));
        tblTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));
        tblSummary.setCellValueFactory(new PropertyValueFactory<>("Summary"));

        // Author Table
        tblFirstName.setCellValueFactory(new PropertyValueFactory<>("FirstName"));
        tblMiddleName.setCellValueFactory(new PropertyValueFactory<>("MiddleName"));
        tblLastName.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        tblSuffix.setCellValueFactory(new PropertyValueFactory<>("Suffix"));

        // Files and SearchFiles Tables
        tblFileName.setCellValueFactory(new PropertyValueFactory<>("FileName"));
        tblFilePath.setCellValueFactory(new PropertyValueFactory<>("FilePath"));
        tblFiles.setEditable(true);

        tblSearchFileName.setCellValueFactory(new PropertyValueFactory<>("FileName"));
        tblSearchFilePath.setCellValueFactory(new PropertyValueFactory<>("FilePath"));
        tblSearchFiles.setEditable(true);

        tblAuthors.setEditable(true);
        tblAuthors.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        tblMiddleName.setCellFactory(TextFieldTableCell.forTableColumn());
        tblLastName.setCellFactory(TextFieldTableCell.forTableColumn());
        tblSuffix.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    private void checkSourceSelectionBox(){
        try{
            if(this.cbxSource.getValue() == null || this.cbxSource.getValue().isEmpty()){
                for(TextField tField : arrEntrySourceTextFields){
                    tField.setDisable(false);
                }
                this.btnEnter.setDisable(false);
                this.btnRemove.setDisable(false);
                this.cbxSourceType.setDisable(false);
                this.cbxSourceType.getSelectionModel().clearSelection();
                this.cbxAuthors.setDisable(false);
                this.tblAuthors.getSelectionModel().clearSelection();
                this.tblAuthors.setDisable(false);
            }else{
                for(TextField tField : arrEntrySourceTextFields){
                    tField.setDisable(true);
                    tField.clear();
                }
                this.btnEnter.setDisable(true);
                this.btnRemove.setDisable(true);
                this.tblAuthors.setDisable(true);
                this.cbxSourceType.setDisable(true);
                this.cbxAuthors.setDisable(true);
                this.tbxTimeStamp.clear();
                this.tbxPages.clear();
                this.tbxHyperlink.clear();
            }
        }catch (Error ex){
            displayFailureMessage("Error in ResearchController.checkSourceSelectionBox:\n" + ex.toString(),"Error: checkSourceSelectionBox Method" );
        }
    }

    private void loadOnInitializationOnly(){
        loadArrEntrySourceTextFields();
        loadArrEntryTextAreas();
        loadArrEntryTextFields();
        loadArrSearchTextAreas();
        loadArrSearchTextFields();
        loadEntrySourceTypes();
        setupListeners();
        setupTable();
        setupMenu();
        setupToolTips();
        cleanUpTemp();

        // temporary:
        //this.menuImportExport.setVisible(false);
    }

    private void loadArrEntrySourceTextFields(){
        arrEntrySourceTextFields.add(tbxAuthorFirst1);
        arrEntrySourceTextFields.add(tbxAuthorMiddle1);
        arrEntrySourceTextFields.add(tbxAuthorLast1);
        arrEntrySourceTextFields.add(tbxAuthorSuffix);
        arrEntrySourceTextFields.add(tbxTitle);
        arrEntrySourceTextFields.add(dtYear);
        arrEntrySourceTextFields.add(dtMonth);
        arrEntrySourceTextFields.add(dtDay);
        arrEntrySourceTextFields.add(tbxVolume);
        arrEntrySourceTextFields.add(tbxEdition);
        arrEntrySourceTextFields.add(tbxIssue);
    }

    private void loadArrEntryTextAreas(){
        arrEntryTextAreas.add(tbxTerm);
        arrEntryTextAreas.add(tbxQuote);
        arrEntryTextAreas.add(tbxComment);
    }

    private void loadArrEntryTextFields(){
        arrEntryTextFields.add(tbxAuthorFirst1);
        arrEntryTextFields.add(tbxAuthorMiddle1);
        arrEntryTextFields.add(tbxAuthorLast1);
        arrEntryTextFields.add(tbxTitle);
        arrEntryTextFields.add(dtYear);
        arrEntryTextFields.add(dtMonth);
        arrEntryTextFields.add(dtDay);
        arrEntryTextFields.add(tbxVolume);
        arrEntryTextFields.add(tbxEdition);
        arrEntryTextFields.add(tbxIssue);
        arrEntryTextFields.add(tbxPages);
        arrEntryTextFields.add(tbxSummary);
        arrEntryTextFields.add(tbxTimeStamp);
        arrEntryTextFields.add(tbxHyperlink);
    }

    private void loadArrSearchTextAreas(){
        arrSearchTextAreas.add(tbxSearchTerm);
        arrSearchTextAreas.add(tbxSearchQuote);
        arrSearchTextAreas.add(tbxSearchComment);
    }

    private void loadArrSearchTextFields(){
        arrSearchTextFields.add(tbxSearchDate);
        arrSearchTextFields.add(tbxSearchVolume);
        arrSearchTextFields.add(tbxSearchEdition);
        arrSearchTextFields.add(tbxSearchIssue);
        arrSearchTextFields.add(tbxSearchPages);
        arrSearchTextFields.add(tbxSearchTimeStamp);
        //arrSearchTextFields.add(tbxCustomSearch);
    }

    private void loadEntrySourceTypes(){
        this.cbxSourceType.setItems(FXCollections.observableArrayList(
                "Article", "Audio", "Book", "Journal", "Periodical", "Question",
                "Quote", "Term", "Video", "Website", "Other"));
    }

    private void setup(){
        initializeSearchTab();
        initializeTopicBox();
        initializeQuestionBox();
        initializeTextBoxes();
        initializeSourceBox();
        initializeAuthorBox();
        initializeTableView();
        initializeSummaryBox();
        setupAutoCompletion();
    }

    private void setupMenu(){
        menuNewDatabase.setVisible(true);
        menuOpen.setVisible(true);
        if(tabEntry.isSelected()) {
            menuAdd.setDisable(false);
        }else{
            menuAdd.setDisable(true);}
        menuUpdate.setDisable(true);
        menuPrint.setDisable(true);
        menuExport.setDisable(true);
        menuMarkDelete.setDisable(true);
        menuUnMarkForDelete.setDisable(true);
        menuPermDelete.setDisable(true);

        menuImport.setDisable(false);
        menuReviewForDelete.setDisable(false);

    }

    private void setupListeners(){
        // Search tab
        this.tbxSearchTimeStamp.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue) {
                validateSearchTimeStamp();
            }
        });
        this.tbxTimeStamp.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue) {
                validateTimeStamp();
            }
        });
        this.dtMonth.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue) {
                validateMonth();
            }
        });
        this.dtDay.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue) {
                validateDay();
            }
        });
        this.dtYear.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (oldValue) {
                validateYear();
            }
        });
        this.paneTab.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
            tabEntry.setOnSelectionChanged(event -> {
                if (tabEntry.isSelected()) {
                    menuAdd.setDisable(false);
                    menuPrint.setDisable(true);
                    menuUpdate.setDisable(true);
                    menuExport.setDisable(true);
                    menuMarkDelete.setDisable(true);
                    menuUnMarkForDelete.setDisable(true);
                    menuReviewForDelete.setDisable(true);
                    menuPermDelete.setDisable(true);
                    menuUpdate.setDisable(true);
                }
            });
            tabSearch.setOnSelectionChanged(event -> {
                if (tabSearch.isSelected()) {
                    if(!this.tbxSearchComment.isDisable()){
                        menuUpdate.setDisable(false);
                        menuPrint.setDisable(false);
                        menuExport.setDisable(false);
                        menuMarkDelete.setDisable(false);
                        menuUnMarkForDelete.setDisable(false);
                        menuPermDelete.setDisable(false);

                    }
                    menuAdd.setDisable(true);
                    menuReviewForDelete.setDisable(false);
                }
            });

        });
        this.cbxSearchQuestion.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if (newValue != null) {
                if (!boolSkipSearchData) {
                    searchQuestion();
                }
            } else {
                if (!boolSkipSearchData) {
                    this.cbxSearchQuestion.getSelectionModel().clearSelection();
                    clearSearchFields();
                }
            }

        });
        this.cbxSearchTopic.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if (newValue != null) {
                if (!boolSkipSearchData) {
                    searchTopic(newValue);
                }
            } else {
                if (!boolSkipSearchData) {
                    this.cbxSearchTopic.getSelectionModel().clearSelection();
                    clearSearchFields();
                }
            }

        });

        // Entry tab
        this.cbxSource.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (this.cbxSource.getValue() != null) {
                for (Integer key : Sources.keySet()) {
                    if (Sources.get(key).get(2).contentEquals(cbxSource.getValue().split(" ~ ")[0]) &&
                            Sources.get(key).get(3).equals(cbxSource.getValue().split(" ~ ")[1])) {
                        this.cbxSourceType.getSelectionModel().select(Sources.get(key).get(1));
                    }
                }
            }
            checkSourceSelectionBox();
        });
        this.cbxAuthors.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            {
                if(newValue != null) {
                    if(newValue.contains(" - id: "))
                        if (!boolClearAuthorPressed) uploadAuthor(newValue);
                }else{
                    this.cbxAuthors.getSelectionModel().clearSelection();
                }
            }
        });

    }

    private void setupTable(){this.tblSources.setEditable(true);
    }

    private void setupToolTips(){
        Tooltip tooltipEntryTopic = new Tooltip();
        tooltipEntryTopic.setText("Enter one to three words. Use the 'Summary' to be point specific.");
        this.cbxTopic.setTooltip(tooltipEntryTopic);

        Tooltip tooltipEntrySummary = new Tooltip();
        tooltipEntrySummary.setText("Summary expands the Topic but is brief and point specific.");
        this.tbxSummary.setTooltip(tooltipEntrySummary);

    }
    @SuppressWarnings("unchecked")
    private void setupAutoCompletion(){

        if(firstSession){
            // Search tab:
            skinSearchTopic = new ComboBoxAutoComplete<>(this.cbxSearchTopic);
            skinSearchQuestion = new ComboBoxAutoComplete<>(this.cbxSearchQuestion);
            // Entry tab:
            skinAuthors = new ComboBoxAutoComplete<>(this.cbxAuthors);
            firstSession = false;
        }else{
            // Search tab:
            skinSearchTopic.setOriginalItems(FXCollections.observableArrayList(this.cbxSearchTopic.getItems()));
            skinSearchQuestion.setOriginalItems(FXCollections.observableArrayList(this.cbxSearchQuestion.getItems()));
            // Entry tab:
            skinAuthors.setOriginalItems(FXCollections.observableArrayList(this.cbxAuthors.getItems()));
        }

        // Entry tab:
        autoQuestion = TextFields.bindAutoCompletion(this.cbxQuestion.getEditor(), this.cbxQuestion.getItems());
        autoQuestion.setPrefWidth(this.cbxQuestion.getPrefWidth());

        autoTopic = TextFields.bindAutoCompletion(this.cbxTopic.getEditor(), this.cbxTopic.getItems());
        autoTopic.setPrefWidth(this.cbxTopic.getPrefWidth());

        autoSummaries = TextFields.bindAutoCompletion(this.tbxSummary, this.Summaries);
        autoSummaries.setPrefWidth(this.tbxSummary.getPrefWidth());

        autoSource = TextFields.bindAutoCompletion(this.cbxSource.getEditor(), this.cbxSource.getItems());
        autoSource.setPrefWidth(this.cbxSource.getPrefWidth());
    }



    /* =========================================== JAVA METHODS ======================================================

    ==================================================================================================================*/

    // A
    private void activateSearchFields(){
        for (TextField tField : arrSearchTextFields){
            tField.setDisable(false);
        }
        for(TextArea tArea : arrSearchTextAreas){
            tArea.setDisable(false);
        }
        this.tbxSearchHyperlink.setDisable(false);
        this.btnSearchAttachFile.setDisable(false);
        this.btnSearchRemoveFile.setDisable(false);
        this.btnOpenFile.setDisable(false);
        this.tblSearchFiles.setDisable(false);
        this.btnOpenFile.setDisable(false);
        this.btnSearchAttachFile.setDisable(false);
        this.btnSearchRemoveFile.setDisable(false);

        this.editHyperlink.setOnMouseClicked(event -> editHyperlink());
        this.editTable.setOnMouseClicked(event -> editTable());
        this.editTopic.setOnMouseClicked(event -> editTopic());
        this.editQuestion.setOnMouseClicked(event -> editQuestion());
    }

    // C
    private void changeDatabaseConnection(){
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            setup();
            loadOnInitializationOnly();
            if (conn != null) {
                conn.close();
            }
            LoggerWrapper.getInstance().myLogger.config("setup complete");
        }catch(SQLException ex ){
            LoggerWrapper.getInstance().myLogger.config("Connection failure on changeDatabaseConnection");
        }finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }catch(SQLException e){
                e.printStackTrace();
                displayFailureMessage(e.toString(), "Error: changeDatabaseConnection");
            }
        }
    }
    private void cleanUpTemp(){
        try {
            File file = new File(GlobalVariables.TEMP);
            String[] myFiles;
            if(file.isDirectory()){
                myFiles = file.list();
                if(myFiles != null) {
                    for (String s : myFiles) {
                        File myFile = new File(GlobalVariables.TEMP + s);
                        //noinspection ResultOfMethodCallIgnored
                        myFile.delete();
                    }
                }
            }
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
    }
    private void clearConnection(){
        try{
            DBConnection.close();
            resetGlobalVariables();
        }
        catch (SQLException e){
            e.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("Error: clearConnection");
        }
    }
    private String concatenateAuthor(String fName, String mName, String lName, String sfx){
        StringBuilder sb = new StringBuilder();
        for(String str : new String[]{fName, mName, lName, sfx}){
            if (!str.isEmpty()){
                //noinspection ExcessiveRangeCheck
                if(fName.length() > 0 && fName.length() < 2){
                    sb.append(str).append( ". ");
                }else if(str.length() > 1){
                    sb.append(str).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }

    // D
    private void deactivateSearchFields(){
        for (TextField tField : arrSearchTextFields){
            tField.setDisable(true);
        }
        for(TextArea tArea : arrSearchTextAreas){
            tArea.setDisable(true);
        }
        this.tbxSearchHyperlink.setDisable(true);

        this.editHyperlink.setOnMouseClicked(null);
        this.editTable.setOnMouseClicked(null);
        this.editTopic.setOnMouseClicked(null);
        this.editQuestion.setOnMouseClicked(null);

        this.tblSearchFiles.setDisable(true);
        this.btnOpenFile.setDisable(true);
        this.btnSearchAttachFile.setDisable(true);
        this.btnSearchRemoveFile.setDisable(true);
    }
    private void deleteDB(){
        Stage stage = new Stage();
        Label lbl = new Label();
        ListView<String> listView;
        listView = listDBs("delete");
        listView.setLayoutX(20);
        listView.setLayoutY(30);

        listView.setMaxHeight(150);
        listView.setMinHeight(listView.getMaxHeight());
        listView.setMaxWidth(260);
        //listView.setMinWidth(listView.getMaxWidth());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        lbl.setLayoutX(20);
        lbl.setLayoutY(5);
        lbl.setText("Please select a database to delete:");

        Button btnDelete = new Button();
        btnDelete.setOnAction( e -> getDBFromList(stage, listView));
        btnDelete.setLayoutX(40);
        btnDelete.setLayoutY(200);
        btnDelete.setText("Delete");

        Button btnCancel = new Button();
        btnCancel.setLayoutX(190);
        btnCancel.setLayoutY(200);
        btnCancel.setText("Cancel");
        btnCancel.setOnAction(e -> stage.close());

        AnchorPane anchorPane = new AnchorPane(lbl, listView, btnDelete, btnCancel);

        stage.setScene(new Scene(anchorPane));
        stage.setTitle("Delete Database");
        stage.getIcons().add(new Image("images/icon.png"));
        stage.setWidth(295);
        stage.setHeight(275);
        stage.setResizable(false);
        stage.showAndWait();
    }
    private void displayFailureMessage(String msg, String title){
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE );
    }
    private void displaySuccessMessage(String msg, String title){
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE);
    }
    private Integer displayQuestionMessage(boolean permDel){
        if(permDel)
            return JOptionPane.showConfirmDialog(null, "Do you want to permanently delete the displayed note.\n\n " +
                    "Do you wish to continue?", "Confirm Delete",JOptionPane.YES_NO_OPTION);
        else
            return JOptionPane.showConfirmDialog(null, "This will mark the selected note for deleting only. " +
                    "It can be \nreviewed for permanent deleting later or restored. It \nwill not be displayed in searches unless restored.\n\n" +
                    "Do you want to continue", "Confirm Mark for Deleting",JOptionPane.YES_NO_OPTION);
    }

    // E
    private String exportDate(Integer arrPos){
        String[] temp = this.tbxSearchDate.getText().split("/");
        int len = temp.length;
        if(len > 0){
            if((len-1) == 2){
                return temp[arrPos];
            }else if((len-1) == 1) {
                if(arrPos == 2) return temp[arrPos-1];
                if(arrPos == 1) return "";
                if(arrPos == 0) return temp[arrPos];
            }else if((len-1) == 0 && (arrPos-2) >= 0){
                return temp[arrPos-2];
            }

        }
        return "";
    }
    private ObservableMap<String, Object[]> exportNote(Source item){

        this.CurrentNoteID = item.getID();
        selectionDataDetails();
        ObservableMap<String, Object[]> temp = FXCollections.observableHashMap();

        temp.put("Author", xmlAuthorData(this.CurrentNoteID));
        if (CurrentFiles != null) temp.put("File", xmlFileData(this.CurrentNoteID));
        temp.put("Comment", new Object[]{item.getSummary(),this.tbxSearchComment.getText(), exportText(this.tbxSearchPages.getText()),exportText(this.tbxSearchTimeStamp.getText()), exportText(this.tbxSearchHyperlink.getText())});
        temp.put("Question", new Object[]{this.CurrentQuestion});
        temp.put("Quote", new Object[]{exportText(this.tbxSearchQuote.getText())});
        temp.put("Source", new Object[]{item.getSourceType(), item.getTitle().split("~")[0].trim(), exportDate(2), exportDate(0), exportDate(1), exportText(this.tbxSearchVolume.getText()),
                exportText(this.tbxSearchEdition.getText()), exportText(this.tbxSearchIssue.getText())});
        temp.put("Term", new Object[]{exportText(this.tbxSearchTerm.getText())});
        temp.put("Topic", new Object[]{this.CurrentTopic});
        return temp;

    }
    private Object exportText(Object obj){
        return obj == null ? "" : obj;
    }
    private void exportToXML(ObservableMap<String, Object[]> allNotes){
        ObservableMap<Integer, ObservableMap<String, Object[]>> notes = FXCollections.observableHashMap();
        notes.put(this.CurrentNoteID, FXCollections.observableMap(allNotes));
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
        new CreateXMLFileDOMParser(UserLog.getUserInfo("database") + "_notes_" + timeStamp + ".xml", notes);
    }

    // G
    private void getDBFromList(Stage stage, ListView listView){
        if(listView.getSelectionModel().getSelectedItem() != null) {
            selectedDBPath = listView.getSelectionModel().getSelectedItem().toString();
            stage.close();
        }else{
            JOptionPane.showMessageDialog(null, "Please make a selection or press 'Cancel'.");
        }
    }
    // Called by addFile and addNewFile, this will add any selected file to the tblSearchFiles table
    private NoteFile getFile(){
        FileAttachmentManager attachment = new FileAttachmentManager();
        Path path = attachment.getFile();
        if(path != null)
            return new NoteFile(0, path.getFileName().toString(), path.toString());
        return null;
    }
    private String getFileName(){
        int row;
        TablePosition pos = tblSearchFiles.getSelectionModel().getSelectedCells().get(0);
        row = pos.getRow();
        return tblSearchFiles.getItems().get(row).getFileName();
    }

    // I
    private boolean isNoteSelected(){
        if(tblSources.getSelectionModel().getSelectedItem() != null){
            return true;
        }else{
            displayFailureMessage("Either the Notes table is empty or no selection was made." +
                    " Please make a selection.", "Error: Notes Selection");
            return false;
        }

    }
    private boolean isFileSelected(){
        if(tblSearchFiles.getSelectionModel().getSelectedItem() != null){
            return true;
        }else{
            displayFailureMessage("Either the Search tab Files table is empty or no selection was made." +
                    " Please make a selection.", "Error: File Selection");
            return false;
        }
    }

    // L
    private ListView listDBs(String window){
        ListView<String> items = new ListView<>();
        File files = new File(GlobalVariables.RESEARCH_DB_PATH);
        File[] list = files.listFiles();
        for(File f : list){
            if(f.getName().substring(f.getName().length() - 3).contentEquals(".db") && !f.getName().toLowerCase().equals("blank.db")) {
                if (window.equals("open") )
                    items.getItems().add(f.getName());
                else if(!f.getName().toLowerCase().equals("sample.db")){
                    items.getItems().add(f.getName());
                }
            }
        }
        return items;
    }

    // N
    private Integer noteID(){
        int row;
        TablePosition pos = tblSources.getSelectionModel().getSelectedCells().get(0);
        row = pos.getRow();
        return tblSources.getItems().get(row).getID();
    }

    // O
    private void open(Path path){
        clearConnection();
        DatabaseSource.setDatabase(path.getFileName().toString());
        UserLog.setUserInfo("prevDBPath", UserLog.getUserInfo("dbPath"));
        if(!connModel.isDatabaseConnected()) {
            this.lblStatus.setText("Connection failed: " + path.toString());
            this.lblDatabase.setText("");
            LoggerWrapper.getInstance().myLogger.fine("Opened database failure: " + path.getFileName().toString());
            displayFailureMessage("Attempt to open database " + path.getFileName().toString() + " failed.", "Error: openDatabase");
        }else{
            changeDatabaseConnection();
            clearAll();
            this.lblStatus.setText("Connected..." + path.toString());
            this.lblDatabase.setText(DatabaseSource.getDatabase());
            UserLog.setUserInfo("database", path.getFileName().toString());
            UserLog.setUserInfo("dbPath", path.toAbsolutePath().toString());
            LoggerWrapper.getInstance().myLogger.fine("Opened database: " + path.getFileName().toString());
        }
    }
    private void openDB(){
        Stage stage = new Stage();
        Label lbl = new Label();
        ListView<String> listView;
        //noinspection unchecked
        listView = listDBs("open");
        listView.setLayoutX(20);
        listView.setLayoutY(30);

        listView.setMaxHeight(150);
        listView.setMinHeight(listView.getMaxHeight());
        listView.setMaxWidth(260);
        //listView.setMinWidth(listView.getMaxWidth());
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        lbl.setLayoutX(20);
        lbl.setLayoutY(5);
        lbl.setText("Please select a database to open:");

        Button btnOpen = new Button();
        btnOpen.setOnAction( e -> getDBFromList(stage, listView));
        btnOpen.setLayoutX(40);
        btnOpen.setLayoutY(200);
        btnOpen.setText("Open");

        Button btnCancel = new Button();
        btnCancel.setLayoutX(190);
        btnCancel.setLayoutY(200);
        btnCancel.setText("Cancel");
        btnCancel.setOnAction(e -> stage.close());

        AnchorPane anchorPane = new AnchorPane(lbl, listView, btnOpen, btnCancel);

        stage.setScene(new Scene(anchorPane));
        stage.setTitle("Open Database");
        stage.getIcons().add(new Image("images/icon.png"));
        stage.setWidth(295);
        stage.setHeight(275);
        stage.setResizable(false);
        stage.showAndWait();
    }

    // R
    private boolean requiredFieldsValid(String strSource, String strSourceType, String strTopic, String strTitle,
                                        TableView tblAuth, String strSummary){
        boolean rslt = false;
        String msg = "";

        if ((strTopic == null || strTopic.contentEquals(""))){
            msg = "Please select or enter a topic.";
        }else {
            try {
                if ((strSource == null) && (strTitle.isEmpty())) {
                    msg = "Please select an existing source or enter a title for a new source.";

                } else if (strSource == null && tblAuth.getItems().isEmpty()) {
                    msg = "Please enter an author or username.";

                } else if (strSource == null && (strSourceType == null || strSourceType.contentEquals(""))) {
                    msg = "Please select a source type.";

                } else if (strSourceType.equals("Question") && (this.cbxQuestion.getValue() == null)) {
                    msg = "Please enter or select a question because 'Question' was selected as a source. A comment should expand on the meaning.";

                } else if (strSourceType.equals("Quote") && (this.tbxQuote.getText().isEmpty())) {
                    msg = "Please enter a quote because 'Quote' was selected as a source. A comment should expand on the meaning.";

                } else if (strSourceType.equals("Term") && (this.tbxTerm.getText().isEmpty())) {
                    msg = "Please enter the term and definition. Expand within comments of other sources of interpretation.";

                } else if ((this.cbxSourceType.getValue().equals("Video") || this.cbxSourceType.getValue().equals("Audio")) && this.tbxTimeStamp.getText().isEmpty()) {
                    msg = "Please enter a TimeStamp value for an audio or video source.";

                } else if (this.tbxComment.getText().isEmpty() || this.tbxComment.getText().contentEquals("")) {
                    msg = "Please enter a comment. Comments are specific details related to the topic and summary.";

                } else if (strSummary.isEmpty()) {
                    msg = "Please enter a summary point. This expands upon your Topic entry or selection.";

                } else {
                    rslt = true;
                }
            }catch(NullPointerException ne){
                //ne.printStackTrace();
                msg = "Undetermined Source error. Check your Source fields.";
            }
        }

        if(!rslt){
            JOptionPane.showMessageDialog(null, msg, "Incomplete Source Data", JOptionPane.ERROR_MESSAGE);
        }
        return rslt;
    }
    private void resetGlobalVariables(){
        autoTopic.dispose();
        autoQuestion.dispose();
        autoSummaries.dispose();
        autoSource.dispose();

        if(cbxTopic != null) cbxTopic.setItems(null);
        cbxSearchTopic.getItems().clear();
        cbxSearchQuestion.getItems().clear();
    }

    // S
    private Integer searchRowPosition(){
        int row;
        TablePosition pos = tblSources.getSelectionModel().getSelectedCells().get(0);
        row = pos.getRow();
        return row;
    }
    private void selectionDataDetails(){
        if(this.CurrentFiles != null) this.CurrentFiles = null;
        /*this.CurrentSourceType = sourceType();
        this.CurrentSource = sourceInfo();*/

        Search search = new Search();
        search.displaySelection(CurrentNoteID);
        this.CurrentQuestion = search.SourceData.get(0);            // Question
        this.tbxSearchQuote.setText(search.SourceData.get(1));      // Quote
        this.tbxSearchTerm.setText(search.SourceData.get(2));       // Term
        this.tbxSearchDate.setText(search.SourceData.get(3));       // Date
        this.tbxSearchVolume.setText(search.SourceData.get(4));     // Volume
        this.tbxSearchEdition.setText(search.SourceData.get(5));    // Edition
        this.tbxSearchIssue.setText(search.SourceData.get(6));      // Issue
        this.tbxSearchHyperlink.setText(search.SourceData.get(7));  // Hyperlink
        this.tbxSearchComment.setText(search.SourceData.get(8));    // Comment
        this.tbxSearchPages.setText(search.SourceData.get(9));      // Page
        this.tbxSearchTimeStamp.setText(search.SourceData.get(10)); // TimeStamp
        this.CurrentSummary = search.SourceData.get(11);            // Summary
        this.CurrentTopic = search.SourceData.get(12);              // Topic

        this.CurrentSelectionData = search.SourceData;
        if(search.count("File_By_Note", "noteID", this.CurrentNoteID) > 0){
            tblSearchFiles.getItems().clear();
            this.CurrentFiles = search.searchForAttachments(this.CurrentNoteID); // Attachments
            tblSearchFiles.getItems().addAll(CurrentFiles);
            tabFile.setText("Files(" + this.CurrentFiles.size() + ")");
        }else{
            tabFile.setText("Files(0)");
            tblSearchFiles.getItems().clear();
        }
    }
    // Sets the current data to Search tab ComboBoxes
    private void setSearchComboBoxes(){
        // Handles topic ComboBox ...
        try {
            this.boolSkipSearchData = true;
            // cbxSearchTopic
            this.cbxSearchTopic.setValue(this.CurrentTopic);
            // cbxSearchQuestion...
            this.cbxSearchQuestion.setValue(this.CurrentQuestion);
            boolSkipSearchData = false;
        }catch (NullPointerException ex){
            displayFailureMessage(ex.toString(), "Error: setSearchComboBoxes");
        }
    }
    private Source sourceInfo(){
        int row;
        TablePosition pos = tblSources.getSelectionModel().getSelectedCells().get(0);
        row = pos.getRow();
        return tblSources.getItems().get(row);
    }
    private String sourceType(){
        int row;
        TablePosition pos = tblSources.getSelectionModel().getSelectedCells().get(0);
        row = pos.getRow();
        return tblSources.getItems().get(row).getSourceType();
    }

    // U
    private void uploadAuthor(String strAuthors){
        Connection conn = null;
        try {
            String authorQuery = "SELECT a.AuthorID, a.FirstName, a.MiddleName, a.LastName, a.Suffix FROM Author as a";
            conn = DBConnection.getConnection();
            if(conn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure: initializeUploadAuthor");
                System.exit(0);
            }
            PreparedStatement ps = conn.prepareStatement(authorQuery);
            ResultSet rsAuthors = ps.executeQuery();
            int id = Integer.parseInt(strAuthors.split(" - id: ")[1]);
            while (rsAuthors.next()) {
                if (rsAuthors.getInt("AuthorID") == id) {
                    this.tbxAuthorFirst1.setText(rsAuthors.getString("FirstName"));
                    this.tbxAuthorMiddle1.setText(rsAuthors.getString("MiddleName"));
                    this.tbxAuthorLast1.setText(rsAuthors.getString("LastName"));
                    this.tbxAuthorSuffix.setText(rsAuthors.getString("Suffix"));
                }
            }
            conn.close();
        }catch (SQLException ex){
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: uploadAuthor");
        }finally{
            try{
                if (conn != null) {
                    conn.close(); // throws exception here
                }
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                displayFailureMessage(e.toString(), "Error: uploadAuthor");
            }
        }

    }

    // V
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean valuesAreDifferent(Object obj1, Object obj2){
        if(obj1 == null || obj1.toString().isEmpty() || obj1.toString().trim().isEmpty()){obj1 = "";}
        if(obj2 == null || obj2.toString().isEmpty() || obj2.toString().trim().isEmpty()){obj2 = "";}
        return obj1.equals(obj2);
    }

    // X
    private Object[] xmlAuthorData(Integer currentNoteID){
        Search search = new Search();
        return new Object[]{search.captureAuthors(search.findPKIDonNotes(currentNoteID, "SourceID"))};
    }
    private Object[] xmlFileData(Integer currentNoteID){
        FileAttachmentManager fileMgr = new FileAttachmentManager();
        ObservableMap<String, byte[]> fileData = FXCollections.observableHashMap();
        CurrentFiles.forEach(f ->
            fileData.put(f.getFileName(), fileMgr.retrieveFileFromDB(f.getFileName(), currentNoteID)));
        return new Object[]{fileData};
    }




    /* =========================================== FXML METHODS =======================================================

    ==================================================================================================================*/

    // A
    @FXML   // When clicking "Enter", this will add author field data to the Author table
    private void addAuthor(){
        if(this.tbxAuthorFirst1.getText().equals("") && this.tbxAuthorMiddle1.getText().equals("") &&
                this.tbxAuthorLast1.getText().equals("") && this.tbxAuthorSuffix.getText().equals("")){
            displayFailureMessage("Author fields are empty. Select an author or add a new author.", "Error: Missing Author");
        }else{
            Author newAuthor = new Author(this.tbxAuthorFirst1.getText(),
                    this.tbxAuthorMiddle1.getText(), this.tbxAuthorLast1.getText(), this.tbxAuthorSuffix.getText());
            // get items from the table
            tblAuthors.getItems().add(newAuthor);
            this.tbxAuthorFirst1.clear();
            this.tbxAuthorMiddle1.clear();
            this.tbxAuthorLast1.clear();
            this.tbxAuthorSuffix.clear();
        }

    }
    @FXML   // When clicking "Attach", this will add any selected file to the tblFiles table
    private void addFile(){
        NoteFile file = getFile();
        if(file != null) tblFiles.getItems().add(file);

    }
    @FXML   // When clicking "Attach", this will add any selected file to the tblSearchFiles table
    private void addNewFile(){
        NoteFile file = getFile();
        if(file != null) tblSearchFiles.getItems().add(file);
    }
    @FXML   // Add Source and Source data to the ResearchDB tables
    private void addData(){
        /*
        There is an order of importance:
        1) The Source ComboBox should be checked first to see if the Current source is new or previously entered
        2) If the source does not exist then a Source and  SourceID will be created
        3) The author needs to be validated
         */
        int idSource = 0;
        int idComment = 0;
        int idQuestion = 0;
        int idQuote = 0;
        int idTerm = 0;
        int idTopic = 0;
        Connection conn = null;
        PreparedStatement stmt;
        ResultSet rslt;
        Search search = new Search();
        FileAttachmentManager fileMgr = new FileAttachmentManager();

        if(!requiredFieldsValid(this.cbxSource.getValue(), this.cbxSourceType.getValue(), this.cbxTopic.getValue(), this.tbxTitle.getText(),
                tblAuthors, this.tbxSummary.getText())){
            return;
        }

        try{
            conn = DBConnection.getConnection();
            search.passSearchConnection(conn);
            if(conn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure");
                return;
            }

            stmt = conn.prepareStatement("SAVEPOINT 'theKraken'");
            stmt.execute();

            if(this.cbxSource.getValue() == null || this.cbxSource.getValue().isEmpty()) {
                if(search.doesSourceAlreadyExist(tbxTitle.getText(), tblAuthors.getItems())) {
                    if(!importingData) displayFailureMessage("The following source already exists:\n\n" + tbxTitle.getText(), "Error: Existing Source");
                    //return;
                    this.cbxSource.setValue(this.tbxTitle.getText());
                }

            }

            if(this.cbxSource.getValue() != null && !this.cbxSource.getValue().isEmpty()){
                String sourceTitle = this.cbxSource.getValue().split("~")[0].trim();
                idSource = search.searchID("Source", "SourceID", "Title", sourceTitle );
                if (idSource == 0 && tbxTitle.getText().equals("") && tblAuthors.getItems().size() == 0){
                    displayFailureMessage("The Source selected cannot be found. Enter new sources using the Title, Author and Source Type fields.", "Error: Invalid Source");
                    return;
                }
            }

            if (idSource == 0){
                String insertSource = "INSERT INTO Source(SourceType, Title, Year, Month, Day, Volume, Edition, Issue)" +
                        " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";/**/
                stmt = conn.prepareStatement(insertSource);
                stmt.setString(1, this.cbxSourceType.getValue());
                stmt.setString(2, this.tbxTitle.getText());
                stmt.setString(3, this.dtYear.getText());
                stmt.setString(4, this.dtMonth.getText());
                stmt.setString(5, this.dtDay.getText());
                stmt.setString(6, this.tbxVolume.getText());
                stmt.setString(7, this.tbxEdition.getText());
                stmt.setString(8, this.tbxIssue.getText());
                stmt.execute();

                stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                rslt = stmt.executeQuery();
                if (rslt.next()) {
                    idSource = rslt.getInt(1);
                    rslt.close();
                }
            }

            if(!tblAuthors.getItems().isEmpty()){
                search.addAuthors(tblAuthors.getItems());
            }

            if(!tblFiles.getItems().isEmpty()){
                search.addFiles(tblFiles.getItems());
            }

            if (this.cbxTopic.getValue() != null && !this.cbxTopic.getValue().isEmpty()) {
                idTopic = search.searchID("Topic", "TopicID", "Topic", this.cbxTopic.getValue());

                if( idTopic == 0){
                    String insertTopic = "INSERT INTO Topic(Topic) VALUES(?)";
                    stmt = conn.prepareStatement(insertTopic);
                    stmt.setString(1,this.cbxTopic.getValue());
                    stmt.execute();

                    stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                    rslt = stmt.executeQuery();
                    if (rslt.next()) {
                        idTopic = rslt.getInt(1);
                        rslt.close();
                    }
                }
            }

            if (this.cbxQuestion.getValue() != null && !this.cbxQuestion.getValue().isEmpty()) {
                idQuestion = search.searchID("Question", "QuestionID", "Question", this.cbxQuestion.getValue());

                if(idQuestion == 0){
                    String insertQuestion = "INSERT INTO Question(Question) VALUES(?)";
                    stmt = conn.prepareStatement(insertQuestion);
                    stmt.setString(1,this.cbxQuestion.getValue());
                    stmt.execute();

                    stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                    rslt = stmt.executeQuery();
                    if (rslt.next()) {
                        idQuestion = rslt.getInt(1);
                        rslt.close();
                    }
                }
            }

            if (this.tbxTerm.getText() != null && !this.tbxTerm.getText().isEmpty()){
                String insertTerm = "INSERT INTO Term(Term) VALUES(?)";
                stmt = conn.prepareStatement(insertTerm);
                stmt.setString(1,this.tbxTerm.getText());
                stmt.execute();

                stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                rslt = stmt.executeQuery();
                if (rslt.next()) {
                    idTerm = rslt.getInt(1);
                    rslt.close();
                }
            }

            if (!this.tbxQuote.getText().isEmpty()){
                String insertQuote = "INSERT INTO Quote(Quote) VALUES(?)";
                stmt = conn.prepareStatement(insertQuote);
                stmt.setString(1,this.tbxQuote.getText());
                stmt.execute();

                stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                rslt = stmt.executeQuery();
                if (rslt.next()) {
                    idQuote = rslt.getInt(1);
                    rslt.close();
                }

            }

            if (!this.tbxComment.getText().isEmpty()){
                /*this.tbxTimeStamp.setText(CheckTimeStamp(tbxTimeStamp.getText()));*/
                String insertComment = "INSERT INTO Comment(Summary, Comment, Page, TimeStamp, Hyperlink) VALUES(?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(insertComment);
                stmt.setString(1,this.tbxSummary.getText());
                stmt.setString(2,this.tbxComment.getText());
                stmt.setString(3,this.tbxPages.getText());
                stmt.setString(4,this.tbxTimeStamp.getText());
                stmt.setString(5, this.tbxHyperlink.getText());
                stmt.execute();

                stmt = conn.prepareStatement("SELECT last_insert_rowid()");
                rslt = stmt.executeQuery();
                if (rslt.next()) {
                    idComment = rslt.getInt(1);
                    rslt.close();
                }
            }

            search.addToNotesTable(idSource, idComment, idQuestion, idQuote, idTerm, idTopic);

            if(search.getErrors() || fileMgr.getErrors()){
                stmt = conn.prepareStatement("ROLLBACK TO SAVEPOINT 'theKraken'");
                stmt.execute();
            }else{
                if(!importingData) displaySuccessMessage("New data saved successfully!", "Data Added");
            }
            stmt = conn.prepareStatement("RELEASE SAVEPOINT 'theKraken'");
            stmt.execute();
            conn.close();

        }catch(SQLException ex){
            ex.printStackTrace();
            try {
                if(conn != null) {
                    stmt = conn.prepareStatement("ROLLBACK TO SAVEPOINT 'theKraken'");
                    stmt.execute();
                    stmt = conn.prepareStatement("RELEASE SAVEPOINT 'theKraken'");
                    stmt.execute();
                    //search.CloseSearchConnection();
                    conn.close();
                    displayFailureMessage(ex.toString(), "Error: addData");
                }
            }catch(SQLException ex2){
                ex2.printStackTrace();

            }
        }finally {
            try{
                if (conn != null) {
                    conn.close(); // throws exception here
                }
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                displayFailureMessage(e.toString(), "Error: InitializeQuestionBox");
            }
        }
        clear();
        resetGlobalVariables();
        setup();
    }
    @FXML   // Simply closes the application
    private void applicationClose(){
        System.exit(0);
    }

    // B
    @FXML
    private void backup(){
        try {
            Path backupPath = new File(GlobalVariables.BACKUP + "backup_" + UserLog.getUserInfo("database")).toPath();
            Path path = new File(GlobalVariables.RESEARCH_DB_PATH + UserLog.getUserInfo("database")).toPath();
            if(Files.exists(backupPath))
                Files.delete(backupPath);
            Files.copy(path, backupPath);
        }catch(IOException ex){
            ex.printStackTrace();
            LoggerWrapper.getInstance().myLogger.severe("ResearchController.backup() could not backup");
        }
    }

    // C
    @FXML   // When double clicking, this event allows edit the First/User Name field
    private void changeFirstNameCellEvent(TableColumn.CellEditEvent editCell){
        Author personSelected = tblAuthors.getSelectionModel().getSelectedItem();
        personSelected.setFirstName(editCell.getNewValue().toString());
    }
    @FXML   // When double clicking, this event allows edit the Middle Name field
    private void changeMiddleNameCellEvent(TableColumn.CellEditEvent editCell){
        Author personSelected = tblAuthors.getSelectionModel().getSelectedItem();
        personSelected.setMiddleName(editCell.getNewValue().toString());
    }
    @FXML   // When double clicking, this event allows edit the Last Name field
    private void changeLastNameCellEvent(TableColumn.CellEditEvent editCell){
        Author personSelected = tblAuthors.getSelectionModel().getSelectedItem();
        personSelected.setLastName(editCell.getNewValue().toString());
    }
    @FXML   // When double clicking, this event allows edit the Last Name field
    private void changeSuffixCellEvent(TableColumn.CellEditEvent editCell){
        Author personSelected = tblAuthors.getSelectionModel().getSelectedItem();
        personSelected.setSuffix(editCell.getNewValue().toString());
    }
    @FXML   // When clicking clear, the current tab is cleared only
    private void clear(){
        if(tabEntry.isSelected()){
            clearEntryData();
            cbxTopic.setValue(null);
            cbxQuestion.setValue(null);

        }else if(tabSearch.isSelected()){
            clearSearchFields();
            cbxSearchTopic.setValue(null);
            cbxSearchQuestion.setValue(null);
            tbxCustomSearch.clear();
            menuUpdate.setDisable(true);
            menuPrint.setDisable(true);
            menuExport.setDisable(true);
            menuMarkDelete.setDisable(true);
            menuUnMarkForDelete.setDisable(true);
            menuPermDelete.setDisable(true);
        }
    }
    @FXML   // When creating a New Database or Opening another this will clear all tab fields
    private void clearAll(){
        clearEntryData();
        clearSearchFields();
        cbxSearchTopic.setValue(null);
        cbxSearchQuestion.setValue(null);
        tbxCustomSearch.clear();
    }
    @FXML// When clicking clear, clearEntryData clears all the Entry tab data present
    private void clearEntryData(){
        for(TextField tField : arrEntryTextFields){
            tField.clear();
        }
        for(TextArea tArea : arrEntryTextAreas){
            tArea.clear();
        }
        // TableView
        this.tblAuthors.getItems().clear();
        this.tblFiles.getItems().clear();

        // ComboBoxes / ChoiceBoxes
        this.cbxSourceType.getSelectionModel().clearSelection();
        //this.cbxTopic.getSelectionModel().clearAndSelect(0);  // <-- this works but didn't resolve the issue of clearing when active
        this.cbxTopic.getSelectionModel().clearSelection();
        if(this.cbxTopic != null) this.cbxTopic.setValue(null);
        //this.cbxQuestion.getSelectionModel().clearAndSelect(0);  // <-- this works but didn't resolve the issue of clearing when active
        this.cbxQuestion.getSelectionModel().clearSelection();
        if(this.cbxQuestion != null) this.cbxQuestion.setValue(null);

        this.cbxSource.getSelectionModel().clearSelection();
        if(this.cbxSource != null) this.cbxSource.setValue(null);

        this.boolClearAuthorPressed = true;
        this.cbxAuthors.getSelectionModel().clearSelection();
        this.boolClearAuthorPressed = false;

    }
    @FXML// When called, clearSearchFields clears all the Search tab data present
    private void clearSearchFields(){
        for(TextField tField : arrSearchTextFields){
            tField.clear();
            tField.setDisable(true);
        }
        for(TextArea tArea : arrSearchTextAreas){
            tArea.clear();
            tArea.setDisable(true);
        }
        this.tblSources.getItems().clear();
        this.tblSearchFiles.getItems().clear();
        this.tblSearchFiles.setDisable(true);
        this.tbxSearchHyperlink.setText("");

        this.editHyperlink.setTextFill(Color.web("#1787e5"));
        this.editTable.setTextFill(Color.web("#1787e5"));
        this.editTopic.setTextFill(Color.web("#1787e5"));
        this.editQuestion.setTextFill(Color.web("#1787e5"));

        this.editHyperlink.setOnMouseClicked(null);
        this.editTable.setOnMouseClicked(null);
        this.editTopic.setOnMouseClicked(null);
        this.editQuestion.setOnMouseClicked(null);

        this.btnOpenFile.setDisable(true);
        this.btnSearchAttachFile.setDisable(true);
        this.btnSearchRemoveFile.setDisable(true);

        lblRecords.setText("Records: " + tblSources.getItems().size());
        tabFile.setText("Files(" + tblFiles.getItems().size() + ")");
    }
    @FXML   // When selecting Custom Search, completeSearch will allow fully searching the database
    private void completeSearch(){
        if(this.tbxCustomSearch != null && !this.tbxCustomSearch.getText().isEmpty()) {
            this.cbxSearchTopic.setValue(null);
            this.cbxSearchQuestion.setValue(null);
            deactivateSearchFields();
            clearSearchFields();
            this.menuExport.setDisable(false);
            this.menuExportAll.setDisable(false);
            Search search = new Search();
            search.completeSearch(this.ckxContainsAll.isSelected(), this.tbxCustomSearch.getText());
            if (search.SourceList != null && search.SourceList.size() > 0)
                tblSources.setItems(search.SourceList);
            else
                displaySuccessMessage("Custom search failed to return results.", "Custom Search");
            lblRecords.setText("Records: " + tblSources.getItems().size());
        }
    }

    // D
    @FXML   // Call by OnAction event for menuDeleteDatabase (Delete Database)
    private void deleteDatabase(){
        selectedDBPath = null;
        deleteDB();
        if(selectedDBPath == null) return;
        Path selectedFile = new File(GlobalVariables.RESEARCH_DB_PATH + selectedDBPath).toPath();
        //Path selectedFile = openDeleteDialogBox("Please select a database to delete:");

        if(JOptionPane.showConfirmDialog(null,"Are you sure you want to delete the following database:\n\n" +
                selectedFile.getFileName().toString(),"Confirm Delete: " + selectedFile.getFileName().toString(), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

            if(DatabaseSource.getDatabase().toLowerCase().equals(selectedFile.getFileName().toString().toLowerCase())) {
                JOptionPane.showMessageDialog(null, "Please close the current database by selecting another database then try again.");
            }else if(selectedFile.getFileName().toString().toLowerCase().equals("sample.db") || selectedFile.getFileName().toString().toLowerCase().equals("blank.db")){
                JOptionPane.showMessageDialog(null, "The sample.db and blank.db should not be deleted.");
            }else {
                try {
                    //clearConnection();
                    if (Files.exists(selectedFile))
                        Files.delete(selectedFile);
                    LoggerWrapper.getInstance().myLogger.fine("Delete successful: " + selectedFile.getFileName().toString());
                    displaySuccessMessage("The database " + selectedFile.getFileName().toString() + " was deleted successfully!", "Database Deleted" );
                    UserLog.setUserInfo("prevDBPath", UserLog.getUserInfo("dbPath"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    LoggerWrapper.getInstance().myLogger.severe("Delete failed: " + selectedFile.getFileName().toString());
                    displayFailureMessage("The database " + selectedFile.getFileName().toString() + " failed to delete", "Error: deleteDatabase" );
                }
            }
        }
    }
    @FXML   // When clicking deletePermanently, This will use the noteID to permanently delete the selected note"
    private void deletePermanently(){
        if( isNoteSelected() && displayQuestionMessage(true) == 0){
            Connection conn = null;
            PreparedStatement stmt;
            try{
                conn = DBConnection.getConnection();
                if(conn == null){
                    displayFailureMessage("The database 'conn' is null.", "Connection Failure");
                    return;
                }

                Search search = new Search();
                search.passSearchConnection(conn);
                stmt = conn.prepareStatement("SAVEPOINT 'theRecord'");
                stmt.execute();
                search.deleteRecord(noteID());

                if(search.getErrors()) {
                    stmt = conn.prepareStatement("ROLLBACK TO SAVEPOINT 'theRecord'");
                    stmt.execute();
                    displaySuccessMessage("Attempt to delete this note failed!", "Attempt to Delete Failed");
                }else {
                    clear();
                    displaySuccessMessage("Deleted note successfully!", "Deleted Note");
                }
                stmt = conn.prepareStatement("RELEASE SAVEPOINT 'theRecord'");
                stmt.execute();
                conn.close();

                if(search.removeTopic) this.cbxSearchTopic.getItems().remove(this.CurrentTopic);

            }catch(SQLException ex){
                ex.printStackTrace();
            }finally {
                try{
                    if (conn != null) {
                        conn.close(); // throws exception here
                    }
                }
                catch(SQLException e)
                {
                    e.printStackTrace();
                    displayFailureMessage(e.toString(), "Error: InitializeQuestionBox");
                }
            }
        }
    }

    // E
    @FXML   // Called by OnAction event
    private void editHyperlink(){
        Object ans = JOptionPane.showInputDialog(null, "Enter a hyperlink or leave an empty field to clear then press OK.", "Edit Hyperlink",
                JOptionPane.PLAIN_MESSAGE, null, null, this.tbxSearchHyperlink.getText());
        if(ans == ""){
            this.tbxSearchHyperlink.setText("");
        }else if(ans != null){
            this.tbxSearchHyperlink.setText(ans.toString());
            this.editHyperlink.setTextFill(Color.web("#b118e2"));
        }
    }
    @FXML   // Called by OnAction event
    @SuppressWarnings("unchecked")  // Call by OnAction event for label editQuestion (Edit Question)
    private void editQuestion(){
        JComboBox jCombo = new JComboBox(QuestionList);
        jCombo.setEditable(true);
        if(JOptionPane.showConfirmDialog(null, jCombo, "clear, select or enter a question:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.CANCEL_OPTION)
            return;
        Object ans = jCombo.getSelectedItem();

        this.boolSkipSearchData = true;
        if(ans != null){
            this.cbxSearchQuestion.setValue(ans.toString());
            this.CurrentQuestion = ans.toString();
        }
        this.editQuestion.setTextFill(Color.web("#b118e2"));
        this.boolSkipSearchData = false;
    }
    @FXML   // Call by OnAction event for label editTable (Edit Summary Statement)
    private void editTable(){

        Object ans = JOptionPane.showInputDialog(null, "Enter a new Summary then press OK.", "Edit Table Summary",
                JOptionPane.PLAIN_MESSAGE, null, null, this.CurrentSummary);
        if(ans != null && !ans.equals("")){
            this.tblSummary.setCellValueFactory(new PropertyValueFactory<>("Summary"));
            this.tblSummary.setCellFactory(TextFieldTableCell.forTableColumn());
            this.tblSources.getItems().get(this.CurrentRowPos).setSummary(ans.toString());
            this.CurrentSummary = ans.toString();
            this.editTable.setTextFill(Color.web("#b118e2"));
        }
    }
    @FXML   // Called by OnAction event
    @SuppressWarnings("unchecked")  // Call by OnAction event for label editTopic (Edit Topic)
    private void editTopic(){
        JComboBox jCombo = new JComboBox(TopicList);
        jCombo.setEditable(true);
        if(JOptionPane.showConfirmDialog(null, jCombo, "Select or enter a 1-3 word topic:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.CANCEL_OPTION)
            return;
        Object ans = jCombo.getSelectedItem();

        this.boolSkipSearchData = true;
        //this.cbxSearchTopic.setEditable(true);
        if(ans == "" || ans == null){
            this.cbxSearchTopic.setValue(this.CurrentTopic);
        }else {
            String noSpc1 = "[^ \\[\\]!@#$%&?*()+-/]";
            String range = "[a-zA-Z0-9'-]";
            String regEx = "^(" + range + "*)\\.?$|" +
                    "^(" + noSpc1 + range + "*\\.?([: ]|[ ])" + range + "*)\\.?$|" +
                    "^(" + noSpc1 + range + "*\\.?([: ]|[ ])" + range + "*\\.?([: ]|[ ])" + range + "*)\\.?$";
            if (!ans.toString().matches(regEx)) {
                displayFailureMessage("Keep your topic one to three words. No special characters.", "Topic Tooltip" );
            }else{
                this.cbxSearchTopic.setValue(ans.toString());
                this.CurrentTopic = ans.toString();
                this.editTopic.setTextFill(Color.web("#b118e2"));
            }
        }
        //this.cbxSearchTopic.setEditable(false);
        boolSkipSearchData = false;
    }
    @FXML
    private void exportAll(){

        ObservableMap<Integer, ObservableMap<String, Object[]>> notes = FXCollections.observableHashMap();

        for (Source item : tblSources.getItems()) {
            notes.put(item.getID(), FXCollections.observableMap(exportNote(item)));
        }
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
        new CreateXMLFileDOMParser(UserLog.getUserInfo("database") + "_notes_" + timeStamp + ".xml", notes);
    }
    @FXML
    private void exportSelection(){
        if(isNoteSelected()){
            exportToXML(exportNote(tblSources.getSelectionModel().getSelectedItem()));
        }
    }

    // I
    @FXML
    private void importNote(){
        ObservableList<ObservableMap<String, ObservableList<String>>> importableNotes;
        FileChooser importXML = new FileChooser();
        importXML.setTitle("Select an XML file for importing notes:");
        importXML.setInitialDirectory(new File(GlobalVariables.USER_DESKTOP));
        importXML.getExtensionFilters().add( new FileChooser.ExtensionFilter("XML", "*.xml"));
        Path path =  importXML.showOpenDialog(null).toPath();
        ReadXMLFileDOMParser read = null;
        if(Files.exists(path))
            read = new ReadXMLFileDOMParser(path.toString());
        else
            System.out.println("No valid xml");

        importableNotes = read != null ? read.importNotes : null;
        if(importableNotes!= null){
            loadImportedDataToDB(importableNotes);
            displaySuccessMessage("Imported data saved successfully!", "Imported Data Added");
        }else{
            displayFailureMessage("ResearchController.importNotes failure: Notes couldn't be imported", "ResearchController.importNotes");
            LoggerWrapper.getInstance().myLogger.severe("ResearchController.importNotes failure");
        }
        importingData = false;

    }

    private void loadImportedDataToDB(ObservableList<ObservableMap<String, ObservableList<String>>> data){
        importingData = true;
        data.forEach((n) -> {

            //("\nSource: =====================================================");(
            this.cbxSourceType.setValue(n.get("Source").get(0)); // SourceType
            this.tbxTitle.setText(n.get("Source").get(1)); // Title
            this.dtYear.setText(n.get("Source").get(2)); // Year
            this.dtMonth.setText(n.get("Source").get(3)); // Month
            this.dtDay.setText(n.get("Source").get(4)); // Day
            this.tbxVolume.setText(n.get("Source").get(5)); // Volume
            this.tbxEdition.setText(n.get("Source").get(6)); // Edition
            this.tbxIssue.setText(n.get("Source").get(7)); // Issue

            //("\nAuthor: =====================================================");
            ObservableList<Author> auths = FXCollections.observableArrayList();
            n.get("Author").forEach(a -> {

                System.out.println(a);

                String[] arr = a.split("\\*");
                if(arr.length == 4)
                    auths.add(new Author(arr[0], arr[1], arr[2], arr[3]));
                if(arr.length == 3)
                    auths.add(new Author(arr[0], arr[1], arr[2], ""));
                if(arr.length == 2)
                    auths.add(new Author(arr[0], arr[1], "", ""));
                if(arr.length == 1)
                    auths.add(new Author(arr[0], "", "", ""));
            });
            tblAuthors.setItems(auths);

            //("\nTopic: =====================================================");
            this.cbxTopic.setValue(n.get("Topic").get(0)); // Topic

            //("\nQuestion: =====================================================");
            this.cbxQuestion.setValue(n.get("Question").get(0)); // Question

            //("\nTerm: =====================================================");
            this.tbxTerm.setText(n.get("Term").get(0)); // Term

            //("\nQuote: =====================================================");
            this.tbxQuote.setText(n.get("Quote").get(0)); // Quote

            //("\nComment: =====================================================");
            this.tbxSummary.setText(n.get("Comment").get(0)); // Summary
            this.tbxComment.setText(n.get("Comment").get(1)); // Comment
            this.tbxPages.setText(n.get("Comment").get(2)); // Page(s)/Para(s)
            this.tbxTimeStamp.setText(n.get("Comment").get(3)); // TimeStamp
            this.tbxHyperlink.setText(n.get("Comment").get(4)); // Hyperlink

            //("\nFiles: =====================================================");
            if(n.containsKey("File")) {
                ObservableList<NoteFile> files = FXCollections.observableArrayList();
                n.get("File").forEach(f -> {
                    String[] ary = f.split("\\*", 2);
                    String filePath = GlobalVariables.TEMP + ary[0];
                    byte[] bytes = ary[1].getBytes();
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(new File(filePath));
                        os.write(Base64.getDecoder().decode(bytes));
                        os.flush();
                        os.close();
                        files.add(new NoteFile(ary[0], filePath));
                    }catch(IOException io){
                        io.printStackTrace();
                    }finally{
                        try {
                            if(os != null){
                                os.flush();
                                os.close();
                            }
                            System.gc();
                        }catch(IOException | NullPointerException io){
                            LoggerWrapper.getInstance().myLogger.fine("ResearchController.loadImportedDataToDB cannot release files");
                        }
                    }
                    //files.add(new NoteFile(ary[0], filePath));
                });
                if(files.size() > 0)
                    tblFiles.setItems(files);
                else
                    displayFailureMessage("Importing file data failed!", "Imported File(s) Failure");
            }
            addData();
            clearEntryData();
            if(n.containsKey("File")){
                cleanUpTemp();
            }
        });

    }

    // M
    @FXML   // When clicking Delete, Delete will use the CurrentNoteID to flag the Notes Deleted field to "1"
    private void markDelete() {
        if(isNoteSelected()) {
            Search search = new Search();
            if (displayQuestionMessage(false) == 0){
                search.deleteNote(CurrentNoteID, false, 1);
                clear();
                displaySuccessMessage("Note marked for review. The Topic will remain but may return no result.", "Note Marked for Delete");
            }
        }
    }

    // N
    @FXML   // Call by OnAction event for menuNew (New Database)
    private void newDatabase() {
        Object ans = JOptionPane.showInputDialog("Please enter a name for the database:");

        if(ans.toString().toLowerCase().equals("blank") || ans.toString().toLowerCase().equals("sample")) {
            ans = JOptionPane.showInputDialog(null, "The name 'blank' and 'sample' cannot be used. Please enter a new name:", "Error: Invalid name", JOptionPane.ERROR_MESSAGE);
        }else if(ans.toString().isEmpty()){
            ans = JOptionPane.showInputDialog(null, "No name was provided. Please enter a name:", "Error: No Database Name", JOptionPane.ERROR_MESSAGE);
        }
        try {
            String newFile = ans.toString() + ".db";
            Files.copy(new File(dbPath + "/templates/blank.db").toPath(), new File(dbPath + newFile).toPath());
            Path createdDB = new File(dbPath + newFile).toPath();
            open(createdDB);
            LoggerWrapper.getInstance().myLogger.fine("New database creation successful: " + ans.toString());
            displaySuccessMessage("New database " + DatabaseSource.getDatabase() + " created successfully!", "Database Created");
        } catch (IOException ex) {
            LoggerWrapper.getInstance().myLogger.severe("New database creation failed: " + ans.toString());
        }
    }

    // O
    @FXML   // When clicking a hyperlink, onClickLaunchHyperlink opens the link in the systems default browser
    private void onClickLaunchHyperlink(){
        String url = this.tbxSearchHyperlink.getText();

        try {
            if(url.matches("((?i)(?s)[A-Z]):.*"))
                Desktop.getDesktop().open(new File(url));
            else
                Desktop.getDesktop().browse(new URI(url));
        }catch (IOException ei){
            ei.printStackTrace();
            displayFailureMessage(ei.toString(), "Error: onClickLaunchHyperlink - IOException");
        }catch (URISyntaxException ex){
            ex.printStackTrace();
            displayFailureMessage(ex.toString(), "Error: onClickLaunchHyperlink - URISyntaxException");
        }
    }
    @FXML   // Call by OnAction event for menuOpen (Open Database)
    private void openDatabase(){
        selectedDBPath = null;
        openDB();
        if(selectedDBPath == null) return;
        Path selectedFile = new File(GlobalVariables.RESEARCH_DB_PATH + selectedDBPath).toPath();
        //Path selectedFile = openDeleteDialogBox("Please select a database to open:");
        open(selectedFile);
        LoggerWrapper.getInstance().myLogger.fine("Opened database: " + selectedFile.getFileName().toString());
    }
    /*@FXML   // Called by openDatabase and newDatabase with a generic prompt for selecting db files
    private Path openDeleteDialogBox(String msgTitle){
        FileChooser openFile = new FileChooser();
        openFile.setTitle(msgTitle);
        openFile.setInitialDirectory(new File(dbPath));
        openFile.getExtensionFilters().add( new FileChooser.ExtensionFilter("SQLite", "*.db"));
        return openFile.showOpenDialog(null).toPath();
    }*/
    @FXML   // Call by OnAction event for Open File (capture filenames and open)
    private void openFile(){
        if(isFileSelected()){
            String fName = getFileName();
            try{
                FileAttachmentManager fileMgr = new FileAttachmentManager();
                fileMgr.openFile(fName, noteID());
            }catch(Exception ex){
                LoggerWrapper.getInstance().myLogger.severe("ResearchController.openFile failed.");
                displayFailureMessage(fName + " failed to open.", "Error ResearchController.openFile");
            }
        }
    }
    @FXML   // Call by OnAction event for menuManual (ResearchDB Manual)
    private void openManual(){
        if(Desktop.isDesktopSupported()){
            try{
                Desktop.getDesktop().open(new File(GlobalVariables.RESEARCH_DB_PATH + "app_info/ResearchDBManual.pdf"));
            }catch(IOException ex){
                LoggerWrapper.getInstance().myLogger.severe("Launching manual failed.");
                displayFailureMessage("Launching ResearchDB manual (PDF) failed.", "Error openManual");
            }
        }
    }

    // P
    @FXML
    private void print(){
        PrintOut printNote = new PrintOut(this.CurrentSelectionData, this.CurrentFiles, this.CurrentSource);
        printNote.preview();
    }

    // R
    @FXML   // When clicking "Remove", this will remove any selected rows in the Author table
    private void removeAuthor(){
        ObservableList<Author> selectedRows, allAuthors;
        allAuthors = tblAuthors.getItems();
        selectedRows = tblAuthors.getSelectionModel().getSelectedItems();
        for(Author author : selectedRows){
            allAuthors.remove(author);
        }
    }
    @FXML   // When clicking "Remove", this will remove any selected rows in the tblFiles table
    private void removeFile(){
        ObservableList<NoteFile> selectedRows, allFiles;
        allFiles = tblFiles.getItems();
        selectedRows = tblFiles.getSelectionModel().getSelectedItems();
        for(NoteFile file : selectedRows){
            allFiles.remove(file);
        }
    }
    @FXML   // When clicking "Remove", this will remove any selected rows in the tblSearchFiles table
    private void removeNewFile(){
        ObservableList<NoteFile> selectedRows, allFiles;
        allFiles = tblSearchFiles.getItems();
        selectedRows = tblSearchFiles.getSelectionModel().getSelectedItems();
        for(NoteFile file : selectedRows){
            allFiles.remove(file);
        }
    }
    @FXML   // Call by OnAction event for menuReviewForDelete (Review for Delete)
    private void reviewForDelete(){
        deactivateSearchFields();
        clearSearchFields();
        this.tbxCustomSearch.clear();
        this.cbxSearchQuestion.setValue(null);
        this.cbxSearchTopic.setValue(null);

        Search search = new Search();
        search.searchForMarkedDeleted();
        tblSources.setItems(search.SourceList);
        if(tblSources.getItems().size() == 0){
            displayFailureMessage("No notes were identified for review before deleting.", "No Notes Identified");
            return;
        }
        lblRecords.setText("Records: " + tblSources.getItems().size());

    }

    // S
    @FXML   // When clicking Search, SearchData populates the Source table based upon the Topic selected
    private void searchQuestion(){
        deactivateSearchFields();
        clearSearchFields();
        this.tbxCustomSearch.clear();
        this.cbxSearchTopic.setValue(null);
        if(cbxSearchQuestion.getValue() != null){
            Search search = new Search();
            search.searchQuestion(this.cbxSearchQuestion.getValue());
            tblSources.setItems(search.SourceList);
            lblRecords.setText("Records: " + tblSources.getItems().size());
        }
    }
    @FXML   // When clicking Search, SearchData populates the Source table based upon the Topic selected
    private void searchTopic(String topic){
        deactivateSearchFields();
        clearSearchFields();
        this.tbxCustomSearch.clear();
        this.cbxSearchQuestion.setValue(null);
        this.menuUpdate.setDisable(true);
        this.menuPrint.setDisable(true);
        menuMarkDelete.setDisable(true);
        menuUnMarkForDelete.setDisable(true);
        menuPermDelete.setDisable(true);

        this.menuExport.setDisable(false);
        this.menuExportAll.setDisable(false);
        if(!topic.isEmpty()){
            Search search = new Search();
            search.searchTopic(topic);
            tblSources.setItems(search.SourceList);
            lblRecords.setText("Records: " + tblSources.getItems().size());
        }
    }
    @FXML   // When clicking a row in the Source table, selectionData populates fields specific to that Source note
    private void selectionData(){
        if(isNoteSelected() || boolRefresh) {
            menuUpdate.setDisable(false);
            menuPrint.setDisable(false);
            menuExport.setDisable(false);
            menuMarkDelete.setDisable(false);
            menuUnMarkForDelete.setDisable(false);
            menuPermDelete.setDisable(false);
            tblSearchFiles.setEditable(true);


            this.CurrentRowPos = searchRowPosition();
            this.CurrentNoteID = noteID();
            this.CurrentSourceType = sourceType();
            this.CurrentSource = sourceInfo();
            selectionDataDetails();
            if(this.boolRefresh) this.boolRefresh = false;
            activateSearchFields();
            setSearchComboBoxes();
        }
    }

    // V
    @FXML   // Called by OnAction or focusedProperty events
    private void validateDate(){
        TextField x = this.tbxSearchDate;
        if (x.getText() != null && !x.getText().isEmpty() && !x.getText().matches("^((0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])/([\\d]?[\\d]?[\\d]?\\d))$|^([\\d]?[\\d]?[\\d]?\\d)$")){
            JOptionPane.showMessageDialog(null, "Please enter the following format or leave blank.\n\n" +
                    "Example: 01:45:59 = hh:mm:ss");
        }
    }
    @FXML   // Called by OnAction or focusedProperty events
    private void validateMonth(){
        TextField x = this.dtMonth;
        if (x.getText() != null && !x.getText().isEmpty() && !x.getText().matches("^(0?[1-9]|1[0-2])$")){
            displayFailureMessage("Please enter a month value between 01 - 12.", "Error: Invalid Month");
        }
    }
    @FXML   // Called by OnAction or focusedProperty events
    private void validateDay(){
        TextField x = this.dtDay;
        if (x.getText() != null && !x.getText().isEmpty() && !x.getText().matches("^(0?[1-9]|[1-2][0-9]|3[0-1])$")){
            displayFailureMessage("Please enter a value between 01 - 31", "Error: Invalid Day");
        }
    }
    @FXML   // Called by OnAction or focusedProperty events
    private void validateYear(){
        TextField x = this.dtYear;
        if (x.getText() != null && !x.getText().isEmpty() && !x.getText().matches("^([1-2]\\d\\d\\d)$")){
            displayFailureMessage("Please enter a value between 1000 and 2999", "Error: Invalid Year");
        }
    }
    @FXML   // Called by OnAction
    private void validateTopic(){
        String noSpc1 = "[^ \\[\\]!@#$%&?*()+-/]";
        String range = "[a-zA-Z0-9'-]";
        String regEx = "^(" + range + "*)\\.?$|" +
                "^(" + noSpc1 + range + "*\\.?([: ]|[ ])" + range + "*)\\.?$|" +
                "^(" + noSpc1 + range + "*\\.?([: ]|[ ])" + range + "*\\.?([: ]|[ ])" + range + "*)\\.?$";
        String text = this.cbxTopic.getValue();
        if (!text.isEmpty() && !text.matches(regEx)) {
            displayFailureMessage("Keep your topic one to three words. No special characters.", "Topic Tooltip" );
        }

    }
    @FXML   // Called by OnAction or focusedProperty events
    private void validateSearchTimeStamp(){
        TextField time = this.tbxSearchTimeStamp;
        if (time.getText() != null && !time.getText().isEmpty() && !time.getText().matches("^((\\d\\d):([0-5]\\d):([0-5]\\d))$")){

            displayFailureMessage("Please enter the following format or leave blank.\n\n" +
                    "Example: 01:45:59 = hh:mm:ss","Error: Invalid TimeStamp" );
        }
    }
    @FXML   // Called by OnAction or focusedProperty events
    private void validateTimeStamp(){
        TextField time = this.tbxTimeStamp;
        if (time.getText() != null && !time.getText().isEmpty() && !time.getText().matches("^((\\d\\d):([0-5]\\d):([0-5]\\d))$")){

            displayFailureMessage("Please enter the following format or leave blank.\n\n" +
                    "Example: 01:45:59 = hh:mm:ss","Error: Invalid TimeStamp" );
        }
    }

    // U
    @FXML   // When clicking Delete, Delete will use the CurrentNoteID to flag the Notes Deleted field to "1"
    private void unDelete() {
        if(isNoteSelected()) {
            Search search = new Search();
            search.deleteNote(CurrentNoteID, false, 0);
            clear();
            displaySuccessMessage("Note reinstated successfully!", "Note Available");
        }
    }
    @FXML   // When clicking update, update checks compares data and updates those specific fields in the ResearchDB
    private void update(){
        boolean updated = false;
        Connection conn = null;
        int intTopicCount = 0;
        int intQuestionCount = 0;
        String prevTopic = CurrentSelectionData.get(12);
        String prevQuestion = CurrentSelectionData.get(0);

        try{
            Search search = new Search();
            conn = DBConnection.getConnection();conn = DBConnection.getConnection();
            PreparedStatement stmt;
            if(conn == null){
                displayFailureMessage("The database 'conn' is null.", "Connection Failure");
                return;
            }
            search.passSearchConnection(conn);
            stmt = conn.prepareStatement("SAVEPOINT 'theHounds'");
            stmt.execute();
            if(!valuesAreDifferent(CurrentQuestion, CurrentSelectionData.get(0))) {
                search.updateField(CurrentNoteID, "Question", "QuestionID", "Question", CurrentQuestion);
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchQuote.getText(), CurrentSelectionData.get(1))){
                search.updateField(CurrentNoteID, "Quote", "QuoteID", "Quote", tbxSearchQuote.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchTerm.getText(), CurrentSelectionData.get(2))){
                search.updateField(CurrentNoteID, "Term", "TermID", "Term", tbxSearchTerm.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchDate.getText(), CurrentSelectionData.get(3))){
                String[] arr = tbxSearchDate.getText().split("/");
                if(arr.length == 3){
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Month", arr[0]);
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Day", arr[1]);
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Year", arr[2]);
                }else if(tbxSearchDate.getText().split("/").length == 2){
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Month", arr[0]);
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Year", arr[1]);
                }else if(tbxSearchDate.getText().split("/").length == 1){
                    search.updateField(CurrentNoteID, "Source", "SourceID", "Year", arr[0]);
                }
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchVolume.getText(), CurrentSelectionData.get(4))){
                search.updateField(CurrentNoteID, "Source", "SourceID", "Volume", tbxSearchVolume.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchEdition.getText(), CurrentSelectionData.get(5))){
                search.updateField(CurrentNoteID, "Source", "SourceID", "Edition", tbxSearchEdition.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchIssue.getText(), CurrentSelectionData.get(6))){
                search.updateField(CurrentNoteID, "Source", "SourceID", "Issue", tbxSearchIssue.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchHyperlink.getText(), CurrentSelectionData.get(7))){
                search.updateField(CurrentNoteID, "Comment", "CommentID","Hyperlink", tbxSearchHyperlink.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchComment.getText(), CurrentSelectionData.get(8))){
                search.updateField(CurrentNoteID, "Comment", "CommentID","Comment", tbxSearchComment.getText());
                updated = true;
            }
            if(!valuesAreDifferent(tbxSearchPages.getText(), CurrentSelectionData.get(9))){
                search.updateField(CurrentNoteID, "Comment", "CommentID","Page", tbxSearchPages.getText());
                updated = true;
            }
            if (this.CurrentSourceType.equals("Video") || this.CurrentSourceType.equals("Audio")) {
                if (!valuesAreDifferent(tbxSearchTimeStamp.getText(), CurrentSelectionData.get(10))) {
                    search.updateField(CurrentNoteID, "Comment", "CommentID", "TimeStamp", tbxSearchTimeStamp.getText());
                    updated = true;
                }
            }
            if(!valuesAreDifferent(CurrentSummary, CurrentSelectionData.get(11))){
                search.updateField(CurrentNoteID, "Comment", "CommentID", "Summary", CurrentSummary);
                updated = true;
            }
            if(!valuesAreDifferent(CurrentTopic, CurrentSelectionData.get(12))){
                search.updateField(CurrentNoteID, "Topic", "TopicID", "Topic", CurrentTopic);
                updated = true;
            }
            search.updateFiles(this.CurrentNoteID, this.tblSearchFiles.getItems(), this.CurrentFiles);

            if(search.getErrors()){
                stmt = conn.prepareStatement("ROLLBACK TO SAVEPOINT 'theHounds'");
                stmt.execute();
                displayFailureMessage("Database update failed!", "Database update");
            }else{
                if(updated || search.fileUpdate)
                    displaySuccessMessage("Database update successful!", "Database update");
                else
                    displaySuccessMessage("No database updates detected.", "Database update");
            }
            stmt = conn.prepareStatement("RELEASE SAVEPOINT 'theHounds'");
            stmt.execute();
            intTopicCount = search.count("Topic", "Topic", prevTopic);
            intQuestionCount = search.count("Question", "Question", prevQuestion);
            conn.close();
            this.boolRefresh = true;

            if(!Arrays.asList(TopicList).contains(CurrentTopic) || intTopicCount == 0){
                updatedTopicBox();
            }

            if(!Arrays.asList(QuestionList).contains(CurrentQuestion) || intQuestionCount == 0){
               updateQuestionBox();
            }

            selectionData();




        }catch(Exception ex){
            displayFailureMessage("There was a problem performing the update.\n\n" + ex.toString(), "Error: Problem with Updating");
        }finally {
            try{
                if (conn != null) {
                    conn.close(); // throws exception here
                }
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                displayFailureMessage(e.toString(), "Error: InitializeQuestionBox");
            }
        }
    }

    private void updatedTopicBox(){
        initializeTopicBox();
        searchTopic(CurrentTopic);
        tblSources.requestFocus();
        for (int row = 0; row < tblSources.getItems().size(); row++){
            Source note = tblSources.getItems().get(row);
            if(note.getID() == CurrentNoteID){
                tblSources.getSelectionModel().select(row);
                tblSources.getFocusModel().focus(row);
                break;
            }
        }
    }

    private void updateQuestionBox(){
        initializeQuestionBox();
        searchTopic(CurrentTopic);
        tblSources.requestFocus();
        for (int row = 0; row < tblSources.getItems().size(); row++){
            Source note = tblSources.getItems().get(row);
            if(note.getID() == CurrentNoteID){
                tblSources.getSelectionModel().select(row);
                tblSources.getFocusModel().focus(row);
                break;
            }
        }
    }

    private void updateTopicQuestionBoxes(){
        searchTopic(CurrentTopic);
        tblSources.requestFocus();
        for (int row = 0; row < tblSources.getItems().size(); row++){
            Source note = tblSources.getItems().get(row);
            if(note.getID() == CurrentNoteID){
                tblSources.getSelectionModel().select(row);
                tblSources.getFocusModel().focus(row);
                break;
            }
        }
    }
}