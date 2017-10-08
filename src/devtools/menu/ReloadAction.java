package devtools.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.twelvemonkeys.util.LinkedMap;
import devtools.configuration.ApplicationSelected;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.util.DevToolsUtil;
import devtools.util.ProfileConstants;
import devtools.toolbar.SelectApplicationComponent;
import devtools.util.GeneralConstants;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class ReloadAction extends AnAction {

    private static final String LABEL = "Reload Application";
    private static final Icon ICON = IconLoader.getIcon("/rocket-16.png");

    public ReloadAction() {
        super(LABEL, null, ICON);
    }

    /** FileNotificationMBean name */
    private final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";
    /** FileNotificationMBean method notifyFileChanges signature */
    private final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };

    @Override
    public void actionPerformed(AnActionEvent event) {

        ApplicationManager.getApplication().invokeLater(() -> {
            final Project project = event.getData(PlatformDataKeys.PROJECT);
            final WindowManager windowManager = WindowManager.getInstance();
            final Window parentWindow;
            if (windowManager != null) {
                parentWindow = windowManager.suggestParentWindow(project);
            } else {
                Messages.showMessageDialog(project, "Unknown error", GeneralConstants.ERROR, Messages.getErrorIcon());
                return;
            }
//        Project project = event.getData(PlatformDataKeys.PROJECT);
//        Collection<Module> modules = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
//        System.out.println(modules);

            JMXConnector jmxConnector = null;
            try {
                final DevToolsProperties toolsProperties = new DevToolsProperties();
                final Configuration configuration = toolsProperties.loadConfigurationToReload();
                if(configuration.profilePathIsBlank() || configuration.profileUseIsBlank()) {
                    Messages.showMessageDialog(project,
                            "Plugin is not configured. Menu > DevTools > Configuration.",
                            GeneralConstants.ERROR,
                            Messages.getErrorIcon());
                    return;
                }

                final Map<String, File> applications = DevToolsUtil.getApplications(configuration);
                if (applications == null) {
                    Messages.showMessageDialog(project,
                            "The 'apps' directory within the '" + configuration.getProfileUse() + "' profile doesn't exist.",
                            GeneralConstants.ERROR,
                            Messages.getErrorIcon());
                    return;
                } else if (applications.keySet().size() == 0) {
                    Messages.showMessageDialog(project,
                            "The profile '" + configuration.getProfileUse() + "' didn't contains any virtual applications (EAR/WAR) in the 'apps' directory.",
                            GeneralConstants.ERROR,
                            Messages.getErrorIcon());
                    return;
                }

                SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
                ApplicationSelected applicationSelected = component.getApplicationSelected();
                String[] appsToChoose = applications.keySet().toArray(new String[applications.size()]);
                String appName = applicationSelected.getAppName() != null
                        ? applicationSelected.getAppName() : appsToChoose[0];
                final String appSelectedStr = (String) JOptionPane.showInputDialog(parentWindow,
                        "Choose the application:", "Application",
                        JOptionPane.QUESTION_MESSAGE, null, appsToChoose, appName);

                if(appSelectedStr != null) {
                    File appSelected = new File(applications.get(appSelectedStr).getAbsolutePath());

                    applicationSelected.setAppName(appSelectedStr);
                    applicationSelected.setApplication(appSelected);
                    toolsProperties.save(DevToolsProperties.PROP_APPLICATION_SELECTED, appSelectedStr);

                    final String pathUrl = DevToolsUtil.getJndiPath(configuration);
                    if(!Files.exists(Paths.get(pathUrl))) {
                        Messages.showMessageDialog(project,
                                "The file '" + pathUrl + "'\n was not found",
                                GeneralConstants.ERROR, Messages.getErrorIcon());
                        return;
                    }

                    List<String> lines = Files.readAllLines(Paths.get(pathUrl));
                    String urlJndi = lines.get(0);

                    JMXServiceURL serviceUrl = new JMXServiceURL(urlJndi);
                    jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

                    notifyFileChange(project, jmxConnector, applicationSelected);
                }
            } catch (Exception ex) {
                if (ex.getMessage().trim().equals("")) {
                    Messages.showMessageDialog(project, "Unknown error", GeneralConstants.ERROR, Messages.getErrorIcon());
                } else {
                    Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            } finally {
                try {
                    if(jmxConnector != null) {
                        jmxConnector.close();
                    }
                } catch (IOException e) {
                    Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }

        });
//        ProjectManager pm = ProjectManager.getInstance();

    }

    private void notifyFileChange(Project project, JMXConnector jmxConnector, ApplicationSelected applicationSelected) throws IOException, MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        // Invoke FileNotificationMBean
        ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);
        if (mbean.isRegistered(fileMonitorMBeanName)) {
            // Create a list of absolute paths of each file to be checked
            applicationSelected.getApplication().setLastModified(new Date().getTime());
            List<String> modifiedFilePaths = new ArrayList<>();
            modifiedFilePaths.add(applicationSelected.getApplication().getAbsolutePath());

            // Set MBean method notifyFileChanges parameters (createdFiles, modifiedFiles, deletedFiles)
            Object[] params = new Object[]{null, modifiedFilePaths, null};

            // Invoke FileNotificationMBean method notifyFileChanges
            mbean.invoke(fileMonitorMBeanName, "notifyFileChanges", params,
                    MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE);
        } else {
            Messages.showMessageDialog(project,
                    "MBean invoke request failed " + MBEAN_FILE_NOTIFICATION + " is not registered.",
                    GeneralConstants.ERROR,
                    Messages.getErrorIcon());
        }
    }
}
