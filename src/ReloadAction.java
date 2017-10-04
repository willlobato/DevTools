import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.twelvemonkeys.util.LinkedMap;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.configuration.ProfileConstants;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ReloadAction extends AnAction {

    private final static String FILE_CONFIGURATION = ".devtoolsIntellij";
    private final static String PROP_PROFILE_PATH = "profile.path";

    private final static String PROFILE_PATH = "C:/devtools/var/was_liberty_profile/servers/mxlocal201711/";
    private final static String DIR_APPS = "/apps";

    /** FileNotificationMBean name */
    private final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";
    /** FileNotificationMBean method notifyFileChanges signature */
    private final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };

    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);

        JMXConnector jmxConnector = null;
        try {

            DevToolsProperties toolsProperties = new DevToolsProperties();
            Configuration configuration = toolsProperties.loadConfiguration();
            String fileAppsStr = configuration.getProfilePath() +
                    ProfileConstants.SERVERS +
                    "/" +
                    configuration.getProfileUse() +
                    ProfileConstants.APPS;
            File fileApps = new File(fileAppsStr);
            File[] apps = fileApps.listFiles();
            String[] appsChoose = new String[apps.length];
            LinkedMap<String, File> appsMap = new LinkedMap<>();
            for (int i=0; i<apps.length; i++) {
                appsMap.put(apps[i].getName(), apps[i]);
                appsChoose[i] = apps[i].getName();
            }

            final String app = (String) JOptionPane.showInputDialog(null,
                    "Choose the application",
                    "Application",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    appsChoose,
                    appsChoose[0]);

            if(app != null) {
                String pathUrl = PROFILE_PATH + "/logs/state/com.ibm.ws.jmx.local.address";
                List<String> lines = Files.readAllLines(Paths.get(pathUrl));
                String url = lines.get(0);

                JMXServiceURL serviceUrl = new JMXServiceURL(url);
                jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

                MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();

                // Invoke FileNotificationMBean
                ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);
                if (mbean.isRegistered(fileMonitorMBeanName)) {
                    // Create a list of absolute paths of each file to be checked
                    new File(appsMap.get(app).getAbsolutePath()).setLastModified(new Date().getTime());
                    List<String> modifiedFilePaths = new ArrayList<>();
                    modifiedFilePaths.add(appsMap.get(app).getAbsolutePath());

                    // Set MBean method notifyFileChanges parameters (createdFiles, modifiedFiles, deletedFiles)
                    Object[] params = new Object[]{null, modifiedFilePaths, null};

                    // Invoke FileNotificationMBean method notifyFileChanges
                    mbean.invoke(fileMonitorMBeanName, "notifyFileChanges", params,
                            MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE);
                } else {
                    Messages.showMessageDialog(project,
                            "MBean invoke request failed " + MBEAN_FILE_NOTIFICATION + " is not registered.",
                            "Error",
                            Messages.getErrorIcon());
                }
            }
        } catch (Exception ex) {
            Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
        } finally {
            try {
                if(jmxConnector != null) {
                    jmxConnector.close();
                }
            } catch (IOException e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        }
    }


    public static void main(String[] args) {
//        Project project = event.getData(PlatformDataKeys.PROJECT);

        JMXConnector jmxConnector = null;
        try {

            DevToolsProperties toolsProperties = new DevToolsProperties();
            Configuration configuration = toolsProperties.loadConfiguration();
            String fileAppsStr = configuration.getProfilePath() +
                    ProfileConstants.SERVERS +
                    "/" +
                    configuration.getProfileUse() +
                    ProfileConstants.APPS;
            File fileApps = new File(fileAppsStr);
            File[] apps = fileApps.listFiles();
            String[] appsChoose = new String[apps.length];
            LinkedMap<String, File> appsMap = new LinkedMap<>();
            for (int i=0; i<apps.length; i++) {
                appsMap.put(apps[i].getName(), apps[i]);
                appsChoose[i] = apps[i].getName();
            }

            final String app = (String) JOptionPane.showInputDialog(null,
                    "Choose the application",
                    "Application",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    appsChoose,
                    appsChoose[0]);

            if(app != null) {
                String pathUrl = PROFILE_PATH + "/logs/state/com.ibm.ws.jmx.local.address";
                List<String> lines = Files.readAllLines(Paths.get(pathUrl));
                String url = lines.get(0);

                JMXServiceURL serviceUrl = new JMXServiceURL(url);
                jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

                MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();

                // Invoke FileNotificationMBean
                ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);
                if (mbean.isRegistered(fileMonitorMBeanName)) {
                    // Create a list of absolute paths of each file to be checked
                    new File(appsMap.get(app).getAbsolutePath()).setLastModified(new Date().getTime());
                    List<String> modifiedFilePaths = new ArrayList<>();
                    modifiedFilePaths.add(appsMap.get(app).getAbsolutePath());

                    // Set MBean method notifyFileChanges parameters (createdFiles, modifiedFiles, deletedFiles)
                    Object[] params = new Object[]{null, modifiedFilePaths, null};

                    // Invoke FileNotificationMBean method notifyFileChanges
                    mbean.invoke(fileMonitorMBeanName, "notifyFileChanges", params,
                            MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE);
                } else {
                    System.out.println("AQUI 1");
//                    Messages.showMessageDialog(project,
//                            "MBean invoke request failed " + MBEAN_FILE_NOTIFICATION + " is not registered.",
//                            "Error",
//                            Messages.getErrorIcon());
                }
            }
        } catch (Exception ex) {
//            System.out.println("AQUI 2");
            ex.printStackTrace();
//            Messages.showMessageDialog(null, ex.getMessage(), "Error", Messages.getErrorIcon());
        } finally {
            try {
                if(jmxConnector != null) {
                    jmxConnector.close();
                }
            } catch (IOException e) {
                System.out.println("AQUI 3");
//                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        }
    }

}
