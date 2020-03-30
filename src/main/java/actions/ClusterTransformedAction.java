package actions;

import clustering.MutantDistanceCalculator;
import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import heatmap.HeatMapTransform;
import parsers.DataMatrixTransformed;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by markusmueller on 21.05.19.
 */
public class ClusterTransformedAction extends AbstractAction {

    private final HeatMapTransform heatMap;
    private final String transformName;

    public ClusterTransformedAction(String transformName, HeatMapTransform heatMap) {
        this.heatMap = heatMap;
        this.transformName = transformName;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        sortMutants();
    }

    private void sortMutants() {

        DataMatrixTransformed dataMatrixTransformed = DataMatrixTransformed.getInstance(transformName);

        MutantDistanceCalculator calculator = new MutantDistanceCalculator(dataMatrixTransformed.getValueArrays());

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
