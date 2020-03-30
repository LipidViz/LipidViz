package parsers;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.*;

/**
 * Created by markusmueller on 12.03.19.
 */
public class DataMatrixCorrelation extends DataMatrix {

    public DataMatrixCorrelation(double[][] data, List<String> rowNames) {

        super(rowNames, rowNames);

        for (int i=0;i<data.length;i++) {
            List<Double> v = new ArrayList<>();
            for (int j=0;j<data[0].length;j++) {
                v.add(data[i][j]);
            }
            values.add(v);
        }
    }

    public List<Double> getValues(String mutant, ValueType valueType) {
        return getValues(mutant);
    }

    public Double getValue(String mutant,String columnName, ValueType valueType) {
        return getValue(mutant,columnName);
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    @Override
    public String getName() {
        return "Correlation matrix";
    }

}
