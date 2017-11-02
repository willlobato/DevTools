package devtools.configuration;

import devtools.exception.ConfigurationException;
import devtools.exception.DevToolsException;
import devtools.exception.PluginNotConfiguratedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DevToolsProperties {

    private Properties propConfig;

    private final static String FILE_CONFIGURATION = ".devtoolsConfig";

    private final static String PROP_PROFILE_PATH = "profile.path";
    private final static String PROP_PROFILE_USE = "profile.use";
    public final static String PROP_APPLICATION_SELECTED = "application.selected";

    private final static String DEFAULT_PROFILE_PATH = "C:/devtools/var/was_liberty_profile";

    public DevToolsProperties() throws DevToolsException {
        propConfig = new Properties();
        createConfigurationIfNotExist();
    }

    public void load() throws ConfigurationException {
        createConfigurationIfNotExist();
    }

    private void createConfigurationIfNotExist() throws ConfigurationException {
        try {
            final File fileConfiguration = getFileConfiguration();
            if(!fileConfiguration.exists()) {
                if(!fileConfiguration.createNewFile()) {
                    throw new ConfigurationException("Failed to create configuration file DevTools");
                }
                save(new Configuration());
            }
            propConfig.load(new FileInputStream(fileConfiguration));
        } catch (IOException e) {
            throw new ConfigurationException("Failed to create configuration file DevTools", e);
        }
    }

    public Configuration loadConfiguration() throws DevToolsException {
        saveIfNotExist(PROP_PROFILE_PATH, DEFAULT_PROFILE_PATH);
        saveIfNotExist(PROP_PROFILE_USE, "");
        return convertToBean();
    }

    public Configuration loadConfigurationToReload() throws DevToolsException {
        load();
        Configuration configuration = convertToBean();
        if(configuration.profilePathIsBlank() || configuration.profileUseIsBlank()) {
            throw new PluginNotConfiguratedException("Plugin is not configured. Menu > DevTools > Configuration.");
        }
        return configuration;
    }

    private void saveIfNotExist(String key, String value) throws DevToolsException {
        try {
            File fileConfiguration = getFileConfiguration();
            if (!existPropertiesValue(key)) {
                propConfig.setProperty(key, value);
                propConfig.store(new FileOutputStream(fileConfiguration), "");
            }
        } catch (Exception e) {
            throw new ConfigurationException("Failed to save Configuration with key = "+key + " value = " + value, e);
        }
    }

    private boolean existPropertiesValue(String key) {
        return (propConfig.getProperty(key) != null
                        && !propConfig.getProperty(key).trim().equals(""));
    }

    public void save(Configuration configuration) throws ConfigurationException {
        try {
            File fileConfiguration = getFileConfiguration();
            propConfig.load(new FileInputStream(fileConfiguration));
            convertToProperties(configuration);
            propConfig.store(new FileOutputStream(fileConfiguration), "");
        } catch (Exception e) {
            throw new ConfigurationException("Failed to save " + configuration.toString() , e);
        }
    }

    public void save(String key, String value) throws ConfigurationException {
        try {
            File fileConfiguration = getFileConfiguration();
            propConfig.load(new FileInputStream(fileConfiguration));
            propConfig.setProperty(key, value);
            propConfig.store(new FileOutputStream(fileConfiguration), "");
        } catch (Exception e) {
            throw new ConfigurationException("Failed to save Configuration with key = "+key + " value = " + value, e);
        }
    }

    public String getProperty(String key) throws ConfigurationException {
        try {
            File fileConfiguration = getFileConfiguration();
            propConfig.load(new FileInputStream(fileConfiguration));
            return propConfig.getProperty(key);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to get Configuration with key = "+key, e);
        }
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

}
