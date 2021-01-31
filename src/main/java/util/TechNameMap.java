package util;

import java.util.HashMap;
import java.util.Map;

/*
 * This class is the record of reflecting the name of technique
 */
public class TechNameMap {

    private static Map<String, String> nameMap = new HashMap<>();

    static {
        nameMap.put("SimFix", "A");
        nameMap.put("CapGen", "B");
        nameMap.put("Nopol2015", "C");
        nameMap.put("Nopol2017", "D");
        nameMap.put("Cardumen", "E");
        nameMap.put("JGenProg2017", "F");
        nameMap.put("jKali", "G");
        nameMap.put("ACS", "H");
        nameMap.put("HDRepair", "I");
        nameMap.put("DeepRepair", "J");
        nameMap.put("ssFix", "K");
    }

    public static Map<String, String> getMap() {
        return nameMap;
    }
}
