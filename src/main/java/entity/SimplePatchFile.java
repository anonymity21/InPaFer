package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
This file is used in script.PatchProcession.
The purpose of simplePatchFile is implementing the equals method
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SimplePatchFile {
    private File patchFile;
    private String patchContent;
    //private String patchFileName;

    public SimplePatchFile(File _patchFile){
        this.patchFile = _patchFile;
    }

    public void initContent(){
        List<String> arrayList = new LinkedList<>();
        try {
            FileReader fr = new FileReader(patchFile.getAbsolutePath());
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            int i = 0;
            while ((str = bf.readLine()) != null) {
                str = str.replaceAll(" ","").replaceAll("\t","");
                if (i > 1){
                    arrayList.add(str);
                }
                i++;
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        patchContent = arrayList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimplePatchFile)) return false;
        SimplePatchFile that = (SimplePatchFile) o;
        return patchContent.equals(that.patchContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patchContent);
    }
}
