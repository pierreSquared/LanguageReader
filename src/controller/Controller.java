package controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import model.*;
import view.LanguageMenuItem;
import view.OurButton;
import view.View;

import java.io.File;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pierre on 30/04/16.
 */
public class Controller {
    private View view;
    private Model model;
    public WordsToFile newFile = new WordsToFile("English", "Polish", "txt");
    private OurButton currentButton = null;
    private boolean paginationStatus = false;
    ExecutorService daemonExecutorService = Executors.newCachedThreadPool(r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });
    ExecutorService executorService = Executors.newCachedThreadPool();

    public Controller(Model model) {
        this.model = model;
    }

    public void setTranslator(LanguageTranslator translator){
        model.setTranslator(translator);
    }

    public void addListeners() {
        view.addOpenFileListener(new OpenFileListener());
        for(MenuItem lang: view.getLanguagesFrom().getItems()) {
            view.addLanguageButtonListener((LanguageMenuItem) lang,
                    new LanguageButtonListener(((LanguageMenuItem) lang).getLanguagePair(), true));
        }
        for(MenuItem lang: view.getLanguagesTo().getItems()) {
            view.addLanguageButtonListener((LanguageMenuItem) lang,
                    new LanguageButtonListener(((LanguageMenuItem) lang).getLanguagePair(), false));
        }
        view.getScene().setOnKeyPressed(this::handldeKeyPress);
    }

    public void handldeKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ADD:
                view.getPagination().setCurrentPageIndex(view.getPagination().getCurrentPageIndex()-1);
        }
    }

    public void openBook(File file) {
        model.openFile(file);
        TextContainer text = model.getText();
        view.name = text.filname;
        view.createPagination(text.getNumberOfPages());
    }

    public void setPage(int page) {
        view.getPagination().setCurrentPageIndex(page);
        view.reloadPage();
    }

    public class OpenFileListener implements EventHandler<ActionEvent> {
        String userDir = System.getProperty("user.home");
        FileChooser fileChooser = new FileChooser();
        @Override
        public void handle(ActionEvent event) {
            File file = fileChooser.showOpenDialog(view.getPrimaryStage());
            openBook(file);
            App.settings.setBook(file);
          //  view.showText(text, 0);
        }
    }

    public class LanguageButtonListener implements EventHandler<ActionEvent> {
        LanguageClass language;
        boolean type;
        LanguageButtonListener(LanguageClass language, boolean type) {
            this.language = language;
            this.type = type;
        }
        @Override
        public void handle(ActionEvent event) {
            if(type) {
                model.setLanguageFrom(language);
                if(newFile!=null)
                    newFile.export();
                newFile = new WordsToFile(model.getLanguageFrom(), model.getLanguageTo(), "txt");
            } else {
                model.setLanguageTo(language);
                if(newFile!=null)
                    newFile.export();
                newFile = new WordsToFile(model.getLanguageFrom(), model.getLanguageTo(), "txt");
            }
            cacheCurrentPage();
        }
    }

    public void addFontStyle() { view.selectTogglePropertyStyleView(new SelectTogglePropertyStyle()); }

    public class SelectTogglePropertyStyle implements ChangeListener<Toggle> {
        final ToggleGroup group = view.fontStyleToggleGroup;
        @Override
        public void changed(ObservableValue<? extends Toggle> on, Toggle oldToggle, Toggle newToggle){
            if(group.getSelectedToggle() != null) {
                System.out.println("dziala");
                view.fontStyl = newToggle.getUserData().toString();
                view.reloadPage();
                //   if(paginationStatus)
                System.out.println(view.fontStyl);
                //  newToggle.setSelected(true);
            }
        }
    }

    public void addFontSize() { view.selectTogglePropertySizeView(new SelectTogglePropertySize()); }

    public class SelectTogglePropertySize implements ChangeListener<Toggle> {
        final ToggleGroup group = view.fontSizeToggleGroup;
        @Override
        public void changed(ObservableValue<? extends Toggle> on, Toggle oldToggle, Toggle newToggle){
            //System.out.println(group.getSelectedToggle().getUserData().toString());
            if(group.getSelectedToggle() != null) {
                view.fontSiz = newToggle.getUserData().toString();
                view.reloadPage();
              //  System.out.println(view.fontSiz);
                newToggle.setSelected(true);
            }
        }
    }

    public void cacheCurrentPage() {
        if(model.getText()!=null)
            model.getText().cacheTranslation(view.getCurrentPageNumber());
    }

    public void paginationSetPageFactory() {
        view.changePageInPagination(new PaginationControl());
    }

    public void PaginationStatus(){
        if(paginationStatus == true)
            view.deletePagination();
        paginationStatus =  true;
    }

    public class PaginationControl implements Callback<Integer, Node> {
        final int numberOfPages=model.getText().getNumberOfPages();
        final TextContainer text = model.getText();
        @Override
        public Node call(Integer pageIndex) {
            if(pageIndex>=numberOfPages)
                return null;
            else{
                return view.showText(text, pageIndex);
            }
        }
    }


    public void showPopup(OurButton button) {
        view.showPopup(button.localToScene(0, 0).getX(), button.localToScene(0, 0).getY(),
                model.getTranslation(button.getText()));
    }

    public void deletePopups() {
        view.deletePopups();
    }

    public class AddWord implements EventHandler<ActionEvent>{
        final OurButton ourButton;
        AddWord(OurButton ourButton){
            this.ourButton = ourButton;
        }
        @Override
        public void handle(ActionEvent event) {
           // System.out.println(ourButton.getText()+":)");
             newFile.addWord(model.getProperWord(ourButton.getText()), getTranslation(ourButton.getText()));
        }

    }

    public void addWordToTranslation(OurButton button){
        view.addWordToFile(new AddWord(button));
    }

    public EventHandler<MouseEvent> buttonClick(OurButton button){
        return new ContextMenuClick(button);
    }

    class ContextMenuClick implements EventHandler<MouseEvent>{
        OurButton button;
        public EventHandler<ActionEvent> actionEvent;
        ContextMenuClick(OurButton button){
            this.button = button;
            currentButton = button;
            actionEvent = new AddWord(button);
       //     System.out.println(currentButton.getText());
        }
        @Override
        public void handle(MouseEvent e){
            if(e.getButton() == MouseButton.SECONDARY) {
                view.addContextMenu(e.getScreenX(), e.getScreenY(), button);
                addWordToTranslation(button);
            }
            if(e.getButton() == MouseButton.PRIMARY)
                showPopup(button);
        }
    }
    public void serializeDictionary() {
        model.serializeDictionary();
    }

    public java.lang.String getTranslation(java.lang.String word) {
        return model.getTranslation(word);
    }

    public void setView(View view) {
        this.view = view;
    }

    public ExecutorService getDaemonExecutorService() {
        return daemonExecutorService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public List<LanguageClass> getAvailableLanguages(){
        return model.getAvailableLanguages();
    }
}
