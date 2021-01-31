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
public class VariableLine extends Line {
    private String varName;
    private String value;
  //  private StateType stateType;
  //  private List<PatchFile> patchFiles = new ArrayList<>();

    public VariableLine(String _varName, String _value, StateType _stateType, List<PatchFile> _patchList){
        super(_stateType, _patchList);
        this.varName = _varName;
        this.value = _value;
    }

    @Override
    public String toString() {
        return "Variable [varName=" + varName + ", value " +  value + ", patchList=" + super.getPatchList().size() + "]";
    }
}
