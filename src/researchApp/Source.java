package researchApp;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Source {
    private final SimpleIntegerProperty ID;
    private final SimpleStringProperty SourceType;
    private final SimpleStringProperty Title;
    private final SimpleStringProperty Summary;

    public Source(int id, String sourceType, String title, String summary) {
        this.ID = new SimpleIntegerProperty(id);
        this.SourceType = new SimpleStringProperty(sourceType);
        this.Title = new SimpleStringProperty(title);
        this.Summary = new SimpleStringProperty(summary);
    }

    public int getID() {
        return ID.get();
    }

    public String getSourceType() {
        return SourceType.get();
    }

    public String getTitle() {
        return Title.get();
    }

    public String getSummary() {
        return Summary.get();
    }

    public void setID(int ID) {
        this.ID.set(ID);
    }

    public void setSourceType(String sourceType) {
        this.SourceType.set(sourceType);
    }

    public void setTitle(String title) {
        this.Title.set(title);
    }

    public void setSummary(String summary) {
        this.Summary.set(summary);
    }

    public SimpleIntegerProperty IDProperty() {
        return ID;
    }

    public SimpleStringProperty sourceTypeProperty() {
        return SourceType;
    }

    public SimpleStringProperty titleProperty() {
        return Title;
    }

    public SimpleStringProperty summaryProperty() {
        return Summary;
    }
}
