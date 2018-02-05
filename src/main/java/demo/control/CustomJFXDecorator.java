package demo.control;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * Created by Snart Lu on 2018/2/5. <br/>
 */
public class CustomJFXDecorator extends JFXDecorator {
    public CustomJFXDecorator(Stage stage, Node node) {
        this(stage, node, true, true, true);
    }

    public CustomJFXDecorator(Stage stage, Node node, boolean fullScreen, boolean max, boolean min) {
        super(stage, node, fullScreen, max, min);
        // top area is a buttons container and with a class 'jfx-decorator-buttons-container'
        Node btnContainerOpt = this.lookup(".jfx-decorator-buttons-container");
        if (btnContainerOpt != null) {
            // buttons container is a HBox
            final HBox buttonsContainer = (HBox) btnContainerOpt;

            // check and get the index of maximum button
            ObservableList<Node> buttons = buttonsContainer.getChildren();
            int btnMaxIdx = 0;
            if (fullScreen) {
                btnMaxIdx++;
            }
            if (min) {
                btnMaxIdx++;
            }
            if (buttons.size() >= btnMaxIdx) {
                final JFXButton btnMax = (JFXButton) buttons.get(btnMaxIdx);
                // set max button triggered when buttons container is double clicked
                buttonsContainer.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        btnMax.fire();
                    }
                });
            }

            // add HBox in the left of buttons container
            HBox leftBox = new HBox();
            leftBox.setAlignment(Pos.CENTER_LEFT);
            leftBox.setPadding(new Insets(0, 0, 0, 10));
            leftBox.setSpacing(10);

            // add icon in the left of HBox
            HBox iconBox = new HBox();
            iconBox.setAlignment(Pos.CENTER_LEFT);
            iconBox.setSpacing(5);

            // bind icon
            stage.getIcons().addListener((ListChangeListener<Image>) c -> {
                while (c.next()) {
                    iconBox.getChildren().clear();
                    ObservableList<? extends Image> icons = c.getList();
                    if (icons != null && !icons.isEmpty()) {
                        ImageView imageView;
                        for (Image icon : icons) {
                            imageView = new ImageView();
                            imageView.setFitWidth(20);
                            imageView.setFitHeight(20);
                            imageView.setImage(icon);
                            iconBox.getChildren().add(imageView);
                        }
                    }
                }
            });

            // bind title
            Label title = new Label();
            title.textProperty().bindBidirectional(stage.titleProperty());
            // set title to white because of the black background
            title.setTextFill(Paint.valueOf("#fdfdfd"));

            leftBox.getChildren().addAll(iconBox, title);

            HBox.setHgrow(leftBox, Priority.ALWAYS);
            buttonsContainer.getChildren().add(0, leftBox);
        }
    }
}
