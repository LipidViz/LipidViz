package actions;

import clustering.MutantDistanceCalculator;
import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import heatmap.CorrelationMatrixFrame;
import heatmap.HeatMapLipids;
import heatmap.HeatMapMutantCorrelation;
import parsers.DataMatrix;
import parsers.DataMatrixLipids;
import parsers.Parameters;
import utils.ListMultipleSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created by markusmueller on 21.05.19.
 */
public class CorrelationMatrixAction extends AbstractAction {

    private JFrame lipidSelectionFrame;

    public CorrelationMatrixAction() {

    }


    @Override
    public void actionPerformed(ActionEvent e) {

        lipidSelectionFrame = new JFrame("Select lipid subclasses");
        lipidSelectionFrame.setBounds(100, 100, 500, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        final ListMultipleSelectionPanel listPanel = new ListMultipleSelectionPanel(Parameters.getInstance().getLipidSubClass(), true);

        panel.add(listPanel);


        JButton clusterButton = new JButton("Correlate");
        clusterButton.setSize(10,10);
        clusterButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sortMutants(new HashSet<>(listPanel.getSelectedElements()));
                lipidSelectionFrame.setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setSize(10,10);
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                lipidSelectionFrame.setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(clusterButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        lipidSelectionFrame.add(panel);

        lipidSelectionFrame.pack();
        lipidSelectionFrame.setVisible(true);
    }

    private void sortMutants(Set<String> selectedLipids) {

        List<String> lipids = DataMatrixLipids.getInstance().getLipids();
        Parameters parameters = Parameters.getInstance();
        final int[] mask = new int[lipids.size()];

        for (int i=0;i<mask.length;i++) {
            if (selectedLipids.contains(parameters.getSubClass(lipids.get(i))))
                mask[i] = 1;
            else
                mask[i] = 0;
        }

        MutantDistanceCalculator calculator = new MutantDistanceCalculator(DataMatrixLipids.getInstance().getValueArrays(DataMatrix.ValueType.ZSCORE),mask);

        double[][] dist = calculator.calcDistances(MutantDistanceCalculator.DistanceType.COSINE);

        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        String[] mutants = new String[DataMatrixLipids.getInstance().getRowNames().size()];
        mutants = DataMatrixLipids.getInstance().getRowNames().toArray(mutants);
        Cluster cluster = alg.performClustering(dist, mutants, new AverageLinkageStrategy());

        List<String> mutantsInOrder = new ArrayList<>();

        parseClusters(cluster,mutantsInOrder);

        dist = sortMutants(dist,mutantsInOrder,DataMatrixLipids.getInstance().getRowNames());

        CorrelationMatrixFrame correlationMatrixFrame = new CorrelationMatrixFrame(dist,mutantsInOrder);
        correlationMatrixFrame.setVisible(true);

    }

    private void parseClusters(Cluster cluster, List<String> mutantsInOrder) {

        if (cluster.isLeaf()) {
            mutantsInOrder.add(cluster.getName());
            return;
        }

        List<Cluster> children = cluster.getChildren();

        for (Cluster child :children) {
            parseClusters(child,mutantsInOrder);
        }
    }

    public double[][] sortMutants(double[][] data, List<String> mutantsInOrder, List<String> mutants) {

        Map<String,Integer> indexMap = new HashMap<>();

        for (int i=0; i<mutantsInOrder.size();i++) {
            indexMap.put(mutantsInOrder.get(i),i);
        }

        double[][] orderedData = new double[data.length][data.length];

        for (int i = 0; i < data.length; i++) {
            int iNew = indexMap.get(mutants.get(i));
            for (int j = 0; j < data.length; j++) {
                int jNew = indexMap.get(mutants.get(j));
                orderedData[iNew][jNew] = 1.0-data[i][j];
            }
        }

        return orderedData;
    }

}
