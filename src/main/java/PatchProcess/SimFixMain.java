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
 * This file is formating patches of SimFix
 */
@Slf4j
public class SimFixMain {
    static String tech = "SimFix";

    static String project = "Lang";
    static int bugNumber = 39;
    static String astorPatch = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/" + tech + "/" + project;
    static String logFile = Constant.LOG_FILE + "/formatPatch/" + tech + "-" + project + ".log";
    static String result = "/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/" + project + "/";

    public static void main(String[] args) {

        System.out.println(System.getProperty("java.version"));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd :hh:mm:ss");
        System.out.println(dateFormat.format(date));

        FileIO.writeStringToLog(logFile, dateFormat.format(date) + "\n");

        //File f = new File(astorPatch);
        //System.out.println("Totol: " + f.listFiles().length);

        //File[] flist = f.listFiles();
        for (int i = 39; i <= bugNumber; i++) {
            File patchFile = new File(astorPatch + "/" + i);
            if (!patchFile.exists()) {
                continue;
            }
            System.out.println("Process: " + i);
            getFormatDiffPatch(patchFile, i);
        }
    }

    private static void getFormatDiffPatch(File patchFile, int bugid) {
        File[] flist = patchFile.listFiles();
        if (Objects.isNull(flist)) {
            log.error("patch file is empty!");
            return;
        }
        int i = 0;
        for (File dir : flist) {
            File[] dirFiles = dir.listFiles();
            if (Objects.isNull(dirFiles) || dirFiles.length <= 0) {
                continue;
            }
            File f = dirFiles[0];
            i++;
            String fileName = f.getName().split("_")[1];
            String fileContent = FileIO.readFileToString(f);
            String packageName = "";
            for (String line : fileContent.split("\n")) {
                if (line.startsWith("package")) {
                    // package org.jfree.chart.renderer.category;
                    packageName = line.split("package")[1].trim().split(";")[0];
                    break;
                }
            }
            Subject subject = new Subject(project, bugid);
            Format format = new Format(subject);
            String fixedFile = subject.getHome() + "/" + subject.getSsrc() + "/" + packageName.replaceAll("\\.", "/") + "/" + fileName;

            FileIO.backUpFile(fixedFile, fixedFile + ".bak");
            //FileIO.backUpFile(fixedFile, fixedFile+".patched");
            FileIO.backUpFile(f.getAbsolutePath(), fixedFile + ".patched");
            Runner.compileSubject(subject);
            format.formatSingleFile(fixedFile);
            format.formatSingleFile(fixedFile + ".patched");

            String diffFile = result + bugid;
            if (!new File(diffFile).exists()) {
                new File(diffFile).mkdirs();
            }

            diffFile = diffFile + "/" + tech + "-" + i;
            Runner.diff2File(fixedFile, fixedFile + ".patched", diffFile);

            FileIO.restoreFile(fixedFile, fixedFile + ".bak");
            FileIO.deleteFile(fixedFile + ".bak");
            FileIO.deleteFile(fixedFile + ".patched");

        }
    }

}
