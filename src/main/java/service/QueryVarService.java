package service;

import entity.PatchFile;
import entity.Subject;
import entity.VariableLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.StateType;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryVarService {

    private Subject subject;
    private List<VariableLine> processList = new ArrayList<VariableLine>();
    private List<PatchFile> candidatePatchFiles = new ArrayList<PatchFile>();

    public QueryVarService(Subject subject, List<VariableLine> varLineList) {
        this.subject = subject;
        this.processList.addAll(varLineList);
        this.candidatePatchFiles.addAll(subject.getPatchList());
    }

    public void processAfterRightTrace(VariableLine currentLine) {
        candidatePatchFiles.clear();
        candidatePatchFiles.addAll(currentLine.getPatchList());
        currentLine.setStateType(StateType.YES);
        updateListByCandidates();
    }

    public void processAfterWrongTrace(VariableLine currentLine) {
        Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
        candidatePatchFiles.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
        currentLine.setStateType(StateType.NO);
        updateListByCandidates();
    }

    public void updateCandidates(List<PatchFile> currentPatches) {
        this.candidatePatchFiles.clear();
        this.candidatePatchFiles.addAll(currentPatches);
    }

    public void updateListByCandidates() {
        Set<String> currentPatchSet = candidatePatchFiles.stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                .collect(Collectors.toSet());

        for (VariableLine lineInfo : processList) {
            List<PatchFile> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
                    .filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toCollection(LinkedList::new));
            lineInfo.setPatchList(modifyPatchList);
        }
        processList = processList.stream().filter(Objects::nonNull)
                // .filter(lineInfo -> !currentLineInfo.equals(lineInfo))
                // .filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
                .filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
                .filter(lineInfo -> lineInfo.getStateType().equals(StateType.UNCLEAR))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean isTerminate() {
        List<VariableLine> testLineInfos = processList.stream().filter(Objects::nonNull)
                .filter(variableLine -> variableLine.getStateType().equals(StateType.UNCLEAR))
                .filter(variableLine -> variableLine.getPatchList().size() > 0).collect(Collectors.toList());
        return testLineInfos.size() == 0 || candidatePatchFiles.size() == 1;
    }

    public VariableLine pickOne() {
        return processList.get(0);
    }
}
