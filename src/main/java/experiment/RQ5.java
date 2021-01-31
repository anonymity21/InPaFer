package experiment;

import Main.MethodInitialization;
import config.Constant;
import entity.PatchFile;
import entity.Subject;
import lombok.extern.slf4j.Slf4j;
import run.Runner;

import java.util.List;

@Slf4j

public class RQ5 {
    public static void main(String[] args){
        String project = "Math";
        int start = 53;
        int end = 53;
        boolean modificationMethod = true;
        boolean variable = true;
        boolean trace = true;

        for (String arg : args) {
            if (arg.startsWith("-project=")) {
                // Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
                project = arg.substring("-project=".length());
            } else if (arg.startsWith("-start=")) {
                start = Integer.parseInt(arg.substring("-start=".length()));
            } else if (arg.startsWith("-end=")) {
                end = Integer.parseInt(arg.substring("-end=".length()));
            } else if (arg.startsWith("-t=")){
                if(arg.substring("-t=".length()).equalsIgnoreCase("method")){
                    modificationMethod = false;
                }else  if (arg.substring("-t=".length()).equalsIgnoreCase("variable")){
                    variable = false;
                }else if(arg.substring("-t=".length()).equalsIgnoreCase("trace")){
                    trace = false;
                }
            }
        }
            for(int times = 0; times < 10; times++){
                log.info("Times: " + times + " Start Running <<" + project +">> AllQuery from " + start + " to " + end );
                for (int i = start; i <= end; i++) {
                    Subject subject = new Subject(project, i);
                    if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                        log.info("Process " + subject.toString());
                        //FileIO.writeStringToLog(resultFile, "Process " + subject.toString());
                        List<PatchFile> patchList = subject.getPatchList();
                        if(!subject.exist()){
                            if(! Runner.downloadSubject(subject)){
                                continue;
                            }
                        }
                        AllQuery allQuery = new AllQuery(subject);
                        // if(! allQuery.containsCorretPatch()){
                        MethodInitialization methodInitialization = new MethodInitialization(subject);
                        methodInitialization.MainProcess();
                        allQuery.queryProcess(modificationMethod, variable, trace);


                    }
                }
            }
        }

}
