package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created by markusmueller on 26.02.19.
 */
public class ListMultipleSelectionPanel extends JPanel {

    private final List<String> elements;
    private final DefaultListModel<String> selectedLipidModel;
    private final DefaultListModel<String> unselectedLipidModel;
    private final boolean initSelected;

    public ListMultipleSelectionPanel(Set<String> elements, boolean initSelected) {
        this.elements = new ArrayList<>(elements);
        this.selectedLipidModel = new DefaultListModel();
        this.unselectedLipidModel = new DefaultListModel();

        this.initSelected = initSelected;

        init();
    }

    public ListMultipleSelectionPanel(List<String> elements, boolean initSelected) {
        this.elements = new ArrayList<>(elements);
        this.selectedLipidModel = new DefaultListModel();
        this.unselectedLipidModel = new DefaultListModel();

        this.initSelected = initSelected;

        init();
    }

    private void init() {

        setLayout(new FlowLayout());
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        for (String element : elements) {
            if (initSelected)
                selectedLipidModel.addElement(element);
            else
                unselectedLipidModel.addElement(element);
        }

        JList<String> selectedLipids = new JList<>();

        selectedLipids.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedLipids.setFont(new Font("Tahoma", Font.PLAIN, 16));
        selectedLipids.setVisibleRowCount(elements.size());
        selectedLipids.setModel(selectedLipidModel);

        JScrollPane selectedLipidsPane = new JScrollPane(selectedLipids);
        selectedLipidsPane.setPreferredSize(new Dimension(150, 300));
        selectedLipidsPane.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Selected");
        label.setLabelFor(selectedLipids);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(selectedLipidsPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(listPane);


        JList<String> unselectedLipids = new JList<>();
        unselectedLipids.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        unselectedLipids.setFont(new Font("Tahoma", Font.PLAIN, 16));
        unselectedLipids.setVisibleRowCount(elements.size());
        unselectedLipids.setModel(unselectedLipidModel);

        JButton remove = new JButton("<<");
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unselectedLipids.getSelectedValuesList().stream().forEach((data) -> {
                    selectedLipidModel.addElement(data);
                    unselectedLipidModel.removeElement(data);
                });
                selectedLipids.revalidate();
                unselectedLipids.revalidate();
            }
        });
        add(remove);

        JButton add = new JButton(">>");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get list of selected values and for each one of them do following
                selectedLipids.getSelectedValuesList().stream().forEach((data) -> {
                    // moving data
                    unselectedLipidModel.addElement(data);
                    // remove from other side
                    selectedLipidModel.removeElement(data);
                });
                // refreshing the view after changes
                selectedLipids.revalidate();
                unselectedLipids.revalidate();
            }
        });

        add(add);

        JScrollPane unselectedLipidsPane = new JScrollPane(unselectedLipids);
        unselectedLipidsPane.setPreferredSize(new Dimension(150, 300));
        unselectedLipidsPane.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        label = new JLabel("Removed");
        label.setLabelFor(unselectedLipids);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(unselectedLipidsPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(listPane);
    }

    public List<String> getSelectedElements() {
        List<String> selected = new ArrayList<>();

        for (int i=0;i<selectedLipidModel.size();i++) {
            selected.add(selectedLipidModel.get(i));
        }

        return selected;
    }

}
