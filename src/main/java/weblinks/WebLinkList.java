package weblinks;

import parsers.Parameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by markusmueller on 19.03.19.
 */
public class WebLinkList extends JPanel {

    private final List<Map<String, String>> extendedInfoMap;
    private final String chebiAddress = "https://www.ebi.ac.uk/chebi/searchId.do?chebiId=";
    private final String swiliAddress = "http://www.swisslipids.org/#/entity/";

    public WebLinkList(List<Map<String, String>> extendedInfoMap){
        this.extendedInfoMap = extendedInfoMap;

        createFrame();
    }

    private void createFrame() {

        if (extendedInfoMap==null) return;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (Map<String, String> map : extendedInfoMap) {
            if (!map.get("CheBI_ID").isEmpty()) {

                JLabel hyperlink = new JLabel("Chebi: "+map.get("CheBI Name"));
                hyperlink.setForeground(Color.BLUE.darker());
                hyperlink.setBackground(Color.WHITE);
                hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                hyperlink.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI(chebiAddress+map.get("CheBI_ID")));
                        } catch (IOException | URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                        // the user clicks on the label
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        Font font = hyperlink.getFont();
                        Map attributes = font.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        hyperlink.setFont(font.deriveFont(attributes));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        Font font = hyperlink.getFont();
                        Map attributes = font.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, -1);
                        hyperlink.setFont(font.deriveFont(attributes));
                    }
                });

                add(hyperlink);
            }
            if (!map.get("SLM_ID").isEmpty()) {

                JLabel hyperlink = new JLabel("SwissLipids: "+map.get("Name"));
                hyperlink.setForeground(Color.BLUE.darker());
                hyperlink.setBackground(Color.WHITE);
                hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                hyperlink.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI(swiliAddress+map.get("SLM_ID")+"/"));
                        } catch (IOException | URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                        // the user clicks on the label
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        Font font = hyperlink.getFont();
                        Map attributes = font.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        hyperlink.setFont(font.deriveFont(attributes));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        Font font = hyperlink.getFont();
                        Map attributes = font.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, -1);
                        hyperlink.setFont(font.deriveFont(attributes));
                    }
                });

                add(hyperlink);
            }
        }

    }

}
