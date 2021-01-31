package patchfilter.model.experiment;

import java.util.List;

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
import patchfilter.model.util.FileIO;

@Slf4j
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAnalysis {
	
	private Subject subject;
	
	public void MainProcess() {
		String resultFile = "./SubjectAnalysis.csv";
		StringBuilder result = new StringBuilder();	
		result.append(subject.getName()).append(subject.getId()).append(",")
		.append(subject.getPatchList().size()).append(",").append(containsCorretPatch()).append("\n");
		System.out.println(result.toString());
		FileIO.writeStringToFile(resultFile, result.toString(), true);
		
	}
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
	
    public static void main(String[] args) {
        String project = "Time";
        int Start = 1;
        int end = 27;
        for (int i = Start; i <= end; i++) {
            Subject subject = new Subject(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                SubjectAnalysis subjectAnalysis = new SubjectAnalysis(subject);
                subjectAnalysis.MainProcess();
            }
        }
    }
}
