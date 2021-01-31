package patchfilter.model.script;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmpInfo {

	private String ID;
	private String tool;
	private String correctness;
	private String bug_id;
	private String project;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
