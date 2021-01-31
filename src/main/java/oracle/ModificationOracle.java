package oracle;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.CorrectPatch;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ModificationOracle {

    public static void calculateCorrectness(){

        String resultFile = "./analysis.csv";
        StringBuilder result = new StringBuilder();

        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        Set<String> correctProjectSet = correctPatchList.stream().filter(Objects::nonNull)
                .map(correctPatch -> correctPatch.getProject() + correctPatch.getBugid())
                .collect(Collectors.toSet());

        for(File project : Objects.requireNonNull(new File(Constant.AllPatchPath ).listFiles())) {
            if (project.getName().startsWith(".DS")) {
                continue;
            }
            log.info("Process "  + project.getName());
            for(File bugId: project.listFiles()){
                if (bugId.getName().startsWith(".DS")) {
                    continue;
                }
                String projectid = project.getName()+bugId.getName();
                if(correctProjectSet.contains(projectid)){
                    result.append(projectid).append(",");
                    List<CorrectPatch> tmpCorrectPatches =  correctPatchList.stream().filter(Objects::nonNull)
                            .filter(correctPatch ->
                                    correctPatch.getBugid().equals(bugId.getName())
                                            && correctPatch.getProject().equals(project.getName()))
                            .collect(Collectors.toList());

                }
            }
        }
        FileIO.writeStringToFile(resultFile, result.toString(), true);
    }



    public static void main(String[] arg){
        calculateCorrectness();
    }
}
