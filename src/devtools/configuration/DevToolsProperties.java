package devtools.configuration;

import java.io.*;
import java.util.Properties;

public class DevToolsProperties {

    private Properties propConfig;

    private final static String FILE_CONFIGURATION = ".devtoolsConfig";

    private final static String PROP_PROFILE_PATH = "profile.path";
    private final static String PROP_PROFILE_USE = "profile.use";
    public final static String PROP_APPLICATION_SELECTED = "application.selected";

    private final static String DEFAULT_PROFILE_PATH = "C:/devtools/var/was_liberty_profile";

    public DevToolsProperties() throws IOException {
        propConfig = new Properties();
        createConfigurationIfNotExist();
    }

    public void load() throws IOException {
        createConfigurationIfNotExist();
    }

    private void createConfigurationIfNotExist() throws IOException {
        final File fileConfiguration = getFileConfiguration();
        if(!fileConfiguration.exists()) {
            if(!fileConfiguration.createNewFile()) {
                throw new IOException("Failed to create configuration file DevTools");
            }
            save(new Configuration());
        }
        propConfig.load(new FileInputStream(fileConfiguration));
    }

    public Configuration loadConfiguration() throws IOException {
        saveIfNotExist(PROP_PROFILE_PATH, DEFAULT_PROFILE_PATH);
        saveIfNotExist(PROP_PROFILE_USE, "");
        return convertToBean();
    }

    public Configuration loadConfigurationToReload()  {
        return convertToBean();
    }

    private void saveIfNotExist(String key, String value) throws IOException {
        File fileConfiguration = getFileConfiguration();
        if (!existPropertiesValue(key)) {
            propConfig.setProperty(key, value);
            propConfig.store(new FileOutputStream(fileConfiguration), "");
        }
    }

    private boolean existPropertiesValue(String key) {
        return (propConfig.getProperty(key) != null
                        && !propConfig.getProperty(key).trim().equals(""));
    }

    public void save(Configuration configuration) throws IOException {
        File fileConfiguration = getFileConfiguration();
        propConfig.load(new FileInputStream(fileConfiguration));
        convertToProperties(configuration);
        propConfig.store(new FileOutputStream(fileConfiguration), "");
    }

    public void save(String key, String value) throws IOException {
        File fileConfiguration = getFileConfiguration();
        propConfig.load(new FileInputStream(fileConfiguration));
        propConfig.setProperty(key, value);
        propConfig.store(new FileOutputStream(fileConfiguration), "");
    }

    public String getProperty(String key) throws IOException {
        File fileConfiguration = getFileConfiguration();
        propConfig.load(new FileInputStream(fileConfiguration));
        return propConfig.getProperty(key);
    }

    private Configuration convertToBean() {
        Configuration configuration = new Configuration();
        configuration.setProfilePath(propConfig.getProperty(PROP_PROFILE_PATH));
        configuration.setProfileUse(propConfig.getProperty(PROP_PROFILE_USE));
        return configuration;
    }

    private void convertToProperties(Configuration configuration) {
        propConfig.setProperty(PROP_PROFILE_PATH, configuration.getProfilePath());
        propConfig.setProperty(PROP_PROFILE_USE, configuration.getProfileUse());
    }

    private File getFileConfiguration() {
        return new File(getUserHome() + "/" + FILE_CONFIGURATION);
    }

    private static String getUserHome() {
        return System.getProperties().getProperty("user.home");
    }

    public static void main(String args[]) {
        System.out.println();
    }

}
