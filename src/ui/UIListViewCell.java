package ui;

import com.sun.istack.internal.NotNull;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

/**
 * Created by Antonio Zaitoun on 15/12/2017.
 */

/**
 * UIListViewCell is a ListCell subclass, created for ease of use and convenience.
 * @param <D> The type of the data. Same as your ListView.
 * @param <N> The type of the node you wish to display.
 */
public abstract class UIListViewCell<D,N extends Node> extends ListCell<D> {

    /**
     * Method is called when cell needs to load.
     * @param item The object containing the data. passed from the ListView.
     * @return The graphic you wish to display.
     */
    public abstract N load(@NotNull D item);

    @Override
    protected void updateItem(D item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            clearContent();
        } else {
            setGraphic(load(item));
        }

    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }
}
