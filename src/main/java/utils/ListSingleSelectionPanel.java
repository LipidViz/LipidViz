package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by markusmueller on 26.02.19.
 */
public class ListSingleSelectionPanel extends JPanel {

    private final JList<String> jList;
    private final List<String> elements;
    private final String name;

    public ListSingleSelectionPanel(Set<String> elements, String name) {
        this.elements = new ArrayList<>(elements);
        this.jList = new JList<>();
        this.name = name;

        init();
    }

    private void init() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        DefaultListModel<String> listModel = new DefaultListModel();
        for (String element : elements) {
            listModel.addElement(element);
        }

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jList.setVisibleRowCount(elements.size());
        jList.setModel(listModel);

        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(name);
        label.setLabelFor(jList);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(30,100)));
        listPane.add(scrollPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(listPane);
    }

    public String getSelectedElement() {
        return jList.getSelectedValue();
    }
}
