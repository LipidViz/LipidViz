package heatmap;

import actions.*;
import parsers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by markusmueller on 22.01.19.
 */


public class TransformedValuesFrame extends JFrame {

    private HeatMapTransform heatMap;
    private JScrollPane scrollPane;
    private final String transformName;

    public TransformedValuesFrame( String transformName) {

        this.transformName = transformName;

        initUI();
    }

    private void initUI() {

        createMenuBar();

        setTitle(transformName);
        setSize(Parameters.getInstance().getWindowWidth(), Parameters.getInstance().getWindowHeight());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }



    private void createMenuBar() {

        JMenuBar menubar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem eMenuItem = new JMenuItem("Exit");

        eMenuItem.setToolTipText("Exit heatmap");
        eMenuItem.addActionListener((event) ->  setVisible(false));

        final JFileChooser fc = new JFileChooser(Parameters.getInstance().getDefaultReportDirectory());

        JMenuItem sMenuItem = new JMenuItem("Save");
        sMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showSaveDialog(scrollPane);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File reportFile = fc.getSelectedFile();
                    heatMap.writeToFile(reportFile);
                }
            }
        });

        fileMenu.add(sMenuItem);
        fileMenu.add(eMenuItem);
        menubar.add(fileMenu);

        setJMenuBar(menubar);

        Parameters parameters = Parameters.getInstance();

        DataMatrixTransformed dataMatrix = DataMatrixTransformed.getInstance(transformName);

        Color[] colors = ColorMap.getPlasma(255);
        heatMap = new HeatMapTransform(transformName, colors, parameters.getCellWidth(), parameters.getCellHeight());

        scrollPane = new JScrollPane(heatMap);
        heatMap.setAutoscrolls(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        int w = parameters.getCellWidth()* dataMatrix.getNumberCols()+60;
        int h = parameters.getCellHeight()* DataMatrixLipids.getInstance().getNumberRows()+60;
        heatMap.setPreferredSize(new Dimension(2*w, 2*h));

        JMenu viewMenu = new JMenu("View");

        JMenuItem zoomInItem = new JMenuItem("Zoom in");
        JMenuItem zoomOutItem = new JMenuItem("Zoom out");
        JMenuItem showProfileItem = new JMenuItem("Profile");
        JMenuItem showScatterPlotItem = new JMenuItem("Correlation");

        zoomInItem.addActionListener(new ZoomAction(heatMap,true));
        zoomOutItem.addActionListener(new ZoomAction(heatMap,false));
        showProfileItem.addActionListener(new ShowMutantProfileAction(dataMatrix, heatMap));
        showScatterPlotItem.addActionListener(new ShowMutantCorrAction(dataMatrix, heatMap));

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(showProfileItem);
        viewMenu.add(showScatterPlotItem);

        getJMenuBar().add(viewMenu);

        JMenu toolsMenu = new JMenu("Tools");

        JMenuItem clusterItem = new JMenuItem("Cluster rowNames");
        clusterItem.addActionListener(new ClusterTransformedAction(transformName, heatMap));

        JMenuItem findItem = new JMenuItem("Find rowNames");
        findItem.addActionListener(new FindAction(heatMap));

        toolsMenu.add(clusterItem);
        toolsMenu.add(findItem);

        getJMenuBar().add(toolsMenu);

        getContentPane().add(scrollPane);
        getContentPane().revalidate();
    }

}