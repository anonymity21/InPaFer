package statistics;

import com.alibaba.fastjson.JSONObject;
import config.Constant;
import entity.CorrectPatch;
import entity.PatchFile;
import entity.SimplePatchFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import util.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Calculation {



    public static void bugTechStatistic(){

        Set<String> techNameSet = new HashSet<>();
        for(File projectFile : Objects.requireNonNull(new File(Constant.AllPatchPath).listFiles())){
            if(projectFile.getName().startsWith(".DS")){
                continue;
            }
            System.out.println("Process " + projectFile.getName());
            for(File file: projectFile.listFiles() ){
                if(file.getName().startsWith(".DS")){
                    continue;
                }

                Set<String>   tmpSet = Arrays.stream(file.listFiles()).filter(Objects::nonNull)
                        .filter(patchFile->! patchFile.getName().startsWith(".DS"))
                        .map(patchFile->patchFile.getName().split("-")[0])
                        .collect(Collectors.toSet());
                techNameSet.addAll(tmpSet);
            }

        }
        System.out.println(techNameSet.size());
        System.out.println(techNameSet.stream().collect(Collectors.joining(" ")));

    }
    public static void bugPatchNumber(){

        String resultFile = "./analysis.csv";
        StringBuilder result = new StringBuilder();

        String project = "Time";
        for(File file : Objects.requireNonNull(new File(Constant.AllPatchPath + "/" + project).listFiles())){
            if(file.getName().startsWith(".DS")){
                continue;
            }
            Set<SimplePatchFile> simplePatchFileList = new HashSet<>();
            log.info("Process " + project + file.getName() );

            for(File patchFile: Objects.requireNonNull(file.listFiles())){
                if(patchFile.getName().startsWith(".DS")){
                    continue;
                }
                SimplePatchFile simplePatchFile = new SimplePatchFile(patchFile);
                simplePatchFile.initContent();
                simplePatchFileList.add(simplePatchFile);
            }
            Set<File> fileSet = Arrays.stream(file.listFiles()).filter(Objects::nonNull)
                    .filter(patchFile->!patchFile.getName().startsWith(".DS"))
                    .collect(Collectors.toSet());
            result.append(project + file.getName()).append(",")
                    .append(simplePatchFileList.size()).append(",")
                    .append(fileSet.size()).append("\n");

        }
        FileIO.writeStringToFile(resultFile, result.toString(), true);
    }
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
                    result.append(projectid).append(",C\n");
                }else {
                    result.append(projectid).append(",NC\n");
                }
            }
        }
        FileIO.writeStringToFile(resultFile, result.toString(), true);
    }



    public static void main(String[] arg){
        calculateCorrectness();
    }
}
