package devtools.configuration;

import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class ConfigurationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtProfilePath;
    private JComboBox comboProfile;

    private DefaultComboBoxModel<ComboServer> comboBoxModel = new DefaultComboBoxModel<>();

    private DevToolsProperties devToolsProperties;
    private Configuration configuration;

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
                    ComboServer comboServer = new ComboServer(server.getName(), server.getAbsolutePath());
                    comboBoxModel.addElement(comboServer);
                }

                if(profileUseStr != null) {
                    ComboServer comboServer = new ComboServer(profileUseStr);
                    comboProfile.setSelectedItem(comboServer);
                }
            } else {
                comboProfile.setEnabled(false);
            }
        } else {
            comboProfile.setEnabled(false);
        }
    }

    public ConfigurationDialog() throws Exception {

        init();

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
        ComboServer comboServer = ((ComboServer)comboProfile.getSelectedItem());
        if(comboServer != null) {
            configuration.setProfileUse(comboServer.getKey());
        }
        devToolsProperties.save(configuration);
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
        ConfigurationDialog dialog = new ConfigurationDialog();
        dialog.setLocationRelativeTo(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
