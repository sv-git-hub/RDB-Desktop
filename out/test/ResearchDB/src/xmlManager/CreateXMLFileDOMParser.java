package xmlManager;

import javafx.collections.ObservableList;
import org.apache.commons.text.StringEscapeUtils;
import researchApp.Author;
import researchApp.GlobalVariables;
import javafx.collections.ObservableMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Base64;

public class CreateXMLFileDOMParser {

    private Document doc;


    public String xmlFilePath;
    private String fileName;
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public CreateXMLFileDOMParser(String fileName){
        setFileName(fileName);
        xmlFilePath = GlobalVariables.USER_DESKTOP + getFileName();
        try{
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new Resolver());
            doc = docBuilder.newDocument();
        }catch(ParserConfigurationException ex){
            ex.printStackTrace();
        }

        createNote();
        createFile();
    }

    public CreateXMLFileDOMParser(String fileName, ObservableMap map){
        setFileName(fileName);
        xmlFilePath = GlobalVariables.USER_DESKTOP + getFileName();
        try{
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        }catch(ParserConfigurationException ex){
            ex.printStackTrace();
        }

        createNote(map);
        createFile();
    }

    private void createNote(){
        // root:
        Element root = doc.createElement("note");
        doc.appendChild(root);

            // table:
            Element table = doc.createElement("table");
            root.appendChild(table);

            Attr attr = doc.createAttribute("name");
            attr.setValue("Author");
            table.setAttributeNode(attr);

                // first, middle, last, suffix
                Element firstName = doc.createElement("firstname");
                firstName.appendChild(doc.createTextNode("C."));
                table.appendChild(firstName);

                Element middleName = doc.createElement("middlename");
                middleName.appendChild(doc.createTextNode("S."));
                table.appendChild(middleName);

                Element lastName = doc.createElement("lastname");
                lastName.appendChild(doc.createTextNode("Lewis"));
                table.appendChild(lastName);

                Element suffix = doc.createElement("suffix");
                suffix.appendChild(doc.createTextNode(""));
                table.appendChild(suffix);
    }

    private void createNote(ObservableMap<Integer, ObservableMap<String, Object[]>> map){

        Element notes = doc.createElement("notes");
        doc.appendChild(notes);
        System.out.println(map.size());
        map.forEach((k, v) -> {
                //System.out.println("Note: " + k.toString());

                Element note = doc.createElement("note");
                notes.appendChild(note);
                Attr nAttr = doc.createAttribute("id");
                nAttr.setValue(StringEscapeUtils.escapeXml10(k.toString()));
                note.setAttributeNode(nAttr);

                v.forEach((x, y) -> {
                    //System.out.println("Table: " + x + ":");

                    Element tbl = doc.createElement("table");
                    note.appendChild(tbl);
                    Attr tblAttr = doc.createAttribute("name");
                    tblAttr.setValue(StringEscapeUtils.escapeXml10(x));
                    tbl.setAttributeNode(tblAttr);

                    int i = 0;
                    if(!x.equals("File") && !x.equals("Author")) {
                        for (Object z : y) {
                            i++;
                            Element element = doc.createElement("field" + i);
                            element.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(z.toString())));
                            tbl.appendChild(element);
                        }
                    }else if(x.equals("File")){
                        for (Object z : y){
                            ObservableMap fd = (ObservableMap) z;

                            int n = 0;
                            for(Object f : fd.keySet()){

                                Element files = doc.createElement("file" + ++n);
                                tbl.appendChild(files);

                                Element elem1 = doc.createElement("filename");
                                elem1.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(f.toString())));
                                files.appendChild(elem1);

                                try {
                                    Element elem2 = doc.createElement("filedata");
                                    byte[] bytes = (byte[])fd.get(f.toString());
                                    elem2.appendChild(doc.createTextNode(Base64.getEncoder().encodeToString(bytes)));
                                    files.appendChild(elem2);
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }


                        }
                    }else if(x.equals("Author")){
                        int n = 0;
                        for (Object z : y){
                            ObservableList<Author> ad = (ObservableList<Author>) z;

                            for (Author a : ad){
                                Element author = doc.createElement("author" + ++n);
                                tbl.appendChild(author);


                                Element firstName = doc.createElement("firstname");
                                firstName.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(a.getFirstName())));
                                author.appendChild(firstName);

                                Element middleName = doc.createElement("middlename");
                                middleName.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(a.getMiddleName())));
                                author.appendChild(middleName);

                                Element lastName = doc.createElement("lastname");
                                lastName.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(a.getLastName())));
                                author.appendChild(lastName);

                                Element suffix = doc.createElement("suffix");
                                suffix.appendChild(doc.createTextNode(StringEscapeUtils.escapeXml10(a.getSuffix())));
                                author.appendChild(suffix);
                            }
                        }
                    }

                });});

    }

    private String setValue(String value){
        return value.isEmpty() ? "" : value;
    }

    private void createFile(){
        try{
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer transformer = tranFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));

            transformer.transform(domSource, streamResult);

        }catch(TransformerException ex){
            ex.printStackTrace();
        }
    }

}
