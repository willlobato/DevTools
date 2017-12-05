package devtools.view.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.DevToolsException;
import devtools.liberty.serverxml.EnterpriseApplication;
import devtools.liberty.serverxml.ManipulationLibertyServer;
import devtools.util.DevToolsUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ApplicationToolWindowPanel extends SimpleToolWindowPanel implements Disposable {
    //TODO implementar
    public ApplicationToolWindowPanel() {
        super(true, true);

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshToolBar("Refresh", "Refresh", AllIcons.Actions.Refresh));
        group.addSeparator();
        group.add(new AddToolBar("Add Application", "Add Application", AllIcons.ToolbarDecorator.Add));
        group.add(new RemoveToolBar("Remove Application", "Remove Application", AllIcons.ToolbarDecorator.Remove));

        toolBarPanel.add(ActionManager.getInstance().createActionToolbar("ApplicationsViewToolbar", group, true).getComponent());

        this.setToolbar(toolBarPanel);

        DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        Tree tree = new Tree(model);


//        DefaultMutableTreeNode top = new DefaultMutableTreeNode("The Java Series");

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        DevToolsProperties toolsProperties = null;
        try {
            toolsProperties = new DevToolsProperties();
            Configuration configuration = toolsProperties.loadConfiguration();
            root.setUserObject(configuration.getProfileUse());
            Path serverXml = Paths.get(DevToolsUtil.getProfilePath(configuration), "server.xml");
            ManipulationLibertyServer libertyServer = new ManipulationLibertyServer(serverXml.toString());

            List<EnterpriseApplication> enterpriseApplications = libertyServer.listApplications();
            for (EnterpriseApplication application : enterpriseApplications) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(application.getName());
                root.add(node);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }




        this.setContent(ScrollPaneFactory.createScrollPane(tree));
    }

    @Override
    public void dispose() {

    }

    private class RefreshToolBar extends AnAction {

        public RefreshToolBar(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {

        }
    }

    private class AddToolBar extends AnAction {

        public AddToolBar(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {

        }
    }

    private class RemoveToolBar extends AnAction {

        public RemoveToolBar(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {

        }
    }

}
