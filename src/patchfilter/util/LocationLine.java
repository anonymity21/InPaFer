package patchfilter.util;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.util.StateType;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LocationLine {
	private String modifyMethod;
	private StateType stateType;
	private List<PatchFile> patchList = new ArrayList<PatchFile>();	
}
