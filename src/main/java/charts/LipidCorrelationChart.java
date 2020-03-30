package charts;

import heatmap.ColorMap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import parsers.DataMatrix;
import parsers.DataMatrixLipids;
import parsers.Parameters;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by markusmueller on 26.03.19.
 */
public class LipidCorrelationChart extends JPanel {

    private final String mutantX;
    private final String mutantY;

    private final DataMatrix.ValueType valueType;
    private final DataMatrix dataMatrix;

    private final List<String> columnNames;
    private final Map<String,List<Integer>> subClass;


    public LipidCorrelationChart(String mutantX, String mutantY, DataMatrix dataMatrix, DataMatrix.ValueType valueType) {

        this.mutantX = mutantX;
        this.mutantY = mutantY;
        this.dataMatrix = dataMatrix;
        this.valueType = valueType;

        this.columnNames = dataMatrix.getColumnNames();
        this.subClass = new HashMap<>();

        if (dataMatrix.getName().equals("Lipid")) {
            for (int i = 0; i < columnNames.size(); i++) {
                String subClass = Parameters.getInstance().getSubClass(columnNames.get(i));
                if (!this.subClass.containsKey(subClass)) this.subClass.put(subClass, new ArrayList<>());
                this.subClass.get(subClass).add(i);
            }
        } else {
            this.subClass.put("subClass",new ArrayList<>());
            for (int i = 0; i < columnNames.size(); i++) {
                this.subClass.get("subClass").add(i);
            }
        }

        String dataLabel = dataMatrix.getValueLabel(valueType);

        JFreeChart chart = ChartFactory.createScatterPlot(
                mutantX+" versus "+mutantY+": "+dataLabel,
                mutantX, mutantY, createDataset());


        //Changes background color
        final XYPlot plot = chart.getXYPlot( );
        plot.setBackgroundPaint(new Color(240,240,240));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new java.awt.Dimension( 600 , 600 ) );

        XYDotRenderer renderer = new XYDotRenderer( );
        Color[] colors = ColorMap.getPlasma(subClass.size());

        for (int i=0; i<subClass.keySet().size();i++) {
            renderer.setSeriesPaint(i,colors[i]);
            renderer.setSeriesShape(i, ShapeUtilities.createDiamond(3));
            renderer.setDotWidth(7);
            renderer.setDotHeight(7);
        }

        plot.setRenderer( renderer );

        add(panel);
    }

   private XYSeriesCollection createDataset( ) {

        XYSeriesCollection dataset = new XYSeriesCollection();

        List<Double> lipidValuesX = dataMatrix.getValues(mutantX,valueType);
        List<Double> lipidValuesY = dataMatrix.getValues(mutantY,valueType);


        for (String subClass : subClass.keySet()) {

            final XYSeries series = new XYSeries(subClass);

            for (Integer i : this.subClass.get(subClass)) {
                series.add(lipidValuesX.get(i),lipidValuesY.get(i));
            }

            dataset.addSeries(series);
        }

        return dataset;
    }
}
