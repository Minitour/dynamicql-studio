package ui;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.Collection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * Created by Antonio Zaitoun on 23/08/2017.
 *
 * A dynamic UITableView class
 * @param <T> is the data type you wish to display.
 */
abstract public class UITableView<T> extends UIView {

    /**
     * The primary JavaFX Table View.
     */
    @FXML
    protected TableView<T> tableView;

    /**
     * The data supplied from the data source @see dataSource.
     */
    protected ObservableList<T> data;

    /**
     * A static array that holds the table's columns.
     */
    private TableColumn columns[];

    /**
     * A static array that holds the bundle ids that are fetched from bundleIdForIndex.
     */
    private String[] bundleIds;

    /**
     * A static array that holds cell data factories.
     */
    private Callback<TableColumn.CellDataFeatures<T,String>, ObservableValue<T>> cellDataFactories[];

    public UITableView() {
        super("/ui/res/xml/tableview.fxml");
    }

    @Override
    public void layoutSubviews(ResourceBundle bundle) {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        int size = numberOfColumns();
        columns = new TableColumn[size];
        bundleIds = new String[size];
        cellDataFactories = new Callback[size];
        Collection<? extends T> dataSource = dataSource();
        if(dataSource != null) {
            data = FXCollections.observableArrayList(dataSource());
        }else{
            data = FXCollections.observableArrayList();
        }


            for (int i = 0; i < size; i++) {
                String id = bundleIdForIndex(i);
                bundleIds[i] = id;

                String name = getSafeValueFromBundle(id, bundle);
                columns[i] = new TableColumn(name);
                cellDataFactories[i] = getCallback(cellValueForColumnAt(i));
                columns[i].setCellValueFactory(cellDataFactories[i]);
                tableView.getColumns().add(columns[i]);
            }

            tableView.setItems(data);
    }

    @Override
    public void layoutBundle(ResourceBundle bundle) {
        int size = bundleIds.length;
        for(int i = 0;i<size;i++){
            String id = bundleIds[i];
            String name = null;
            if (id != null)
                try { name = bundle.getString(id); }
                catch (MissingResourceException e){ name = id; }

            columns[i].setText(name);
        }
    }

    @Override
    final public String resource() {
        return "/ui/res/xml/tableview.fxml";
    }

    public abstract int numberOfColumns();

    @NotNull
    public abstract Collection<? extends T> dataSource();

    @Nullable
    public abstract String bundleIdForIndex(int index);

    /**
     *
     * @param index
     * @return
     */
    public abstract TableColumnValue<T> cellValueForColumnAt(int index);

    public String dataForColumn(T data, int column){
        return null;
    }

    public void reloadData(){
        data.clear();
        data.addAll(dataSource());
    }

    protected String getStringValue(int row,int column){
        String value = null;
        try{
            ObservableValue s = ((TableColumn) tableView.getColumns().get(column)).getCellObservableValue(row);
            value = s.getValue().toString();
        }catch (ClassCastException e){
            value = ((TableColumn) tableView.getColumns().get(column)).getCellObservableValue(row).toString();
        }finally {
            return value
                    .replace(",","")
                    .replace("\"","")
                    .replace("\n"," ");
        }
    }

    public Pane getToolBar(){
        return findViewById("toolbar");
    }

    public TableColumn getTableColumnAt(int index){
        return columns[index];
    }

    private String getSafeValueFromBundle(String id,ResourceBundle bundle){
        String name = null;
        if (id != null){
            try {
                name = bundle.getString(id);
            }catch (Exception e){
                name = id;
            }
        }
        return name;
    }

    public interface TableColumnValue<T>{
        Object value(T object);
    }

    private <T>Callback getCallback(TableColumnValue<T> value){
        return  (Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>>)
                 param -> {

                    String val = null;
                    try {
                        val = value.value(param.getValue()).toString();
                    }catch (NullPointerException e) {val = ""; };
                    return new SimpleStringProperty(val);
                 };
    }

}
