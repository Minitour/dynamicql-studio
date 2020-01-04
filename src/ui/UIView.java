package ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by Antonio Zaitoun on 18/08/2017.
 */

public class UIView extends StackPane implements Initializable, LocalizationManager.Translatable{

    /**
     * The delegate.
     */
    private Delegate delegate;

    /**
     * The translation id of this view. This is used to register the view inside the Localization Manager.
     * Without this, the view will not receive translation updates. More over, if a translation id is not set
     * and the view is not registered in the localization manager then layoutBundle will never be called.
     */
    private String translationId;

    public void setDelegate(Delegate delegate){
        this.delegate = delegate;
    }

    protected Delegate getDelegate(){
        return this.delegate;
    }

    @Override
    final public void initialize(URL location, ResourceBundle resources) {
        observeParent();
        //setupNodes();
        layoutSubviews(LocalizationManager.shared().getBundle());
    }

    /**
     * Init from FXML file
     * @param fxml
     */
    public UIView(String fxml){
        try{
            load(fxml);
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public UIView(){
        if(resource() != null){
            try{
                load();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    private void load(String fxml) throws IOException{
        LocalizationManager manager = LocalizationManager.shared();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml),manager.getBundle());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();
        translationId = UUID.randomUUID().toString();
        manager.register(translationId,this);
    }

    protected void load() throws IOException{
        load(resource());
    }

    public <E extends Node> E findViewById(String identifier){
        return (E) lookup("#"+identifier);
    }

    public boolean remove(Node node){
        return removeRecursively(getChildren(),node);
    }

    public boolean remove(String id) {
        Node n = findViewById(id);
        return n != null && remove(n);
    }

    /**
     * Called when view is loaded.
     * @param bundle The resource bundle for localization.
     */
    public void layoutSubviews(ResourceBundle bundle){}

    /**
     * Called when system localization has changed.
     * @param bundle the resource bundle for localization.
     */
    public void layoutBundle(ResourceBundle bundle){}

    /**
     * A data source method.
     * @return The full path for the FXML file to load. Cannot be null.
     */
    public String resource(){
        return null;
    }

    public interface Delegate {
        default StackPane getRoot(){return null;}

        default Stage getStage(){return null;}
    }

    private void setupNodes(){
        try {
            Class current = Class.forName(this.getClass().getName());
            Field[]  fields = current.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                try{
                    Object o = fields[i].get(current);
                    if(o instanceof Node && !(o instanceof Pane)){
                        Node n = (Node) fields[i].get(current);
                        n.managedProperty().bind(n.visibleProperty());
                    }
                }catch (IllegalAccessException ignored){}
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean removeRecursively(ObservableList<Node> children, Node toRemove){
        if(toRemove == null)
            return false;

        if(children.contains(toRemove)){
            children.remove(toRemove);
            return true;
        }

        for (Node n : children) {
            if(n instanceof Pane){
                Pane pane = (Pane) n;
                boolean val = removeRecursively(pane.getChildren(),toRemove);
                if(val)
                    return true;
            }
        }

        return false;
    }

    private void observeParent(){
        parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                LocalizationManager.shared().remove(translationId);
            else
                LocalizationManager.shared().register(translationId,this);
        });
    }

    /**
     * @see LocalizationManager.Translatable
     *
     * Method is called when localization has changed.
     *
     * @param bundle The new resource bundle.
     */
    @Override
    final public void translate(ResourceBundle bundle) {
        layoutBundle(bundle);
    }

    @Override
    protected void finalize() throws Throwable {
        LocalizationManager.shared().remove(translationId);
        super.finalize();
    }


}
