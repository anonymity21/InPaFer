package service;


import config.Constant;
import entity.LineInfo;
import entity.PatchFile;
import entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;
import util.StateType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryService {

    private Subject subject;
    //private Map<String, List<PatchFile>> processMap;
    private List<LineInfo> processList;
    private List<PatchFile> candidatePatchList;

    public QueryService(Subject subject) {
        this.subject = subject;
    }


    private String buildMapPath() {
        StringBuilder mapDir = new StringBuilder();
        mapDir.append(Constant.Record).append("/").append(subject.getName()).append("/").append(subject.getId()).append("/").append(Constant.INSTRUMENT_LINE_DIR);
        return mapDir.append(Constant.INSTRUMENT_MAP).toString();
    }

    /**
     * 初始化 processList<k,v> 和candidatePatchList
     */
    public void initSet() {
        String content = FileIO.readFileToString(buildMapPath());
        candidatePatchList = subject.getPatchList();
        processList = new LinkedList<>();
        //List<String> candidateNameList = candidatePatchList.stream().filter(Objects::nonNull)
        //       .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedList::new));
        Map<String, PatchFile> patchNameMap = candidatePatchList.stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(PatchFile::getPatchName, Function.identity(), (v1, v2) -> v2, LinkedHashMap::new));

        for (String line : content.split("\n")) {
            if (line.trim().equals("")) {
                continue;
            }
            String[] strings = line.split(":");
            List<PatchFile> patchFiles = new LinkedList<PatchFile>();
            for (String patchName : strings[1].trim().split("\t")) {

                if (patchNameMap.get(patchName) != null) {
                    patchFiles.add(patchNameMap.get(patchName));
                }
            }
            if (patchFiles.size() > 0 && patchFiles.size() < candidatePatchList.size()) {
                processList.add(new LineInfo(strings[0].trim(), patchFiles));
            }
        }
        log.info("ProcessList Size: " + processList.size());
        log.info("PatchList Size: " + candidatePatchList.size());
    }


    /*
    每次都选第一个元素
     */
    public LineInfo pickOne() {
        return processList.get(0);
    }

    /**
     * @param currentLine 当前行的回答为否
     *                    patchlist 中移除当前行的所有补丁
     */
    public void processAfterWrongTrace(LineInfo currentLine) {
        Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
        candidatePatchList.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
        currentLine.setStateType(StateType.NO);
        updateProcessList(currentLine);
    }

    /**
     * @param currentLine 当前行的回答为是
     *                    patchlist中全部变为当前行的补丁
     *                    更新了processMap
     */
    public void processAfterRightTrace(LineInfo currentLine) {
        candidatePatchList = currentLine.getPatchList();
        currentLine.setStateType(StateType.YES);
        updateProcessList(currentLine);
    }

    /**
     * 更新 processList
     * 首先将 不存在candidateList中的补丁去掉
     * 然后将当前行 设为no
     */
    private void updateProcessList(LineInfo currentLineInfo) {
        Set<String> currentPatchSet = candidatePatchList.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());

        for (LineInfo lineInfo : processList) {
            List<PatchFile> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
                    .filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toCollection(LinkedList::new));
            lineInfo.setPatchList(modifyPatchList);
            if (compareTwoLine(currentLineInfo, lineInfo)) {
                if (lineInfo.getStateType() == StateType.UNCLEAR) {
                    lineInfo.setStateType(currentLineInfo.getStateType());
                } else {
                    log.error(lineInfo.toString());
                }
            }
        }
        processList = processList.stream().filter(Objects::nonNull)
                //.filter(lineInfo -> !currentLineInfo.equals(lineInfo))
                //.filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
                .filter(lineInfo -> (lineInfo.getPatchList().size() >= 0))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean compareTwoLine(LineInfo lineInfo1, LineInfo lineInfo2) {
        String currentLinePatchName1 = lineInfo1.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName)
                .sorted()
                .collect(Collectors.joining());
        String currentLinePatchName2 = lineInfo2.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName)
                .sorted()
                .collect(Collectors.joining());
        return currentLinePatchName1.equals(currentLinePatchName2);
    }

    public boolean isTerminate() {
        boolean result = false;
        List<LineInfo> testLineInfos = processList.stream()
                .filter(Objects::nonNull)
                .filter(lineinfo -> lineinfo.getStateType().equals(StateType.UNCLEAR))
                .filter(lineinfo -> lineinfo.getPatchList().size() > 0)
                .collect(Collectors.toList());
        if (testLineInfos.size() == 0 || candidatePatchList.size() == 1) {
            return true;
        }
        return result;
    }
}
