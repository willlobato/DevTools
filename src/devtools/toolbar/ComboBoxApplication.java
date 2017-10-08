package devtools.toolbar;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import devtools.configuration.ApplicationSelected;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.menu.ReloadAction;
import devtools.util.DevToolsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ComboBoxApplication extends ComboBoxAction {

    private DevToolsProperties toolsProperties;
    private Configuration configuration;
    private String appSelected;

    public ComboBoxApplication() {
        try {
            this.toolsProperties = new DevToolsProperties();
            this.appSelected = toolsProperties.getProperty(DevToolsProperties.PROP_APPLICATION_SELECTED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        DefaultActionGroup group = new DefaultActionGroup();
        final Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(jComponent));
        try {
            SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
            if (component.getApplications() == null) {
                this.toolsProperties.load();
                this.configuration = toolsProperties.loadConfigurationToReload();
                if(!(configuration.profilePathIsBlank() || configuration.profileUseIsBlank())) {
                    Map<String, File> applications = DevToolsUtil.getApplications(this.configuration);
                    component.setApplications(applications);
                    addApplicationItems(group, applications);
                }
            } else {
                addApplicationItems(group, component.getApplications());
            }
        } catch (IOException ex) {
            Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
        }
        return group;
    }

    private void addApplicationItems(DefaultActionGroup group, Map<String, File> applications) {
        if (applications != null) {
            for (String key : applications.keySet()) {
                File value = applications.get(key);
                group.addAction(new ApplicationItem(key, value,null, getIcon(key)));
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDefault() || project.isDisposed()) {
            presentation.setEnabled(false);
            presentation.setText("");
            presentation.setIcon(null);
        } else {
            SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
            presentation.setVisible(true);
            presentation.setEnabled(true);
            String appName = component.getApplicationSelected().getAppName();
            if (appName != null) {
                presentation.setText(appName);
                presentation.setIcon(getIcon(appName));
            } else {
                if (appSelected != null && !appSelected.equals("")) {
                    component.getApplicationSelected().setAppName(appSelected);
                    presentation.setText(appSelected);
                    presentation.setIcon(getIcon(appSelected));
                    appSelected = null;
                } else {
                    presentation.setText("");
                }
            }
        }
    }

    private Icon getIcon(String appName) {
        if (appName != null) {
            appName = appName.toLowerCase();
            if (appName.endsWith("ear.xml")) {
                return AllIcons.Javaee.JavaeeAppModule;
            } else if (appName.endsWith("war.xml")) {
                return AllIcons.Javaee.WebService;
            }
        }
        return null;
    }

    private class ApplicationItem extends AnAction {

        private String key;
        private File value;

        public ApplicationItem(@Nullable String text,
                               @Nullable File value,
                               @Nullable String description,
                               @Nullable Icon icon) {
            super(text, description, icon);
            this.key = text;
            this.value = value;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            Project project = anActionEvent.getProject();
            if (project != null) {
                try {
                    DevToolsProperties toolsProperties = new DevToolsProperties();
                    SelectApplicationComponent component = SelectApplicationComponent.getManager(project);
                    ApplicationSelected applicationSelected = component.getApplicationSelected();
                    applicationSelected.setAppName(getKey());
                    applicationSelected.setApplication(getValue());
                    toolsProperties.save(DevToolsProperties.PROP_APPLICATION_SELECTED, getKey());
                } catch (IOException ex) {
                    Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
                }
            }
        }

        public String getKey() {
            return key;
        }

        public File getValue() {
            return value;
        }

    }

}
