package devtools.view.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import devtools.configuration.ApplicationSelected;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.DevToolsException;
import devtools.util.DevToolsUtil;
import devtools.util.GeneralConstants;
import devtools.util.JMXWebsphereConnector;

import javax.management.remote.JMXConnector;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RestartApplicationAction extends AnAction {

    private static final String LABEL = "Toolbar Restart ApplicationVO";

    public RestartApplicationAction() {
        super(LABEL, null, GeneralConstants.ICON_ROCKET);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        ApplicationManager.getApplication().invokeLater(() -> {
            final Project project = event.getData(PlatformDataKeys.PROJECT);
            if (project != null) {
                JMXConnector jmxConnector = null;
                try {
                    final DevToolsProperties toolsProperties = new DevToolsProperties();
                    final Configuration configuration = toolsProperties.loadConfigurationToReload();

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

                    String appNameSelected = applicationSelected.getAppName();
                    if(appNameSelected == null) {
                        Messages.showMessageDialog(project, "Select the application to restart", GeneralConstants.ERROR, Messages.getErrorIcon());
                        return;
                    }

                    File appSelected = new File(applications.get(appNameSelected).getAbsolutePath());

                    applicationSelected.setAppName(appNameSelected);
                    applicationSelected.setApplication(appSelected);
                    toolsProperties.save(DevToolsProperties.PROP_APPLICATION_SELECTED, appNameSelected);


                    final String urlJndi = DevToolsUtil.getJndiPath(configuration);

                    JMXWebsphereConnector jmxWebsphere = new JMXWebsphereConnector();
                    jmxConnector = jmxWebsphere.connect(urlJndi);
                    jmxWebsphere.notifyFileChange(jmxConnector, applicationSelected.getApplication());

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
            }
        });
    }
}
