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

            final DevToolsProperties toolsProperties = new DevToolsProperties();
            final Configuration configuration = toolsProperties.loadConfigurationToReload();

            if((configuration.getProfilePath() != null && configuration.getProfilePath().equals("")) ||
                    (configuration.getProfileUse() != null && configuration.getProfileUse().equals(""))) {
                Messages.showMessageDialog(project,
                        "Plugin is not configured. Menu > DevTools > Configuration.",
                        "Error",
                        Messages.getErrorIcon());
                return;
            }

            final String fileProfilePath = configuration.getProfilePath() + ProfileConstants.SERVERS +
                    "/" + configuration.getProfileUse();
            final File fileApps = new File(fileProfilePath + ProfileConstants.APPS);
            if (fileApps.exists()) {
                final File[] apps = fileApps.listFiles();
                String[] appsChoose = new String[apps.length];
                LinkedMap<String, File> appsMap = new LinkedMap<>();
                for (int i=0; i<apps.length; i++) {
                    appsMap.put(apps[i].getName(), apps[i]);
                    appsChoose[i] = apps[i].getName();
                }

                if (apps.length > 0) {

                    String profileSelected = toolsProperties.getConfiguration(DevToolsProperties.PROP_PROFILE_SELECTED);
                    if (profileSelected.equals("")) {
                        profileSelected = appsChoose[0];
                    }

                    final String appSelected = (String) JOptionPane.showInputDialog(null,
                            "Choose the application:",
                            "Application",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            appsChoose,
                            profileSelected);

                    if(appSelected != null) {

                        toolsProperties.save(DevToolsProperties.PROP_PROFILE_SELECTED, appSelected);

                        String pathUrl = fileProfilePath + ProfileConstants.JNDI_ADDRESS_RELATIVE_PATH;
                        if(!new File(pathUrl).exists()) {
                            Messages.showMessageDialog(project,
                                    "The file '" + pathUrl + "'\n was not found",
                                    "Error",
                                    Messages.getErrorIcon());
                            return;
                        }

                        List<String> lines = Files.readAllLines(Paths.get(pathUrl));
                        String urlJndi = lines.get(0);

                        JMXServiceURL serviceUrl = new JMXServiceURL(urlJndi);
                        jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

                        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();

                        // Invoke FileNotificationMBean
                        ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);
                        if (mbean.isRegistered(fileMonitorMBeanName)) {
                            // Create a list of absolute paths of each file to be checked
                            new File(appsMap.get(appSelected).getAbsolutePath()).setLastModified(new Date().getTime());
                            List<String> modifiedFilePaths = new ArrayList<>();
                            modifiedFilePaths.add(appsMap.get(appSelected).getAbsolutePath());

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
                } else {
                    Messages.showMessageDialog(project,
                            "The profile '" + configuration.getProfileUse() + "' didn't contains any virtual applications (EAR/WAR) in the 'apps' directory.",
                            "Error",
                            Messages.getErrorIcon());
                }
            } else {
                Messages.showMessageDialog(project,
                        "The 'apps' directory within the '" + configuration.getProfileUse() + "' profile doesn't exist.",
                        "Error",
                        Messages.getErrorIcon());
            }
        } catch (Exception ex) {
            if (ex.getMessage().trim().equals("")) {
                Messages.showMessageDialog(project, "Unknown error", "Error", Messages.getErrorIcon());
            } else {
                Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
            }
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


    public static void main(String args[]) {


    }

}
