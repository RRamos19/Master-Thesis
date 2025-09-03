package thesis.view.utils;

import javafx.scene.image.Image;

import java.util.List;

public class AppIcons {
    private static final String ICON_PATH = "/icons/";
    private static final String APP_ICON_NAME = "app_icon";

    private static final List<Image> appIcons = List.of(
            new Image(ICON_PATH + APP_ICON_NAME + "16x16.png"),
            new Image(ICON_PATH + APP_ICON_NAME + "32x32.png"),
            new Image(ICON_PATH + APP_ICON_NAME + "64x64.png"),
            new Image(ICON_PATH + APP_ICON_NAME + "128x128.png")
    );

    private AppIcons() {}

    public static List<Image> getAppIcons() {
        return appIcons;
    }
}
