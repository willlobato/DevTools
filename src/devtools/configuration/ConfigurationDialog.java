package devtools.configuration;

import com.intellij.openapi.project.Project;
import devtools.menu.ReloadAction;
import devtools.toolbar.SelectApplicationComponent;
import devtools.util.DevToolsUtil;
import devtools.util.ProfileConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

public class ConfigurationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtProfilePath;
    private JComboBox comboProfile;

    private DefaultComboBoxModel<ComboBoxItem> comboBoxModel = new DefaultComboBoxModel<>();

    private DevToolsProperties devToolsProperties;
    private Configuration configuration;
    private Project project;

    private void init() throws Exception {
        devToolsProperties = new DevToolsProperties();
        configuration = devToolsProperties.loadConfiguration();
    }

    private void postInit() throws IOException {
        comboProfile.setModel(comboBoxModel);
        txtProfilePath.setText(configuration.getProfilePath());
        populateComboBox(configuration.getProfilePath(), configuration.getProfileUse());
    }

    private void populateComboBox(String profilePathStr, String profileUseStr) throws IOException {
        comboBoxModel.removeAllElements();
        File profilePath = new File(profilePathStr);
        if(profilePath.exists()) {
            comboProfile.setEnabled(true);
            final String servers = profilePath.getAbsolutePath().concat(ProfileConstants.SERVERS);
            File[] listServers = new File(servers).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.getName().contains(".");
                }
            });

            if(listServers != null) {
                for(File server : listServers) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem(server.getName(), server.getAbsolutePath());
                    comboBoxModel.addElement(comboBoxItem);
                }

                if(profileUseStr != null) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem(profileUseStr);
                    comboProfile.setSelectedItem(comboBoxItem);
                }
            } else {
                comboProfile.setEnabled(false);
            }
        } else {
            comboProfile.setEnabled(false);
        }
    }

    public ConfigurationDialog(Window parentWindow, Project project) throws Exception {
        super(parentWindow);
        init();
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        txtProfilePath.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    populateComboBox(txtProfilePath.getText(), null);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        postInit();

    }

    private void saveConfiguration() throws IOException {
        Configuration configuration = new Configuration();
        configuration.setProfilePath(txtProfilePath.getText());
        ComboBoxItem comboBoxItem = ((ComboBoxItem)comboProfile.getSelectedItem());
        if(comboBoxItem != null) {
            configuration.setProfileUse(comboBoxItem.getKey());
        }
        devToolsProperties.save(configuration);

        SelectApplicationComponent applicationComponent = SelectApplicationComponent.getManager(project);
        Map<String, File> applications = DevToolsUtil.getApplications(configuration);
        applicationComponent.setApplications(applications);
        if (applications == null) {
            devToolsProperties.save(DevToolsProperties.PROP_APPLICATION_SELECTED, "");
            applicationComponent.clearApplicationSelected();
        }
    }

    private void onOK() throws IOException {
        saveConfiguration();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) throws Exception {
        ConfigurationDialog dialog = new ConfigurationDialog(null, null);
        dialog.setSize(500,200);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
//        System.exit(0);
    }
}
