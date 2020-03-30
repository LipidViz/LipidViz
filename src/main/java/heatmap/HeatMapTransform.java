package heatmap;

import parsers.DataMatrixTransformed;
import parsers.Parameters;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;


public class HeatMapTransform extends HeatMap {

    private final String transformName;

    public HeatMapTransform(String transformName, Color[] colors, int cellWidth, int cellHeight) {

        super(colors, cellWidth, cellHeight);

        this.transformName = transformName;

        data = DataMatrixTransformed.getInstance(transformName).getValueArrays();

        this.columnNames = DataMatrixTransformed.getInstance(transformName).getComponentNames();
        this.rowNames = DataMatrixTransformed.getInstance(transformName).getRowNames();

        updateGradient(colors);
        updateData(data);

        this.setPreferredSize(new Dimension(marginLeft + marginRight + data[0].length*cellWidth,
                marginTop + marginBottom + data.length*cellHeight));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        // this is the expensive function that draws the data plot into a
        // BufferedImage. The data plot is then cheaply drawn to the screen when
        // needed, saving us a lot of time in the end.
        drawData();
    }

    public void sortMutants(List<String> mutantsInOrder) {

        Map<String,Integer> indexMap = new HashMap<>();

        for (int i=0; i<mutantsInOrder.size();i++) {
            indexMap.put(mutantsInOrder.get(i),i);
        }

        double[][] orderedData = new double[data.length][];

        for (int ix = 0; ix < data.length; ix++) {
            int newIdx = indexMap.get(rowNames.get(ix));

            orderedData[newIdx] = data[ix];
        }

        data = orderedData;
        rowNames = mutantsInOrder;

        updateDataColors();
        drawData();
        repaint();
    }


    protected void drawColumnInfo(Graphics2D g2d, int i) {

        g2d.setColor(new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(),220));
        String str = columnNames.get(i);
        g2d.fillRect(cursorX+8, cursorY+12, str.length()*15, 25 );
        Font f = new Font ("Monospaced", Font.BOLD , 20);
        g2d.setFont (f);
        g2d.setColor(Color.blue);
        g2d.drawString(str, cursorX+16, cursorY+30);
    }


    protected void drawRowInfo(Graphics2D g2d, int j) {

        g2d.setColor(new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(),220));
        String str = rowNames.get(j)+", "+ Parameters.getInstance().getDescription(rowNames.get(j));
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
        JFrame webLinksFrame = new JFrame(columnNames.get(i)+" formula");
        webLinksFrame.setBounds(100, 100, 700, 800);

        JTextArea textArea = new JTextArea(DataMatrixTransformed.getInstance(transformName).getFormulae().get(i));
        textArea.setEditable(false);

        webLinksFrame.add(textArea);

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


}
