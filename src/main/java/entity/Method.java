package entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

@Slf4j
@Data
@NoArgsConstructor
public class Method {

    private String methodNameString;
    private String filePathString;
    private Integer starLineInteger;
    private Integer endLineInteger;
    private String contentString;

    public Method(String methodnameString, String fileString) {
        methodNameString = methodnameString;
        filePathString = fileString;
    }

    public void updateMethodContent() {
        if (!filePathString.equals("") && endLineInteger != null && starLineInteger != null) {
            String[] fileCnt = FileIO.readFileToString(filePathString).split("\n");
            StringBuilder reStringBuilder = new StringBuilder();
            for (int i = starLineInteger - 1; i <= endLineInteger - 1; i++) {
                reStringBuilder.append(fileCnt[i]).append("\n");
            }
            contentString = reStringBuilder.toString();
        }
    }
}
