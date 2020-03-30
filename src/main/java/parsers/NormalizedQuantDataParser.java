package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by markusmueller on 29.01.19.
 */
public class NormalizedQuantDataParser {

    private class LipidSubClassComparator implements Comparator<Integer> {

        private final Map<Integer,Integer> indexMap;

        public LipidSubClassComparator() {
            this.indexMap = calcSortedIndices();
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            int res = indexMap.get(index1).compareTo(indexMap.get(index2));

            if (res==0) {
                return lipids.get(index1).compareTo(lipids.get(index2));
            } else
                return res;
        }

        private int getLipidSubClassIndex(String[] lipidSubClassOrder, String lipidSubClass) {

            int i;
            for (i=0; i<lipidSubClassOrder.length; i++) {
                if (lipidSubClass.equals(lipidSubClassOrder[i])) {
                    return i;
                }
            }
            return -1;
        }

        private Map<Integer,Integer> calcSortedIndices() {
            String[] lipidSubClassOrder = Parameters.getInstance().getLipidClassOrder();

            Map<Integer,Integer> indexMap = new HashMap<>();
            for (int i=0;i<lipids.size();i++) {
                String subClass = Parameters.getInstance().getSubClass(lipids.get(i));
                indexMap.put(i,getLipidSubClassIndex(lipidSubClassOrder,subClass));
            }

            return indexMap;
        }
    }

    private final File dataFile;
    private List<String> lipids;
    private List<List<Double>> values;
    private List<List<Double>> refValues;
    private List<String> mutants;
    private List<String> refMutants;
    private List<String> series;
    private List<String> wts;
    private List<String> batches;

    public NormalizedQuantDataParser(File dataFile) {

        this.dataFile = dataFile;
        this.lipids = new ArrayList<>();
        this.values = new ArrayList<>();
        this.refValues = new ArrayList<>();
        this.mutants = new ArrayList<>();
        this.refMutants = new ArrayList<>();
        this.series = new ArrayList<>();
        this.wts = new ArrayList<>();
        this.batches = new ArrayList<>();
    }

    public void parse() {

        String line = null;
        int offset = 7;

        try {
            // header:
            BufferedReader br = new BufferedReader(new FileReader(dataFile));

            parseHeader(br.readLine(),offset, mutants);
            parseHeader(br.readLine(),offset, series);
            parseHeader(br.readLine(),offset, wts);
            parseHeader(br.readLine(),offset, batches);

            Parameters parameters = Parameters.getInstance();

            while ((line = br.readLine()) != null) {

                String[] fields = line.split(",");

                if (parameters.excludeLipid(fields[0])) continue;

                lipids.add(fields[0]);

                List<Double> mutantValues = new ArrayList<>();
                for (int i=offset; i<fields.length; i++) {
                    mutantValues.add(Double.parseDouble(fields[i]));
                }

                values.add(mutantValues);
            }

            br.close();

            excludeMutants();

        } catch (IOException e) {
            e.printStackTrace();
        }

        sortLipids();
    }

    private void parseHeader(String line, int offset, List<String> headers) {
        String[] fields = line.split(",");

        for (int i=offset;i<fields.length;i++) headers.add(fields[i]);
    }

    public int getNumberCols() {

        return values.get(0).size();
    }

    public int getNumberRows() {

        return values.size();
    }


    public void excludeMutants() {

        List<List<Double>> filteredValues = new ArrayList<>();
        for (int j=0;j<lipids.size();j++) {
            filteredValues.add(new ArrayList<>());
            refValues.add(new ArrayList<>());
        }
        List<String> filteredMutants = new ArrayList<>();
        List<String> filteredSeries = new ArrayList<>();

        Parameters parameters = Parameters.getInstance();

        for (int i=0;i<mutants.size();i++) {

            if (parameters.isReferenceMutant(mutants.get(i))) {
                refMutants.add(mutants.get(i));
                for (int j=0;j<lipids.size();j++) {
                    refValues.get(j).add(values.get(j).get(i));
                }
            }

            if (parameters.excludeMutant(mutants.get(i))) continue;

            filteredMutants.add(mutants.get(i));
            filteredSeries.add(series.get(i));

            for (int j=0;j<lipids.size();j++) {
                filteredValues.get(j).add(values.get(j).get(i));
            }
        }

        mutants = filteredMutants;
        series = filteredSeries;
        values = filteredValues;
    }

    public File getDataFile() {
        return dataFile;
    }

    public List<String> getLipids() {
        return lipids;
    }

    public List<List<Double>> getValues() {
        return values;
    }

    public List<String> getMutants() {
        return mutants;
    }

    public List<String> getSeries() {
        return series;
    }

    public List<String> getWildTypes() {
        return wts;
    }

    public List<String> getBatches() {
        return batches;
    }

    public List<List<Double>> getRefValues() {
        return refValues;
    }

    public List<String> getRefMutants() {
        return refMutants;
    }

    private void sortLipids() {

        List<Integer> indices = new ArrayList<>();
        for (int i=0;i<lipids.size();i++) {
            indices.add(i);
        }

        Collections.sort(indices,new LipidSubClassComparator());

        this.lipids = sortStrings(lipids,indices);
        this.values = sortRows(values,indices);
        this.refValues = sortRows(refValues,indices);

    }

    private List<String> sortStrings(List<String> strings, List<Integer> indices) {
        List<String> sorted = new ArrayList<>();

        for (int i=0;i<strings.size();i++) {
            sorted.add(strings.get(indices.get(i)));
        }

        return sorted;
    }


    private List<List<Double>> sortRows(List<List<Double>> values, List<Integer> indices) {
        List<List<Double>> sorted = new ArrayList<>();

        for (int i=0;i<values.size();i++) {
            sorted.add(values.get(indices.get(i)));
        }

        return sorted;
    }
}
