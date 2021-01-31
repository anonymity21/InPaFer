package PatchProcess;

import config.Constant;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import util.FileIO;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class FindWrongComments {

    public static void main(String[] args) {
        String[] projectName = {"Closure", "Chart", "Lang", "Math", "Time"};
        int[] patchNum = {133, 26, 58, 105, 19};

        for (int index = 0; index < projectName.length; index++) {
            String project = projectName[index];
            int totalBug = patchNum[index];
            for (int i = 1; i <= totalBug; i++) {
                Subject subject = new Subject(project, i);
                String patchPath = Constant.AllPatchPath + "/" + subject.getName() + "/" + subject.getId();
                if (!new File(patchPath).exists()) {
                    continue;
                }
                File[] files = new File(patchPath).listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        long diffNum = Arrays
                                .stream(FileIO.readFileToString(file).split("\n"))
                                .filter(line -> line.startsWith("@@") || line.startsWith("+@@"))
                                .count();
                        if (diffNum >= 2) {
                            log.info("there is more than one diff in the project: " + subject.getName() + "_" +
                                    subject.getId() + "_" + file.getName());
                        }
                        String originalContent = Arrays
                                .stream(FileIO.readFileToString(file).trim().split("\n"))
                                .filter(line -> !line.startsWith("---") && !line.startsWith("+++"))
                                .collect(Collectors.joining("\n"));
                        String deleteCommentContent = originalContent
                                .replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*+\\/", "");
                        if (originalContent.equalsIgnoreCase(deleteCommentContent)) {
                            continue;
                        }
                        log.info("need deal with comment: " + subject.getName() + "_" + subject.getId() + "_" + file.getName());
                    }

                }
                log.info("Complete Project: " + project + " BugId: " + i);
            }
        }
    }
}
