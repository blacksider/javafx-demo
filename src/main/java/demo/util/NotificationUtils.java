package demo.util;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Created by Snart Lu on 2018/2/5
 */
public class NotificationUtils {
    private static final String WARN_IMAGE_PATH = NotificationUtils.class
        .getResource("/image/notification-pane-warning.png").toExternalForm();

    private NotificationUtils() {
    }

    public static void notifyError(final String message, final Object owner) {
        final ImageView WARN_IMAGE = new ImageView(WARN_IMAGE_PATH);
        Notifications.create()
            .title("信息")
            .darkStyle()
            .owner(owner)
            .hideAfter(Duration.seconds(2))
            .position(Pos.CENTER)
            .graphic(WARN_IMAGE)
            .text(message)
            .show();
    }

    public static void notifySuccess(final String message, final Object owner) {
        Notifications.create()
            .title("信息")
            .darkStyle()
            .owner(owner)
            .hideAfter(Duration.seconds(2))
            .position(Pos.CENTER)
            .text(message)
            .showInformation();
    }
}
