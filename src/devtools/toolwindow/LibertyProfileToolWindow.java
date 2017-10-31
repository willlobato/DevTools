package devtools.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.MBeanNotRegistredException;
import devtools.util.DevToolsUtil;
import devtools.util.GeneralConstants;
import devtools.util.JMXWebsphereConnector;
import org.jetbrains.annotations.NotNull;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class LibertyProfileToolWindow implements ToolWindowFactory {

    public static final String COLUMN_APPLICATION = "Application";
    public static final String COLUMN_PID = "Pid";
    public static final String COLUN_STATE = "State";
    private JPanel contentPane;
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;
    private JButton refreshButton;
    private JTable table;
    private JScrollPane scrollPane;

    private DefaultTableModel model;
    private Project project;

    public LibertyProfileToolWindow() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRefresh(project, null);
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRefresh(project, Application.Operation.START);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRefresh(project, Application.Operation.STOP);
            }
        });
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRefresh(project, Application.Operation.RESTART);
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.project = project;
        model.addColumn(COLUMN_PID);
        model.addColumn(COLUMN_APPLICATION);
        model.addColumn(COLUN_STATE);
        model.addColumn("");

        if (doRefresh(project, null)) return;
        table.setModel(model);

        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setViewportView(table);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private boolean doRefresh(@NotNull Project project, Application.Operation operation) {
        DevToolsProperties toolsProperties;
        try {
            toolsProperties = new DevToolsProperties();
            Configuration configuration = toolsProperties.loadConfigurationToReload();

            JMXWebsphereConnector jmxWebsphere = new JMXWebsphereConnector();
            JMXConnector jmxConnector = jmxWebsphere.connect(DevToolsUtil.getJndiPath(configuration));

            if (operation != null) {
                ObjectName objectName = (ObjectName) model.getValueAt(table.getSelectedRow(), 3);
                SwingWorker<Boolean, Void> worker = invokeOperation(project, operation, jmxWebsphere, jmxConnector, objectName);
                worker.execute();
            }

            Set<ObjectName> applications = jmxWebsphere.getApplications(jmxConnector);
            model.setRowCount(0);
            for(ObjectName objectName : applications) {
                Application application = jmxWebsphere.getApplication(jmxConnector, objectName);
                model.addRow(new Object[]{application.getPid(), application.getApplication(), application.getState(), application.getObjectName()});
            }

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
            return true;
        }
        return false;
    }

    @NotNull
    private SwingWorker<Boolean, Void> invokeOperation(@NotNull Project project, Application.Operation operation, JMXWebsphereConnector jmxWebsphere, JMXConnector jmxConnector, ObjectName objectName) {
        return new SwingWorker<Boolean, Void>() {
            @Override
            public Boolean doInBackground() {
                try {
                    jmxWebsphere.invokeOperationApplication(jmxConnector, objectName, operation);
                    return true;
                } catch (Exception e) {
                    Messages.showMessageDialog(project, e.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                    return false;
                }
            }
            @Override
            public void done() {
                try {
                    if (get()) {
                        doRefresh(project, null);
                    }
                } catch (Exception ex) {
                    Messages.showMessageDialog(project, ex.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }
        };
    }

}
