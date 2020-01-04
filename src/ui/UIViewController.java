package ui;

import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Created by Antonio Zaitoun on 28/07/2017.
 */

public class UIViewController implements Initializable, UIView.Delegate, LocalizationManager.Translatable{

    /**
     *
     * Init controller from FXML.
     * Note that the given FXML must have a root view of type UIView with the `fx:id` tag set to `root`
     * as well as having an `fx:controller` property set to the given class type.
     *
     * @{code: <UIView fx:id="root" fx:controller="com.package.ControllerClass">...</UIView>}
     *
     * @param path The path to the FXML file
     * @param type The class type
     * @return An instance of UIViewController
     * @throws IOException
     */
    public static <T extends UIViewController> T init(String path,Class type) throws IOException {
        FXMLLoader loader = new FXMLLoader(type.getResource(path));
        loader.load();
        UIViewController controller = loader.getController();
        controller.root = loader.getRoot();
        return (T) controller;
    }

    /**
     * Create a UIViewController instance from any fxml file.
     * @param path The path for the fxml.
     */
    public UIViewController(@NamedArg("FXML") String path){
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view = root;
    }

    /**
     * The root view declared in the FXML. Might be null.
     */
    @FXML
    private UIView root;

    /**
     * The localization id of the controller.
     */
    private String id;

    public final UIView view;

    @Override
    final public void initialize(URL location, ResourceBundle resources) {
        this.id = identifier();
        LocalizationManager.shared().register(id,this);
        viewWillLoad(LocalizationManager.shared().getBundle());
    }

    /**
     * Method used to get the identifier of the current instance of this controller.
     * This identifier is used to manage the localization of the controller.
     * @return Instance identifier.
     */
    public String identifier(){
        return id == null ? UUID.randomUUID().toString() : id;
    }

    /**
     * Called when controller is loaded. At this point the layout has been initialized.
     *
     * @param bundle The current active resource bundle, containing all the strings.
     */
    public void viewWillLoad(ResourceBundle bundle){}

    /**
     * This method is called when the resource bundle has been changed.
     * Use this method to update the sub views in the controller and change their display according to the new bundle.
     *
     * @param bundle The new bundle.
     */
    public void layoutBundle(ResourceBundle bundle){}

    /**
     * Getter method. Used to return the current root view of the controller.
     * @return The root node of the controller.
     */
    public StackPane getRootView(){
        return root;
    }

    /**
     * The title of the controller.
     * @return Optional title.
     */
    public String title(){ return null; }

    /**
     * A method that returns the node that is present within the root view of this controller.
     * There is no need to include a `#`.
     *
     * Usage example:
     *      Label myLabel = findViewById("titleLabel");
     *
     * @param id The FX ID of the view you wish to find.
     * @param <E> The `Node` class.
     * @return The node or null if does not exist.
     */
    public <E extends Node> E findViewById(String id){
        return (E) getRoot().lookup("#"+id);
    }

    @Override
    final public StackPane getRoot() {
        return getRootView();
    }

    @Override
    final public void translate(ResourceBundle bundle) {
        layoutBundle(bundle);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LocalizationManager.shared().remove(id);
    }
}
