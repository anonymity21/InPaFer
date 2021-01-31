package patchfilter.model.script;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patchfilter.model.util.FileIO;
import patchfilter.util.TechNameMap;

public class ManualPatchProcess {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String patchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/UserStudyPatch";	
		//Map<String, String> map = TechNameMap.getMap();
		for(File file: new File(patchPath).listFiles()) {
			if(file.isDirectory()) {
				System.out.println("Process " + file.getName());
				Set<String> currentNameSet = new HashSet<String>();
				
				for(File patchFile: file.listFiles()) {
					if(patchFile.getName().equalsIgnoreCase(".DS_Store")) {
						continue;
					}
					String newPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/StudyPatch";
					String techName = patchFile.getName().split("-")[0];
					StringBuilder newName = new StringBuilder(getRandomName(currentNameSet));		
					
					newPatchPath =  newPatchPath + "/" + file.getName();
					if(!new File(newPatchPath).exists()) {
						new File(newPatchPath).mkdirs();
					}		
					FileIO.copyFile(patchFile.getAbsolutePath(), newPatchPath + "/" + newName);					
				}
			}
		}
	}
	 private static String getRandomName(Set<String> currentNameSet) {
	    	String chars = "abcdefghijklmnopqrstuvwxyz";
	    	StringBuilder nameBuilder = new StringBuilder();
	    	do {
		    	for(int i = 0; i<6; i++) {
		    		nameBuilder.append(chars.charAt((int)(Math.random() * 26)));
		    	}
			} while (currentNameSet.contains(nameBuilder.toString()));
	    	currentNameSet.add(nameBuilder.toString());
	    	return nameBuilder.toString();
	}
}
