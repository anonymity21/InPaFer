package service;

import entity.LineInfo;
import entity.PatchFile;
import entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.StateType;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryTraceService {
    private Subject subject;
    private List<LineInfo> processList = new ArrayList<>();
    private List<PatchFile> candidatePatchFiles = new ArrayList<>();

    public QueryTraceService(Subject subject, List<LineInfo> traceLinelist) {
        this.subject = subject;
        this.processList.addAll(traceLinelist);
        this.candidatePatchFiles.addAll(subject.getPatchList());
    }

    public void processAfterRightTrace(LineInfo currentLine) {
        Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
        candidatePatchFiles = candidatePatchFiles.stream().filter(Objects::nonNull)
                .filter(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()))
                .collect(Collectors.toList());
        // candidatePatchFiles.addAll(currentLine.getPatchList());
        currentLine.setStateType(StateType.YES);

        String currents = currentLine.getPatchList().stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                .sorted().collect(Collectors.joining());
        for (LineInfo lineInfo : processList) {
            String tmpPatches = lineInfo.getPatchList().stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                    .sorted().collect(Collectors.joining());
            if (tmpPatches.equals(currents)) {
                lineInfo.setStateType(StateType.YES);
            }
        }
    }

    public void processAfterWrongTrace(LineInfo currentLine) {
        Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
        candidatePatchFiles.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
        currentLine.setStateType(StateType.NO);

        String currents = currentLine.getPatchList().stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                .sorted().collect(Collectors.joining());
        for (LineInfo lineInfo : processList) {
            String tmpPatches = lineInfo.getPatchList().stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
                    .sorted().collect(Collectors.joining());
            if (tmpPatches.equals(currents)) {
                lineInfo.setStateType(StateType.NO);
            }
        }

    }

    public void updateCandidates(List<PatchFile> patchFiles) {
        this.candidatePatchFiles.clear();
        this.candidatePatchFiles.addAll(patchFiles);
    }

    public void updateListByCandidates() {
        Set<String> currentPatchSet = candidatePatchFiles.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());

        for (LineInfo lineInfo : processList) {
            List<PatchFile> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
                    .filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toCollection(LinkedList::new));
            lineInfo.setPatchList(modifyPatchList);
        }
        processList = processList.stream().filter(Objects::nonNull)
                .filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
