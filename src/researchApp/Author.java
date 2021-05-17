package researchApp;

import javafx.beans.property.SimpleStringProperty;

public class Author {
    private final SimpleStringProperty FirstName;
    private final SimpleStringProperty MiddleName;
    private final SimpleStringProperty LastName;
    private final SimpleStringProperty Suffix;

    Author(String firstName, String middleName, String lastName, String suffix){
        this.FirstName = new SimpleStringProperty(Convert(firstName));
        this.MiddleName = new SimpleStringProperty(Convert(middleName));
        this.LastName = new SimpleStringProperty(Convert(lastName));
        this.Suffix = new SimpleStringProperty(Convert(suffix));
    }
    @Override
    public String toString(){
        return "Author{FirstName=" + FirstName + "MiddleName=" + MiddleName +
                "LastName=" + LastName + "Suffix=" + Suffix;
    }

    private String Convert(Object obj1) {
        if (obj1 == null || obj1.toString().isEmpty() || obj1.toString().trim().isEmpty()) {
            obj1 = ""; }
        return obj1.toString();
    }

    public String getFirstName() {
        return FirstName.get();
    }
    public String getMiddleName() {
        return MiddleName.get();
    }
    public String getLastName() {
        return LastName.get();
    }
    public String getSuffix() {
        return Suffix.get();
    }

    void setFirstName(String firstName) {
        this.FirstName.set(firstName);
    }
    void setMiddleName(String middleName) {
        this.MiddleName.set(middleName);
    }
    void setLastName(String lastName) {
        this.LastName.set(lastName);
    }
    void setSuffix(String suffix) {
        this.Suffix.set(suffix);
    }

    public SimpleStringProperty firstNameProperty() {
        return FirstName;
    }
    public SimpleStringProperty middleNameProperty() {
        return MiddleName;
    }
    public SimpleStringProperty lastNameProperty() {
        return LastName;
    }
    public SimpleStringProperty suffixProperty() {
        return Suffix;
    }
}
