package actions;

import dialogues.EditFormulaDialogue;
import heatmap.TransformedValuesFrame;
import org.jfree.ui.RefineryUtilities;
import parsers.*;
import utils.ListSingleSelectionPanel;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by markusmueller on 21.05.19.
 */
public class VectorTransformAction extends AbstractAction {

    private final List<JButton> removeButtons;
    private final List<JButton> editButtons;
    private final List<JPanel> componentPanels;
    private JFrame vectorTransformFrame;
    private JTextArea nameTextArea;
    private JComboBox comboBox;
    private List<JTextArea> componentFormulas;
    private List<JTextField> componentNames;
    private boolean saved;
    private final List<String> formattedLipidNames;
    private final Map<String,String> formattedLipidNamesMap;
    private final Map<String,Object> dummyValues;

    public VectorTransformAction() {

        this.componentPanels = new ArrayList<>();
        this.removeButtons = new ArrayList<>();
        this.editButtons = new ArrayList<>();
        this.vectorTransformFrame = null;
        this.componentFormulas = new ArrayList<>();
        this.componentNames = new ArrayList<>();
        this.nameTextArea = new JTextArea("",1,20);
        this.saved = false;
        this.formattedLipidNames = formatLipidNames(DataMatrixLipids.getInstance().getColumnNames());
        this.comboBox = new JComboBox<String>(getValueTypeStrings());

        this.formattedLipidNamesMap = new HashMap<>();
        this.dummyValues = new HashMap<>();
        for (int i=0;i<formattedLipidNames.size();i++) {
            formattedLipidNamesMap.put(formattedLipidNames.get(i), DataMatrixLipids.getInstance().getColumnNames().get(i));
            dummyValues.put(formattedLipidNames.get(i),1.0);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        displayTransforms();
    }

    private void addVectorComponentPanel(String name, String formula) {

        JPanel componentPanel = new JPanel();
        componentPanel.setLayout(new BoxLayout(componentPanel, BoxLayout.X_AXIS));
        componentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextArea formulaText = new JTextArea(formula, 3,50);
        formulaText.setEditable(false);

        final JTextField compName = new JTextField(name, 20);
        compName.setEditable(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));

        JButton removeButton = new JButton("Remove");
        removeButton.setSize(10,10);
        removeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                removeVectorComponentPanel(removeButton);
                displayTransforms();
            }
        });
        removeButtons.add(removeButton);

        final JButton editButton = new JButton("Edit");
        editButton.setSize(10,10);
        editButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                final JFrame editTransformFrame = new JFrame("Edit transform");

                final EditFormulaDialogue editFormulaDialogue = new EditFormulaDialogue(formattedLipidNames,getFormula(editButton),getName(editButton));
                editTransformFrame.add(editFormulaDialogue,BorderLayout.NORTH);

                JButton cancelButton = new JButton("Cancel");
                cancelButton.setSize(10,10);
                cancelButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        editTransformFrame.setVisible(false);
                    }
                });

                JButton checkButton = new JButton("Check");
                checkButton.setSize(10,10);
                checkButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e){
                        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                        try {
                            System.out.println("result = "+engine.eval(editFormulaDialogue.getFormula(), new SimpleBindings(dummyValues)));
                            JOptionPane.showMessageDialog(vectorTransformFrame,
                                    "Formula is valid",
                                    "Formula check",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch(ScriptException ex) {
                            JOptionPane.showMessageDialog(vectorTransformFrame,
                                    ex.getMessage(),
                                    "Formula error",
                                    JOptionPane.ERROR_MESSAGE);
                            System.out.println(ex.getMessage());
                        }
                    }
                });

                JButton updateButton = new JButton("Update");
                updateButton.setSize(10,10);
                updateButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){

                        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                        try {
                            System.out.println("result = "+engine.eval(editFormulaDialogue.getFormula(), new SimpleBindings(dummyValues)));
                            editTransformFrame.setVisible(false);

                            int idx = getIndex(editButton);
                            componentFormulas.get(idx).setText(editFormulaDialogue.getFormula());
                            componentNames.get(idx).setText(editFormulaDialogue.getName());

                            displayTransforms();
                        } catch(ScriptException ex) {
                            JOptionPane.showMessageDialog(vectorTransformFrame,
                                    ex.getMessage(),
                                    "Formula error",
                                    JOptionPane.ERROR_MESSAGE);
                            System.out.println(ex.getMessage());
                        }
                    }
                });

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.add(cancelButton);
                buttonPanel.add(checkButton);
                buttonPanel.add(updateButton);

                editTransformFrame.add(buttonPanel,BorderLayout.SOUTH);

                editTransformFrame.pack();
                editTransformFrame.setVisible(true);
            }
        });
        editButtons.add(editButton);

        buttonPanel.add(removeButton);
        buttonPanel.add(editButton);

        componentPanel.add(formulaText);
        componentPanel.add(buttonPanel);

        componentPanels.add(componentPanel);
        componentFormulas.add(formulaText);
        componentNames.add(compName);
    }

    private void removeVectorComponentPanel(JButton removeButton) {

        int cnt = 0;
        for (JButton button : removeButtons) {
            if (removeButton == button) break;
            cnt++;
        }

        if (cnt>=removeButtons.size()) return;

        componentNames.remove(cnt);
        componentPanels.remove(cnt);
        componentFormulas.remove(cnt);
        editButtons.remove(cnt);
        removeButtons.remove(cnt);
    }

    private int getIndex(JButton editButton) {

        int cnt = 0;
        for (JButton button : editButtons) {
            if (editButton == button) break;
            cnt++;
        }

        if (cnt>=removeButtons.size()) return -1;

        return cnt;
    }


    private String getFormula(JButton editButton) {

        int idx = getIndex(editButton);

        if (idx<0) return "not found";

        return componentFormulas.get(idx).getText();
    }


    private String getName(JButton editButton) {

        int idx = getIndex(editButton);

        if (idx<0) return "not found";

        return componentNames.get(idx).getText();
    }



    private void displayTransforms() {

        if (vectorTransformFrame!=null) vectorTransformFrame.setVisible(false);

        vectorTransformFrame = new JFrame("Lipid Value Vector Transformation");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        JPanel namePanel = new JPanel();
        TitledBorder border = new TitledBorder("Name ");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);

        namePanel.setBorder(border);
        namePanel.add(nameTextArea);

        panel.add(namePanel);

        JPanel valueTypePanel = new JPanel();
        border = new TitledBorder("Value Type ");
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);

        valueTypePanel.setBorder(border);
        valueTypePanel.add(comboBox);

        panel.add(valueTypePanel);

        JPanel topButtonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        addButton.setSize(10,10);
        addButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                int cnt = componentNames.size()+1;
                addVectorComponentPanel("Component_"+cnt,"");
                displayTransforms();
            }
        });

        topButtonPanel.add(addButton);
        panel.add(topButtonPanel);

        JPanel vectorComponentPanel = new JPanel();
        vectorComponentPanel.setLayout(new BoxLayout(vectorComponentPanel, BoxLayout.Y_AXIS));

        for (int i=0;i<componentPanels.size();i++) {

            JPanel component =  componentPanels.get(i);
            JPanel borderedComponent = new JPanel();

            border = new TitledBorder(componentNames.get(i).getText());
            border.setTitleJustification(TitledBorder.LEFT);
            border.setTitlePosition(TitledBorder.TOP);

            borderedComponent.setBorder(border);
            borderedComponent.add(component);

            vectorComponentPanel.add(borderedComponent);
        }

        JScrollPane scrollPane = new JScrollPane(vectorComponentPanel);
        scrollPane.setPreferredSize(new Dimension(800,300));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane);

        JButton applyButton = new JButton("Apply");
        applyButton.setSize(10,10);
        applyButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                List<String> compNames = new ArrayList<>();
                List<String> formulae = new ArrayList<>();

                for (int i=0;i<componentNames.size();i++) {
                    compNames.add(componentNames.get(i).getText());
                    formulae.add(componentFormulas.get(i).getText());
                }

                DataMatrix.ValueType valueType = getValueType(comboBox.getSelectedIndex());
                DataMatrixTransformed.addInstance(nameTextArea.getText(),compNames, formulae, formattedLipidNames, valueType);

                TransformedValuesFrame transformedValuesFrame = new TransformedValuesFrame(nameTextArea.getText());
                transformedValuesFrame.setVisible(true);

                vectorTransformFrame.setVisible(false);
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.setSize(10,10);
        saveButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                VectorTransforms vectorTransforms = VectorTransforms.getInstance();
                String transformName = nameTextArea.getText().trim();
                if (!saved)
                    transformName = vectorTransforms.uniqueName(transformName);

                saved = true;

                vectorTransforms.clear(transformName);
                for (int i=0;i<componentFormulas.size();i++) {

                    String formula = componentFormulas.get(i).getText();
                    formula = formula.replaceAll("\n","#");

                    DataMatrix.ValueType valueType = getValueType(comboBox.getSelectedIndex());
                    vectorTransforms.add(transformName,componentNames.get(i).getText(),formula,valueType);
                }

                vectorTransforms.write(Parameters.getInstance().getVectorTransformFile());
            }
        });

        JButton loadButton = new JButton("Load");
        loadButton.setSize(10,10);
        loadButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                JFrame transformSelectionFrame = new JFrame("Select transform");
                transformSelectionFrame.setBounds(100, 100, 30, 50);

                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));


                final ListSingleSelectionPanel listSelectionPanel = new ListSingleSelectionPanel(VectorTransforms.getInstance().getTransformNames(),"Vector transforms");

                listPanel.add(listSelectionPanel);

                JButton showTransformButton = new JButton("Ok");
                showTransformButton.setSize(10,10);
                showTransformButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        transformSelectionFrame.setVisible(false);

                        componentPanels.clear();
                        componentFormulas.clear();
                        removeButtons.clear();
                        editButtons.clear();
                        componentNames.clear();

                        saved = true;

                        String selectedElement = listSelectionPanel.getSelectedElement();
                        List<String> formulae = VectorTransforms.getInstance().getFormulae(selectedElement);
                        List<String> names = VectorTransforms.getInstance().getComponentNames(selectedElement);
                        DataMatrix.ValueType valueType = VectorTransforms.getInstance().getValueType(selectedElement);

                        for (int i=0;i<formulae.size();i++) {

                            String formula = formulae.get(i).replaceAll("#","\n");
                            addVectorComponentPanel(names.get(i),formula);
                        }

                        nameTextArea.setText(selectedElement);
                        comboBox.setSelectedIndex(getIndex(valueType));
                        displayTransforms();
                    }
                });

                JButton cancelButton = new JButton("Cancel");
                cancelButton.setSize(10,10);
                cancelButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        transformSelectionFrame.setVisible(false);
                    }
                });

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.add(showTransformButton);
                buttonPanel.add(cancelButton);

                listPanel.add(buttonPanel);

                transformSelectionFrame.add(listPanel);

                transformSelectionFrame.pack();
                transformSelectionFrame.setVisible(true);
            }
        });

        JButton newButton = new JButton("New");
        newButton.setSize(10,10);
        newButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

                vectorTransformFrame.setVisible(false);

                componentPanels.clear();
                componentFormulas.clear();
                removeButtons.clear();
                editButtons.clear();
                componentNames.clear();

                saved = false;

                nameTextArea.setText("");

                displayTransforms();
            }
        });


        JButton cancelButton = new JButton("Cancel");
        cancelButton.setSize(10,10);
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                vectorTransformFrame.setVisible(false);                }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        vectorTransformFrame.add(panel);

        vectorTransformFrame.pack( );

        RefineryUtilities.centerFrameOnScreen( vectorTransformFrame );

        vectorTransformFrame.setVisible( true );
    }

    private List<String> formatLipidNames(List<String> elements) {

        List<String> formattedLipids = new ArrayList<>();

        for (String lipid : elements) {
            String formattedLipid = lipid.replaceAll(" ","");
            formattedLipid = formattedLipid.replaceAll("[/\\-\\:\\(\\)]","_");
            formattedLipids.add(formattedLipid);
        }

        return formattedLipids;
    }

    private String[] getValueTypeStrings() {

        String[] valueTypeStrs = new String[DataMatrix.ValueType.values().length];
        int i=0;
        for (DataMatrix.ValueType valueType : DataMatrix.ValueType.values()) {
            valueTypeStrs[i++] = DataMatrix.getValueLabel(valueType);
        }

        return valueTypeStrs;
    }

    private DataMatrix.ValueType getValueType(int index) {

        int i = 0;
        for (DataMatrix.ValueType valueType : DataMatrix.ValueType.values()) {
            if (i == index) return valueType;
            i++;
        }

        return null;
    }

    private int getIndex(DataMatrix.ValueType valueType) {

        int i = 0;
        for (DataMatrix.ValueType vt : DataMatrix.ValueType.values()) {
            if (vt == valueType) return i;
            i++;
        }

        return -1;
    }
}
