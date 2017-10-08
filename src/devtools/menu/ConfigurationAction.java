package devtools.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import devtools.configuration.ConfigurationDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConfigurationAction extends AnAction {

    private static final String LABEL = "Configuration";

    public ConfigurationAction() {
        super(LABEL);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        final WindowManager windowManager = WindowManager.getInstance();
        final Window parentWindow;
        if (windowManager != null) {
            parentWindow = windowManager.suggestParentWindow(project);
        } else {
            Messages.showMessageDialog(project, "Unknown error", "Error", Messages.getErrorIcon());
            return;
        }
        try {
            ConfigurationDialog dialog = new ConfigurationDialog(parentWindow, project);
            dialog.setTitle("Configuration");
            dialog.setSize(500,200);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
        }

    }
}
