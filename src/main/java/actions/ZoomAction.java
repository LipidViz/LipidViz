package actions;

import heatmap.HeatMap;
import heatmap.HeatMapLipids;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by markusmueller on 21.05.19.
 */
public class ZoomAction extends AbstractAction {

    private final HeatMap heatMap;
    private final boolean in;

    public ZoomAction(HeatMap heatMap, boolean in) {

        this.heatMap = heatMap;
        this.in = in;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        if (in) {
            heatMap.zoomIn();
        } else {
            heatMap.zoomOut();
        }

    }
}
