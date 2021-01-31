package script;


import config.Constant;
import entity.PatchFile;
import entity.SimplePatchFile;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
This file processes patches, including moving tmpPatches(in avatar and kpar dir) to corresponding directories
and deleting the patches that is repeated.
 */
@Slf4j
public class PatchProcession {

    //private static String patchPath = Constant.BProcessPatches + "/Avatar/FixedBugs/tmpFixedBugs";
    //private static String patchPath = Constant.BProcessPatches + "/Kpar/FullyFixedBugs/tmpFixedBugs";
    private static String patchPath = Constant.BProcessPatches + "/3SFix/tmpFixedBugs";

    //private static String targetPath = Constant.AllPatchPath;

    public static  void moveFile(){
        for(File file: Objects.requireNonNull(new File(patchPath).listFiles())){
            if(file.getName().startsWith(".DS")){
                continue;
            }
            String fileName = file.getName();
            System.out.println("Process " + fileName);
            String projectName = fileName.split("_")[0];
            String id = fileName.split("_")[1];
            String targetPath = Constant.AllPatchPath + "/" + projectName + "/" + id ;
            if(! new File(targetPath).exists()){
                new File(targetPath).mkdirs();
            }
            for(File patchFile: file.listFiles()){
                if(file.getName().startsWith(".DS")){
                    continue;
                }
                try {
                    //FileUtils.deleteDirectory(new File(targetPath + "/" +patchFile.getAbsolutePath().split(patchFile.getName())[0]));
                   FileUtils.copyFile(patchFile, new File(targetPath + "/" +patchFile.getName()));
                          //  + patchFile.getName().replaceAll("Avatar", "Kpar").split(".txt")[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }
    public static void deleteRepeated(){
        String project = "Chart";
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
            Set<String> patchNameSet = simplePatchFileList.stream().filter(Objects::nonNull)
                    .map(simplePatchFile -> simplePatchFile.getPatchFile().getName())
                    .collect(Collectors.toCollection(HashSet::new));

            for(File patchFile: Objects.requireNonNull(file.listFiles())){
                if(patchFile.getName().startsWith(".DS")){
                    continue;
                }

                if(!patchNameSet.contains(patchFile.getName()) ){
                    log.info("Delete Dirctory " + project + file.getName() + " " + patchFile.getName() + " is repeated");
                    //patchFile.delete();
                    String targetPath = patchFile.getAbsolutePath().replaceAll("TotalPatch", "tmp");
                    try {
                        FileUtils.copyFile(patchFile, new File( targetPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    patchFile.delete();
                }
            }
        }
    }
    public static void deleteSinlgeAndTxt(){
        String project = "Closure";
        for(File file : Objects.requireNonNull(new File(Constant.AllPatchPath + "/" + project).listFiles())){
            if(file.getName().startsWith(".DS")){
                continue;
            }
            log.info("Process " + project + file.getName() );
            // delete the dic which has only 1 patch
            Set<File> fileSet = Arrays.stream(file.listFiles()).filter(Objects::nonNull)
                    .filter(patchFile-> ! patchFile.getName().startsWith(".DS"))
                    .collect(Collectors.toSet());
            if(Objects.requireNonNull(fileSet).size() <= 1){
                log.info("Delete Dirctory " + project + file.getName() + ", Length is 1");
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for(File patchFile: Objects.requireNonNull(file.listFiles())){

                if(patchFile.getName().endsWith(".txt")){
                    log.info("Delete Dirctory " + project + file.getName() + " " + patchFile.getName());
                    patchFile.delete();
                    continue;
                }
            }
        }
    }
    public static void bugTechStatistic(){
        //String project = "Math";
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


    public static void main(String[] arg){
        //deleteRepeated();
        deleteSinlgeAndTxt();
       //bugTechStatistic();
       // moveFile();
    }

}
