package clustering;

/**
 * Created by markusmueller on 22.02.19.
 */
public class MutantDistanceCalculator {
    
    public enum DistanceType {COSINE,SQUARE,MANHATTEN};

    private final double[][] values;
    private final double[][] distances;
    private final int[] mask;

    public MutantDistanceCalculator(double[][] values, int[] mask) {

        this.values = normalize(values,mask);
        this.mask = mask;

        this.distances = new double[values.length][values.length];
    }

    public MutantDistanceCalculator(double[][] orgValues) {

        final int[] mask = new int[orgValues[0].length];
        for (int i=0;i<mask.length;i++) mask[i] = 1;

        this.values = normalize(orgValues,mask);
        this.mask = mask;

        this.distances = new double[this.values.length][this.values.length];
    }

    public double[][] calcDistances(DistanceType type) {

        for (int i=0;i<values.length;i++) {
            distances[i][i] = 0.0;
            for (int j=i+1;j<values.length;j++) {
                double d = 0.0;
                if (type == DistanceType.COSINE) {
                    d = calcDistanceCosine(values[i],values[j]);
                } else if (type == DistanceType.SQUARE) {
                    d = calcDistanceSquare(values[i],values[j]);
                } else if (type == DistanceType.MANHATTEN) {
                    d = calcDistanceManhatten(values[i],values[j]);
                }

                distances[i][j] = d;
                distances[j][i] = d;
            }
        }

        return distances;
    }

    private double[][] normalize(double[][] values, int[] mask) {

        int dim = 0;
        for (int i=0;i<mask.length;i++) {
            if (mask[i]>0) dim++;
        }

        if (dim==0) return null;

        double[][] normValues = new double[values.length][dim];
        for (int i=0;i<values.length;i++) {
            double norm = 0.0;
            for (int j=0;j<values[0].length;j++) {
                if (mask[j]>0)
                    norm += values[i][j]*values[i][j];
            }

            norm = Math.sqrt(norm);
            int newIdx = 0;
            for (int j=0;j<values[0].length;j++) {
                if (mask[j]>0) {
                    normValues[i][newIdx++] = (norm>0.0000001)?values[i][j] / norm:0.0;
                }
            }
        }

        return normValues;
    }

    private double calcDistanceCosine(double[] values1, double[] values2) {

        double dist = 0.0;
        for (int i=0;i<values[0].length;i++) {
            dist += values1[i]*values2[i];
        }
        
        return 1.0-dist;
    }

    private double calcDistanceSquare(double[] values1, double[] values2) {

        double dist = 0.0;
        for (int i=0;i<values[0].length;i++) {
            double di = values1[i]-values2[i];
            dist += di*di;
        }

        return Math.sqrt(dist);
    }

    private double calcDistanceManhatten(double[] values1, double[] values2) {

        double dist = 0.0;
        for (int i=0;i<values[0].length;i++) {
            dist += Math.abs(values1[i]-values2[i]);
        }

        return dist;
    }

    public double[][] getDistances() {
        return distances;
    }
}
