package devtools.view.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;
import java.awt.*;

public class ApplicationToolWindowPanel extends SimpleToolWindowPanel implements Disposable {

    public ApplicationToolWindowPanel() {
        super(false, true);

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup leftGroup = new DefaultActionGroup();
        leftGroup.add(new AnAction("xxx", null, AllIcons.Actions.Refresh) {

            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                System.out.println(">>>>");
            }
        });
        toolBarPanel.add(ActionManager.getInstance().createActionToolbar("TodoViewToolbar", leftGroup, false).getComponent());

        this.setToolbar(toolBarPanel);
    }

    @Override
    public void dispose() {

    }


}
