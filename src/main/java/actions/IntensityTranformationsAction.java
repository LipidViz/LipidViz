package actions;

import heatmap.HeatMap;
import heatmap.HeatMapLipids;
import parsers.DataMatrix;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by markusmueller on 21.05.19.
 */
public class IntensityTranformationsAction extends AbstractAction {

    private final HeatMap heatMap;
    private final DataMatrix.ValueType valueType;

    public IntensityTranformationsAction(HeatMapLipids heatMap, DataMatrix.ValueType valueType) {

        this.heatMap = heatMap;
        this.valueType = valueType;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if (heatMap==null) return;

        heatMap.setValueType(valueType);

    }
}

