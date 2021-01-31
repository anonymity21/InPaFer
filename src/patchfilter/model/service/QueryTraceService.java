package patchfilter.model.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationLine;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryTraceService {
	private Subject subject;
	private List<LineInfo> processList = new ArrayList<LineInfo>();
	private List<PatchFile> candidatePatchFiles = new ArrayList<PatchFile>();

	public QueryTraceService(Subject subject, List<LineInfo> traceLinelist) {
		// TODO Auto-generated constructor stub
		this.subject = subject;
		this.processList.addAll(traceLinelist);
		this.candidatePatchFiles.addAll(subject.getPatchList());
	}

	public void processAfterRightTrace(LineInfo currentLine) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
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
        List<LineInfo> tmpList = processList.stream().filter(Objects::nonNull)
                .filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
                .collect(Collectors.toCollection(LinkedList::new));
        processList = tmpList;
    }

}
