package actions;

import heatmap.HeatMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by markusmueller on 21.05.19.
 */
public class FindAction extends AbstractAction {

    private final HeatMap heatMap;
    private final JFrame findFrame;

    public FindAction(HeatMap heatMap) {

        this.heatMap = heatMap;
        this.findFrame = new JFrame("Find Gene Name");
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        final JTextField textField = new JTextField(30);
        textField.setToolTipText("OLE1|LCB2|WT.*");

        JButton findButton = new JButton("Find");
        findButton.setSize(10,10);
        findButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                String geneRegEx = textField.getText();
                Pattern pattern = Pattern.compile(geneRegEx);

                java.util.List<String> selectedGenes = new ArrayList<>();
                for (String gene : heatMap.getRowNames()) {
                    if (pattern.matcher(gene).find()) selectedGenes.add(gene);
                }

                findFrame.setVisible(false); //you can't see me!
                findFrame.dispose();

                heatMap.markMutants(selectedGenes);
            }
        });



        panel.add(textField);
        panel.add(findButton);

        findFrame.add(panel);

        findFrame.pack();
        findFrame.setVisible(true);

    }
}
