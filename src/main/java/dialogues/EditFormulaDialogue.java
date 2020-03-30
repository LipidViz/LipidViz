package dialogues;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by markusmueller on 28.05.19.
 */
public class EditFormulaDialogue extends JPanel {
    private JTextField nameText;
    private JTextArea formulaText;
    private final JList<String> jList;
    private final List<String> lipids;

    public EditFormulaDialogue(List<String> lipids, String formula, String name) {

        this.lipids = lipids;
        this.jList = new JList<>();
        this.formulaText = new JTextArea(formula,3, 50);
        this.nameText = new JTextField(name,20);

        init();
    }


    private void init() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setSize(600,400);

        add(nameText);

        JPanel formulaPanel = new JPanel();
        formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.X_AXIS));

        DefaultListModel<String> listModel = new DefaultListModel();
        for (String element : lipids) {
            listModel.addElement(element);
        }

        jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jList.setFont(new Font("Tahoma", Font.PLAIN, 12));
        jList.setVisibleRowCount(lipids.size());
        jList.setModel(listModel);

        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setPreferredSize(new Dimension(150, 200));
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();

        TitledBorder border = new TitledBorder("Lipids ");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);

        listPane.setBorder(border);
        listPane.add(scrollPane);

        formulaPanel.add(listPane);

        border = new TitledBorder("Math signs ");
        border.setTitleJustification(TitledBorder.TOP);
        border.setTitlePosition(TitledBorder.TOP);

        JPanel signPanel = new JPanel();
        signPanel.setLayout(new BoxLayout(signPanel, BoxLayout.Y_AXIS));
        signPanel.setSize(50,300);
        signPanel.setBorder(border);

        JButton plusButton = new JButton("+");
        plusButton.setSize(10,10);
        plusButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                List<String> selectedLipids = jList.getSelectedValuesList();
                String formulaStr = formulaText.getText();
                formulaStr = applySign(formulaStr,selectedLipids,"+");
                formulaText.append(formulaStr);
                formulaText.update(formulaText.getGraphics());
            }
        });

        JButton minusButton = new JButton("-");
        minusButton.setSize(10,10);
        minusButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                List<String> selectedLipids = jList.getSelectedValuesList();
                String formulaStr = formulaText.getText();
                formulaStr = applySign(formulaStr,selectedLipids,"-");
                formulaText.append(formulaStr);
                formulaText.update(formulaText.getGraphics());
            }
        });

        JButton multButton = new JButton("*");
        multButton.setSize(10,10);
        multButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                List<String> selectedLipids = jList.getSelectedValuesList();
                String formulaStr = formulaText.getText();
                formulaStr = applySign(formulaStr,selectedLipids,"*");
                formulaText.append(formulaStr);
                formulaText.update(formulaText.getGraphics());
            }
        });

        JButton divButton = new JButton("/");
        divButton.setSize(10,10);
        divButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                List<String> selectedLipids = jList.getSelectedValuesList();
                String formulaStr = formulaText.getText();
                formulaStr = applySign(formulaStr,selectedLipids,"/");
                formulaText.append(formulaStr);
                formulaText.update(formulaText.getGraphics());
            }
        });

        JButton openPButton = new JButton("(");
        openPButton.setSize(10,10);
        openPButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                formulaText.insert("(", formulaText.getCaretPosition());
                formulaText.update(formulaText.getGraphics());
            }
        });

        JButton closePButton = new JButton(")");
        closePButton.setSize(10,10);
        closePButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                formulaText.insert(")", formulaText.getCaretPosition());
                formulaText.update(formulaText.getGraphics());
            }
        });

        signPanel.add(plusButton);
        signPanel.add(minusButton);
        signPanel.add(multButton);
        signPanel.add(divButton);
        signPanel.add(openPButton);
        signPanel.add(closePButton);

        formulaPanel.add(signPanel);

        border = new TitledBorder("Formula ");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);

        formulaText.setBorder(border);

        formulaPanel.add(formulaText);

        add(formulaPanel);

    }

    private String applySign(String currText, List<String> selLipids, String sign) {
        String res = "";
        for (String lipid : selLipids) {
         res += (res.isEmpty()&&(currText.isEmpty()||currText.endsWith("(")))?lipid:sign+lipid;
        }

        if (res.isEmpty()) res = sign;

        return res;
    }

    public String getFormula() {
        return formulaText.getText();
    }

    public String getName() {
        return nameText.getText();
    }

}
