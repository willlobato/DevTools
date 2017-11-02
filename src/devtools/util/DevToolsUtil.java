package devtools.util;

import com.intellij.openapi.ui.Messages;
import com.twelvemonkeys.util.LinkedMap;
import devtools.configuration.Configuration;
import devtools.exception.DevToolsException;
import devtools.exception.FileNotFoundException;
import devtools.exception.ReadPathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

    public static String getJndiPath(final Configuration configuration) throws DevToolsException {
        String path = getProfilePath(configuration) + ProfileConstants.JNDI_ADDRESS_RELATIVE_PATH;
        try {
            if(!Files.exists(Paths.get(path))) {
                throw new FileNotFoundException("The file '" + path + "'\n was not found");
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            return lines.get(0);
        } catch (IOException e) {
            throw new ReadPathException("Failed to read path: " + path);
        }
    }

}
