package heatmap;

import parsers.Parameters;

import java.awt.*;

/**
 * Created by markusmueller on 26.02.19.
 */
public class StartLipidViz {

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            Parameters.getInstance(args[0]);
            LipidVizMainFrame app = new LipidVizMainFrame();
            app.setVisible(true);
        });
    }
}
