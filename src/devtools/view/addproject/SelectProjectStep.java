package devtools.view.addproject;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;

public class SelectProjectStep extends JPanel {

    private JButton add;
    private JButton addAll;
    private JButton remove;
    private JButton removeAll;
    private JList left;
    private JList right;


    public SelectProjectStep() {

        add = new JButton();
        add.setIcon(AllIcons.Actions.Right);
        addAll = new JButton();
        addAll.setIcon(AllIcons.Actions.AllRight);
        remove = new JButton();
        remove.setIcon(AllIcons.Actions.Left);
        removeAll = new JButton();
        removeAll.setIcon(AllIcons.Actions.AllLeft);

        left = new JBList();
        right = new JBList();


        this.setLayout(new GridBagLayout());





    }

}
