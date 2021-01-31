package patchfilter.model.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;

import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.CorrectPatch;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.run.Runner;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodTraceAnalysis {
	
	private Subject subject;
	
	
	public void MainProcess() {
		List<PatchFile> patchFiles = subject.getPatchList();
	
		HashMap<String, List<PatchFile>> traceMap = new LinkedHashMap<String, List<PatchFile>>();
		for(PatchFile patchFile: patchFiles) {
			String trace = FileIO.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject));
			if(traceMap.containsKey(trace)) {
				traceMap.get(trace).add(patchFile);
			}else {
				List<PatchFile> tmpList = new LinkedList<PatchFile>();
				tmpList.add(patchFile);
				traceMap.put(trace, tmpList);
			}
		}				
		//findDifferenceTraces(traceMap);
		/*String resultFile = "./MethodLineAnalysis.csv";
		StringBuilder result = new StringBuilder();	
		result.append(subject.getName()).append(subject.getId()).append(",")
		.append(traceMap.size()).append(",").append("\n");
		System.out.println(result.toString());
		FileIO.writeStringToFile(resultFile, result.toString(), true);		*/
		
	}
	
	// set correctness for every patch
    private boolean containsCorretPatch() {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<PatchFile> patchFileList = subject.getPatchList();
        for (PatchFile patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(), String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {
                patchFile.setCorrectness(true);
                contains = true;
            }
        }
        return contains;
    }
    
	public String getDiff(String initFile, String patchFile) {
			String resultFilePath = "./test";
			Runner.diff2File(initFile, patchFile, resultFilePath);
		    return FileIO.readFileToString(resultFilePath);       
    }
 
	
	public void findDifferenceTraces(HashMap<String, List<PatchFile>> traceMap) {
		//  颜色标识 不同的trace
		if(traceMap.size() == 1) {		
			
		}else if(traceMap.size() == 2) {   // 两组 trace 不同，需要找到第一条不同的语句
	        List<List<PatchFile>> list = new ArrayList<>(traceMap.values());
	        String patchFilePath1 = BuildFilePath.tmpMapTraceLine(list.get(0).get(0).getPatchName(), subject);
	        String patchFilePath2 = BuildFilePath.tmpMapTraceLine(list.get(1).get(0).getPatchName(), subject);
	        String result = getDiff(patchFilePath1, patchFilePath2);
		}else {
			
		}
		
	}
	
	 public static void main(String[] args) {
	        String project = "Chart";
	        int Start = 25;
	        int end = 25;
	        for (int i = Start; i <= end; i++) {
	            Subject subject = new Subject(project, i);
	            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
	                log.info("Process " + subject.toString());
	                MethodTraceAnalysis methodTraceAnalysis = new MethodTraceAnalysis(subject);
	                methodTraceAnalysis.MainProcess();
	            }
	        }
	    }
}
