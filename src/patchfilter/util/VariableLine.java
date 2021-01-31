package patchfilter.util;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import patchfilter.model.entity.PatchFile;
import patchfilter.model.util.StateType;
import patchfilter.util.LocationLine.LocationLineBuilder;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableLine {
	private String varName;
	private String value;
	private StateType stateType;
	private List<PatchFile> patchFiles = new ArrayList<PatchFile>();
}
