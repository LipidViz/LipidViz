package parsers;

import java.io.*;
import java.util.*;

/**
 * Created by markusmueller on 21.05.19.
 */
public class VectorTransforms {

    private static VectorTransforms instance = null;

    private final Map<String,List<String>> transforms;
    private final Map<String,List<String>> names;
    private final Map<String,List<DataMatrix.ValueType>> valueTypes;

    public VectorTransforms() {
        this.transforms = new HashMap<>();
        this.names = new HashMap<>();
        this.valueTypes = new HashMap<>();
    }

    public String uniqueName(String name) {
        String newName = name;
        int cnt = 1;
        while (transforms.containsKey(newName)) {
            newName = name+"("+cnt+")";
            cnt++;
        }

        return newName;
    }

    public Set<String> getTransformNames() {
        return transforms.keySet();
    }

    public void clear(String transformName) {

        if (transforms.containsKey(transformName)) {
            transforms.get(transformName).clear();
            names.get(transformName).clear();
            valueTypes.get(transformName).clear();
        }
    }

    public void add(String transformName, String componentName, String formula, DataMatrix.ValueType valueType) {

        if (!transforms.containsKey(transformName)) {
            transforms.put(transformName,new ArrayList<>());
            names.put(transformName,new ArrayList<>());
            valueTypes.put(transformName,new ArrayList<>());
        }

        transforms.get(transformName).add(formula);
        names.get(transformName).add(componentName);
        valueTypes.get(transformName).add(valueType);
    }

    public Map<String, List<String>> getTransforms() {
        return transforms;
    }

    public List<String> getFormulae(String transformName) {

        if (!transforms.containsKey(transformName)) {
            return new ArrayList<>();
        }

        return transforms.get(transformName);
    }

    public List<String> getComponentNames(String transformName) {

        if (!names.containsKey(transformName)) {
            return new ArrayList<>();
        }

        return names.get(transformName);
    }

    public DataMatrix.ValueType getValueType(String transformName) {

        if (!names.containsKey(transformName)) {
            return null;
        }

        return this.valueTypes.get(transformName).get(0);
    }

    private void parse(File vectorTransformsFile) {

        String line = null;

        if (!vectorTransformsFile.exists()) return;

        try {
            // header:
            BufferedReader br = new BufferedReader(new FileReader(vectorTransformsFile));

            while ((line = br.readLine()) != null) {

                if (line.isEmpty()) continue;

                String[] fields = line.split("\t",-1);

                if (!transforms.containsKey(fields[0])) transforms.put(fields[0],new ArrayList<>());
                if (!names.containsKey(fields[0])) names.put(fields[0],new ArrayList<>());
                if (!valueTypes.containsKey(fields[0])) valueTypes.put(fields[0],new ArrayList<>());
                transforms.get(fields[0]).add(fields[2]);
                names.get(fields[0]).add(fields[1]);
                valueTypes.get(fields[0]).add(DataMatrix.ValueType.valueOf(fields[3]));
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(File vectorTransformsFile) {

        String line = null;

        try {
            // header:
            BufferedWriter bw = new BufferedWriter(new FileWriter(vectorTransformsFile));

            for (String transformName : transforms.keySet()) {
                for (int i=0;i<transforms.get(transformName).size();i++) {

                    bw.write(transformName+"\t"+names.get(transformName).get(i)+"\t"+transforms.get(transformName).get(i)+"\t"+
                            valueTypes.get(transformName).get(i).name()+"\n");
                }
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VectorTransforms makeInstance(File vectorTransformsFile) {
        if (instance==null) {
            instance = new VectorTransforms();
            instance.parse(vectorTransformsFile);
        }

        return instance;
    }

    public static VectorTransforms getInstance() {
        return instance;
    }
}
