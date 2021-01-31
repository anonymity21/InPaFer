package service;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import util.BuildFilePath;
import util.FileIO;
import util.StateType;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllQueryService {

    private List<Line> questionList = new ArrayList<>();
    private List<PatchFile> currentPatches = new ArrayList<>();
    private Map<String, Boolean> patchCorrectnessMap = new HashMap<>();
    private Subject subject;

    private static Random random = new Random();

    public AllQueryService(Subject subject) {
        this.subject = subject;
    }

    public boolean initPatchCorrectnessMap() {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<PatchFile> patchFileList = subject.getPatchList();
        for (PatchFile patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(),
                    String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {
                patchFile.setCorrectness(true);
                patchCorrectnessMap.put(patchFile.getPatchName(), true);
                contains = true;
            } else {
                patchCorrectnessMap.put(patchFile.getPatchName(), false);
            }
        }
        return contains;
    }


    public void initQuestionList(String failingTest, boolean modificationMethod, boolean variable, boolean trace) {
        if (modificationMethod) {
            initLocationLineList();
        }
        if (variable) {
            iniVarLineList(failingTest);
        }
        if (trace) {
            initTraceLinelist(failingTest);
        }

        Map<String, Line> lineInfoMap = questionList.stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(line -> line.getPatchList().stream()
                        .map(PatchFile::getPatchName).sorted(String::compareToIgnoreCase)
                        .collect(Collectors.joining(",")) + getLineType(line), Function.identity(), (v1, v2) -> v2));
        questionList.clear();
        questionList.addAll(lineInfoMap.values());
    }

    private String getLineType(Line line) {
        if (line instanceof LineInfo) {
            return "_trace";
        } else if (line instanceof VariableLine) {
            return "_variable";
        } else if (line instanceof LocationLine) {
            return "_location";
        }
        return "";
    }

    /**
     * 初始化 最初的提问列表 stateType 都为unclear，并且以 traceline 对象表示
     */
    public void initTraceLinelist(String failingTest) {
        // tmp test
        String content = FileIO.readFileToString(BuildFilePath.tmpMapLine(subject, failingTest));
        if (content.equals("")) {
            log.info("All lines are same");
            return;
        }
        Map<String, PatchFile> patchNameMap = currentPatches.stream().filter(Objects::nonNull).collect(
                Collectors.toMap(PatchFile::getPatchName, Function.identity(), (v1, v2) -> v2, LinkedHashMap::new));
        Set<String> patchNameSet = currentPatches.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());
        List<LineInfo> traceLinelist = new ArrayList<>();
        for (String line : content.split("\n")) {
            if (line.trim().equals("")) {
                continue;
            }
            String[] strings = line.split(":");
            List<PatchFile> patchFiles = new LinkedList<PatchFile>();
            for (String patchName : strings[1].trim().split("\t")) {
                if (patchNameSet.contains(patchName) && patchNameMap.get(patchName) != null) {
                    patchFiles.add(patchNameMap.get(patchName));
                }
            }
            // 只提问能区分开的
            if (patchFiles.size() > 0 && patchFiles.size() < currentPatches.size()) {
                traceLinelist.add(new LineInfo(strings[0].trim(), patchFiles));
            }
        }
        questionList.addAll(traceLinelist);
    }

    private void initLocationLineList() {
        List<LocationLine> locationLines = new ArrayList<>();
        Set<String> patchNameSet = currentPatches.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());
        Map<String, List<PatchFile>> methodPatchMap = subject.getPatchList().stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(PatchFile::getModifiedMethod, Collectors.toCollection(LinkedList::new)));
        for (Map.Entry<String, List<PatchFile>> entry : methodPatchMap.entrySet()) {
            List<PatchFile> tmpPatchFiles = entry.getValue().stream().filter(Objects::nonNull)
                    .filter(patchFile -> patchNameSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toList());
            if (tmpPatchFiles.size() > 0 && tmpPatchFiles.size() < currentPatches.size()) {
                LocationLine locationLine = new LocationLine(entry.getKey(), StateType.UNCLEAR, tmpPatchFiles);
                locationLines.add(locationLine);
            }
        }
        if (locationLines.size() == 0) {
            log.info("All modified methods are same");
        }
        questionList.addAll(locationLines);
    }

    private void iniVarLineList(String failingTest) {
        Set<String> patchNameSet = currentPatches.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());

        VariableCollection variableCollection = new VariableCollection(subject);
        variableCollection.mainProcess(failingTest, currentPatches.size());
        List<VariableLine> variableLines = variableCollection.getVariableLines();
        for (VariableLine variableLine : variableLines) {
            List<PatchFile> tmpPatchFiles = variableLine.getPatchList().stream().filter(Objects::nonNull)
                    .filter(patchFile -> patchNameSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toList());
            if (tmpPatchFiles.size() > 0 && tmpPatchFiles.size() < currentPatches.size()) {
                variableLine.setPatchList(tmpPatchFiles);
                questionList.add(variableLine);
            }
        }
    }

    public void initCurrentPatches() {

        initPatchCorrectnessMap();
        File file = new File(Constant.AllPatchPath + subject.getName() + "/" + subject.getId());
        List<File> sortedFileList = Arrays.stream(file.listFiles())
                .sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        Map<String, String> simplePatchMap = new HashMap<>();
        for (File patchFile : sortedFileList) {
            if (patchFile.getName().startsWith(".DS")) {
                continue;
            }
            SimplePatchFile simplePatchFile = new SimplePatchFile(patchFile);
            simplePatchFile.initContent();
            String patchFileName = simplePatchMap.get(simplePatchFile.getPatchContent());
            if (StringUtils.isEmpty(patchFileName)) {
                simplePatchMap.put(simplePatchFile.getPatchContent(), patchFile.getName());
            } else {
                if (patchCorrectnessMap.size() != 0) {
                    if (!patchCorrectnessMap.get(patchFileName)) {
                        simplePatchMap.put(simplePatchFile.getPatchContent(), patchFile.getName());
                    }
                }
            }
        }
        Set<String> patchNameSet = simplePatchMap.values().stream().filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
        this.currentPatches = subject.getPatchList().stream().filter(Objects::nonNull)
                .filter(patchFile -> patchNameSet.contains(patchFile.getPatchName()))
                .collect(Collectors.toList());


        // this.currentPatches.addAll(subject.getPatchList());
    }

    public boolean isTerminate() {
        List<Object> testLineInfos = questionList.stream().filter(Objects::nonNull)
                //  .filter(variableLine -> variableLine.getStateType().equals(StateType.UNCLEAR))
                .filter(line -> line.getPatchList().size() > 0).collect(Collectors.toList());
        //return testLineInfos.size() == 0 || currentPatches.size() == 1;
        return testLineInfos.size() == 0;
    }


    public Line pickOne() {
        return questionList.get(random.nextInt(questionList.size()));
    }

    public Line pickByOrder(int type) {
        if (CollectionUtils.isEmpty(questionList)) {
            return null;
        }
        List<Line> traceLineList = questionList.stream().filter(Objects::nonNull)
                .filter(line -> line instanceof LineInfo).collect(Collectors.toList());
        List<Line> variableLineList = questionList.stream().filter(Objects::nonNull)
                .filter(line -> line instanceof VariableLine).collect(Collectors.toList());
        List<Line> locationLineList = questionList.stream().filter(Objects::nonNull)
                .filter(line -> line instanceof LocationLine).collect(Collectors.toList());

        switch (type) {
            case 0: {
                return questionList.get(random.nextInt(questionList.size()));
            }
            case 1:   // Method Trace Variable
            {
                if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else {
                    return null;
                }
            }
            case 2: //Method Variable Trace
            {
                if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else {
                    return null;
                }
            }
            case 3: //Trace Variable Method
            {
                if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else {
                    return null;
                }
            }
            case 4: //Trace Method Variable
            {
                if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else {
                    return null;
                }
            }
            case 5: //Variable Method Trace
            {
                if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else {
                    return null;
                }
            }
            case 6: //Variable Trace Method
            {
                if (CollectionUtils.isNotEmpty(traceLineList)) {
                    return traceLineList.get(random.nextInt(traceLineList.size()));
                } else if (CollectionUtils.isNotEmpty(variableLineList)) {
                    return variableLineList.get(random.nextInt(variableLineList.size()));
                } else if (CollectionUtils.isNotEmpty(locationLineList)) {
                    return locationLineList.get(random.nextInt(locationLineList.size()));
                } else {
                    return null;
                }
            }
            case 7: // minimax branch
            {
                int patchNum = this.currentPatches.size();
                int diff = Integer.MAX_VALUE;
                Line findLine = null;
                for (Line line : questionList) {
                    if (CollectionUtils.isEmpty(line.getPatchList())) {
                        continue;
                    }
                    int currentDiff = Math.abs(line.getPatchList().size() - patchNum / 2);
                    if (diff > currentDiff) {
                        diff = currentDiff;
                        findLine = line;
                    }
                }
                return findLine;
            }
            default:
                return null;
        }
    }



    public void processAfterWrongTrace(Line currentLine) {
        Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
        currentPatches.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
        updateListByCandidates(currentLine);
    }

    public void processAfterRightTrace(Line currentLine) {
        currentPatches.clear();
        currentPatches.addAll(currentLine.getPatchList());
        updateListByCandidates(currentLine);

    }

    private void updateListByCandidates(Line currentLine) {

        Set<String> currentPatchSet = currentPatches.stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                .collect(Collectors.toSet());
        String currentPatchString = currentLine.getPatchList().stream().map(PatchFile::getPatchName)
                .sorted(String::compareToIgnoreCase).collect(Collectors.joining(","));
        for (Line lineInfo : questionList) {
            String linePatchString = lineInfo.getPatchList().stream().map(PatchFile::getPatchName)
                    .sorted(String::compareToIgnoreCase).collect(Collectors.joining(","));

            if (linePatchString.equalsIgnoreCase(currentPatchString) &&
                    (getLineType(currentLine).equalsIgnoreCase(getLineType(lineInfo)))) {
                lineInfo.setPatchList(Collections.emptyList());
            } else {
                List<PatchFile> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
                        .filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
                        .collect(Collectors.toCollection(LinkedList::new));
                lineInfo.setPatchList(modifyPatchList);
            }
        }
        Map<String, Line> lineInfoMap = questionList.stream().filter(Objects::nonNull)
                .filter(line -> line.getPatchList().size() > 0)
                .collect(Collectors.toMap(line -> line.getPatchList().stream()
                        .map(PatchFile::getPatchName).sorted(String::compareToIgnoreCase)
                        .collect(Collectors.joining(",")) + getLineType(line), Function.identity(), (v1, v2) -> v2));
        questionList.clear();
        questionList.addAll(lineInfoMap.values());
        questionList.removeIf(line -> getLineType(line).equalsIgnoreCase(getLineType(currentLine)) &&
                line.getPatchList().stream().map(PatchFile::getPatchName)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining(",")).equals(currentPatchString));
    }
}
