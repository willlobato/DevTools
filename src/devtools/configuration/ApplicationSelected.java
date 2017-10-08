package devtools.configuration;

import java.io.File;

public class ApplicationSelected {

    private String appName;
    private File application;

    public ApplicationSelected() {
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
