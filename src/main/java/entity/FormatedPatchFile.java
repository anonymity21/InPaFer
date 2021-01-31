package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.FileIO;

import java.io.File;

/**
 * Created by liangjingjing on 2019/3/14.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormatedPatchFile {
    private String patchfile;
    private String fixedFile;
    private boolean multiModify;
    private String diffContent;
    private String patchName;

    public FormatedPatchFile(String patchfile) {
        this.patchfile = patchfile;
        this.multiModify = false;
        this.diffContent = "";
        this.patchName = new File(patchfile).getName();
    }

    public void ParseFile() {
        String fileContent = FileIO.readFileToString(patchfile);
        int atNum = 0;
        for (String line : fileContent.split("\n")) {
            if (line.startsWith("---")) {
                //--- /Users/liangjingjing/WorkSpace/Data/Defects4J/projects/Chart/Chart_26_buggy//source///org/jfree/chart/axis/Axis.java	2019-03-12 18:30:32.000000000 +0800
                fixedFile = "/" + line.split("/", 2)[1].split("\t")[0];
                if (!fixedFile.endsWith(".java")) {
                    fixedFile = fixedFile.split("\\.java")[0] + ".java";
                }
                fixedFile = fixedFile.replace("//", "/");
            } else if ((line.startsWith("@@") || (line.startsWith("+@@") || line.startsWith("-@@"))) && line.endsWith("@@")) {
                //@@ -166,10 +166,10 @@
                atNum++;
            } else if (line.startsWith("+++")) {

            } else {
                diffContent = diffContent + line.trim() + "\n";
            }
        }
        if (atNum > 1) {
            multiModify = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormatedPatchFile that = (FormatedPatchFile) o;

        if (multiModify != that.multiModify) {
            return false;
        }
        if (!fixedFile.equals(that.fixedFile)) {
            return false;
        }
        return diffContent.equals(that.diffContent);
    }

    @Override
    public int hashCode() {
        int result = fixedFile.hashCode();
        result = 31 * result + (multiModify ? 1 : 0);
        result = 31 * result + diffContent.hashCode();
        return result;
    }

    public static void main(String[] args) {
        FormatedPatchFile p1 = new FormatedPatchFile("/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/Math/5/SimFix-1");
        FormatedPatchFile p2 = new FormatedPatchFile("/Users/liangjingjing/WorkSpace/Project/astor-experiments/total/Math/5/JGenProg2017-patch1");
        p1.ParseFile();
        p2.ParseFile();
        if (p1.equals(p2)) {
            System.out.println("ok!");
        }
    }
}