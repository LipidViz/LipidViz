package parsers;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

/**
 * Created by markusmueller on 12.03.19.
 */
public class DataMatrixLipids extends DataMatrix {

    private static DataMatrixLipids instance = null;

    private enum ReferenceAlgo {MEAN_WT,NN_WT};

    private final List<String> series;
    private final List<String> wildTypes;
    private final List<String> batches;
    private final List<List<Double>> zScores;
    private final List<List<Double>> diffs;
    private final List<List<Double>> ints;
    private final List<List<Double>> logInts;
    private final List<List<Double>> logIntStd;
    private final List<Double> refLogInts;
    private final List<Double> refLogStd;
    private final List<List<Double>>  referenceLogInts;

    private final double sqrt12;
    private final double sqrtNrRefMutants;

    private ReferenceAlgo referenceAlgo = ReferenceAlgo.MEAN_WT;

    private DataMatrixLipids(List<String> lipids, List<String> mutants, List<String> series, List<String> wildTypes, List<String> batches,
                             List<String> referenceMutants, List<List<Double>> referenceLogInts, List<List<Double>> logInts, List<List<Double>> logIntStd) {

        super(lipids, mutants);

        this.series = series;
        this.wildTypes = wildTypes;
        this.batches = batches;

        this.zScores = new ArrayList<>();
        this.diffs = new ArrayList<>();
        this.logIntStd = new ArrayList<>();
        this.ints = new ArrayList<>();
        this.refLogInts = new ArrayList<>();
        this.refLogStd = new ArrayList<>();
        this.referenceLogInts = new ArrayList<>();

        sqrt12 = Math.sqrt(12.0);
        sqrtNrRefMutants = Math.sqrt(referenceMutants.size());

        for (int i=0;i<referenceMutants.size();i++) {
            List<Double> rl = new ArrayList<>();
            for (int j = 0; j< lipids.size(); j++) {
                rl.add(referenceLogInts.get(j).get(i));
            }
            this.referenceLogInts.add(rl);
        }

        calcReferenceStats();

        for (int i=0;i<mutants.size();i++) {

            List<Double> l = new ArrayList<>();
            List<Double> s = new ArrayList<>();
            List<Double> v = new ArrayList<>();

            for (int j = 0; j< lipids.size(); j++) {
                double value = logInts.get(j).get(i);
                l.add(value);
                value = Math.pow(10.0,value);
                v.add(value);
                s.add(logIntStd.get(j).get(i));
            }
            this.values.add(l);
            this.ints.add(v);
            this.logIntStd.add(s);
        }

        this.logInts = values; // just keep pointer here for compatibility

        calcZScoresDiff();
    }

    private void calcReferenceStats() {

        for (int j = 0; j< columnNames.size(); j++) {
            DescriptiveStatistics statistics = new DescriptiveStatistics();
            for (int i=0;i<referenceLogInts.size();i++) {
                statistics.addValue(referenceLogInts.get(i).get(j));
            }

            refLogInts.add(statistics.getPercentile(50.0));
            refLogStd.add(statistics.getStandardDeviation());
        }
    }

    private int getClosestRef(int i) {

        List<Double> values = this.logInts.get(i);

        double maxCos = -2.0;
        int maxIdx = -1;
        for (int j=0;j<referenceLogInts.size();j++) {
            double cos = calcCosine(values,referenceLogInts.get(j));
            if (cos>maxCos) {
                maxCos = cos;
                maxIdx = j;
            }
        }

        return maxIdx;
    }

    private void calcZScoresDiff() {

        List<Double> meanStd = new ArrayList<>();
        for (int j = 0; j< columnNames.size(); j++) {

            DescriptiveStatistics statistics = new DescriptiveStatistics();
            for (int i = 0; i < rowNames.size(); i++) {
                double sd = logIntStd.get(i).get(j);
                if (sd > 0) statistics.addValue(sd);
            }

            meanStd.add(statistics.getPercentile(50.0));
        }

        for (int i = 0; i< rowNames.size(); i++) {

            List<Double> refValues = getRefLogInts(i);
            List<Double> z = new ArrayList<>();
            List<Double> d = new ArrayList<>();

            for (int j = 0; j< columnNames.size(); j++) {

                double refStd = refLogStd.get(j);
                double medianSd = meanStd.get(j);

                z.add(calcZScore(medianSd,refStd,refValues.get(j),i,j));
                d.add(ints.get(i).get(j)-Math.pow(10.0,refValues.get(j)));
            }

            this.zScores.add(z);
            this.diffs.add(d);
        }
    }

    private List<Double> getRefLogInts(int i) {

        if (referenceAlgo == ReferenceAlgo.NN_WT)
            return referenceLogInts.get(getClosestRef(i));
        else
            return refLogInts;
    }

    private double calcCosine(List<Double> l1, List<Double> l2) {

       double c = 0.0;
       for (int i=0;i<l1.size();i++) {
           c += l1.get(i)*l2.get(i);
       }

       return c/(calcNorm(l1)*calcNorm(l2));
    }

    private double calcNorm(List<Double> values) {
        double sn = 0.0;

        for (Double d : values) {
            sn += d*d;
        }

        return Math.sqrt(sn);
    }

    private double calcZScore(double medianSd, double refStd, double refValue, int mutantIdx, int lipidIdx) {
        double w = 0.9;

        double std = (w*medianSd + (1.0-w)*logIntStd.get(mutantIdx).get(lipidIdx))/sqrt12;
        std = Math.sqrt(std*std+refStd*refStd);

        return (logInts.get(mutantIdx).get(lipidIdx)-refValue)/std;
    }

    public List<String> getSeries() {
        return series;
    }

    public List<String> getWildTypes() {
        return wildTypes;
    }

    public List<String> getBatches() {
        return batches;
    }

    public List<Double> getValues(String mutant, ValueType valueType) {

        int idxM = rowMap.get(mutant);

        if (valueType == ValueType.ZSCORE) return zScores.get(idxM);
        else if (valueType == ValueType.DIFF) return diffs.get(idxM);
        else if (valueType == ValueType.INT) return ints.get(idxM);
        else if (valueType == ValueType.LOG_INT) return logInts.get(idxM);
        else return logIntStd.get(idxM);
    }

    public Double getValue(String mutant,String columnName, ValueType valueType) {

        int idxL = columnMap.get(columnName);
        int idxM = rowMap.get(mutant);

        if (valueType == ValueType.ZSCORE) return zScores.get(idxM).get(idxL);
        else if (valueType == ValueType.DIFF) return diffs.get(idxM).get(idxL);
        else if (valueType == ValueType.INT) return ints.get(idxM).get(idxL);
        else if (valueType == ValueType.LOG_INT) return logInts.get(idxM).get(idxL);
        else return logIntStd.get(idxM).get(idxL);
    }

    public double[][] getValueArrays(ValueType valueType) {

        if (valueType == ValueType.ZSCORE) return convertToArray(zScores);
        else if (valueType == ValueType.DIFF) return convertToArray(diffs);
        else if (valueType == ValueType.INT) return convertToArray(ints);
        else if (valueType == ValueType.LOG_INT) return convertToArray(logInts);
        else return convertToArray(logIntStd);
    }

    public List<String> getLipids() {
        return columnNames;
    }

    public String getName() {
        return "Lipid";
    }

    public static DataMatrixLipids makeInstance(NormalizedQuantDataParser logIntParser, NormalizedQuantDataParser logIntStdParser) {

        if (instance==null)
            instance = new DataMatrixLipids(logIntParser.getLipids(),logIntParser.getMutants(),
                    logIntParser.getSeries(), logIntParser.getWildTypes(), logIntParser.getBatches(), logIntParser.getRefMutants(),
                    logIntParser.getRefValues(), logIntParser.getValues(), logIntStdParser.getValues());

        return instance;
    }

    public static DataMatrixLipids getInstance() {
        return instance;
    }

}
