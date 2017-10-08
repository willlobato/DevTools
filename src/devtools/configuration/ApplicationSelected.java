package devtools.configuration;

import java.io.File;

public class ApplicationSelected {

    private String appName;
    private File application;

    public ApplicationSelected() {
    }

    public ApplicationSelected(String appName, File application) {
        this.appName = appName;
        this.application = application;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public File getApplication() {
        return application;
    }

    public void setApplication(File application) {
        this.application = application;
    }
}
