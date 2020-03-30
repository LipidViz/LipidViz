package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by markusmueller on 03.03.19.
 */
public class MutantInfoParser {

    public class MutantInfo {

        private String description;
        private String sgdID;

        public MutantInfo(String description, String sgdID) {
            this.description = description;
            this.sgdID = sgdID;
        }

        public String getDescription() {
            return description;
        }

        public String getSgdID() {
            return sgdID;
        }
    }

    private final File dataFile;
    private final Map<String,MutantInfo> mutantInfoMap;

    public MutantInfoParser(File dataFile) {

        this.dataFile = dataFile;
        this.mutantInfoMap = new HashMap<>();
    }


    public void parse() {

        String line = null;

        try {
            // header:
            BufferedReader br = new BufferedReader(new FileReader(dataFile));

            br.readLine();
            while ((line = br.readLine()) != null) {

                String[] fields = line.split("\t");

                if (fields[1].equals("OTHER")) continue;

                mutantInfoMap.put(fields[0],new MutantInfo(fields[5],fields[8]));
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, MutantInfo> getMutantInfoMap() {
        return mutantInfoMap;
    }
}
