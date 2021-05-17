package printer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import researchApp.NoteFile;
import researchApp.Source;


import java.util.HashMap;
import java.util.Map;

public class PrintOut {
    private VBox vBox;
    private Label jobStatus = new Label();
    private ComboBox<Printer> printers;
    private Button btnPrint;
    private Button btnPrintPage;
    private Button btnExit;
    private ObservableList<String> note;
    private ObservableList<NoteFile> files;
    private Source source;
    private String[] pages;
    private Map<String, String> map = new HashMap<>();
    private ObservableMap<String, String> noteData = FXCollections.observableMap(map);
    private String[] categories = {"Question", "Quote", "Term", "Date", "Volume", "Edition", "Issue", "Hyperlink", "Comment", "Page", "TimeStamp",
            "Summary", "Topic", "Files", "ID", "Type", "Title"};

    public PrintOut(ObservableList<String> note, ObservableList<NoteFile> files, Source source){
        this.note = note;
        this.files = files;
        this.source = source;
        setupData();
    }

    public void preview(){

        ParsePrint parseText = new ParsePrint(note());
        pages = parseText.docPages;
        PrinterJob printerJob = PrinterJob.createPrinterJob();

        Pagination pagination = new Pagination(pages.length);
        pagination.setStyle("-fx-border-color: dimGrey");
        pagination.setPageFactory((Integer pageIndex) -> {
            if (pageIndex <= pages.length) {
                vBox = createPage(pageIndex, pages, printerJob);
                return vBox;
            } else {
                return null;
            }
        });
        Stage stage = new Stage();
        setupButtons(stage, printerJob);
        setupPrintersComboBox();
        setupStatusLabel();

        AnchorPane anchor = new AnchorPane(btnPrint, btnPrintPage, btnExit, printers, jobStatus);
        AnchorPane.setTopAnchor(pagination, printerJob.getJobSettings().getPageLayout().getTopMargin());
        AnchorPane.setBottomAnchor(pagination, printerJob.getJobSettings().getPageLayout().getBottomMargin());
        AnchorPane.setLeftAnchor(pagination, printerJob.getJobSettings().getPageLayout().getLeftMargin());
        AnchorPane.setRightAnchor(pagination, printerJob.getJobSettings().getPageLayout().getRightMargin());
        anchor.getChildren().addAll(pagination);

        Scene scene = new Scene(anchor);
        stage.setScene(scene);
        stage.getIcons().add(new Image("images/icon.png"));
        stage.setResizable(false);
        stage.setTitle("Print Preview");
        stage.show();
    }

    private String note(){
        return noteData.get("Topic") + noteData.get("Summary") + noteData.get("Question") +
                noteData.get("Comment") + noteData.get("Quote") + noteData.get("Term") +
                noteData.get("Date") + noteData.get("Volume") + noteData.get("Edition") +
                noteData.get("Issue") + noteData.get("Page") + noteData.get("TimeStamp") +
                noteData.get("Type") + noteData.get("Title") + noteData.get("Hyperlink") +
                noteData.get("Files");
    }

    private int itemsPerPage(){ return 1;}

    private VBox createPage(int pageIndex, String[] docs, PrinterJob pj) {
        PageLayout pg = pj.getJobSettings().getPageLayout();
        VBox vBox = new VBox();
        int page = pageIndex * itemsPerPage();

        for (int p = page; p < page + itemsPerPage(); p++) {
            TextFlow text = new TextFlow(new Text(docs[p]));
            text.setStyle("-fx-background-color: white;");
            text.setMaxHeight(pg.getPrintableHeight());
            text.setMaxWidth(pg.getPrintableWidth());
            vBox.getChildren().add(text);
        }
        return vBox;
    }

    private Button setupPrintButton(PrinterJob job){
        Button btn = new Button();
        btn.setText("Print");
        btn.setLayoutX(30);
        btn.setLayoutY(10);
        btn.setPrefSize(50, 20);
        btn.setOnAction(e -> print(job, pages));
        return btn;
    }

    private Button setupPrintPageButton(PrinterJob job){
        Button btn = new Button();
        btn.setText("Print Page");
        btn.setLayoutX(90);
        btn.setLayoutY(10);
        btn.setPrefSize(80, 20);
        btn.setOnAction(e -> printPage(job, vBox));
        return btn;
    }

    private Button setupExitButton(Stage stage){
        Button btn = new Button();
        btn.setText("Exit");
        btn.setLayoutX(180);
        btn.setLayoutY(10);
        btn.setOnAction(e -> exit(stage));
        btn.setPrefSize(50, 20);
        return btn;
    }

    private String nullCheck(Object obj){
        if(obj == null)
            return "";
        return obj.toString();
    }

    private ObservableSet<Printer> getPrinters(){
        return Printer.getAllPrinters();
    }

    private Printer getDefaultPrinter(){
        return Printer.getDefaultPrinter();
    }

    private Printer getSelectedPrinter(){
        return printers.getValue();
    }

    private void exit(Stage stage){
        stage.close();
    }

    private void print(PrinterJob pJob, String[] pages){
        if (!pJob.getPrinter().getName().equals(getSelectedPrinter().getName())){
            pJob = PrinterJob.createPrinterJob(getSelectedPrinter());
        }
        jobStatus.textProperty().bind(pJob.jobStatusProperty().asString());
        PageRange pgRange = new PageRange(1, pages.length);
        pJob.getJobSettings().setPageRanges(pgRange);
        PageLayout pgLayout = pJob.getJobSettings().getPageLayout();
        JobSettings js = pJob.getJobSettings();

        boolean printed = false;
        for (PageRange pr : js.getPageRanges()){
            for(int p = pr.getStartPage(); p <= pr.getEndPage(); p++){
                TextFlow text = new TextFlow(new Text(pages[p-1]));
                text.setPrefHeight(pgLayout.getPrintableHeight());
                text.setPrefWidth(pgLayout.getPrintableWidth());
                printed = pJob.printPage(pgLayout, text);
                if (!printed) {
                    break;
                }
            }
        }
        if(printed){
            pJob.endJob();
            btnExit.fire();
        }else{
            jobStatus.textProperty().unbind();
        }
    }

    private void printPage(PrinterJob pJob, Node node){
        if (!pJob.getPrinter().getName().equals(getSelectedPrinter().getName())){
            pJob = PrinterJob.createPrinterJob(getSelectedPrinter());
        }
        jobStatus.textProperty().bind(pJob.jobStatusProperty().asString());
        PageLayout pageLayout = pJob.getJobSettings().getPageLayout();

        boolean printed = pJob.printPage(pageLayout, node);
        if(printed){
            pJob.endJob();
            btnExit.fire();
        }else{
            jobStatus.textProperty().unbind();
        }
    }

    private void setupButtons(Stage stage, PrinterJob job){
        btnPrint = setupPrintButton(job);
        btnPrintPage = setupPrintPageButton(job);
        btnExit = setupExitButton(stage);
    }

    private void setupPrintersComboBox(){
        printers = new ComboBox<>();
        printers.getItems().addAll(getPrinters());
        printers.setValue(getDefaultPrinter());
        printers.setLayoutX(240);
        printers.setLayoutY(10);
        printers.setMaxWidth(200);
        printers.setPrefWidth(300);
        printers.setOnAction(e-> System.out.println(getSelectedPrinter()));
    }

    private void setupStatusLabel(){
        jobStatus.setText("Printer Ready...");
        jobStatus.setLayoutX(460);
        jobStatus.setLayoutY(15);
        jobStatus.setPrefWidth(100);
    }

    private void setupData(){
        int index = 0;
        String CRLF = "\r\n";
        for (String cat: categories) {
            if(cat.equals("Files")){
                if(files != null){
                    StringBuilder str = new StringBuilder();
                    for (NoteFile file : files) {
                        if (!str.toString().equals("")) str.append(";");
                        str.append(file.getFileName());
                    }
                    noteData.put(cat, cat + ":\r\n\t" + str + CRLF + CRLF);
                }else{
                    noteData.put(cat, cat + ":\r\n\t" + "" + CRLF + CRLF);
                }

            }else if(cat.equals("ID") || cat.equals("Type") || cat.equals("Title")) {
                if (cat.equals("ID")) noteData.put(cat, cat + ":\r\n\t" + nullCheck(source.getID()) + CRLF + CRLF);
                if (cat.equals("Type"))
                    noteData.put(cat, cat + ":\r\n\t" + nullCheck(source.getSourceType()) + CRLF + CRLF);
                if (cat.equals("Title"))
                    noteData.put(cat, cat + ":\r\n\t" + nullCheck(source.getTitle()) + CRLF + CRLF);
            }else if(cat.equals("Comment")){
                noteData.put(cat, cat + ":\r\n" + nullCheck(note.get(index)) + CRLF + CRLF);
            }else{
                noteData.put(cat, cat + ":\r\n\t" + nullCheck(note.get(index)) + CRLF + CRLF);
            }
            index++;

        }
    }
}
