package heatmap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import parsers.DataMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;


public abstract class HeatMap extends JPanel implements MouseListener, MouseMotionListener {

    protected DataMatrix.ValueType valueType;

    protected static final long serialVersionUID = 1L;
    protected double[][] data;
    protected int[][] dataColorIndices;

    protected boolean drawInfo = false;

    protected int cursorX;
    protected int cursorY;
    protected float zoomFactor = 1;

    protected final int cellWidth;
    protected final int cellHeight;

    protected int marginTop;
    protected int marginBottom;
    protected int marginLeft;
    protected int marginRight;

    protected double neutralValue = 0.0;
    protected boolean dimAroundNeutral = true;


    protected List<String> columnNames;
    protected List<String> rowNames;
    protected Set<String> markedMutants;

    protected Color[] colors;
    protected Font currentAxisFont;

    protected BufferedImage bufferedImage;
    protected Graphics2D bufferedGraphics;

    protected boolean mouseClicked;
    protected boolean mouseMoved;

    public HeatMap(Color[] colors, int cellWidth, int cellHeight) {
        super();

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        this.colors = colors;

        this.columnNames = null;
        this.rowNames = null;
        this.markedMutants = new HashSet<>();

        currentAxisFont = new Font ("Monospaced", Font.BOLD ,15);

        this.marginTop = 150;
        this.marginBottom = 150;
        this.marginLeft = 150;
        this.marginRight = 150;

        this.setDoubleBuffered(true);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.mouseClicked = false;
        this.mouseMoved = false;
    }

    public int getCellWidth() {
        return cellWidth;
    }
    public int getCellHeight() { return cellHeight; }

    public void zoomIn() {

        float f = zoomFactor*2;

        if (f*cellWidth>=1 && f*cellWidth<=32 && f*cellHeight>=1 || f*cellHeight<=32) {
            this.zoomFactor = f;

            int w = (int)(zoomFactor*cellWidth*data[0].length+marginLeft+marginRight);
            int h = (int)(zoomFactor*cellHeight* rowNames.size()+marginTop+marginBottom);
            setPreferredSize(new Dimension(2*w, 2*h));

            drawData();
            repaint();
        }
    }

    public void zoomOut() {

        float f = zoomFactor/2;

        if (f*cellWidth>=1 && f*cellWidth<=32 && f*cellHeight>=1 || f*cellHeight<=32) {
            this.zoomFactor = f;

            drawData();
            repaint();
        }
    }

    public DataMatrix.ValueType getValueType() {
        return valueType;
    }

    public void setValueType(DataMatrix.ValueType valueType) {
        this.valueType = valueType;
    }

    public abstract void sortMutants(List<String> mutantsInOrder);

    public void markMutants(List<String> markedMutants) {
        this.markedMutants.clear();
        this.markedMutants.addAll(markedMutants);

        repaint();
    }

    /**
     * Updates the gradient used to display the data. Calls drawData() and
     * repaint() when finished.
     *
     * @param colors
     *            A variable of type Color[]
     */
    public void updateGradient(Color[] colors) {
        this.colors = (Color[]) colors.clone();

        if (data != null) {
            updateDataColors();
            drawData();
            repaint();
        }
    }

    protected abstract void drawDataCellInfo(Graphics2D g2d, int i, int j);
    protected abstract void drawRowInfo(Graphics2D g2d, int j);
    protected abstract void drawColumnInfo(Graphics2D g2d, int i);
    protected abstract void openColumnWebLink(int i);
    protected abstract void openRowWebLink(int j);



        /**
         * This uses the current array of colors that make up the gradient, and
         * assigns a color index to each data point, stored in the dataColorIndices
         * array, which is used by the drawData() method to plot the points.
         */
    protected void updateDataColors() {
        // We need to find the range of the data values,
        // in order to assign proper colors.
        dataColorIndices = new int[data.length][data[0].length];

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {
                statistics.addValue(data[x][y]);
            }
        }

        addColorGradient(statistics,neutralValue);
    }


    protected void addColorGradient(DescriptiveStatistics statistics, double neutralValue) {

        double[] dValues = new double[colors.length];
        for (int i=0;i<colors.length;i++) {
            dValues[i] = statistics.getPercentile((100.0*i)/colors.length+0.000001);
        }

        if (dimAroundNeutral)
            setColorSaturation(statistics, dValues, neutralValue);

        // assign a Color to each data point
        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {

                double value = data[x][y];
                dataColorIndices[x][y] = Math.min(colors.length-1,getPercentileIdx(dValues,value));
            }
        }
    }

    protected void setColorSaturation(DescriptiveStatistics statistics,double[] dValue, double neutralValue) {

        double var = statistics.getVariance();
        if (var<0.000001) return;

        for (int i=0;i<dValue.length-1;i++) {
            double m = 0.5*(dValue[i+1]+dValue[i]);
            double dv = m-neutralValue;
            dv = -dv*dv/var;
            float alpha = (float)((Math.abs(dv)>5)?0.0:Math.exp(dv));
            alpha = 1.0f-0.85f*alpha;

            colors[i] = new Color(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue(),(int)(alpha*255));
        }
    }

    protected int getPercentileIdx(double[] dValue, double v) {

        for (int i=0;i<dValue.length;i++) {
            if (v<=dValue[i]) return i;
        }

        return dValue.length-1;
    }

    /**
     * Updates the data display, calls drawData() to do the expensive re-drawing
     * of the data plot, and then calls repaint().
     *
     * @param data
     *            The data to display, must be a complete array (non-ragged)
     */
    public void updateData(double[][] data) {
        this.data = new double[data.length][data[0].length];
        for (int ix = 0; ix < data.length; ix++) {
            for (int iy = 0; iy < data[0].length; iy++) {
                // we use the graphics Y-axis internally
                this.data[ix][iy] = data[ix][iy];
            }
        }

        updateDataColors();

    }

    /**
     * Creates a BufferedImage of the actual data plot.
     *
     * After doing some profiling, it was discovered that 90% of the drawing
     * time was spend drawing the actual data (not on the axes or tick marks).
     * Since the Graphics2D has a drawImage method that can do scaling, we are
     * using that instead of scaling it ourselves. We only need to draw the data
     * into the bufferedImage on startup, or if the data or gradient changes.
     * This saves us an enormous amount of time. Thanks to Josh Hayes-Sheen
     * (grey@grevian.org) for the suggestion and initial code to use the
     * BufferedImage technique.
     *
     * Since the scaling of the data plot will be handled by the drawImage in
     * paintComponent, we take the easy way out and draw our bufferedImage with
     * 1 pixel per data point. Too bad there isn't a setPixel method in the
     * Graphics2D class, it seems a bit silly to fill a rectangle just to set a
     * single pixel...
     *
     * This function should be called whenever the data or the gradient changes.
     */
    protected void drawData() {

        int width = (int)Math.round(this.cellWidth*zoomFactor);
        int height = (int)Math.round(this.cellHeight*zoomFactor);

        bufferedImage = new BufferedImage(data[0].length*width, data.length* height,
                BufferedImage.TYPE_INT_ARGB);
        bufferedGraphics = bufferedImage.createGraphics();

        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[0].length; x++) {
                bufferedGraphics.setColor(colors[dataColorIndices[y][x]]);
                bufferedGraphics.fillRect(x*width, y* height, width, height);
            }
        }
    }

    /**
     * The overridden painting method, now optimized to simply draw the data
     * plot to the screen, letting the drawImage method do the resizing. This
     * saves an extreme amount of time.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = (int) Math.round(this.getWidth()*zoomFactor);
        int height = (int) Math.round(this.getHeight()*zoomFactor);

        this.setOpaque(true);

        // clear the panel
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // draw the heat map
        if (bufferedImage == null) {
            // Ideally, we only to call drawData in the constructor, or if we
            // change the data or gradients. We include this just to be safe.
            updateDataColors();
            drawData();
        }

        int ml = (int) Math.round(marginLeft*zoomFactor);
        int mr = (int) Math.round(marginRight*zoomFactor);
        int mt = (int) Math.round(marginTop*zoomFactor);
        int mb = (int) Math.round(marginBottom*zoomFactor);

        // The data plot itself is drawn with 1 pixel per data point, and the
        // drawImage method scales that up to fit our current window size. This
        // is very fast, and is much faster than the previous version, which
        // redrew the data plot each time we had to repaint the screen.
        g2d.drawImage(bufferedImage, ml, mt, width - ml - mr, height - mt - mb,this);

        // border
        g2d.setColor(Color.darkGray);
        g2d.drawRect(ml, mt, width - ml - mr, height - mt - mb);

         // axis ticks - ticks start even with the bottom left coner, end very
        // close to end of line (might not be right on)
        int numYTicks = rowNames.size();
        int numXTicks = data[0].length;

        if (drawInfo) {
            drawInfo(g2d);
        }

        currentAxisFont = new Font ("Monospaced", Font.BOLD , (int)(15 * zoomFactor));
        g2d.setFont(currentAxisFont);
        g2d.setColor(Color.darkGray);

        int offset1 = (int) Math.round(4*zoomFactor);
        int offset2 = (int) Math.round(100*zoomFactor);
        int offset3 = (int) Math.round(150*zoomFactor);

        // Y-Axis ticks
        double yDist = (height - mt - mb) / (double) numYTicks; // distance
        int r = (int) Math.round(yDist);


        for (int y = 0; y < numYTicks; y++) {
            int yCoord = (int)Math.round(mt + yDist/2 + y * yDist);
            g2d.drawLine(ml-offset1, yCoord, ml, yCoord);
            // to get the text to fit nicely, we need to rotate the graphics
            g2d.drawString(rowNames.get(y), ml-offset2, yCoord+offset1);

            if (markedMutants.contains(rowNames.get(y))) {
                g2d.setColor(Color.RED);
                g2d.fillOval(ml-offset3, yCoord-r/2, r, r);
                g2d.setColor(Color.darkGray);
            }
        }

        offset2 = (int) Math.round(6*zoomFactor);
        double xDist = (width - ml - mr) / (double) numXTicks; // distance
        for (int x = 0; x < numXTicks; x++) {
            int xCoord = (int)Math.round(ml + xDist/2 + x * xDist);
            g2d.drawLine(xCoord,height-mb+offset1,  xCoord, height-mb);
            // to get the text to fit nicely, we need to rotate the graphics
            g2d.rotate(Math.toRadians(90), ml, height-mb);
            g2d.drawString(columnNames.get(x), ml+offset2,height-mb+ml-xCoord+offset1);
            g2d.rotate(Math.toRadians(-90), ml, height-mb);
        }

    }

    protected void drawInfo(Graphics2D g2d) {

        int i = getXCursorIndex();
        int j = getYCursorIndex();

        if (i>=0 && i<data[0].length && j>=0 && j< rowNames.size()) {
            drawDataCellInfo(g2d, i, j);
        } else if (i<0 && j>=0 && j< rowNames.size()) {
            drawRowInfo(g2d, j);
        } else if (i>=0 && i<data[0].length && j>= rowNames.size()){
            drawColumnInfo(g2d, i);
        }
    }


    protected int getXCursorIndex() {
        int width = (int) (this.getWidth()*zoomFactor);

        int ml = (int) (marginLeft*zoomFactor);
        int mr = (int) (marginRight*zoomFactor);

        double cellWidth = 1.0*(width-ml-mr)/data[0].length;

        return  (int) Math.floor((cursorX-ml)/cellWidth);
    }

    protected int getYCursorIndex() {
        int height = (int) (this.getHeight()*zoomFactor);

        int mt = (int) (marginTop*zoomFactor);
        int mb = (int) (marginBottom*zoomFactor);

        double cellHeight = 1.0*(height-mt-mb)/ rowNames.size();

        return  (int) Math.floor((cursorY-mt)/cellHeight);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("Mouse clicked: x = "+e.getX()+", y = "+e.getY());

        if (!mouseClicked) {
            openWebLink();
            mouseClicked = true;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        drawInfo = true;
        cursorX = e.getX();
        cursorY = e.getY();

        repaint();

//        System.out.println("Mouse moved: x = "+e.getX()+", y = "+e.getY());
    }

    protected void openWebLink() {

        int i = getXCursorIndex();
        int j = getYCursorIndex();

        if (i<0 && j>=0 && j< rowNames.size()) {
            openRowWebLink(j);
        } else if (i>=0 && i<data[0].length && j>=rowNames.size()){
            openColumnWebLink(i);
        }
    }


    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        mouseClicked = false;
//        System.out.println("Mouse released: x = "+e.getX()+", y = "+e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

//        System.out.println("Mouse entered: x = "+e.getX()+", y = "+e.getY());
    }

    @Override
    public void mouseExited(MouseEvent e) {

//        System.out.println("Mouse exited: x = "+e.getX()+", y = "+e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {

//        System.out.println("Mouse dragged: x = "+e.getX()+", y = "+e.getY());
    }


    public double[][] getData() {
        return data;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getRowNames() {
        return rowNames;
    }



    public void writeToFile(File reportFile) {
        String line = null;

        try {
            // header:
            BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile));

            bw.write("Mutant");
            for (String columnName : columnNames) {
                bw.write("\t"+columnName);
            }
            bw.write("\n");

            for (int i = 0; i< rowNames.size(); i++) {
                bw.write(rowNames.get(i));

                for (int j=0;j<columnNames.size();j++) {
                    bw.write("\t"+String.format("%.5f",data[i][j]));
                }

                bw.write("\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
