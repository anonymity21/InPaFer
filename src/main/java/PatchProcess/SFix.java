package PatchProcess;

import config.Constant;
import entity.DiffFile;
import entity.Project;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import util.FileIO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/*
process the patches of 3sFix, and make the patches to be formatted.
 */
@Slf4j
public class SFix {
    //private static String patchPath = Constant.BProcessPatches + "/Avatar/FixedBugs/";
    private static String patchPath = Constant.BProcessPatches + "/3SFix/";

    public static  void  mainProcess(Subject subject, String patchDirPath){
        for(File patchFile: new File(patchDirPath).listFiles()){
            if(! patchFile.isDirectory()){
                continue;
            }
            /*if(! patchFile.getName().equals("Patch_1444.txt")){
                continue;
            }*/
            System.out.println(patchFile.getName());
            DiffFile diffFile = new DiffFile(patchFile.getAbsolutePath() + "/diff");
            diffFile.parseDiffFile4SFix();
            //String deleteFile = FileIO.readFileToString(patchFile).split("\n")[2];
            String sourceFile = subject.getHome() + subject.getSsrc() +  diffFile.getFixedFile();
            String targetFile = sourceFile + ".patched";
            String newDiffFile = patchPath + "/tmpFixedBugs/" + subject.getName() + "_" + subject.getId();
            if(! new File(newDiffFile).exists()){
                new File(newDiffFile).mkdirs();
            }

            FileIO.backUpFile(sourceFile, sourceFile + ".bak");

            resetPatch(sourceFile,diffFile);

            subject.deleteTarget();
            if(! Runner.compileSubject(subject)){
                log.error("Compile Fail");
                continue;
            }
            Format format = new Format(subject);
            format.formatSingleFile(sourceFile);
            format.formatSingleFile(targetFile);

            Runner.diff2File(sourceFile, targetFile, newDiffFile +"/3sFix-" + patchFile.getName());
            FileIO.restoreFile(sourceFile, sourceFile+".bak");
        }
    }
    public static  void resetPatch(String sourceFile, DiffFile diffFile){
        String content[] = FileIO.readFileToString(sourceFile).split("\n");
        //String diffContent[] = FileIO.readFileToString(diffFile).split("\n");
        List<String> newContent = new LinkedList<>();
        int startLineNumber = diffFile.getStartLineNumber(); //@@ -2163,7 +2163,10 @@
        for(int i = 0; i < content.length; i++){
            if(i >= startLineNumber -1 && i < startLineNumber - 1 + diffFile.getBuggyContent().size() ){
                newContent.add(diffFile.getFixedContent().stream().collect(Collectors.joining("\n")));
                newContent.add("\n");
                i = startLineNumber + diffFile.getBuggyContent().size() -2;
            }else {
                newContent.add(content[i] + "\n");
            }

        }
        FileIO.writeStringToFile(sourceFile+".patched", newContent.stream().collect(Collectors.joining()));

    }


    public static void main(String[] args) {

        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));

        String projectName = "Math";
        //int id = ;
        // String patchDirPath = patchPath + projectName + "_" + id;

        //Subject subject = new Subject(projectName, id);

        Project project = new Project(projectName);
        project.initTotalNum();
        int end = project.getTotalNum();

        for(int i = 28; i <= 28; i++){
            Subject subject = new Subject(projectName, i);

            String patchDirPath = patchPath + projectName + "/" + projectName + "_" + i;
            if(new File(patchDirPath).exists()){
                System.out.println("Start Process " + subject.getName() + "_" + subject.getId());
                if(!subject.exist()){
                    Runner.downloadSubject(subject);
                }
                mainProcess(subject, patchDirPath);
            }

        }

        //   mainProcess(subject, patchDirPath);


    }

}
