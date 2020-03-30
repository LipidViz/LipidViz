package heatmap;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by markusmueller on 05.02.19.
 */
public class ColorMap {

    public static Color[] getRainBow(int nr) {

        nr = (nr-1)/6;
        List<Color> colors = new ArrayList<Color>();
        for (int r=0; r<nr; r++) colors.add(new Color((r*255)/nr,       255,         0));
        for (int g=nr; g>0; g--) colors.add(new Color(      255, (g*255)/nr,         0));
        for (int b=0; b<nr; b++) colors.add(new Color(      255,         0, (b*255)/nr));
        for (int r=nr; r>0; r--) colors.add(new Color((r*255)/nr,         0,       255));
        for (int g=0; g<nr; g++) colors.add(new Color(        0, (g*255)/nr,       255));
        for (int b=nr; b>0; b--) colors.add(new Color(        0,       255, (b*255)/nr));
        colors.add(new Color(        0,       255,         0));

        Color[] colArray = new Color[colors.size()];
        colors.toArray(colArray);

        return colArray;
    }

    public static Color[] getGrays(int nr) {

        List<Color> colors = new ArrayList<Color>();
        for (int i=0;i<nr;i++) {
            int c = (255*i)/nr;
            colors.add(new Color(c,c,c));
        }

        Color[] colArray = new Color[colors.size()];
        colors.toArray(colArray);

        return colArray;
    }

    public static Color[] getViridis(int nr) {
        return getColors("viridis.cmap", nr);
    }

    public static Color[] getMagma(int nr) {
        return getColors("magma.cmap", nr);
    }

    public static Color[] getPlasma(int nr) {
        return getColors("plasma.cmap", nr);
    }

    public static Color[] getInferno(int nr) {
        return getColors("inferno.cmap", nr);
    }


    public static Color[] getColors(String colorMap, int nr) {

        List<Color> colors = new ArrayList<Color>();

        ClassPathResource classPathResource = new ClassPathResource(colorMap, ColorMap.class);

        try {
            String[] cmapStr = IOUtils.toString(classPathResource.getInputStream()).split("\n");

            for (int i=0;i<cmapStr.length;i++) {

                String[] fields = cmapStr[i].split(" ");
                float red = Float.valueOf(fields[0]);
                float green = Float.valueOf(fields[1]);
                float blue = Float.valueOf(fields[2]);
                colors.add(new Color(red, green, blue));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        colors = sample(colors, nr);

        Color[] colArray = new Color[colors.size()];
        colors.toArray(colArray);

        return colArray;
    }

    private static List<Color> sample(List<Color> colors, int nr) {

        List<Color> samples;

        if (nr < colors.size()) {

            samples = new ArrayList<>();

            double step = 1.0*colors.size()/nr;

            for (int i=0;i<nr;i++) {

                int j = (int) Math.round(i*step);
                samples.add(colors.get(j));
            }

        } else if (nr > colors.size()) {

            samples = new ArrayList<>();

            double step = 1.0*colors.size()/nr;

            for (int i=0;i<nr;i++) {

                int j = (int) Math.round(i*step);
                j = (j>=colors.size())?colors.size()-1:j;
                samples.add(colors.get(j));
            }

        } else {
            samples = colors;
        }

        return samples;
    }

}
