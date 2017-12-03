package devtools.liberty;

import devtools.exception.ConfigurationException;
import devtools.liberty.exception.JMXLibertyException;
import devtools.liberty.exception.LibertyAccessException;
import devtools.util.ProfileConstants;

import javax.management.ObjectName;
import java.io.File;
import java.util.*;

public class LibertyAccess {

    private JMXLibertyConnector connector;

    public List<Application> getApplications(LibertyConfiguration configuration) throws LibertyAccessException {
        try {
            String jmxLocalAddress = configuration.getJmxLocalAddresssPath();
            connector = new JMXLibertyConnector(jmxLocalAddress);
            connector.connect();
            Set<ObjectName> applicationsObjectName = connector.getApplications();
            List<Application> applications = new ArrayList<>();
            for (ObjectName objectName : applicationsObjectName) {
                applications.add(connector.getApplication(objectName));
            }
            connector.disconnect();
            return applications;
        } catch (JMXLibertyException | ConfigurationException e) {
            throw new LibertyAccessException("Falha ao recuperar applications", e);
        }
    }

    public Map<String, File> getServersProfile(LibertyConfiguration configuration) {
        final String servers = configuration.getProfilePath().concat(ProfileConstants.SERVERS);
        File[] listServers = new File(servers).listFiles(pathname -> !pathname.getName().contains("."));
        Map<String, File> map = new LinkedHashMap<>();

        assert listServers != null;
        Arrays.stream(listServers).forEach(file -> {
            map.put(file.getName(), file);
        });

        return map;
    }

}
