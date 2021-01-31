package script;

import util.FileIO;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ManualPatchProcess {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        String patchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/UserStudyPatch";
        //Map<String, String> map = TechNameMap.getMap();
        for (File file : Objects.requireNonNull(new File(patchPath).listFiles())) {
            if (file.isDirectory()) {
                System.out.println("Process " + file.getName());
                Set<String> currentNameSet = new HashSet<String>();

                for (File patchFile : Objects.requireNonNull(file.listFiles())) {
                    if (patchFile.getName().equalsIgnoreCase(".DS_Store")) {
                        continue;
                    }
                    String newPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/patchfilter/StudyPatch";
                    String techName = patchFile.getName().split("-")[0];
                    newPatchPath = newPatchPath + "/" + file.getName();
                    if (!new File(newPatchPath).exists()) {
                        new File(newPatchPath).mkdirs();
                    }
                    FileIO.copyFile(patchFile.getAbsolutePath(), newPatchPath + "/" + getRandomName(currentNameSet));
                }
            }
        }
    }

    private static String getRandomName(Set<String> currentNameSet) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder nameBuilder = new StringBuilder();
        do {
            for (int i = 0; i < 6; i++) {
                nameBuilder.append(chars.charAt((int) (Math.random() * 26)));
            }
        } while (currentNameSet.contains(nameBuilder.toString()));
        currentNameSet.add(nameBuilder.toString());
        return nameBuilder.toString();
    }
}
