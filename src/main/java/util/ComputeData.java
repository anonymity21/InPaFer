package util;

import java.io.File;
import java.util.Objects;

/**
 * Created by liangjingjing on 2019/1/22.
 */
public class ComputeData {
    public static void main(String[] args) {
        String project = "Time";
        String path = "/Users/liangjingjing/WorkSpace/PatchFilter/cardumen-patches/" + project;
        File patch_dic = new File(path);
        File[] patches = patch_dic.listFiles();
        if (Objects.isNull(patches)) {
            return;
        }
        StringBuilder result = new StringBuilder();
        for (File f : patches) {
            if (f.isDirectory()) {
                String tmp = f.getName().replace(project, "");
                result.append(tmp).append(" ");
            }
        }
        System.out.print(result);
    }
}
