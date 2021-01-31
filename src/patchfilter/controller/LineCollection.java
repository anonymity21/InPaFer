package patchfilter.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Method;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.service.QueryLocationService;
import patchfilter.model.service.QueryService;
import patchfilter.model.service.QueryTraceService;
import patchfilter.model.service.QueryVarService;
import patchfilter.model.entity.Subject;
import patchfilter.util.LocationLine;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.StateType;
import patchfilter.util.TestLine;
//import patchfilter.util.TraceLine;
import patchfilter.util.VariableLine;

@Data
@Slf4j
public class LineCollection {
	private List<LineInfo> traceLinelist = new LinkedList<LineInfo>();
	private List<TestLine> testLinesList = new ArrayList<TestLine>();
	private List<LocationLine> locationLineList = new ArrayList<LocationLine>();
	private List<PatchFile> currentPatches = new ArrayList<PatchFile>();
	private List<VariableLine> varLineList = new ArrayList<VariableLine>();
	

	private Subject subject;

	private QueryLocationService queryLocationService;
	private QueryTraceService queryTraceService;
	private QueryVarService queryVarService;

	public LineCollection(Subject subject) {
		// TODO Auto-generated constructor stub
		this.subject = subject;
	}

	/**
	 * 初始化所有的补丁列表和修改文件列表 生成了 testLineList traceLineList locationLineList processMap
	 */
	public void initSet() {
		clear();
		subject.initPatchListByPath(Constant.AllPatchPath);
		currentPatches.addAll(subject.getPatchList());

		this.initTraceLinelist();
		this.initLocationLineList();
		this.initTestLineList();
		this.iniVarLineList();
		queryLocationService = new QueryLocationService(subject, locationLineList);
		queryTraceService = new QueryTraceService(subject, traceLinelist);
		queryVarService = new QueryVarService(subject, varLineList);
		
	}


	private void clear() {
		traceLinelist.clear();
		testLinesList.clear();
		locationLineList.clear();
		currentPatches.clear();
		varLineList.clear();
	}

	/**
	 * 初始化 最初的提问列表 stateType 都为unclear，并且以 traceline 对象表示
	 */
	public void initTraceLinelist() {
		// tmp test
		this.traceLinelist.clear();
		String content = FileIO.readFileToString(BuildFilePath.tmpMapLine(subject));

		Map<String, PatchFile> patchNameMap = currentPatches.stream().filter(Objects::nonNull).collect(
				Collectors.toMap(PatchFile::getPatchName, Function.identity(), (v1, v2) -> v2, LinkedHashMap::new));

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
			// 只提问能区分开的
			if (patchFiles.size() > 0 && patchFiles.size() < currentPatches.size()) {
				traceLinelist.add(new LineInfo(strings[0].trim(), patchFiles));
			}
		}
	}

	private void initLocationLineList() {

		locationLineList.clear();
		// set modification method for every patch
		for (PatchFile patchFile : subject.getPatchList()) {
			String patchMethodFile = Constant.CACHE + patchFile.getSubject().getName() + "/"
					+ patchFile.getSubject().getId() + "/" + patchFile.getPatchName() + "_method";
			Method method = JSONObject.parseObject(FileIO.readFileToString(patchMethodFile), Method.class);
			patchFile.setModifiedMethod(method.getMethodNameString());
		}

		Map<String, List<PatchFile>> methodPatchMap = subject.getPatchList().stream().filter(Objects::nonNull)
				.collect(Collectors.groupingBy(PatchFile::getModifiedMethod, Collectors.toCollection(LinkedList::new)));

		for (Entry<String, List<PatchFile>> entry : methodPatchMap.entrySet()) {
			LocationLine locationLine = new LocationLine(entry.getKey(), StateType.UNCLEAR, entry.getValue());
			locationLineList.add(locationLine);
			System.out.println(locationLine);
		}

	}

	private void initTestLineList() {
		// TODO Auto-generated method stub
		testLinesList.clear();
		for (String failingTest : subject.getFailedTestList()) {
			TestLine testLine = new TestLine(failingTest, 1, currentPatches.size()); // score is 1 tmp
			testLinesList.add(testLine);
		}
	}
	
	private void iniVarLineList() {
		// TODO Auto-generated method stub
		varLineList.clear();
		VariableCollection variableCollection = new VariableCollection(subject);
		variableCollection.mainProcess();
		this.varLineList.addAll(variableCollection.getVariableLines());
		
	}
	

	/**
	 * @return
	 */
	public List<TestLine> getFailTestList() {
		return testLinesList;
	}


	public void setTraceLinelist(List<LineInfo> traceLinelist) {
		this.traceLinelist = traceLinelist;
	}

	public List<LineInfo> getTraceLinelist() {
		return traceLinelist;
	}

	/**
	 * @param indexLine 给定提问的一行
	 * @param answer    给定答案
	 * @return 一个更新之后的traceLine
	 */
	public void updateTraceLinelist(int indexLine, String answer) {
		// tmp test
		LineInfo currentLine = this.traceLinelist.get(indexLine);

		// StateType stateType = StateType.UNCLEAR;
		if (answer.equals("yes")) {
			queryTraceService.processAfterRightTrace(currentLine);
		} else if (answer.equals("no")) {
			queryTraceService.processAfterWrongTrace(currentLine);
		}
		queryTraceService.updateListByCandidates();
		// can not recorret the answer
		// need to modify
		this.traceLinelist.clear();
		List<LineInfo> lineInfoList = queryTraceService.getProcessList();
		for (LineInfo lineInfo : lineInfoList) {
			// TraceLine traceLine = new TraceLine(lineInfo, lineInfo.getStateType());
			this.traceLinelist.add(lineInfo);
		}
		updateCurrentPatches(queryTraceService.getCandidatePatchFiles());
		//updateTraceByCandidates();
		updateLocationByCandidates();
		updateVariableByCandidates();

	}

	public void restoreList() {
		log.info("Restore Answer!");
		// subject.initPatchList();
		this.initSet();
	}

	public void updateTestLineList() {
		// TODO Auto-generated method stub
		this.testLinesList.clear();
		for (String failingTest : subject.getFailedTestList()) {
			TestLine testLine = new TestLine(failingTest, 1, currentPatches.size()); // score is 1																										// tmp
			testLinesList.add(testLine);
		}
	}

	public void updateLocationLineList(int index, String answer) {
		// TODO Auto-generated method stub
		LocationLine locationLine = this.locationLineList.get(index);
		if (answer.equals("yes")) {
			queryLocationService.processAfterRightTrace(locationLine);
		} else if (answer.equals("no")) {
			queryLocationService.processAfterWrongTrace(locationLine);
		}
		queryLocationService.updateListByCandidates();
		
		this.locationLineList.clear();
		for(LocationLine l: queryLocationService.getProcessList()) {
			this.locationLineList.add(l);
		}
		//this.locationLineList.addAll(queryLocationService.getProcessList());
		updateCurrentPatches(queryLocationService.getCandidatePatchList()); 
		
		updateTraceByCandidates();
		updateVariableByCandidates();
	}
	public void updateVariableLineList(int index, String answer) {
		// TODO Auto-generated method stub
		VariableLine variableLine = this.varLineList.get(index);
		if (answer.equals("yes")) {
			queryVarService.processAfterRightTrace(variableLine);
		} else if (answer.equals("no")) {
			queryVarService.processAfterWrongTrace(variableLine);
		}
		queryVarService.updateListByCandidates();
		
		this.varLineList.clear();
		varLineList.addAll(queryVarService.getProcessList());
		//this.locationLineList.addAll(queryLocationService.getProcessList());
		updateCurrentPatches(queryVarService.getCandidatePatchFiles()); 	
		updateTraceByCandidates();
		updateLocationByCandidates();
	}
	
	public void updateCurrentPatches(List<PatchFile> patchFiles) {
		this.currentPatches.clear();
		this.currentPatches.addAll(patchFiles);
	}
	
	
	public void deleteCandidatePatches(PatchFile patchFile) {
		//this.currentPatches.clear();
		currentPatches = currentPatches.stream().filter(Objects::nonNull)
				.filter(patch -> !patchFile.getPatchName().equalsIgnoreCase(patch.getPatchName())).collect(Collectors.toList());
		
		this.updateLocationByCandidates();
		this.updateTraceByCandidates();
		this.updateVariableByCandidates();
	}
	
	public void updateTraceByCandidates() {
		queryTraceService.updateCandidates(this.currentPatches);
		queryTraceService.updateListByCandidates();
		this.traceLinelist.clear();
		this.traceLinelist.addAll(queryTraceService.getProcessList());		
	}
	public void updateLocationByCandidates() {
		queryLocationService.updateCandidates(this.currentPatches);
		queryLocationService.updateListByCandidates();
		this.locationLineList.clear();
		this.locationLineList.addAll(queryLocationService.getProcessList());
	}
	
	public void updateVariableByCandidates() {
		queryVarService.updateCandidates(this.currentPatches);
		queryVarService.updateListByCandidates();
		this.varLineList.clear();
		varLineList.addAll(queryVarService.getProcessList());
	}



}
