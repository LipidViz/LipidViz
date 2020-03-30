package parsers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by markusmueller on 29.01.19.
 */
public class Parameters {
    private static Parameters instance = null;

    private final Properties properties;
    private final Map<String, MutantInfoParser.MutantInfo> mutantInfoMap;
    private final Map<String,LipidInfoParser.LipidInfo> lipidInfoMap;
    private final Set<String> lipidSubClass;
    private final Pattern excludeLipidRegEx;
    private final Pattern excludeMutantRegEx;
    private final Pattern referenceMutantRegEx;

    private Parameters(String propertiesFile) {

        this.properties = new Properties();
        try {
            this.properties.load(new FileReader(new File(propertiesFile)));
        } catch(IOException e) {
            System.out.println("Properties file "+propertiesFile+" not found. exit LipidVizMainFrame ...");
        }

        MutantInfoParser mutantInfoParser = new MutantInfoParser(new File(properties.getProperty("mutantInfoFile")));
        mutantInfoParser.parse();
        mutantInfoMap = mutantInfoParser.getMutantInfoMap();

        LipidInfoParser lipidInfoParser = new LipidInfoParser(new File(properties.getProperty("lipidInfoFile")),
                new File(properties.getProperty("lipidWebInfoFile")));
        lipidInfoParser.parse();
        lipidInfoMap = lipidInfoParser.getLipidInfoMap();
        lipidSubClass = lipidInfoParser.getLipidSubClass();

        if (!properties.getProperty("excludeLipids").isEmpty())
            excludeLipidRegEx = Pattern.compile(properties.getProperty("excludeLipids"));
        else
            excludeLipidRegEx =null;

        if (!properties.getProperty("excludeMutants").isEmpty())
            excludeMutantRegEx = Pattern.compile(properties.getProperty("excludeMutants"));
        else
            excludeMutantRegEx =null;

        if (!properties.getProperty("referenceMutants").isEmpty())
            referenceMutantRegEx = Pattern.compile(properties.getProperty("referenceMutants"));
        else
            referenceMutantRegEx = null;
    }


    public File getVectorTransformFile() {
        return new File(properties.getProperty("vectorTranformFile"));
    }

    public File getDefaultDirectory() {
        return new File(properties.getProperty("defaultDirectory"));
    }

    public File getDefaultReportDirectory() {
        return new File(properties.getProperty("defaultReportDirectory"));
    }

    public File getNormDataLogIntFile() {
        return new File(properties.getProperty("normDataLogIntFile"));
    }

    public File getNormDataLogIntStdFile() {
        return new File(properties.getProperty("normDataLogIntStdFile"));
    }

    public int getWindowHeight() {
        return Integer.parseInt(properties.getProperty("initWindowHeight"));
    }

    public int getWindowWidth() {
        return Integer.parseInt(properties.getProperty("initWindowWidth"));
    }

    public int getCellHeight() {
        return Integer.parseInt(properties.getProperty("initCellHeight"));
    }

    public int getCellWidth() {
        return Integer.parseInt(properties.getProperty("initCellWidth"));
    }

    public String[] getLipidClassOrder() {
        return properties.getProperty("lipidClassOrder").split(",");
    }

    public String getDescription(String mutant) {

        if (mutant.contains(" (AM)")) {
            mutant = mutant.replace(" (AM)","");
        }

        if (!mutantInfoMap.containsKey(mutant)) return "";

        return mutantInfoMap.get(mutant).getDescription();
    }


    public String getSgdID(String mutant) {

        if (mutant.contains(" (AM)")) {
            mutant = mutant.replace(" (AM)","");
        }

        if (!mutantInfoMap.containsKey(mutant)) return "";

        return mutantInfoMap.get(mutant).getSgdID();
    }


    public String getSubClass(String lipid) {

        if (!lipidInfoMap.containsKey(lipid)) return "";

        return lipidInfoMap.get(lipid).getSubClass();
    }

    public List<Map<String,String>> getExtendedInfo(String lipid) {

        if (!lipidInfoMap.containsKey(lipid)) return new ArrayList<>();

        return lipidInfoMap.get(lipid).getExtendedInfoMap();
    }


    public int getNrDB(String lipid) {

        if (!lipidInfoMap.containsKey(lipid)) return 0;

        return lipidInfoMap.get(lipid).getNrDB();
    }

    public Set<String> getLipidSubClass() {
        return lipidSubClass;
    }

    public boolean excludeLipid(String lipid) {
        if (excludeLipidRegEx==null) return false;
        else
            return excludeLipidRegEx.matcher(lipid).find();
    }


    public boolean excludeMutant(String mutant) {
        if (excludeMutantRegEx==null) return false;
        else
            return excludeMutantRegEx.matcher(mutant).find();
    }


    public boolean isReferenceMutant(String mutant) {
        if (referenceMutantRegEx==null) return false;
        else
            return referenceMutantRegEx.matcher(mutant).find();
    }

    public static Parameters getInstance(String propertiesFile) {
        if (instance==null)
            instance = new Parameters(propertiesFile);

        return instance;
    }

    public static Parameters getInstance() {
        return instance;
    }
}
