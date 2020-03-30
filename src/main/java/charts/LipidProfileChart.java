package charts;

import heatmap.ColorMap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.*;
import parsers.DataMatrix;
import parsers.DataMatrixLipids;
import parsers.Parameters;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by markusmueller on 12.03.19.
 */
public class LipidProfileChart extends JPanel {

    public class LipidBarRenderer extends BarRenderer {

        private Map<String,Color> lipidSubClassColorMap;
        private List<String> columnNames;

        public LipidBarRenderer() {
            setItemMargin(0);

            Set<String> lipidSubClasses = Parameters.getInstance().getLipidSubClass();

            Color[] colors = ColorMap.getInferno(2);

            lipidSubClassColorMap = new HashMap<>();

            int i=0;
            for (String lipidSubClass : lipidSubClasses) {
                lipidSubClassColorMap.put(lipidSubClass,colors[i%2]);
                i++;
            }

            columnNames = DataMatrixLipids.getInstance().getColumnNames();
        }

        public Paint getItemPaint(int row, int column) {

            String lipid = columnNames.get(column);
            Color color =  lipidSubClassColorMap.get(Parameters.getInstance().getSubClass(lipid));

            int alpha = color.getAlpha();
            int nrDB = Parameters.getInstance().getNrDB(lipid);
            if (nrDB==1) {
                color = new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha/2);
            } else if (nrDB>1) {
                color = new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha/4);
            }

            return color;
        }

    }

    public class BasicBarRenderer extends BarRenderer {

        private Map<String,Color> columnColorMap;
        private List<String> columnNames;

        public BasicBarRenderer() {
            setItemMargin(0);

            columnNames = dataMatrix.getColumnNames();

            Color[] colors = ColorMap.getInferno(2);

            columnColorMap = new HashMap<>();

            int i=0;
            for (String columnName : columnNames) {
                columnColorMap.put(columnName,colors[i%2]);
                i++;
            }
        }

        public Paint getItemPaint(int row, int column) {

            String columnName = columnNames.get(column);
            Color color =  columnColorMap.get(columnName);

            return color;
        }

    }

    private final String mutant;
    private final DataMatrix dataMatrix;
    private final DataMatrix.ValueType valueType;

    public LipidProfileChart(String mutant, DataMatrix dataMatrix, DataMatrix.ValueType valueType) {

        this.mutant = mutant;
        this.dataMatrix = dataMatrix;
        this.valueType = valueType;

        String yLabel = dataMatrix.getValueLabel(valueType);

        JFreeChart barChart = ChartFactory.createBarChart(
                mutant+", "+ Parameters.getInstance().getDescription(mutant),
                dataMatrix.getName(),
                yLabel,
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot categoryPlot = barChart.getCategoryPlot();

        BarRenderer barRenderer = getBarRenderer();

        barRenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {

                String lipid = dataset.getColumnKey(column).toString();
                Number value = dataset.getValue(row, column);

                String s = lipid+" = "+String.format("%.3f",value.doubleValue());
                return s;
            }
        });

        barRenderer.setShadowVisible(false);
        barRenderer.setDrawBarOutline(true);
        barRenderer.setItemMargin(0);
        barRenderer.setBarPainter(new StandardBarPainter());

        categoryPlot.setRenderer(barRenderer);
        categoryPlot.setBackgroundPaint(new Color(240, 240, 240));
        final CategoryAxis domainAxis = categoryPlot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font ("Monospaced", Font.BOLD , 3));
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2.0)
        );

        ChartPanel chartPanel = new ChartPanel( barChart );
        chartPanel.setPreferredSize(new java.awt.Dimension( 1400 , 250 ) );

        add(chartPanel);
    }

    private CategoryDataset createDataset( ) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

        List<Double> values = dataMatrix.getValues(mutant,valueType);

        List<String> columnNames = dataMatrix.getColumnNames();

        for (int i=0;i<values.size();i++) {
            dataset.addValue(values.get(i), mutant, columnNames.get(i));
        }

        return dataset;
    }

    private BarRenderer getBarRenderer() {
        if (dataMatrix.getName().equals("Lipid"))
            return new LipidBarRenderer();
        else
            return new BasicBarRenderer();
    }

}
