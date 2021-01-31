package patchfilter.model.experiment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import patchfilter.controller.VariableCollection;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.entity.Variable;
import patchfilter.model.util.FileIO;
import patchfilter.util.VariableLine;

import java.util.*;


@Slf4j
public class ValueAnalysis {

    Subject subject;
    Map<String, Map<String, List<PatchFile>>> valueMap = new HashMap<String,  Map<String, List<PatchFile>>>();

    public ValueAnalysis(Subject subject) {
        this.subject = subject;
    }
    private String buildValuePath(String patchName) {
        StringBuilder valueDir = new StringBuilder();
        valueDir.append(Constant.Record).append("/").append(subject.getName()).append("/").append(subject.getId()).append("/").append(Constant.INSTRUMENT_STATE_DIR);
        String valuePath = valueDir.append(patchName).append(Constant.INSTRUMENT_STATE_SEPARATORINIT).toString();
        return valuePath;
    }

    public void partitionEquivalence() {
    	
    	StringBuilder resultBuilder = new StringBuilder();
    	String resultPath = "./varanalysis.csv";    	
    	resultBuilder.append(subject.getName()+subject.getId()).append(",").append(subject.getPatchList().size()).append(",");
    	
    	VariableCollection variableCollection = new VariableCollection(subject);
		variableCollection.mainProcess();
		List<VariableLine> variableLines = variableCollection.getVariableLines();
		for(VariableLine variableLine: variableLines) {
			String key = variableLine.getVarName();
			if(valueMap.containsKey(key)) {
					//if(valueMap.get(key).get(key))		
				  valueMap.get(key).put(variableLine.getValue(), variableLine.getPatchFiles());			   
			}else {
				Map<String, List<PatchFile>> tmpMap = new HashMap<String, List<PatchFile>>();
				tmpMap.put(variableLine.getValue(), variableLine.getPatchFiles());
				valueMap.put(key, tmpMap);
			}
		}
		int maxPartition = 0;
		for(Map<String, List<PatchFile>> map: valueMap.values()) {
			if(map.size() > maxPartition) {
				maxPartition = map.size();
			}
		}
		
        log.info("Total Patches: " + subject.getPatchList().size());
        log.info("Equivalence Size: " + maxPartition);
        resultBuilder.append(maxPartition).append("\n");
        FileIO.writeStringToFile(resultPath, resultBuilder.toString(), true);
    }

    public static void main(String[] args) {
        String project = "Chart";
        int Start = 13;
        int end = 13;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
           // subject.initPatchListByPath(Constant.AllPatchPath);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                //FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                ValueAnalysis valueAnalysis = new ValueAnalysis(subject);
                valueAnalysis.partitionEquivalence();
                //valueAnalysis.findDifference();
                //valueAnalysis.findDiff4SingleJson();
            }
        }
    }
}
