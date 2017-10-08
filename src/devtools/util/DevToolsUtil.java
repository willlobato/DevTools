package devtools.util;

import com.twelvemonkeys.util.LinkedMap;
import devtools.configuration.Configuration;

import java.io.File;
import java.util.Map;

public class DevToolsUtil {

    public static Map<String, File> getApplications(final Configuration configuration) {
        final String fileProfilePath = getProfilePath(configuration);
        final File fileApps = new File(fileProfilePath + ProfileConstants.APPS);
        if (fileApps.exists()) {
            final File[] apps = fileApps.listFiles();
            if (apps != null) {
                LinkedMap<String, File> appsMap = new LinkedMap<>();
                for (int i = 0; i < apps.length; i++) {
                    appsMap.put(apps[i].getName(), apps[i]);
                }
                return appsMap;
            }
        }
        return null;
    }

    public static String getProfilePath(final Configuration configuration) {
        return configuration.getProfilePath() + ProfileConstants.SERVERS +
                "/" + configuration.getProfileUse();
    }

    public static String getJndiPath(final Configuration configuration) {
        return getProfilePath(configuration) + ProfileConstants.JNDI_ADDRESS_RELATIVE_PATH;
    }

}
