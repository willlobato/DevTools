package devtools.view.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import devtools.configuration.ApplicationSelected;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.DevToolsException;
import devtools.util.DevToolsUtil;
import devtools.util.JMXWebsphereConnector;
import devtools.view.toolbar.SelectApplicationComponent;
import devtools.util.GeneralConstants;

import javax.management.remote.JMXConnector;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RestartApplicationAction extends AnAction {

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
//        ProjectManager pm = ProjectManager.getInstance();
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
                            "The profile '" + configuration.getProfileUse() + "' don't have virtual applications (EAR/WAR) in the 'apps' directory.",
                            GeneralConstants.ERROR,
                            Messages.getErrorIcon());
                    return;
                }

                SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
                ApplicationSelected applicationSelected = component.getApplicationSelected();
                final String[] appsToChoose = applications.keySet().toArray(new String[applications.size()]);

                String appNameSelected = applicationSelected.getAppName() != null
                        ? applicationSelected.getAppName() : appsToChoose[0];
                appNameSelected = (String) JOptionPane.showInputDialog(parentWindow,
                        "Choose the application:", "ApplicationVO",
                        JOptionPane.QUESTION_MESSAGE, null, appsToChoose, appNameSelected);

                if(appNameSelected != null) {
                    File appSelected = new File(applications.get(appNameSelected).getAbsolutePath());

                    applicationSelected.setAppName(appNameSelected);
                    applicationSelected.setApplication(appSelected);
                    toolsProperties.save(DevToolsProperties.PROP_APPLICATION_SELECTED, appNameSelected);

                    final String urlJndi = DevToolsUtil.getJndiPath(configuration);

                    JMXWebsphereConnector jmxWebsphere = new JMXWebsphereConnector();
                    jmxConnector = jmxWebsphere.connect(urlJndi);
                    jmxWebsphere.notifyFileChange(jmxConnector, applicationSelected.getApplication());
                }
            } catch (DevToolsException ex) {
                Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
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
    }

    private static final String LABEL = "Restart ApplicationVO";

    public RestartApplicationAction() {
        super(LABEL, null, GeneralConstants.ICON_ROCKET);
    }

}
