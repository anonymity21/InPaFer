package entity;

import lombok.*;
import util.StateType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LocationLine extends Line {
    private String modifyMethod;
    //private StateType stateType;
    //private List<PatchFile> patchList = new ArrayList<PatchFile>();
    public LocationLine(String _modifyMethod, StateType _stateType, List<PatchFile> _patchList){
        super(_stateType, _patchList);
        this.modifyMethod = _modifyMethod;
    }

    @Override
    public String toString() {
        return "LocationLine [modifyMethod=" + modifyMethod + ", patchList=" + super.getPatchList().size() + "]";
    }
}
