package heatmap;

import parsers.DataMatrix;
import parsers.DataMatrixLipids;
import parsers.Parameters;
import weblinks.WebLinkList;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import javax.swing.*;



public class HeatMapLipids extends HeatMap {

    private List<String> series;
    private List<String> wildTypes;
    private List<String> batches;

    public HeatMapLipids(DataMatrix.ValueType dataType, Color[] colors, int cellWidth, int cellHeight) {

        super(colors, cellWidth, cellHeight);

        data = DataMatrixLipids.getInstance().getValueArrays(valueType);

        setValueType(dataType);

        this.columnNames = DataMatrixLipids.getInstance().getLipids();
        this.rowNames = DataMatrixLipids.getInstance().getRowNames();
        this.series = DataMatrixLipids.getInstance().getSeries();
        this.wildTypes = DataMatrixLipids.getInstance().getWildTypes();
        this.batches = DataMatrixLipids.getInstance().getBatches();

        updateData(data);
        drawData();
        repaint();

        currentAxisFont = new Font ("Monospaced", Font.BOLD ,15);

        this.setPreferredSize(new Dimension(marginLeft + marginRight + data[0].length*cellWidth,
                marginTop + marginBottom + data.length*cellHeight));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

    }

    public void setValueType(DataMatrix.ValueType valueType) {

        if (valueType==this.valueType) return;

        this.valueType = valueType;
        data = DataMatrixLipids.getInstance().getValueArrays(valueType);

        if (valueType== DataMatrix.ValueType.ZSCORE || valueType== DataMatrix.ValueType.DIFF) {
            neutralValue = 0.0;
            dimAroundNeutral = true;
        } else {
            dimAroundNeutral = false;
        }

        this.rowNames = DataMatrixLipids.getInstance().getRowNames();
        this.series = DataMatrixLipids.getInstance().getSeries();
        this.wildTypes = DataMatrixLipids.getInstance().getWildTypes();
        this.batches = DataMatrixLipids.getInstance().getBatches();

        // keep previous ordering
        sortMutants(rowNames);
    }


    public void sortMutants(List<String> mutantsInOrder) {

        Map<String,Integer> indexMap = new HashMap<>();

        for (int i=0; i<mutantsInOrder.size();i++) {
            indexMap.put(mutantsInOrder.get(i),i);
        }

        double[][] orderedData = new double[data.length][];
        List<String> orderedSeries = new ArrayList<>(series);
        List<String> orderedWildTypes = new ArrayList<>(wildTypes);
        List<String> orderedBatches = new ArrayList<>(batches);

        for (int ix = 0; ix < data.length; ix++) {
            int newIdx = indexMap.get(rowNames.get(ix));

            orderedData[newIdx] = data[ix];
            orderedSeries.set(newIdx,series.get(ix));
            orderedWildTypes.set(newIdx,wildTypes.get(ix));
            orderedBatches.set(newIdx,batches.get(ix));
        }

        data = orderedData;
        rowNames = mutantsInOrder;
        series = orderedSeries;
        wildTypes = orderedWildTypes;
        batches = orderedBatches;

        updateDataColors();
        drawData();
        repaint();
    }

    protected void drawColumnInfo(Graphics2D g2d, int i) {

        g2d.setColor(new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(),220));
        String str = columnNames.get(i)+", "+ Parameters.getInstance().getSubClass(columnNames.get(i));
        g2d.fillRect(cursorX+8, cursorY+12, str.length()*15, 25 );
        Font f = new Font ("Monospaced", Font.BOLD , 20);
        g2d.setFont (f);
        g2d.setColor(Color.blue);
        g2d.drawString(str, cursorX+16, cursorY+30);
    }


    protected void drawRowInfo(Graphics2D g2d, int j) {

        g2d.setColor(new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(),220));
        String str = rowNames.get(j)+", "+ Parameters.getInstance().getDescription(rowNames.get(j))+" : "+series.get(j)+","+batches.get(j)+","+wildTypes.get(j);
        g2d.fillRect(cursorX+8, cursorY+12, str.length()*15, 25 );
        Font f = new Font ("Monospaced", Font.BOLD , 20);
        g2d.setFont (f);
        g2d.setColor(Color.blue);
        g2d.drawString(str, cursorX+16, cursorY+30);
    }


    protected void drawDataCellInfo(Graphics2D g2d, int i, int j) {

        g2d.setColor(new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(),220));
        String str = rowNames.get(j)+","+columnNames.get(i)+": "+String.format("%.6f",data[j][i]);
        g2d.fillRect(cursorX+8, cursorY+12, str.length()*15, 25 );
        Font f = new Font ("Monospaced", Font.BOLD , 20);
        g2d.setFont (f);
        g2d.setColor(Color.darkGray);
        g2d.drawString(str, cursorX+16, cursorY+30);
    }


    protected void openColumnWebLink(int i) {
        JFrame webLinksFrame = new JFrame(columnNames.get(i)+" weblinks");
        webLinksFrame.setBounds(100, 100, 700, 800);

        WebLinkList webLinkList = new WebLinkList(Parameters.getInstance().getExtendedInfo(columnNames.get(i)));

        webLinksFrame.add(webLinkList);

        webLinksFrame.pack();
        webLinksFrame.setVisible(true);
    }


    protected void openRowWebLink(int j) {

        try {
            Desktop.getDesktop().browse(new URI("https://www.yeastgenome.org/locus/"+Parameters.getInstance().getSgdID(rowNames.get(j))));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }


    public List<String> getLipids() {
        return columnNames;
    }

}
