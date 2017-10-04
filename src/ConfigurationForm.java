import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigurationForm {
    public JPanel panel;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel panelSouth;
    private JPanel panelCenter;
    private JTextField textProfilePath;
    private JLabel labelProfilePath;

    public ConfigurationForm() {

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                dispose();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ConfigurationForm");
        frame.setContentPane(new ConfigurationForm().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
