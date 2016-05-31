package view;

import model.LanguageClass;

/**
 * Created by pierre on 30/05/16.
 */
public class LanguageMenuItem  extends javafx.scene.control.MenuItem{
    LanguageClass language;
    public LanguageMenuItem(LanguageClass language) {
        super(language.getName());
        this.language = language;
    }
    public java.lang.String getLanguageName() {
        return language.getName();
    }
    public java.lang.String getLanguageCode() {
        return language.getCode();
    }
    public LanguageClass getLanguagePair() {
        return language;
    }
}