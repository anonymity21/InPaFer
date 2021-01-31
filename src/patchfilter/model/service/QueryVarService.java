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
import patchfilter.util.VariableLine;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryVarService {

	private Subject subject;
	private List<VariableLine> processList = new ArrayList<VariableLine>();
	private List<PatchFile> candidatePatchFiles = new ArrayList<PatchFile>();

	public QueryVarService(Subject subject, List<VariableLine> varLineList) {
		// TODO Auto-generated constructor stub
		this.subject = subject;
		this.processList.addAll(varLineList);
		this.candidatePatchFiles.addAll(subject.getPatchList());
	}

	public void processAfterRightTrace(VariableLine currentLine) {
		// TODO Auto-generated method stub
		candidatePatchFiles.clear();
		candidatePatchFiles.addAll(currentLine.getPatchFiles());
		currentLine.setStateType(StateType.YES);
		updateListByCandidates();
	}

	public void processAfterWrongTrace(VariableLine currentLine) {
		// TODO Auto-generated method stub
		Set<String> currentPatchNameSet = currentLine.getPatchFiles().stream().filter(Objects::nonNull)
				.map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
		candidatePatchFiles.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
		currentLine.setStateType(StateType.NO);
		updateListByCandidates();
	}

	public void updateCandidates(List<PatchFile> currentPatches) {
		// TODO Auto-generated method stub
		this.candidatePatchFiles.clear();
		this.candidatePatchFiles.addAll(currentPatches);
	}

	public void updateListByCandidates() {
		// TODO Auto-generated method stub
		Set<String> currentPatchSet = candidatePatchFiles.stream().filter(Objects::nonNull).map(PatchFile::getPatchName)
				.collect(Collectors.toSet());

		for (VariableLine lineInfo : processList) {
			List<PatchFile> modifyPatchList = lineInfo.getPatchFiles().stream().filter(Objects::nonNull)
					.filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
					.collect(Collectors.toCollection(LinkedList::new));
			lineInfo.setPatchFiles(modifyPatchList);
		}
		List<VariableLine> tmpList = processList.stream().filter(Objects::nonNull)
				// .filter(lineInfo -> !currentLineInfo.equals(lineInfo))
				// .filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
				.filter(lineInfo -> (lineInfo.getPatchFiles().size() > 0))
				.filter(lineInfo -> lineInfo.getStateType().equals(StateType.UNCLEAR))
				.collect(Collectors.toCollection(LinkedList::new));
		processList = tmpList;
	}

	public boolean isTerminate() {
		boolean result = false;
		List<VariableLine> testLineInfos = processList.stream().filter(Objects::nonNull)
				.filter(variableLine -> variableLine.getStateType().equals(StateType.UNCLEAR))
				.filter(variableLine -> variableLine.getPatchFiles().size() > 0).collect(Collectors.toList());
		if (testLineInfos.size() == 0 || candidatePatchFiles.size() == 1) {
			return true;
		}
		return result;
	}

	public VariableLine pickOne() {
		// TODO Auto-generated method stub
		return processList.get(0);
	}
}
