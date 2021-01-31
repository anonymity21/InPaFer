package patchfilter.model.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;

@Slf4j
public class MapService {

	// for all file
	public static void generateMapFile(Subject subject) {
		List<PatchFile> patchFiles = subject.getPatchList();
		Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();

		String traceContent = FileIO.readFileToString(BuildFilePath.tmpLine("initial", subject));
		if (traceContent.equals("")) {
			log.error("Init trace is empty");
		}
		for (String line : traceContent.split("\n")) {
			if (line.contains("START#0")) {
				continue;
			}
			if (linePatchMap.containsKey(line)) {
				linePatchMap.get(line).add("Init");
			} else {
				Set<String> tmpList = new LinkedHashSet<>();
				tmpList.add("Init");
				linePatchMap.put(line, tmpList);
			}
		}

		for (PatchFile patchFile : patchFiles) {
			traceContent = FileIO.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject));
			if (traceContent.equals("")) {
				log.error(patchFile.getPatchName() + " trace is empty");
			}
			for (String line : traceContent.split("\n")) {
				if (line.contains("START#0")) {
					continue;
				}
				if (linePatchMap.containsKey(line)) {
					linePatchMap.get(line).add(patchFile.getPatchName());
				} else {
					Set<String> tmpList = new LinkedHashSet<>();
					tmpList.add(patchFile.getPatchName());
					linePatchMap.put(line, tmpList);
				}
			}
		}
		StringBuilder result = new StringBuilder();
		for (Map.Entry entry : linePatchMap.entrySet()) {
			Set<String> tmpList = (Set<String>) entry.getValue();
			if (tmpList.size() < patchFiles.size() + 1) {
				result.append(entry.getKey() + ":");
				for (String patchName : tmpList) {
					result.append(patchName).append("\t");
				}
				result.append("\n");
			}
		}
		// log.info(subject.getPatchList().stream().map(PatchFile::getPatchName).collect(Collectors.joining("\t")));
		// log.info(result.toString());

		FileIO.writeStringToFile(BuildFilePath.tmpMapLine(subject), result.toString(), false);
	}

	// uncompleted method
	public void compareByParagraphWInitial(Subject subject) {
		List<PatchFile> patchFiles = subject.getPatchList();
		Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();

		String traceContent = FileIO.readFileToString(BuildFilePath.tmpLine("initial", subject));
		String splitLine = traceContent.split("\n")[0];

		if (!splitLine.endsWith("START#0")) {
			log.error("First Line is not START#0");
			return;
		}

		String initialSplitContent[] = traceContent.split(splitLine);
		for (int i = 0; i < initialSplitContent.length; i++) {
			for (PatchFile patchFile : patchFiles) {
				traceContent = FileIO
						.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject));
				if (traceContent.equals("")) {
					log.error(patchFile.getPatchName() + " trace is empty");
				}
				if (!traceContent.split("\n")[0].equals(splitLine)) {
					log.error(patchFile.getPatchName() + " first line is different with initial");
				}
				String patchSplitContent[] = traceContent.split(splitLine);
				if (patchSplitContent[i].equals(initialSplitContent[i])) {
					continue;
				} else {

				}

			}
		}

	}

	// for one paragraph
	public static void compareByParagraphOInitial(Subject subject) {
		List<PatchFile> patchFiles = subject.getPatchList();
		Map<String, Set<String>> linePatchMap = new LinkedHashMap<>();
		StringBuilder result = new StringBuilder();
		int index = 0;
		boolean findDifference = false;
		while (!findDifference) {
			// for index round
			linePatchMap.clear();
			for (PatchFile patchFile : patchFiles) {
				String traceContent = FileIO
						.readFileToString(BuildFilePath.tmpMapTraceLine(patchFile.getPatchName(), subject));
				if (traceContent.equals("")) {
					log.error(patchFile.getPatchName() + " trace is empty");
				}
				String splitLine = traceContent.split("\n")[0];
				String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
				for (String key : fbsArr) {
					if (splitLine.contains(key)) {
						splitLine = splitLine.replace(key, "\\" + key);
					}
				}
				String patchSplitContent[] = traceContent.split(splitLine+"\n");
				//System.out.println(traceContent);
				if (index >= patchSplitContent.length) {
					log.error(patchFile.getPatchName() + " paragraph length is different!");
					continue;
				}
				for (String line : patchSplitContent[index].split("\n")) {
					if (line.contains("START#0") || line.equals("")) {
						continue;
					}
					if (linePatchMap.containsKey(line)) {
						linePatchMap.get(line).add(patchFile.getPatchName());
					} else {
						Set<String> tmpList = new LinkedHashSet<>();
						tmpList.add(patchFile.getPatchName());
						linePatchMap.put(line, tmpList);
					}
				}

			}
			if(linePatchMap.size() != 0) {
				for (Map.Entry entry : linePatchMap.entrySet()) {
					Set<String> tmpList = (Set<String>) entry.getValue();
					if (tmpList.size() < patchFiles.size()) {
						result.append(entry.getKey() + ":");
						for (String patchName : tmpList) {
							result.append(patchName).append("\t");
						}
						result.append("\n");
					}
				}
				if (!result.toString().equals("")) {
					findDifference = true;
				}
			}			
			index++;
		}
		FileIO.writeStringToFile(BuildFilePath.tmpMapLine(subject), result.toString(), false);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Subject subject = new Subject("Lang", 14);
		if (subject.initPatchListByPath(Constant.AllPatchPath)) {
			MapService mapService = new MapService();
			mapService.compareByParagraphOInitial(subject);
		}
	}

}
