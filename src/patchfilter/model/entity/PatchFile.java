package patchfilter.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.model.config.Constant;
import patchfilter.model.run.Runner;
import patchfilter.model.util.FileIO;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.LinkedList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PatchFile {

    private String patchPath;
    private String fixedFile = null;
    private String buggyContent = "";
    private String fixedContent = "";
    private String patch = "";
    private int starLine;
    private int modifyLine = 0;
    private String patchName = "";
    public boolean isMultiModify = false;
    private boolean modify = false;
    private String modifiedMethod = "";
    private String aliaName = "";
    private List<String> deleteLine = new LinkedList<>();

    private boolean correctness = false;

    private Subject subject;

    public PatchFile(Subject subject, String patchPath) {
        this.subject = subject;
        this.patchPath = patchPath;
    }


    public PatchFile(Subject subject, String patchPath, String fixedFile, String buggyContent, String fixedContent) {
        this.subject = subject;
        this.patchPath = patchPath;
        this.fixedFile = fixedFile;
        this.buggyContent = buggyContent;
        this.fixedContent = fixedContent;
    }

    @PostConstruct
    public void parsePatch() {

        File patchFile = new File(patchPath);
        patchName = patchFile.getName();
        String diffConent = FileIO.readFileToString(patchFile);
        int tmpLine = 0;
        for (String line : diffConent.split("\n")) {
            if (line.startsWith("diff") || line.startsWith("+++")) {
                continue;
            }
            //@@ -1794,7 +1794,7
            if (line.startsWith("@@")) {
                starLine = Integer.parseInt(line.split("-")[1].split(",")[0]);
                tmpLine = starLine;
                if (modify) {
                    isMultiModify = true;
                    break;
                } else {
                    modify = true;
                }
                continue;
            }
            // get fixedFile
            if (line.startsWith("---")) {
                //--- original/org/apache/commons/math3/distribution/AbstractIntegerDistribution.java
                if (line.startsWith("--- original")) {
                    fixedFile = subject.getHome() + subject.getSsrc() + line.split("--- original")[1];
                } else {
                    //System.out.println(line);
                    fixedFile = subject.getHome() + subject.getSsrc() + line.split(subject.getSsrc())[1].split("\\.java")[0] + ".java";
                }

                fixedFile = fixedFile.replaceAll("//", "/");
                // get patch
            } else {
                tmpLine++;
                patch = patch + line + "\n";
                if (line.startsWith("-") && !line.startsWith("---")) {
                    //System.out.println(line);
                    if (line.split("-").length > 1) {
                        buggyContent = buggyContent + line.split("-", 2)[1] + "\n";
                        modifyLine = tmpLine;
                        deleteLine.add(fixedFile.replaceAll("\\/\\/", "/")
                                .replaceAll("/", "\\.")
                                + "#" + String.valueOf(tmpLine - 1));
                    }
                } else if (line.startsWith("+") && !line.startsWith("+++")) {
                    if (line.split("\\+").length > 1) {
                        fixedContent = fixedContent + line.split("\\+", 2)[1] + "\n";
                        if (modifyLine == 0) {
                            modifyLine = tmpLine;
                        }
                    }
                } else {
                    buggyContent = buggyContent + line + "\n";
                    fixedContent = fixedContent + line + "\n";
                }
            }
        }      
      //  initAlia();
    }
    
    private void initAlia() {
		// TODO Auto-generated method stub
    	String chars = "abcdefghijklmnopqrstuvwxyz"; 
    	//System.out.println();
    	StringBuilder nameBuilder = new StringBuilder();
    	for(int i = 0; i<6; i++) {
    		nameBuilder.append(chars.charAt((int)(Math.random() * 26)));
    	}
    	aliaName =  nameBuilder.toString() + "-" + patchName.split("-")[0] ;
	}

    public boolean patchToFile() {
        log.info("Patch To File");
        if (Runner.patchFile(fixedFile, patchPath)) {
            return true;
        }
        return false;
    }

    public void disPatchFile() {
        FileIO.restoreFile(fixedFile, fixedFile + ".bak");
    }

    @Override
    public String toString() {  //patch=" + patch + ",
        return "PatchFile [fixedFile=" + fixedFile + ",  patchName=" + patchName + ", subject="
                + subject + "]";
    }

    /*
     * @Override public int compareTo(PatchFile o) { int startIndex =
     * o.getStarLineNumber() - this.getStarLineNumber(); return startIndex; }
     */

    public static void main(String[] args) {
        String project = "Lang";
        int bugid = 35;
        //Subject subject = new Subject(project, bugid);
        String patch_path = Constant.AllPatchPath + "/" + project + "/" + bugid;

        File patch_dic = new File(patch_path);
        File[] patches = patch_dic.listFiles();
        for (int i = 0; i < patches.length; i++) {
            String patch = patches[i].getAbsolutePath();
            if (patch.contains(".DS_Store")) {
                continue;
            }
            PatchFile patchFile = new PatchFile(new Subject(project, bugid), patch);
            patchFile.parsePatch();
            System.out.println(patchFile.isMultiModify);
            System.out.println("=========");
        }
    }


}

