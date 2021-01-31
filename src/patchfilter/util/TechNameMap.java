package patchfilter.util;

import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager.NameMap;

/*
 * This class is the record of reflecting the name of technique
 */
public class TechNameMap {

	private static Map<String, String> nameMap = new HashMap<String, String>();
	
	public static Map<String, String> getMap() {
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
		return nameMap;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
