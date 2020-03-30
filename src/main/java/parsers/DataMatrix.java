package parsers;

import java.util.*;

/**
 * Created by markusmueller on 04.06.19.
 */
public abstract class DataMatrix {

    public enum ValueType {INT,LOG_INT,DIFF,ZSCORE,LOG_INT_STD};

    protected final List<String> columnNames;
    protected final List<String> rowNames;
    protected final Map<String,Integer> columnMap;
    protected final Map<String,Integer> rowMap;
    protected final List<List<Double>> values;


    protected DataMatrix(List<String> columnNames, List<String> rowNames) {

        this.columnNames = columnNames;
        this.rowNames = rowNames;

        this.columnMap = new HashMap<>();
        for (int i = 0; i< columnNames.size(); i++) {
            columnMap.put(columnNames.get(i),i);
        }

        this.rowMap = new HashMap<>();
        for (int i = 0; i< rowNames.size(); i++) {
            rowMap.put(rowNames.get(i),i);
        }

        this.values = new ArrayList<>();

    }

    public List<Double> getValues(String mutant) {
        int idx = rowMap.get(mutant);

        return Collections.unmodifiableList(values.get(idx));
    }

    public Double getValue(String mutant, String columnName) {

        int idxL = columnMap.get(columnName);
        int idxM = rowMap.get(mutant);

        return values.get(idxM).get(idxL);
    }

    public double[][] getValueArrays() {

        return convertToArray(values);
    }

    public abstract List<Double> getValues(String mutant, ValueType valueType);
    public abstract Double getValue(String mutant,String columnName, ValueType valueType);

    public abstract String getName();

    public static String getValueLabel(ValueType type) {
        if (type==ValueType.INT) return "Intensity";
        else if (type==ValueType.LOG_INT) return "log10(Intensity)";
        else if (type==ValueType.DIFF) return "Difference to WT";
        else if (type==ValueType.ZSCORE) return "ZScore";
        else if (type==ValueType.LOG_INT_STD) return "std(log10(Intensity))";
        else return "";
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public int getNumberCols() {

        return columnNames.size();
    }

    public int getNumberRows() {

        return rowNames.size();
    }

    public List<List<Double>> getValues() {
        return values;
    }

    // rows = rowNames
    // columns = columnNames
    protected double[][] convertToArray(List<List<Double>> values) {

        int l = values.size();
        int m = values.get(0).size();
        double[][] matrix = new double[l][m];

        for (int i=0;i<l;i++) {
            List<Double> dList = values.get(i);
            for (int j=0;j<m;j++) {
                matrix[i][j] = dList.get(j);
            }
        }

        return matrix;
    }


}
