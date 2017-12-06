package devtools.view.toolwindow.applications;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.liberty.serverxml.EnterpriseApplication;
import devtools.liberty.serverxml.ManipulationLibertyServer;
import devtools.util.DevToolsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ApplicationToolWindowPanel extends SimpleToolWindowPanel implements Disposable {

    public static final String REFRESH = "Refresh";
    public static final String ADD_APPLICATION = "Add Application";
    public static final String REMOVE_APPLICATION = "Remove Application";
    public static final String APPLICATIONS_VIEW_TOOLBAR = "ApplicationsViewToolbar";
    public static final String SERVER_XML = "server.xml";
    public static final String DIR_APPS = "apps";
    private static final String TARGET_IN_ARCHIVE = "targetInArchive";

    private Project project;

    private DefaultTreeModel model;
    private Tree tree;
    private DevToolsProperties toolsProperties;
    private ManipulationLibertyServer manipulationLibertyServer;
    private Path dirApps;
    private Path serverXml;

    public ApplicationToolWindowPanel(@NotNull Project project) {
        super(true, true);

        this.project = project;

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshToolBar(REFRESH, REFRESH, AllIcons.Actions.Refresh));
        group.addSeparator();
        group.add(new AddToolBar(ADD_APPLICATION, ADD_APPLICATION, AllIcons.ToolbarDecorator.Add));
        group.add(new RemoveToolBar(REMOVE_APPLICATION, REMOVE_APPLICATION, AllIcons.ToolbarDecorator.Remove));

        toolBarPanel.add(ActionManager.getInstance().createActionToolbar(APPLICATIONS_VIEW_TOOLBAR, group, true).getComponent());

        this.setToolbar(toolBarPanel);

        this.model = new DefaultTreeModel(new DefaultMutableTreeNode());
        this.tree = new Tree(model);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        try {
            this.toolsProperties = new DevToolsProperties();
            Configuration configuration = this.toolsProperties.loadConfigurationToReload();
            this.dirApps = Paths.get(DevToolsUtil.getProfilePath(configuration), DIR_APPS);
            this.tree.addMouseListener(new MouseAdapterTree(dirApps));

            this.serverXml = Paths.get(DevToolsUtil.getProfilePath(configuration), SERVER_XML);
            this.manipulationLibertyServer = new ManipulationLibertyServer(serverXml.toString());

            root.setUserObject(String.format("%s [%s]", configuration.getProfileUse(), SERVER_XML));

            loadApplications(root, dirApps, manipulationLibertyServer);

            this.setContent(ScrollPaneFactory.createScrollPane(tree));

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            e.printStackTrace();
        }
    }

    private void loadApplications(DefaultMutableTreeNode root, Path dirApps, ManipulationLibertyServer manipulationLibertyServer) throws Exception {
        List<EnterpriseApplication> enterpriseApplications = manipulationLibertyServer.listApplications();
        for (EnterpriseApplication application : enterpriseApplications) {
            DefaultMutableTreeNode applicationNode = new DefaultMutableTreeNode(application.getId());

            DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(String.format("name: %s", application.getName()));
            applicationNode.add(idNode);

            ApplicationLocationTreeNode locationTreeNode = new ApplicationLocationTreeNode(dirApps, application.getLocation());
            if (Files.exists(locationTreeNode.resolveFullLocation())) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(locationTreeNode.resolveFullLocation().toFile());
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expression = "/archive/archive/archive";
                NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                for (int i=0; i<nodeList.getLength(); i++) {
                    Node item = nodeList.item(i);
                    String targetInArchive = item.getAttributes().getNamedItem(TARGET_IN_ARCHIVE).getNodeValue();
                    Path fileName = Paths.get(targetInArchive).getFileName();
                    locationTreeNode.add(new DefaultMutableTreeNode(fileName));
                }

            }

            applicationNode.add(locationTreeNode);
            root.add(applicationNode);
        }
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
            try {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                root.removeAllChildren();

                Configuration configuration = toolsProperties.loadConfigurationToReload();
                dirApps = Paths.get(DevToolsUtil.getProfilePath(configuration), DIR_APPS);
                Path serverXml = Paths.get(DevToolsUtil.getProfilePath(configuration), SERVER_XML);
                manipulationLibertyServer.setFilePath(serverXml.toString());

                root.setUserObject(String.format("%s [%s]", configuration.getProfileUse(), SERVER_XML));

                loadApplications(root, dirApps, manipulationLibertyServer);
                model.reload((TreeNode) model.getRoot());
            } catch (Exception e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
                e.printStackTrace();
            }
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

    private class MouseAdapterTree extends MouseAdapter {

        private Path dirApps;

        public MouseAdapterTree(Path dirApps) {
            super();
            this.dirApps = dirApps;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if(selRow != -1) {
                if (e.getClickCount() == 2) {
                    if (selPath != null && selPath.getLastPathComponent() instanceof ApplicationLocationTreeNode) {
                        ApplicationLocationTreeNode locationTreeNode = (ApplicationLocationTreeNode) selPath.getLastPathComponent();
                        if (Files.exists(locationTreeNode.resolveFullLocation())) {
                            VirtualFile file = VfsUtil.findFileByIoFile(locationTreeNode.resolveFullLocation().toFile(), true);
                            if (file != null) {
                                new OpenFileDescriptor(project, file).navigate(true);
                            }
                        }
                    }

                }
            }

        }
    }

}
