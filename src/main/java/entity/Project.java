package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Project {
    private String name = "";
    private int totalNum = 0;

    public Project(String _name){
        name = _name;
    }

    public void initTotalNum(){
        switch (name){
            case "Chart":
                totalNum = 26;
                break;
            case "Closure":
                totalNum = 133;
                break;
            case "Lang":
                totalNum = 65;
                break;
            case "Math":
                totalNum = 106;
                break;
            case "Time":
                totalNum = 27;
                break;
        }
    }
}
