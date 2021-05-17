package xmlManager;

/*htps://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
htp://commons.apache.org/proper/commons-lang/download_lang.cgi
htp://commons.apache.org/proper/commons-text/download_text.cgi
htps://howtodoinjava.com/array/convert-byte-array-string-vice-versa/*/

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ReadXMLFileDOMParser {
    public ObservableList<ObservableMap<String, ObservableList<String>>> importNotes = FXCollections.observableArrayList();

    public ReadXMLFileDOMParser(String fileName){
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(fileName));
            NodeList noteList = doc.getElementsByTagName("note");

            for(int n = 0; n < noteList.getLength(); n++) {
                NodeList tableList = noteList.item(n).getChildNodes();
                ObservableMap<String, ObservableList<String>> tables = FXCollections.observableHashMap();
                doc.getDocumentElement().normalize();

                for (int t = 0; t < tableList.getLength(); t++) {

                    Node node = tableList.item(t);
                    Element tElem = (Element) node;

                    NodeList child = tElem.getChildNodes();
                    ObservableList<String> fields = FXCollections.observableArrayList();
                    for (int c = 0; c < child.getLength(); c++) {
                        if (!child.item(c).getNodeName().equals("author" + (c + 1)) && !child.item(c).getNodeName().equals("file" + (c + 1))){
                            fields.add(StringEscapeUtils.unescapeXml(child.item(c).getTextContent()));
                        }else{
                            NodeList childList = child.item(c).getChildNodes();
                            StringBuilder sb = new StringBuilder();
                            for(int cl = 0; cl < childList.getLength(); cl++){
                                if(cl < (childList.getLength()-1)) {
                                    sb.append(childList.item(cl).getTextContent()).append("*");
                                }else {
                                    fields.add(sb.append(childList.item(cl).getTextContent()).toString());
                                }
                            }
                        }
                    }
                    tables.put(tElem.getAttribute("name"), fields);

                }
                importNotes.add(tables);
            }

            System.out.println("tables map created");
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
