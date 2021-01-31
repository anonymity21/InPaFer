package patchfilter.model.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationLine;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

// for plugin locationquery
// any answer will not remove locationline
// only change the statetype

public class QueryLocationService {
	
	private Subject subject;
    private List<LocationLine> processList = new ArrayList<LocationLine>();
    private List<PatchFile> candidatePatchList = new ArrayList<PatchFile>();
    
	public QueryLocationService(Subject subject, List<LocationLine> locationList) {
		this.subject = subject;
		this.processList.addAll(locationList);
		candidatePatchList.addAll(subject.getPatchList());
	}
	
	public void processAfterRightTrace(LocationLine currentLine) {
		// TODO Auto-generated method stub
        candidatePatchList = currentLine.getPatchList();
        currentLine.setStateType(StateType.YES);       
        for(LocationLine locationLine: processList) {
        	if(! compareTwoLine(currentLine, locationLine)) {
        		locationLine.setStateType(StateType.NO);
        	}
        }	
	}
	public void processAfterWrongTrace(LocationLine currentLine) {
		// TODO Auto-generated method stub
		  Set<String> currentPatchNameSet = currentLine.getPatchList().stream().filter(Objects::nonNull)
	                .map(PatchFile::getPatchName).collect(Collectors.toCollection(LinkedHashSet::new));
	        candidatePatchList.removeIf(patchFile -> currentPatchNameSet.contains(patchFile.getPatchName()));
	        currentLine.setStateType(StateType.NO);
	}
	
    private boolean compareTwoLine(LocationLine lineInfo1, LocationLine lineInfo2) {
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
    
    public void updateCandidates(List<PatchFile> patchFiles) {
    	this.candidatePatchList.clear();
    	this.candidatePatchList.addAll(patchFiles);
    }
    
    public void updateListByCandidates() {
        Set<String> currentPatchSet = candidatePatchList.stream().filter(Objects::nonNull)
                .map(PatchFile::getPatchName).collect(Collectors.toSet());

        for (LocationLine lineInfo : processList) {
            List<PatchFile> modifyPatchList = lineInfo.getPatchList().stream().filter(Objects::nonNull)
                    .filter(patchFile -> currentPatchSet.contains(patchFile.getPatchName()))
                    .collect(Collectors.toCollection(LinkedList::new));
            lineInfo.setPatchList(modifyPatchList);
        }
        List<LocationLine> tmpList = processList.stream().filter(Objects::nonNull)
        		//.filter(lineInfo -> !currentLineInfo.equals(lineInfo))
                //.filter(lineInfo -> !compareTwoLine(lineInfo, currentLineInfo))
                .filter(lineInfo -> (lineInfo.getPatchList().size() > 0))
                .collect(Collectors.toCollection(LinkedList::new));
        processList = tmpList;
    }


}
