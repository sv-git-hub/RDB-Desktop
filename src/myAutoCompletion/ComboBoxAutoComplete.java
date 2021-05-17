package myAutoCompletion;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class ComboBoxAutoComplete<T> {

    private ComboBox<T> cmb;
    private String filter = "";
    private ObservableList<T> originalItems;
    private boolean boolSkipDefault = true;
    private ComboBoxListViewSkin cbSkin;
    public void setOriginalItems(ObservableList<T> list){
        this.originalItems = list;
    }

    public ComboBoxAutoComplete(final ComboBox<T> cmb) {
        this.cmb = cmb;
        this. originalItems = FXCollections.observableArrayList(cmb.getItems());
        this.cmb.setOnKeyPressed(this::handleOnKeyPressed);
        this.cmb.setOnHidden(this::handleOnHiding);

        if(this.cmb.getSkin() != null) this.cmb.getSkin().dispose();
        if(cbSkin != null) cbSkin = null;
        cbSkin = new ComboBoxListViewSkin(this.cmb);
        this.cmb.setSkin(cbSkin);
        cbSkin.getPopupContent().addEventFilter(KeyEvent.KEY_PRESSED, (event) -> {
            if(event.getCode() == KeyCode.SPACE){
                filter += event.getText();
                event.consume();}
        });
    }

    private void handleOnKeyPressed(KeyEvent e) {

        boolSkipDefault = false;
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();

        if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.PERIOD) {
            this.filter += e.getText();
        }

        if (code == KeyCode.BACK_SPACE && this.filter.length() > 0) {
            this.filter = this.filter.substring(0, filter.length() - 1);
            cmb.getItems().setAll(originalItems);
        }
        if (code == KeyCode.ESCAPE) {
            this.filter = "";
        }

        if (this.filter.length() == 0) {
            filteredList = originalItems;

        } else {
            cmb.getItems().remove(null);
            Stream<T> itens = cmb.getItems().stream();
            String txtUsr = this.filter.toLowerCase();
            itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
            cmb.show();
        }
        cmb.getItems().setAll(filteredList);
        cmb.getSelectionModel().selectFirst();
        cmb.setValue((T)filter);
    }

    private void handleOnHiding(Event e) {
        this.filter = "";
        T s;
        if(!boolSkipDefault){
            if(cmb.getSelectionModel().getSelectedIndex() != 0){
                s =cmb.getSelectionModel().getSelectedItem();
            }else{
                s = cmb.getItems().get(0);
            }
            cmb.getItems().setAll(originalItems);
            cmb.getSelectionModel().select(s);
        }
    }
}
