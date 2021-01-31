package entity;

import lombok.*;
import util.StateType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Line {

    private StateType stateType;
    private List<PatchFile> patchList = new ArrayList<PatchFile>();

    public Line(List<PatchFile> _patchList){
        this.patchList = _patchList;
    }
}
