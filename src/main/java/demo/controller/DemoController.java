package demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import demo.control.Event;
import demo.control.EventBus;
import demo.model.DemoInfo;
import demo.model.DemoInfoCache;
import demo.util.HttpResponse;
import demo.util.HttpsClient;
import demo.util.NotificationUtils;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Created by Snart Lu on 2018/2/5. <br/>
 */
@ViewController("/views/demo.fxml")
public class DemoController {
    private static Logger logger = LoggerFactory.getLogger(DemoController.class.getName());

    @FXMLViewFlowContext
    private ViewFlowContext context;
    @FXML
    private BorderPane root;
    @FXML
    private ToggleGroup typeGroup;
    @FXML
    private JFXRadioButton radioFood, radioTool;
    @FXML
    private JFXTextField nameField, descField, urlField;
    @FXML
    private JFXButton postBtn;

    private Disposable disposable;
    private Stage spinnerStage;
    private DemoInfo demoInfo;

    private static final int TYPE_FOOD = 1;
    private static final int TYPE_TOOL = 2;
    private static final String CACHE_PATH = "data" + File.separator + "info.data";

    @PostConstruct
    public void init() {
        demoInfo = new DemoInfo();

        bindDemoInfoToControls();
        initSpinner();
        initFields();

        JavaFxObservable.actionEventsOf(postBtn)
            .subscribeOn(Schedulers.computation())
            .subscribe(actionEvent -> {
                showSpinner();
                // runnable for that thread
                new Thread(this::submit).start();
            });

        disposable = EventBus.getInstance().register(event -> {
            if (event.getType() == Event.EventType.SAVE) {
                doSave();
            }
        });
    }

    @PreDestroy
    public void destroy() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void bindDemoInfoToControls() {
        radioFood.setUserData(TYPE_FOOD);
        radioTool.setUserData(TYPE_TOOL);

        demoInfo.typeProperty().addListener((observable, oldValue, newValue) -> {
            Toggle selected = null;
            for (Toggle toggle : typeGroup.getToggles()) {
                if (newValue == toggle.getUserData()) {
                    selected = toggle;
                    break;
                }
            }
            if (selected == null) {
                throw new IllegalArgumentException("Demo info set type value which is not in toggle values");
            }

            typeGroup.selectToggle(selected);
        });

        typeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> demoInfo
            .typeProperty().setValue((Integer) newValue.getUserData()));

        nameField.textProperty().bindBidirectional(demoInfo.nameProperty());
        descField.textProperty().bindBidirectional(demoInfo.descriptionProperty());
    }

    private void submit() {
        try {
            ObjectMapper om = new ObjectMapper();
            String postData = om.writeValueAsString(demoInfo);
            String url = urlField.getText();
            HttpResponse response = HttpsClient.doPostSSL(url, postData);
            if (response == null) {
                closeSpinner(() -> NotificationUtils.notifyError("提交失败！", root));
                return;
            }
            if (response.getStatus() != HttpStatus.SC_OK) {
                Exception exception = response.getException();
                if (exception != null && exception instanceof HttpHostConnectException) {
                    closeSpinner(() -> NotificationUtils.notifyError("无法连接服务器！", root));
                    return;
                }
                closeSpinner(() -> NotificationUtils.notifyError("提交失败！", root));
                return;
            }
            closeSpinner(() -> NotificationUtils.notifySuccess("提交成功！", root));
        } catch (JsonProcessingException e) {
            logger.error("Do post parse post data error", e);
            closeSpinner(() -> NotificationUtils.notifyError("无法解析请求JSON：" + e.getMessage(), root));
        }
    }

    private void initSpinner() {
        StackPane spinnerRoot = new StackPane();
        spinnerRoot.getStyleClass().add("register-dialog");
        JFXSpinner first = new JFXSpinner();
        first.getStyleClass().addAll("spinner-black", "first-spinner");
        first.setStartingAngle(-40);
        JFXSpinner second = new JFXSpinner();
        second.getStyleClass().addAll("spinner-dark", "second-spinner");
        second.setStartingAngle(-90);
        JFXSpinner third = new JFXSpinner();
        third.getStyleClass().addAll("spinner-gray", "third-spinner");
        third.setStartingAngle(-120);
        spinnerRoot.getChildren().addAll(first, second, third);

        spinnerStage = new Stage(StageStyle.TRANSPARENT);
        spinnerStage.initModality(Modality.APPLICATION_MODAL);
        spinnerStage.initOwner((Stage) context.getRegisteredObject("Stage"));
        Scene scene = new Scene(spinnerRoot, Color.TRANSPARENT);
        scene.getStylesheets().add(DemoController.class
            .getResource("/css/register-dialog.css").toExternalForm());
        spinnerStage.setScene(scene);
    }

    private void showSpinner() {
        Stage primaryStage = (Stage) context.getRegisteredObject("Stage");

        spinnerStage.setWidth(primaryStage.getWidth());
        spinnerStage.setHeight(primaryStage.getHeight());

        spinnerStage.setX(primaryStage.getX());
        spinnerStage.setY(primaryStage.getY());
        spinnerStage.show();
    }

    private void closeSpinner(Runnable later) {
        Platform.runLater(() -> {
            spinnerStage.close();
            if (later != null) {
                later.run();
            }
        });
    }

    private void initFields() {
        DemoInfoCache cache = readCache();
        if (cache != null) {
            DemoInfo savedInfo = cache.getDemoInfo();
            this.demoInfo.setType(savedInfo.getType());
            this.demoInfo.setName(savedInfo.getName());
            this.demoInfo.setDescription(savedInfo.getDescription());
            this.urlField.setText(cache.getUrl());
        }
    }

    private void doSave() {
        final DemoInfoCache cache = new DemoInfoCache();
        cache.setDemoInfo(this.demoInfo);
        cache.setUrl(this.urlField.getText());

        ObjectMapper om = new ObjectMapper();
        Path localCache = Paths.get(CACHE_PATH);
        Path localCacheDir = localCache.getParent();
        OutputStream os = null;
        try {
            if (!Files.exists(localCacheDir) || !Files.isDirectory(localCacheDir)) {
                Files.createDirectory(localCacheDir);
            }
            os = new FileOutputStream(CACHE_PATH);

            byte[] saveObjBytes = om.writeValueAsBytes(cache);
            byte[] saveFileBytes = Base64.getEncoder().encode(saveObjBytes);

            os.write(saveFileBytes);
            os.flush();
        } catch (IOException e) {
            logger.error("Save post data to local file failed, error", e);
            NotificationUtils.notifyError("保存失败！", root);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private DemoInfoCache readCache() {
        Path path = Paths.get(CACHE_PATH);
        if (Files.isRegularFile(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                byte[] localCacheBytes = new byte[is.available()];
                int i = is.read(localCacheBytes);
                logger.debug("read total {} bytes of register cache", i);
                byte[] decrypt = Base64.getDecoder().decode(localCacheBytes);
                ObjectMapper om = new ObjectMapper();
                return om.readValue(decrypt, DemoInfoCache.class);
            } catch (Exception e) {
                logger.error("read register cache failed", e);
            }
        }
        return null;
    }
}
