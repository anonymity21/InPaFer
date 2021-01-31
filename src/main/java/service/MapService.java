package service;

import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import util.BuildFilePath;
import util.FileIO;

import java.util.*;

@Slf4j
public class MapService {

    // for all file
    public static void generateMapFile(Subject subject, String failingTest) {
        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();

        String traceContent = FileIO.readFileToString(BuildFilePath.tmpLine("initial", subject, failingTest));
        if (traceContent.equals("")) {
            log.error("Init trace is empty");
        }
        for (String line : traceContent.split("\n")) {
            if (line.contains("START#0")) {
                continue;
            }
            if (linePatchMap.containsKey(line)) {
                linePatchMap.get(line).add("Init");
            } else {
                Set<String> tmpList = new LinkedHashSet<>();
                tmpList.add("Init");
                linePatchMap.put(line, tmpList);
            }
        }

        for (PatchFile patchFile : patchFiles) {
            traceContent = FileIO.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
            if (traceContent.equals("")) {
                log.error(patchFile.getPatchName() + " trace is empty");
            }
            for (String line : traceContent.split("\n")) {
                if (line.contains("START#0")) {
                    continue;
                }
                if (linePatchMap.containsKey(line)) {
                    linePatchMap.get(line).add(patchFile.getPatchName());
                } else {
                    Set<String> tmpList = new LinkedHashSet<>();
                    tmpList.add(patchFile.getPatchName());
                    linePatchMap.put(line, tmpList);
                }
            }
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry entry : linePatchMap.entrySet()) {
            Set<String> tmpList = (Set<String>) entry.getValue();
            if (tmpList.size() < patchFiles.size() + 1) {
                result.append(entry.getKey()).append(":");
                for (String patchName : tmpList) {
                    result.append(patchName).append("\t");
                }
                result.append("\n");
            }
        }
        // log.info(subject.getPatchList().stream().map(PatchFile::getPatchName).collect(Collectors.joining("\t")));
        // log.info(result.toString());

        FileIO.writeStringToFile(BuildFilePath.tmpMapLine(subject, failingTest), result.toString(), false);
    }

    // uncompleted method
    public void compareByParagraphWInitial(Subject subject, String failingTest) {
        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();

        String traceContent = FileIO.readFileToString(BuildFilePath.tmpLine("initial", subject, failingTest ));
        String splitLine = traceContent.split("\n")[0];

        if (!splitLine.endsWith("START#0")) {
            log.error("First Line is not START#0");
            return;
        }

        String[] initialSplitContent = traceContent.split(splitLine);
        for (int i = 0; i < initialSplitContent.length; i++) {
            for (PatchFile patchFile : patchFiles) {
                traceContent = FileIO
                        .readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
                if (traceContent.equals("")) {
                    log.error(patchFile.getPatchName() + " trace is empty");
                }
                if (!traceContent.split("\n")[0].equals(splitLine)) {
                    log.error(patchFile.getPatchName() + " first line is different with initial");
                }
                String[] patchSplitContent = traceContent.split(splitLine);

            }
        }

    }
    // for one paragraph
    public static void getDifferentIndex(String failingTest, Subject subject) {
        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();
        StringBuilder result = new StringBuilder();
        int index = 0;

        //Map<PatchFile, Integer> lengthPatch = new HashMap<>();
        int maxLength = 0;
        for (PatchFile patchFile : patchFiles) {

            String traceContent = FileIO
                    .readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
            if (traceContent.equals("")) {
                log.error(patchFile.getPatchName() + " trace is empty");
            }
            String splitLine = traceContent.split("\n")[0];
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (splitLine.contains(key)) {
                    splitLine = splitLine.replace(key, "\\" + key);
                }
            }
            String[] patchSplitContent = traceContent.split(splitLine + "\n");
            if(maxLength < patchSplitContent.length){
                maxLength = patchSplitContent.length;
            }
        }

        boolean findDifference = false;
        while (!findDifference && index < maxLength) {
            // for index round
            linePatchMap.clear();
            for (PatchFile patchFile : patchFiles) {
                String traceContent = FileIO
                        .readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
                if (traceContent.equals("")) {
                    log.error(patchFile.getPatchName() + " trace is empty");
                }
                String splitLine = traceContent.split("\n")[0];
                String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
                for (String key : fbsArr) {
                    if (splitLine.contains(key)) {
                        splitLine = splitLine.replace(key, "\\" + key);
                    }
                }
                String[] patchSplitContent = traceContent.split(splitLine + "\n");
                //System.out.println(traceContent);
                if (index >= patchSplitContent.length) {
                    //log.error(patchFile.getPatchName() + " paragraph length is different!");
                    continue;
                }
                for (String line : patchSplitContent[index].split("\n")) {
                    if (line.contains("START#0") || line.equals("")) {
                        continue;
                    }
                    if (linePatchMap.containsKey(line)) {
                        linePatchMap.get(line).add(patchFile.getPatchName());
                    } else {
                        Set<String> tmpList = new LinkedHashSet<>();
                        tmpList.add(patchFile.getPatchName());
                        linePatchMap.put(line, tmpList);
                    }
                }

            }
            if (linePatchMap.size() != 0) {
                for (Map.Entry entry : linePatchMap.entrySet()) {
                    Set<String> tmpList = (Set<String>) entry.getValue();
                    if (tmpList.size() < patchFiles.size()) {
                        result.append(entry.getKey()).append(":");
                        for (String patchName : tmpList) {
                            result.append(patchName).append("\t");
                        }
                        result.append("\n");
                    }
                }
                if (!result.toString().equals("")) {
                    findDifference = true;
                }
            }
            index++;
        }
        if(result.toString().equals("")){
            index = -1;
            log.info("Patches Do not have Difference!");
        }
        log.info("Different Lines in {}", index);
        FileIO.writeStringToFile(BuildFilePath.tmpMapLineIndex(subject,failingTest), String.valueOf(index), false);
    }


    // for one paragraph
    public static void compareByParagraphOInitial(String failingTest, Subject subject) {
        List<PatchFile> patchFiles = subject.getPatchList();
        Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();
        StringBuilder result = new StringBuilder();
        int index = 0;

        //Map<PatchFile, Integer> lengthPatch = new HashMap<>();
        int maxLength = 0;
        for (PatchFile patchFile : patchFiles) {

            String traceContent = FileIO
                    .readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
            if (traceContent.equals("")) {
                log.error(patchFile.getPatchName() + " trace is empty");
            }
            String splitLine = traceContent.split("\n")[0];
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (splitLine.contains(key)) {
                    splitLine = splitLine.replace(key, "\\" + key);
                }
            }
            String[] patchSplitContent = traceContent.split(splitLine + "\n");
            if(maxLength < patchSplitContent.length){
                maxLength = patchSplitContent.length;
            }
        }

        boolean findDifference = false;
        while (!findDifference && index < maxLength) {
            // for index round
            linePatchMap.clear();
            for (PatchFile patchFile : patchFiles) {
                String traceContent = FileIO
                        .readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject, failingTest));
                if (traceContent.equals("")) {
                    log.error(patchFile.getPatchName() + " trace is empty");
                }
                String splitLine = traceContent.split("\n")[0];
                String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
                for (String key : fbsArr) {
                    if (splitLine.contains(key)) {
                        splitLine = splitLine.replace(key, "\\" + key);
                    }
                }
                String[] patchSplitContent = traceContent.split(splitLine + "\n");
                //System.out.println(traceContent);
                if (index >= patchSplitContent.length) {
                    //log.error(patchFile.getPatchName() + " paragraph length is different!");
                    continue;
                }
                for (String line : patchSplitContent[index].split("\n")) {
                    if (line.contains("START#0") || line.equals("")) {
                        continue;
                    }
                    if (linePatchMap.containsKey(line)) {
                        linePatchMap.get(line).add(patchFile.getPatchName());
                    } else {
                        Set<String> tmpList = new LinkedHashSet<>();
                        tmpList.add(patchFile.getPatchName());
                        linePatchMap.put(line, tmpList);
                    }
                }

            }
            if (linePatchMap.size() != 0) {
                for (Map.Entry entry : linePatchMap.entrySet()) {
                    Set<String> tmpList = (Set<String>) entry.getValue();
                    if (tmpList.size() < patchFiles.size()) {
                        result.append(entry.getKey()).append(":");
                        for (String patchName : tmpList) {
                            result.append(patchName).append("\t");
                        }
                        result.append("\n");
                    }
                }
                if (!result.toString().equals("")) {
                    findDifference = true;
                }
            }
            index++;
        }
        if(result.toString().equals("")){
            log.info("Patches Do not have Difference!");
        }
        log.info("Different Lines in {}", index);
        FileIO.writeStringToFile(BuildFilePath.tmpMapLine(subject,failingTest), result.toString(), false);
    }

    public static void main(String[] args) {
        Subject subject = new Subject("Lang", 14);
        if (subject.initPatchListByPath(Constant.AllPatchPath)) {
            MapService mapService = new MapService();
           // compareByParagraphOInitial(subject);
        }
    }

}
