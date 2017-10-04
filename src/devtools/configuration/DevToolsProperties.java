package devtools.configuration;

import java.io.*;
import java.util.Properties;

public class DevToolsProperties {

    private Properties propConfig;

    private final static String FILE_CONFIGURATION = ".devtoolsConfig";
    private final static String PROP_PROFILE_PATH = "profile.path";
    private final static String PROP_PROFILE_USE = "profile.use";

    public DevToolsProperties() throws Exception {
        createConfigurationIfNotExist();
        propConfig = new Properties();
    }

    private void createConfigurationIfNotExist() throws Exception {
        final File fileConfiguration = getFileConfiguration();
        if(!fileConfiguration.exists()) {
            if(!fileConfiguration.createNewFile()) {
                throw new IOException("Failed to create configuration file DevTools");
            }
            Configuration configuration = new Configuration();
            configuration.setProfilePath("");
            configuration.setProfileUse("");
            save(configuration);
        }
    }

    public Configuration loadConfiguration() throws IOException {
        File fileConfiguration = getFileConfiguration();
        Configuration configuration = new Configuration();

        propConfig.load(new FileInputStream(fileConfiguration));
        if(existPropertiesValue(PROP_PROFILE_PATH)) {
            propConfig.setProperty(PROP_PROFILE_PATH, "C:/devtools/var/was_liberty_profile");
            propConfig.store(new FileOutputStream(fileConfiguration), "");
        }
        if(existPropertiesValue(PROP_PROFILE_USE)) {
            propConfig.setProperty(PROP_PROFILE_USE, "");
            propConfig.store(new FileOutputStream(fileConfiguration), "");
        }

        configuration.setProfilePath(propConfig.getProperty(PROP_PROFILE_PATH));
        configuration.setProfileUse(propConfig.getProperty(PROP_PROFILE_USE));

        return configuration;
    }

    private boolean existPropertiesValue(String key) {
        return propConfig.getProperty(key) == null ||
                (propConfig.getProperty(key) != null
                        && propConfig.getProperty(key).trim().equals(""));
    }

    public void save(Configuration configuration) throws IOException {
        File fileConfiguration = getFileConfiguration();
        propConfig.load(new FileInputStream(fileConfiguration));
        propConfig.setProperty(PROP_PROFILE_PATH, configuration.getProfilePath());
        propConfig.setProperty(PROP_PROFILE_USE, configuration.getProfileUse());
        propConfig.store(new FileOutputStream(fileConfiguration), "");
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
