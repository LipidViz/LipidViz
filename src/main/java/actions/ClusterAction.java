package actions;

import clustering.MutantDistanceCalculator;
import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import heatmap.HeatMapLipids;
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
public class ClusterAction extends AbstractAction {

    private final HeatMapLipids heatMap;
    private JFrame lipidSelectionFrame;

    public ClusterAction(HeatMapLipids heatMap) {

        this.heatMap = heatMap;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        lipidSelectionFrame = new JFrame("Select lipid subclasses");
        lipidSelectionFrame.setBounds(100, 100, 500, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        final ListMultipleSelectionPanel listPanel = new ListMultipleSelectionPanel(Parameters.getInstance().getLipidSubClass(), true);

        panel.add(listPanel);


        JButton clusterButton = new JButton("Cluster");
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

        List<String> lipids = heatMap.getLipids();
        Parameters parameters = Parameters.getInstance();
        final int[] mask = new int[lipids.size()];

        for (int i=0;i<mask.length;i++) {
            if (selectedLipids.contains(parameters.getSubClass(lipids.get(i))))
                mask[i] = 1;
            else
                mask[i] = 0;
        }

        MutantDistanceCalculator calculator = new MutantDistanceCalculator(heatMap.getData(),mask);

        double[][] dist = calculator.calcDistances(MutantDistanceCalculator.DistanceType.COSINE);

        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        String[] mutants = new String[heatMap.getRowNames().size()];
        mutants = heatMap.getRowNames().toArray(mutants);
        Cluster cluster = alg.performClustering(dist, mutants, new AverageLinkageStrategy());

        List<String> mutantsInOrder = new ArrayList<>();

        parseClusters(cluster,mutantsInOrder);

        heatMap.sortMutants(mutantsInOrder);

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
}
