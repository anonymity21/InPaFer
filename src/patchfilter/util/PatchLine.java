package patchfilter.util;

import lombok.Data;
import patchfilter.model.config.Constant;
import patchfilter.model.util.FileIO;
import patchfilter.model.entity.Subject;

@Data

public class PatchLine {
	private String patchNameString;
	private Subject subject;


	public PatchLine(String patchNameString, Subject subject) {

		this.patchNameString = patchNameString;
		this.subject = subject;
	}
	public void initFormatedPatch() {
		String patchFile = Constant.AllPatchPath + "/" + subject.getName() + "/" + subject.getId() + "/" + patchNameString;

	}
}
