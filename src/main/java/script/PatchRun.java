package script;

import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import run.Runner;
import util.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class PatchRun {


    // for Math_30
    public static void runForMath30() {
        String initialPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/TotalPatch/Math/30";
        int replaceLineNumber = 138;
        for (File file : new File(initialPatchPath).listFiles()) {
            if (!file.getName().startsWith("Cardumen")) {
                continue;
            }
            String content = FileIO.readFileToString(file);
            String replaceFile = content.split("\n")[0].split("--- ")[1].split("\\.java")[0] + ".java".trim();
            String replaceLine = "";
            for (String line : content.split("\n")) {
                if (line.startsWith("+") && !line.startsWith("+++")) {
                    replaceLine = line.split("\\+")[1];
                    break;
                }
            }
            try {
                FileUtils.copyFile(new File(replaceFile), new File(replaceFile + ".patched"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String patchedCon[] = FileIO.readFileToString(replaceFile + ".patched").split("\n");
            patchedCon[replaceLineNumber - 1] = replaceLine;
            StringBuffer resultBuffer = new StringBuffer();
            for (int i = 0; i < patchedCon.length; i++) {
                resultBuffer.append(patchedCon[i]).append("\n");
            }
            FileIO.writeStringToFile(replaceFile + ".patched", resultBuffer.toString(), false);
            String diffFile = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/FinalPatch/Math/30/" + file.getName();
            Runner.diff2File(replaceFile, replaceFile + ".patched", diffFile);
        }
    }

    // 聚类
    public static void samePostion() {
        String name = "Math";
        String id = "41";
        String initialPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/TotalPatch/" + name + "/" + id;
        Map<String, List<String>> samePosMap = new HashMap<String, List<String>>();

        for (File file : new File(initialPatchPath).listFiles()) {
            if (!file.getName().startsWith("Cardumen")) {
                continue;
            }
            String content[] = FileIO.readFileToString(file).split("\n");
            StringBuffer keyBuffer = new StringBuffer();
            keyBuffer.append(content[0].split("\\.java")[0]).append(content[1].split("\\.patched")[0]).append(content[2]).append(3);
            if (samePosMap.containsKey(keyBuffer.toString())) {
                samePosMap.get(keyBuffer.toString()).add(file.getName());
            } else {
                List<String> nameList = new LinkedList<String>();
                nameList.add(file.getName());
                samePosMap.put(keyBuffer.toString(), nameList);
            }
        }
        int i = 1;
        for (Entry entry : samePosMap.entrySet()) {
            String dir = initialPatchPath + "/" + i + "dir" + "/";
            List<String> nameList = (List<String>) entry.getValue();
            nameList.stream().forEach(filename ->
                    {
                        try {
                            FileUtils.copyFile(new File(initialPatchPath + "/" + filename), new File(dir + filename));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
            );
            i++;
        }

    }

    public static void changeFor1Line() {
        String name = "Lang";
        String id = "22";
        String initialPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/TotalPatch/" + name + "/" + id + "/";
        int replaceLineNumber = 584;
        String changeLine = "@@ -242,7 +242,7 @@";
        int patchLineNumber = 7;

        for (File file : new File(initialPatchPath).listFiles()) {
            if (!file.getName().startsWith("Cardumen")) {
                continue;
            }
			/*if( Integer.parseInt(file.getName().split("patch")[1]) != endPatch) {
				continue;
			}*/
            log.info(file.getName().split("patch")[1]);
            String content = FileIO.readFileToString(file);
            String patchChangeLine = content.split("\n")[2];
            if (!patchChangeLine.equals(changeLine)) {
                continue;
            }
            String replaceFile = content.split("\n")[0].split("--- ")[1].split("\\.java")[0] + ".java".trim();
            String replaceLine = content.split("\n")[patchLineNumber].split("\\+")[1];
			/*for(String line: content.split("\n")) {
				if(line.startsWith("+") && ! line.startsWith("+++")) {
					replaceLine = line.split("\\+")[1];
					break;
				}
			}*/
            try {
                FileUtils.copyFile(new File(replaceFile), new File(replaceFile + ".patched"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String patchedCon[] = FileIO.readFileToString(replaceFile + ".patched").split("\n");
            patchedCon[replaceLineNumber - 1] = replaceLine;
			/*while (index > 0) {
				patchedCon[replaceLineNumber-1 +index] = "woshilinshibianliang";
				index--;
			}*/
            StringBuffer resultBuffer = new StringBuffer();
            for (int i = 0; i < patchedCon.length; i++) {
                if (patchedCon[i].equals("woshilinshibianliang")) {
                    continue;
                } else {
                    resultBuffer.append(patchedCon[i]).append("\n");
                }
            }
            FileIO.writeStringToFile(replaceFile + ".patched", resultBuffer.toString(), false);
            String diffFile = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/FinalPatch/" + name + "/" + id + "/" + file.getName();

            Runner.diff2File(replaceFile, replaceFile + ".patched", diffFile);
        }
    }

    public static void getChangeLine() {
        String name = "Math";
        String id = "20";
        String initialPatchPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/TotalPatch/" + name + "/" + id + "/";
        Map<String, StringBuilder> linePatchMap = new LinkedHashMap<String, StringBuilder>();
        for (File file : new File(initialPatchPath).listFiles()) {
            if (!file.getName().startsWith("Cardumen")) {
                continue;
            }
            String content = FileIO.readFileToString(file);
            String line = content.split("\n")[2];
            if (linePatchMap.containsKey(line)) {
                linePatchMap.get(line).append("\t").append(file.getName());
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(file.getName());
                linePatchMap.put(line, stringBuilder);
            }
        }
        for (Entry<String, StringBuilder> entry : linePatchMap.entrySet()) {
            System.out.println(entry.getKey() + ": ");
            System.out.println(entry.getValue().toString());
        }

    }

    // for general case
    public static void generalRun() {
        String fixedFile = "/Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Math/Math_105_buggy/src/java//org/apache/commons/math/stat/regression/SimpleRegression.java";
        String diffFile = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/FinalPatch/Math/105/Nopol2017-Patch177";

        Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
    }

    public static void moveCardumenPatch() {
        String originalPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/Cardumen-formatPatches/";
        String targetPath = "/Users/liangjingjing/WorkSpace/Project/PatchFilterBackUp/PatchBackup/Cardumen/";
        String project = "Math";
        String originalDir = originalPath + project;
        for (File file : new File(originalDir).listFiles()) {
            if (!file.getName().startsWith("patch")) {
                continue;
            }
            String id = file.getName().split("-")[2];
            if (!id.equals("41")) {
                continue;
            }
            Subject subject = new Subject(project, Integer.parseInt(id));
            String patchName = file.getName().split("-")[0];
            String targetDir = targetPath + project + "/" + id + "/";
            String content = FileIO.readFileToString(file);
            String patchedFile = subject.getHome() + content.split("\n")[0].split("--- ")[1];

            FileIO.backUpFile(patchedFile, patchedFile + ".bak");
            FileIO.copyFile(patchedFile, patchedFile + ".patched");
            Runner.patchFile(patchedFile + ".patched", file.getAbsolutePath());
            Runner.diff2File(patchedFile, patchedFile + ".patched", targetDir + "Cardumen-" + patchName);

            FileIO.restoreFile(patchedFile, patchedFile + ".bak");
			
			/*try {
				FileUtils.copyFile(file, new File(targetDir + patchName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

        }
    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //changeFor1Line();
        generalRun();
        //samePostion();
        //getChangeLine();
        //moveCardumenPatch();
    }

}
