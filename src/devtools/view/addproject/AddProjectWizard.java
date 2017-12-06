package devtools.view.addproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddProjectWizard extends DialogWrapper {

    private JPanel myPanel;

    public AddProjectWizard(@Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);

        super.setOKActionEnabled(false);

        init();

        myPanel.setPreferredSize(new Dimension(800, 600));

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        myPanel = new JPanel(new BorderLayout());

        return myPanel;
    }
}
