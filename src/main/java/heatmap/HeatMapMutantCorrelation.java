package heatmap;

import parsers.DataMatrix;
import parsers.DataMatrixLipids;
import parsers.Parameters;
import weblinks.WebLinkList;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class HeatMapMutantCorrelation extends HeatMap {

    public HeatMapMutantCorrelation(double[][] data, List<String> names, Color[] colors, int cellWidth, int cellHeight) {

        super(colors, cellWidth, cellHeight);

        this.data = data;

        this.columnNames = names;
        this.rowNames = names;

        this.neutralValue = 0.0;
        this.dimAroundNeutral = true;

        updateData(this.data);
        drawData();
        repaint();

        currentAxisFont = new Font ("Monospaced", Font.BOLD ,15);

        this.setPreferredSize(new Dimension(marginLeft + marginRight + data[0].length*cellWidth,
                marginTop + marginBottom + data.length*cellHeight));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

    }

    public void sortMutants(List<String> mutantsInOrder) {
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

}
