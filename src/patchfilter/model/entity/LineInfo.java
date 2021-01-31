package patchfilter.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.util.StateType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineInfo {

    private String lineName;
    private List<PatchFile> patchList;
    private double score = 0;
    private StateType stateType = StateType.UNCLEAR;

    public LineInfo(String lineName, List<PatchFile> patchList) {
        this.lineName = lineName;
        this.patchList = patchList;
    }

	@Override
	public String toString() {
		return "LineInfo [lineName=" + lineName + ", patchList=" + patchList + ", stateType=" + stateType + "]";
	}
    
    
}
