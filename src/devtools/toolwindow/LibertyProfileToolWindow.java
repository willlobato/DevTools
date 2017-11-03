package devtools.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.JMXWebsphereException;
import devtools.util.DevToolsUtil;
import devtools.util.GeneralConstants;
import devtools.util.JMXWebsphereConnector;
import devtools.vo.ApplicationVO;
import org.jetbrains.annotations.NotNull;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import static devtools.vo.ApplicationVO.*;

public class LibertyProfileToolWindow implements ToolWindowFactory {

    public static final String ATTRIBUTE_APPLICATION = "Application Name";
    public static final String ATTRIBUTE_PID = "Pid";
    public static final String ATTRIBUTE_STATE = "State";
    public static final String ATTRIBUTE_OBJECTNAME = "ObjectName";

    private static final String CONNECT_TEXT = "Connect";
    private static final String DISCONNECT_TEXT = "Disconnect";

    private static final String NOTIFICATION_APPLICATION_INSTALL_CALLED = "ApplicationsInstallCalled";

    private JPanel contentPane;
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;
    private JButton refreshButton;
    private JTable table;
    private JScrollPane scrollPane;
    private JButton connectButton;

    private DefaultTableModel model;
    private Project project;

    private DevToolsProperties toolsProperties;
    private Configuration configuration;
    private JMXWebsphereConnector jmxWebsphere;
    private JMXConnector jmxConnector;

    //Application Notification
    private ObjectName runtimeUpdateNotificationMBean;
    private MBeanServerConnection mBeanServerConnection;
    private ApplicationNotificationListener applicationNotificationListener;

    public LibertyProfileToolWindow() {

        jmxWebsphere = new JMXWebsphereConnector();
        applicationNotificationListener = new ApplicationNotificationListener();

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(project, Operation.REFRESH);
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(project, Operation.START);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(project, Operation.STOP);
            }
        });
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(project, Operation.RESTART);
            }
        });
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(CONNECT_TEXT.equals(connectButton.getText())) {
                        connectBehavior();
                    } else {
                        disconnectBehavior();
                    }
                } catch (JMXWebsphereException ex) {
                    Messages.showMessageDialog(project, ex.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;

        try {
            toolsProperties = new DevToolsProperties();
            setupTable();
            disconnectBehavior();
        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
        }

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void setupTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(ATTRIBUTE_PID);
        model.addColumn(ATTRIBUTE_APPLICATION);
        model.addColumn(ATTRIBUTE_STATE);
        model.addColumn(ATTRIBUTE_OBJECTNAME);
        table.setModel(model);

        hiddenTableColumn(3);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setViewportView(table);
    }

    private void hiddenTableColumn(int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
    }

    private void connectBehavior() {
        if(execute(project, Operation.REFRESH)) {
            connectButton.setText(DISCONNECT_TEXT);
            startButton.setEnabled(true);
            stopButton.setEnabled(true);
            restartButton.setEnabled(true);

            try {
                runtimeUpdateNotificationMBean = jmxWebsphere.getRuntimeUpdateNotification();
                mBeanServerConnection = jmxWebsphere.getMBeanServerConnection(jmxConnector, runtimeUpdateNotificationMBean);
                applicationNotificationListener = new ApplicationNotificationListener();
                mBeanServerConnection.addNotificationListener(runtimeUpdateNotificationMBean, applicationNotificationListener, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void disconnectBehavior() throws JMXWebsphereException {
        try {
            if (mBeanServerConnection != null) {
                mBeanServerConnection.removeNotificationListener(runtimeUpdateNotificationMBean, applicationNotificationListener);
            }
            jmxWebsphere.disconnect(jmxConnector);
            connectButton.setText(CONNECT_TEXT);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            restartButton.setEnabled(false);
            model.setRowCount(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean execute(@NotNull Project project, Operation operation) {
        try {
            configuration = toolsProperties.loadConfigurationToReload();
            jmxConnector = jmxWebsphere.connect(DevToolsUtil.getJndiPath(configuration));

            if (!Operation.REFRESH.equals(operation)) {
                int row = table.getSelectedRow();
                if (row > -1) {
                    ObjectName applicationObjectName = (ObjectName) model.getValueAt(row, 3);
                    SwingWorker<Boolean, Void> worker = invokeOperation(project, row, operation, jmxWebsphere, jmxConnector, applicationObjectName);
                    worker.execute();
                } else {
                    Messages.showMessageDialog(project, "Select the application", GeneralConstants.INFORMATION, Messages.getInformationIcon());
                    return false;
                }
            }

            refreshTable();

        } catch (Exception e) {
            Messages.showMessageDialog(project, e.getMessage(), GeneralConstants.ERROR, Messages.getErrorIcon());
            try {
                disconnectBehavior();
            } catch (JMXWebsphereException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void refreshTable() throws JMXWebsphereException {
        Set<ObjectName> applications = jmxWebsphere.getApplications(jmxConnector);
        model.setRowCount(0);
        for(ObjectName objectName : applications) {
            ApplicationVO applicationVO = jmxWebsphere.getApplication(jmxConnector, objectName);
            model.addRow(new Object[]{applicationVO.getPid(), applicationVO.getApplication(), applicationVO.getState(), applicationVO.getObjectName()});
        }
    }

    @NotNull
    private SwingWorker<Boolean, Void> invokeOperation(@NotNull Project project, int row, Operation operation, JMXWebsphereConnector jmxWebsphere, JMXConnector jmxConnector, ObjectName objectName) {
        return new SwingWorker<Boolean, Void>() {

            private MBeanServerConnection mBeanServerConnection;
            private ApplicationStateNotificationListener listener = new ApplicationStateNotificationListener(row);

            @Override
            public Boolean doInBackground() {
                try {
                    mBeanServerConnection = jmxWebsphere.getMBeanServerConnection(jmxConnector, objectName);
                    AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
                    filter.enableAttribute(ATTRIBUTE_STATE);
                    mBeanServerConnection.addNotificationListener(objectName, listener, filter, null);

                    jmxWebsphere.invokeOperationApplication(mBeanServerConnection, objectName, operation);
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
                        mBeanServerConnection.removeNotificationListener(objectName, listener);
                    }
                } catch (Exception ex) {
                    Messages.showMessageDialog(project, ex.getMessage(),
                            GeneralConstants.ERROR, Messages.getErrorIcon());
                }
            }
        };
    }

    protected class ApplicationStateNotificationListener implements NotificationListener {

        private int rowEffected;

        public ApplicationStateNotificationListener(int rowEffected) {
            this.rowEffected = rowEffected;
        }

        public int getRowEffected() {
            return rowEffected;
        }

        public void handleNotification(Notification notification, Object obj) {
            if(notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification attributeChange =
                        (AttributeChangeNotification) notification;
                int columnEffected = table.getColumn(attributeChange.getAttributeName()).getModelIndex();
                model.setValueAt(attributeChange.getNewValue(), getRowEffected(), columnEffected);
            }
        }
    }

    protected class ApplicationNotificationListener implements NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            if (notification.getUserData() instanceof Map) {
                Map userData = (Map) notification.getUserData();
                if (NOTIFICATION_APPLICATION_INSTALL_CALLED.equals(userData.get("name"))) {
                    try {
                        refreshTable();
                    } catch (JMXWebsphereException e) {
                        Messages.showMessageDialog(project, e.getMessage(),
                                GeneralConstants.ERROR, Messages.getErrorIcon());
                    }
                }
            }
        }
    }

}
