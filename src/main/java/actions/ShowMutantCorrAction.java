package actions;

import charts.LipidCorrelationChart;
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
public class ShowMutantCorrAction extends AbstractAction {

    private final HeatMap heatMap;
    private final DataMatrix dataMatrix;
    private JFrame mutantSelectionFrame;

    public ShowMutantCorrAction(DataMatrix dataMatrix, HeatMap heatMap) {

        this.dataMatrix = dataMatrix;
        this.heatMap = heatMap;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        mutantSelectionFrame = new JFrame("Select rowNames");
        mutantSelectionFrame.setBounds(100, 100, 500, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        List<String> mutants = new ArrayList<>(heatMap.getRowNames());
        Collections.sort(mutants);

        final ListMultipleSelectionPanel listPanel = new ListMultipleSelectionPanel(mutants,false);

        panel.add(listPanel);

        JButton showCorrButton = new JButton("Show correlations");
        showCorrButton.setSize(10,10);
        showCorrButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                displayCorrelations(listPanel.getSelectedElements());
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
        buttonPanel.add(showCorrButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        mutantSelectionFrame.add(panel);

        mutantSelectionFrame.pack();
        mutantSelectionFrame.setVisible(true);

    }

    private void displayCorrelations(List<String> mutants) {

        System.out.println("Show rowNames: "+mutants);

        JFrame frame = new JFrame("Lipid Correlation Scatterplot");

        JPanel lipidCorrPanel = new JPanel();
        lipidCorrPanel.setLayout(new BoxLayout(lipidCorrPanel, BoxLayout.Y_AXIS));

        for (int i=0;i<mutants.size();i++) {
            for (int j=i+1;j<mutants.size();j++) {
                lipidCorrPanel.add(new LipidCorrelationChart(mutants.get(i),mutants.get(j),dataMatrix, heatMap.getValueType()));
            }
        }

        JScrollPane scrollPane = new JScrollPane(lipidCorrPanel);
        scrollPane.setPreferredSize(new Dimension(500,300));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollPane);

        frame.pack( );
        RefineryUtilities.centerFrameOnScreen( frame );
        frame.setVisible( true );
    }
}

