package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
/*
 This file parses patches' diffFile
 */
public class DiffFile {
    private String filePath;
    private String fixedFile;
    private int startLineNumber;
    private List<String> buggyContent = new LinkedList<>();
    private List<String> fixedContent = new LinkedList<>();

    public  DiffFile(String filePath){
        this.filePath = filePath;
    }

    // This file parses Avatar's diffFile
    public void parseDiffFile(){
        String content[] = FileIO.readFileToString(filePath).split("\n");
        for(int i = 2; i< content.length; i++){
            String line = content[i];
            if(line.startsWith("---")){
                fixedFile = line.split("--- a")[1];
            }else if (line.startsWith("@@")){
                startLineNumber = Integer.parseInt(line.split("-")[1].split(",")[0]); //@@ -2163,7 +2163,10 @@
            }else if(line.startsWith("+++")){
                continue;
            }else{
                if(line.startsWith("-")){
                    line = line.split("-", 2)[1];
                    buggyContent.add(line);
                }else if(line.startsWith("+")){
                    line = line.split("\\+", 2)[1];
                    fixedContent.add(line);
                }else {
                    buggyContent.add(line);
                    fixedContent.add(line);
                }
            }
        }
    }

    // This file parses 3SFix's diffFile
    public void parseDiffFile4SFix(){
        String content[] = FileIO.readFileToString(filePath).split("\n");
        for(int i = 0; i< content.length; i++){
            String line = content[i];
            if(line.startsWith("---")){
                fixedFile = line.split("default")[1].split(".java\t")[0] + ".java";
            }else if (line.startsWith("@@")){
                startLineNumber = Integer.parseInt(line.split("-")[1].split(",")[0]); //@@ -2163,7 +2163,10 @@
            }else if(line.startsWith("+++")){
                continue;
            }else{
                if(line.startsWith("-")){
                    line = line.split("-", 2)[1];
                    buggyContent.add(line);
                }else if(line.startsWith("+")){
                    line = line.split("\\+", 2)[1];
                    fixedContent.add(line);
                }else {
                    buggyContent.add(line);
                    fixedContent.add(line);
                }
            }
        }
    }

}
