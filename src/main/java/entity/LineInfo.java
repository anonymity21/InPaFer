package entity;

import lombok.*;
import util.StateType;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineInfo extends Line{

    private String lineName;
    //private List<PatchFile> patchList;
    private double score = 0;
    //private StateType stateType = StateType.UNCLEAR;

    public LineInfo(String lineName, List<PatchFile> patchList) {
        super(patchList);
        this.lineName = lineName;

    }

    @Override
    public String toString() {
        return "LineInfo [lineName=" + lineName + ", patchList=" + super.getPatchList().size() + "]";
    }
}
