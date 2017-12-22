package live.itrip.client.bean.ui;

import javafx.fxml.Initializable;
import javafx.scene.Node;

/**
 * Created on 2017/5/26.
 *
 * @author JianF
 */
public class PageInfo {
    private Node node;
    private Initializable controller;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Initializable getController() {
        return controller;
    }

    public void setController(Initializable controller) {
        this.controller = controller;
    }
}
