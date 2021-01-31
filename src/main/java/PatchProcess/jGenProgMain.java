package PatchProcess;

import config.Constant;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import run.Runner;
import util.FileIO;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by liangjingjing on 2019/3/12.
 * <p>
 * This File is formating the patches from DeepRepair jGenprog jKali and jMuRepair
 * input : exist patch
 * output: a diff patch from a formated original file and a formated patched program
 */
@Slf4j
public class jGenProgMain {
    static String tech = "JGenProg2017";
    // jMutRepair  jKali JGenProg2017 DeepRepair
    static String project = "Lang";
    static String astorPatch = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/" + tech + "/" + project;
    static String ACSPatch = "/Users/liangjingjing/WorkSpace/Project/Patch-Correctness/tool/patches/";
    static String logFile = Constant.LOG_FILE + "/formatPatch/" + tech + "-" + project + ".log";
    static String result = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/" + project + "/";

    //static String bugid = "";


    // specific for jMutRepair  jKali JGenProg2017 DeepRepair
    private static void getFormatDiffPatch(File patchFile) {

        if (!patchFile.getName().endsWith(".patch")) {
            return;
        }
        // filename patch1-Chart-1-JGenProg2017.patch
        String fileName = patchFile.getName().split(".patch")[0];
        String patchid = fileName.split("-")[0];
        String bugid = fileName.split("-")[2];

        Subject subject = new Subject(project, Integer.parseInt(bugid));
        Format format = new Format(subject);
        String projectHome = subject.getHome();

        String patchContent = FileIO.readFileToString(patchFile);
        if (patchContent.equals("")) {
            return;
        }
        String fixedFile = "";
        for (String line : patchContent.split("\n")) {
            if (line.startsWith("---")) {
                fixedFile = projectHome + "/" + line.split("---")[1].trim();
                break;
            }
        }

        Runner.compileSubject(subject);

        FileIO.backUpFile(fixedFile, fixedFile + ".bak");
        FileIO.backUpFile(fixedFile, fixedFile + ".patched");
        format.formatSingleFile(fixedFile);


        if (Runner.patchFile(fixedFile + ".patched", patchFile.getAbsolutePath())) {
            String diffFile = result + bugid;
            if (!new File(diffFile).exists()) {
                new File(diffFile).mkdirs();
            }
            System.out.println("Patch " + fileName + " Success!");
            format.formatSingleFile(fixedFile + ".patched");
            diffFile = diffFile + "/" + tech + "-" + patchid;
            Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);
        } else {
            System.out.println("Patch " + fileName + " Failed!");
            FileIO.writeStringToFile(logFile, "Patching " + fileName + " Failed!", true);
        }


        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".bak");
        FileIO.deleteFile(fixedFile + ".patched");
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));
        log.info(dateFormat.format(date) + "\n", true);
        File f = new File(astorPatch);
        File[] flist = f.listFiles();
        if (Objects.isNull(flist)) {
            log.error("astor patch file list is empty!");
            return;
        }
        log.info("Total file: {}", flist.length);
        for (int i = 1; i <= flist.length; i++) {
            File patchFile = flist[i - 1];

            if (!patchFile.getName().contains("Lang-39")) {
                continue;
            }
            log.info("Process: " + i + "/" + flist.length);
            getFormatDiffPatch(patchFile);
        }
    }
}
