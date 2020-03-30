package parsers;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.*;

/**
 * Created by markusmueller on 12.03.19.
 */
public class DataMatrixTransformed extends DataMatrix {

    private static Map<String,DataMatrixTransformed> instances = new HashMap<>();

    private final String transformName;
    private final List<String> lipids;
    private final List<String> formattedLipidNames;
    private final List<String> componentNames;
    private final List<String> formulae;
    private final Map<String,String> formattedLipidNamesMap;
    private ValueType valueType;

    private DataMatrixTransformed( String transformName, List<String> componentNames, List<String> formulae,
                                   List<String> formattedLipidNames, ValueType valueType) {

        super(componentNames, DataMatrixLipids.getInstance().getRowNames());

        this.lipids = DataMatrixLipids.getInstance().getColumnNames();
        this.formattedLipidNames = formattedLipidNames;
        this.transformName = transformName;
        this.componentNames = componentNames;
        this.formulae = formulae;
        this.valueType = valueType;

        this.formattedLipidNamesMap = new HashMap<>();
        for (int i=0;i<formattedLipidNames.size();i++) {
            formattedLipidNamesMap.put(formattedLipidNames.get(i),lipids.get(i));
        }

        applyFormulas();
    }

    public List<Double> getValues(String mutant, ValueType valueType) {
        return getValues(mutant);
    }

    public Double getValue(String mutant,String columnName, ValueType valueType) {
        return getValue(mutant,columnName);
    }


    public List<String> getComponentNames() {
        return componentNames;
    }

    public String getTransformName() {
        return transformName;
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public List<String> getFormulae() {
        return formulae;
    }

    public ValueType getValueType() {
        return valueType;
    }

    private void applyFormulas() {

        DataMatrixLipids values = DataMatrixLipids.getInstance();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        for (String mutant : values.getRowNames()) {

            final Map<String,Object> realValues = new HashMap<>();

            for (String fomattedLipid : formattedLipidNames) {
                String lipid = this.formattedLipidNamesMap.get(fomattedLipid);
                realValues.put(fomattedLipid, DataMatrixLipids.getInstance().getValue(mutant, lipid, valueType));
            }

            List<Double> compValues = new ArrayList<>();
            for (String formula : formulae) {
                try {
                    compValues.add((Double)engine.eval(formula, new SimpleBindings(realValues)));
                } catch(ScriptException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            this.values.add(compValues);
        }
    }


    public static DataMatrixTransformed addInstance(String transformName, List<String> componentNames, List<String> formulae,
                                                    List<String> formattedLipidNames,ValueType valueType) {
        if (instances==null) instances = new HashMap<>();
        instances.put(transformName, new DataMatrixTransformed(transformName,componentNames,formulae,formattedLipidNames,
                    valueType));

        return instances.get(transformName);
    }

    public static DataMatrixTransformed getInstance(String transformName) {
        return instances.get(transformName);
    }

    @Override
    public String getName() {
        return transformName;
    }


}
