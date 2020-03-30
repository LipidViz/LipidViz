package actions;

import charts.LipidProfileChart;
import heatmap.HeatMap;
import org.jfree.ui.RefineryUtilities;
import parsers.DataMatrix;
import utils.ListMultipleSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by markusmueller on 21.05.19.
 */
public class ShowMutantProfileAction extends AbstractAction {

    private JFrame mutantSelectionFrame;
    private final DataMatrix dataMatrix;
    private final HeatMap heatMap;

    public ShowMutantProfileAction(DataMatrix dataMatrix, HeatMap heatMap) {

        this.dataMatrix = dataMatrix;
        this.heatMap = heatMap;
    }


    @Override
    public void actionPerformed(ActionEvent e) {


        mutantSelectionFrame = new JFrame("Select rowNames");
        mutantSelectionFrame.setBounds(100, 100, 500, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        List<String> mutants = new ArrayList<>(dataMatrix.getRowNames());
        Collections.sort(mutants);

        final ListMultipleSelectionPanel listPanel = new ListMultipleSelectionPanel(mutants,false);

        panel.add(listPanel);


        JButton clusterButton = new JButton("Show profiles");
        clusterButton.setSize(10,10);
        clusterButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                displayProfiles(listPanel.getSelectedElements());
                mutantSelectionFrame.setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setSize(10,10);
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                mutantSelectionFrame.setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(clusterButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        mutantSelectionFrame.add(panel);

        mutantSelectionFrame.pack();
        mutantSelectionFrame.setVisible(true);

    }

    private void displayProfiles(List<String> mutants) {

        System.out.println("Show rowNames: "+mutants);

        JFrame frame = new JFrame("Lipid Profile Barplots");

        JPanel lipidProfilePanel = new JPanel();
        lipidProfilePanel.setLayout(new BoxLayout(lipidProfilePanel, BoxLayout.Y_AXIS));

        for (String mutant : mutants) {
            lipidProfilePanel.add(new LipidProfileChart(mutant,dataMatrix,heatMap.getValueType()));
        }

        JScrollPane scrollPane = new JScrollPane(lipidProfilePanel);
        scrollPane.setPreferredSize(new Dimension(500,300));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollPane);

        frame.pack( );
        RefineryUtilities.centerFrameOnScreen( frame );
        frame.setVisible( true );
    }

}
