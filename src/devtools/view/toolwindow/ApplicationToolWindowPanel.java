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

    public static final String REFRESH = "Refresh";
    public static final String ADD_APPLICATION = "Add Application";
    public static final String REMOVE_APPLICATION = "Remove Application";
    public static final String APPLICATIONS_VIEW_TOOLBAR = "ApplicationsViewToolbar";
    public static final String SERVER_XML = "server.xml";

    //TODO implementar
    public ApplicationToolWindowPanel() {
        super(true, true);

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshToolBar(REFRESH, REFRESH, AllIcons.Actions.Refresh));
        group.addSeparator();
        group.add(new AddToolBar(ADD_APPLICATION, ADD_APPLICATION, AllIcons.ToolbarDecorator.Add));
        group.add(new RemoveToolBar(REMOVE_APPLICATION, REMOVE_APPLICATION, AllIcons.ToolbarDecorator.Remove));

        toolBarPanel.add(ActionManager.getInstance().createActionToolbar(APPLICATIONS_VIEW_TOOLBAR, group, true).getComponent());

        this.setToolbar(toolBarPanel);

        DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());

        Tree tree = new Tree(model);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        try {
            DevToolsProperties toolsProperties = new DevToolsProperties();
            Configuration configuration = toolsProperties.loadConfiguration();
            Path serverXml = Paths.get(DevToolsUtil.getProfilePath(configuration), SERVER_XML);
            ManipulationLibertyServer libertyServer = new ManipulationLibertyServer(serverXml.toString());

            root.setUserObject(String.format("%s [%s]", configuration.getProfileUse(), SERVER_XML));

            List<EnterpriseApplication> enterpriseApplications = libertyServer.listApplications();
            for (EnterpriseApplication application : enterpriseApplications) {
                DefaultMutableTreeNode applicationNode = new DefaultMutableTreeNode(application.getName());

                DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(String.format("id: %s", application.getId()));
                applicationNode.add(idNode);
                DefaultMutableTreeNode locationNode = new DefaultMutableTreeNode(String.format("location: %s", application.getLocation()));
                applicationNode.add(locationNode);
                root.add(applicationNode);
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
