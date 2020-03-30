package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by markusmueller on 03.03.19.
 */
public class LipidInfoParser {

    public class LipidInfo{
        private final String id;
        private final String subClass;
        private final String group;
        private final int chainLength;
        private final int nrDB;
        private final List<Map<String,String>> extendedInfoMap;

        public LipidInfo(String id, String subClass, String group, int chainLength, int nrDB, List<Map<String, String>> extendedInfoMap) {
            this.id = id;
            this.subClass = subClass;
            this.group = group;
            this.chainLength = chainLength;
            this.nrDB = nrDB;
            this.extendedInfoMap = extendedInfoMap;
        }

        public String getId() {
            return id;
        }

        public String getSubClass() {
            return subClass;
        }

        public String getGroup() {
            return group;
        }

        public int getChainLength() {
            return chainLength;
        }

        public int getNrDB() {
            return nrDB;
        }

        public List<Map<String, String>> getExtendedInfoMap() {
            return extendedInfoMap;
        }
    }

    private final File dataFile;
    private final File webLinkFile;
    private final Map<String,LipidInfo> lipidInfoMap;
    private final Set<String> lipidSubClass;

    public LipidInfoParser(File dataFile, File webLinkFile) {

        this.dataFile = dataFile;
        this.webLinkFile = webLinkFile;
        this.lipidInfoMap = new HashMap<>();
        this.lipidSubClass = new HashSet<>();
    }

    public void parse() {
        parseLipdInfo(parseWebInfo());
    }

    public void parseLipdInfo(Map<String,List<Map<String,String>>> lipidDBIDMap) {

        String line = null;

        try {
            // header:
            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            line = br.readLine();

            String[] fields = line.split(",");
            int lipidIdx = -1;
            int groupIdx = -1;
            int subClassIdx = -1;
            int lengthIdx = -1;
            int nrDBIdx = -1;

            //lipidID,group,class,subClass,chainLength,unsatIndex,nrDB
            for (int i=0;i<fields.length;i++) {
                if (fields[i].equals("lipidID")) lipidIdx = i;
                if (fields[i].equals("group")) groupIdx = i;
                if (fields[i].equals("subClass")) subClassIdx = i;
                if (fields[i].equals("chainLength")) lengthIdx = i;
                if (fields[i].equals("nrDB")) nrDBIdx = i;
            }

            while ((line = br.readLine()) != null) {

                fields = line.split(",");

                int nrDB = (fields[nrDBIdx].equals("NA"))?0:Integer.parseInt(fields[nrDBIdx]);

                lipidInfoMap.put(fields[lipidIdx],new LipidInfo(fields[lipidIdx],fields[subClassIdx],fields[groupIdx],
                        Integer.parseInt(fields[lengthIdx]),nrDB,lipidDBIDMap.get(fields[lipidIdx])));

                lipidSubClass.add(fields[subClassIdx]);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String,List<Map<String,String>>> parseWebInfo() {

        String line = null;

        try {
            // header:
            BufferedReader br = new BufferedReader(new FileReader(webLinkFile));
            line = br.readLine();

            String[] fields = line.split("\t");
            int lipdIdx = -1;
            int chebiIDIdx = -1;
            int chebiNameIdx = -1;
            int swiliIDIdx = -1;
            int swiliNameIdx = -1;
            int smilesIdx = -1;
            int formulaIdx = -1;

// Lipid	CheBI_ID	CheBI Name	SLM_ID	Name	Abbreviation_SLM	SMILES	InChIKey	Formula	Charge
// Mass	Exact Mass	[M.]+	[M+H]+	[M+K]+	[M+Na]+	[M+Li]+	[M+NH4]+	[M+Cl]-	[M+OAc]-

            for (int i=0;i<fields.length;i++) {
                if (fields[i].equals("Lipid")) lipdIdx = i;
                if (fields[i].equals("CheBI_ID")) chebiIDIdx = i;
                if (fields[i].equals("CheBI Name")) chebiNameIdx = i;
                if (fields[i].equals("SLM_ID")) swiliIDIdx = i;
                if (fields[i].equals("Name")) swiliNameIdx = i;
                if (fields[i].equals("SMILES")) smilesIdx = i;
                if (fields[i].equals("Formula")) formulaIdx = i;
            }

            Map<String,List<Map<String,String>>> lipidInfoMap = new HashMap<>();
            while ((line = br.readLine()) != null) {

                fields = line.split("\t");

                String lipid = fields[lipdIdx];

                Map<String,String> hashMap = new HashMap<>();
                hashMap.put("CheBI_ID",fields[chebiIDIdx]);
                hashMap.put("CheBI Name",fields[chebiNameIdx]);
                hashMap.put("SLM_ID",fields[swiliIDIdx]);
                hashMap.put("Name",fields[swiliNameIdx]);
                hashMap.put("SMILES",fields[smilesIdx]);
                hashMap.put("Formula",fields[formulaIdx]);

                if (!lipidInfoMap.containsKey(lipid)) lipidInfoMap.put(lipid,new ArrayList<>());
                lipidInfoMap.get(lipid).add(hashMap);
            }

            br.close();

            return lipidInfoMap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Map<String, LipidInfo> getLipidInfoMap() {
        return lipidInfoMap;
    }

    public Set<String> getLipidSubClass() {
        return lipidSubClass;
    }
}
