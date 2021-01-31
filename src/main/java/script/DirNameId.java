package script;

import config.Constant;

import java.io.File;

public class DirNameId {
    private static  String path = Constant.BProcessPatches + "/Avatar/FixedBugs/";
    private static String[] projectlist = {"Chart", "Closure", "Math", "Time", "Lang"};

    public static void main(String[] arg){

        for(String projectName: projectlist){
            System.out.print(projectName + ": ");
            for(File file: new File(path).listFiles()){
                if(file.getName().startsWith(projectName)){
                    System.out.print(file.getName().split("_")[1] + " ");
                }
            }
            System.out.println();
        }


    }
}
