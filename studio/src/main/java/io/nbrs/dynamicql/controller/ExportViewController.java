package io.nbrs.dynamicql.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nbrs.dynamicql.model.QueryParameter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Antonio Zaitoun on 2020-01-04.
 */
public class ExportViewController extends MainViewController {

    private CodeArea jsonView;

    private CodeArea queryView;

    @FXML
    private Button export;

    @Override
    public void viewWillLoad(ResourceBundle bundle) {
        jsonView = new CodeArea();
        queryView = new CodeArea();

        setUpCodeArea(jsonView, rightMenu);
        setUpCodeArea(queryView, leftMenu);

        export.setVisible(false);
    }

    public void setQuery(String query) {
        queryView.replaceText(query);
    }

    public void setParameterList(List<QueryParameter> parameterList) {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.setPrettyPrinting().create();
        String text = gson.toJson(parameterList);
        jsonView.replaceText(text);
    }
}
