package patchfilter.model.Main;


import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.entity.Subject;
import patchfilter.model.service.MapService;
import patchfilter.model.service.MethodTraceService;
import patchfilter.model.util.BuildFilePath;
import patchfilter.model.util.FileIO;
import patchfilter.model.util.PatchInfo;
import patchfilter.model.service.TraceMapService;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class PatchVariationMain {

	// private Set<String> modifiedMethodSet = new LinkedHashSet<>();
	private Map<String, List<PatchFile>> fixedFileMap = new LinkedHashMap<>();
	public static String resultFile = Constant.LOG_FILE + "ObtainTrace.log";

	public void initModifiedMethodMap(List<PatchFile> patchFileList) {
		fixedFileMap = patchFileList.stream().filter(Objects::nonNull).collect(Collectors
				.groupingBy(PatchFile::getFixedFile, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
	}

	public void mainProcessForLine(Subject subject) {
		List<PatchFile> patchFileList = subject.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<String> allDelteLine = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getDeleteLine)
				.flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));

		log.info("RUNNING inital Program for Line ");

		MethodTraceService methodTraceService = new MethodTraceService();
		fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
		String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
				+ Constant.INSTRUMENT_LINE_DIR + Constant.INSTRUMENT_INIT_SEPARATORINIT
				+ Constant.INSTRUMENT_LINE_SEPARATORINIT;

		if (new File(writeFile).exists()) {
			new File(writeFile).delete();
		}
		
		methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);
		if (methodTraceService.Compile(subject)) {
			methodTraceService.runFailTest(subject);
		}
		deleteFixedLine(writeFile, allDelteLine);
		fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

		for (PatchFile patchFile : patchFileList) {
			log.info("RUNNING Patch " + patchFile.getPatchName());
			fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
			if (patchFile.patchToFile()) {
				log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
				writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
						+ Constant.INSTRUMENT_LINE_DIR + patchFile.getPatchName()
						+ Constant.INSTRUMENT_LINE_SEPARATORINIT;
				if (new File(writeFile).exists()) {
					new File(writeFile).delete();
				}
				methodTraceService.instrumentLineNumber(fixedFileMap, writeFile);
				if (methodTraceService.Compile(subject)) {
					methodTraceService.runFailTest(subject);
				}
			} else {
				log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
				FileIO.writeStringToLog(resultFile, "Patch " + patchFile.getPatchName() + " Patches File Fail.");
			}
			fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

			TraceMapService traceMapService = new TraceMapService(patchFile);
			traceMapService.traceMap(allDelteLine);
		}
		//MapService.generateMapFile(subject);
		MapService.compareByParagraphOInitial(subject);
	}

	private void deleteFixedLine(String writeFile, Set<String> allDelteLine) {

		String content = FileIO.readFileToString(writeFile);
		StringBuilder result = new StringBuilder();
		for (String line : content.split("\n")) {
			String className = line.split("#")[0];
			if(className.contains("$")) {
				className = className.split("\\$")[0];
			}
			className = className + ".java";
			Integer lineNum = Integer.valueOf(line.split("#")[line.split("#").length - 1]);
			String tmpLine = className + "#" + lineNum;
			boolean delete = false;
			for (String delteLine : allDelteLine) {
				if (delteLine.contains(tmpLine)) {
					delete = true;
					break;
				}
			}
			if (delete) {
				continue;
			}
			result.append(line).append("\n");

		}
		FileIO.writeStringToFile(writeFile, result.toString(), false);

	}

	public void mainProcessForState(Subject subject) {

		List<PatchFile> patchFileList = subject.getPatchList();
		Set<String> fixedFileSet = patchFileList.stream().filter(Objects::nonNull).map(PatchFile::getFixedFile)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		log.info("RUNNING inital Program for State ");

		MethodTraceService methodTraceService = new MethodTraceService();

		fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
		String writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
				+ Constant.INSTRUMENT_STATE_DIR + Constant.INSTRUMENT_INIT_SEPARATORINIT
				+ Constant.INSTRUMENT_STATE_SEPARATORINIT;
		if (new File(writeFile).exists()) {
			new File(writeFile).delete();
		}
		methodTraceService.instrumentState(fixedFileMap, writeFile);
		if (methodTraceService.Compile(subject)) {
			//FileIO.writeStringToFile(Constant.HOME + "/math80-97.log", subject.toString() + " need to check!", true);
			methodTraceService.runFailTest(subject);
		}
		fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));

		for (PatchFile patchFile : patchFileList) {
			log.info("RUNNING Patch " + patchFile.getPatchName());
			fixedFileSet.forEach(fixedFile -> FileIO.backUpFile(fixedFile, fixedFile + ".bak"));
			if (patchFile.patchToFile()) {
				log.info("Patch " + patchFile.getPatchName() + " Patches File Success.");
				writeFile = Constant.Record + subject.getName() + "/" + subject.getId() + "/"
						+ Constant.INSTRUMENT_STATE_DIR + patchFile.getPatchName()
						+ Constant.INSTRUMENT_STATE_SEPARATORINIT;
				if (new File(writeFile).exists()) {
					new File(writeFile).delete();
				}
				methodTraceService.instrumentState(fixedFileMap, writeFile);
				if (methodTraceService.Compile(subject)) {
					methodTraceService.runFailTest(subject);
				}
			} else {
				log.error("Patch " + patchFile.getPatchName() + " Patches File Fail.");
				FileIO.writeStringToLog(resultFile, "Patch " + patchFile.getPatchName() + " Patches File Fail.");
			}
			fixedFileSet.forEach(fixedFile -> FileIO.restoreFile(fixedFile, fixedFile + ".bak"));
		}
	}


	public static void main(String[] args) {
		String project = "Math";
		int Start = 41;
		int end = 41;
		for(int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-project=")) {
                // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = args[i].substring("-project=".length());
            } else if (args[i].startsWith("-start=")) {
                start = Integer.parseInt(args[i].substring("-start=".length()));
            } else if (args[i].startsWith("-end=")) {
                end = Integer.parseInt(args[i].substring("-end=".length()));
            }
        }
		for (int i = Start; i <= end; i++) {
			Subject subject = new Subject(project, i);
			if (subject.initPatchListByPath(Constant.AllPatchPath)) {
				log.info("Process " + subject.toString());
				FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
				List<PatchFile> patchList = subject.getPatchList();
				
				MethodInitialization methodInitialization = new MethodInitialization(subject);
				methodInitialization.MainProcess();
				methodInitialization.MainProcess4TestFile();
				
				PatchVariationMain patchVariationMain = new PatchVariationMain();		
				patchVariationMain.initModifiedMethodMap(patchList);
				patchVariationMain.mainProcessForLine(subject);
				patchVariationMain.mainProcessForState(subject);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
