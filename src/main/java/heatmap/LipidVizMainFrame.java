package heatmap;

import actions.*;
import parsers.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by markusmueller on 22.01.19.
 */
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.crypto.Data;


public class LipidVizMainFrame extends JFrame {

    private HeatMapLipids heatMap;
    private JScrollPane scrollPane;

    public LipidVizMainFrame() {

        initUI();
    }

    private void initUI() {

        createMenuBar();

        setTitle("LipidVizMainFrame");
        setSize(Parameters.getInstance().getWindowWidth(), Parameters.getInstance().getWindowHeight());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }



    private void createMenuBar() {

        JMenuBar menubar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem eMenuItem = new JMenuItem("Exit");

        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((event) -> System.exit(0));
        fileMenu.add(eMenuItem);
        menubar.add(fileMenu);

        setJMenuBar(menubar);

        Parameters parameters = Parameters.getInstance();

        System.out.println("Parsing "+parameters.getNormDataLogIntFile().getAbsolutePath()+"...");
        NormalizedQuantDataParser logIntParser = new NormalizedQuantDataParser(parameters.getNormDataLogIntFile());
        logIntParser.parse();

        System.out.println("Parsing "+parameters.getNormDataLogIntStdFile().getAbsolutePath()+"...");
        NormalizedQuantDataParser logIntStdParser = new NormalizedQuantDataParser(parameters.getNormDataLogIntStdFile());
        logIntStdParser.parse();

        DataMatrixLipids.makeInstance(logIntParser,logIntStdParser);

        VectorTransforms.makeInstance(parameters.getVectorTransformFile());

        Color[] colors = ColorMap.getPlasma(255);
        heatMap = new HeatMapLipids(DataMatrix.ValueType.ZSCORE, colors, parameters.getCellWidth(), parameters.getCellHeight());

        scrollPane = new JScrollPane(heatMap);
        heatMap.setAutoscrolls(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        int w = parameters.getCellWidth()* DataMatrixLipids.getInstance().getNumberCols()+60;
        int h = parameters.getCellHeight()* DataMatrixLipids.getInstance().getNumberRows()+60;
        heatMap.setPreferredSize(new Dimension(2*w, 2*h));

        JMenu viewMenu = new JMenu("View");

        JMenuItem zoomInItem = new JMenuItem("Zoom in");
        JMenuItem zoomOutItem = new JMenuItem("Zoom out");
        JMenuItem showProfileItem = new JMenuItem("Lipid profile");
        JMenuItem showScatterPlotItem = new JMenuItem("Lipid correlation");
        JMenu dataTransMenu = new JMenu("Data transformation");

        JMenuItem zScores = new JMenuItem("ZScores");
        zScores.addActionListener(new IntensityTranformationsAction(heatMap, DataMatrix.ValueType.ZSCORE));
        JMenuItem logInt = new JMenuItem("Log Intensity");
        logInt.addActionListener(new IntensityTranformationsAction(heatMap, DataMatrix.ValueType.LOG_INT));
        JMenuItem ints = new JMenuItem("Intensity");
        ints.addActionListener(new IntensityTranformationsAction(heatMap, DataMatrix.ValueType.INT));
        JMenuItem intDiff = new JMenuItem("Intensity Diff");
        intDiff.addActionListener(new IntensityTranformationsAction(heatMap, DataMatrix.ValueType.DIFF));
        JMenuItem logIntStd = new JMenuItem("Log Intensity Std");
        logIntStd.addActionListener(new IntensityTranformationsAction(heatMap, DataMatrix.ValueType.LOG_INT_STD));

        dataTransMenu.add(zScores);
        dataTransMenu.add(ints);
        dataTransMenu.add(intDiff);
        dataTransMenu.add(logInt);
        dataTransMenu.add(logIntStd);


        zoomInItem.addActionListener(new ZoomAction(heatMap,true));
        zoomOutItem.addActionListener(new ZoomAction(heatMap,false));
        showProfileItem.addActionListener(new ShowMutantProfileAction(DataMatrixLipids.getInstance(), heatMap));
        showScatterPlotItem.addActionListener(new ShowMutantCorrAction(DataMatrixLipids.getInstance(), heatMap));

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(showProfileItem);
        viewMenu.add(showScatterPlotItem);
        viewMenu.add(dataTransMenu);

        getJMenuBar().add(viewMenu);

        JMenu toolsMenu = new JMenu("Tools");

        JMenuItem clusterItem = new JMenuItem("Cluster mutants");
        clusterItem.addActionListener(new ClusterAction(heatMap));

        JMenuItem correlationMItem = new JMenuItem("Correlation matrix");
        correlationMItem.addActionListener(new CorrelationMatrixAction());

        JMenuItem findItem = new JMenuItem("Find mutants");
        findItem.addActionListener(new FindAction(heatMap));

        JMenuItem transformItem = new JMenuItem("Transform lipid vector");
        transformItem.addActionListener(new VectorTransformAction());

        toolsMenu.add(clusterItem);
        toolsMenu.add(correlationMItem);
        toolsMenu.add(findItem);
        toolsMenu.add(transformItem);

        getJMenuBar().add(toolsMenu);

        getContentPane().add(scrollPane);
        getContentPane().revalidate();
    }

}