package patchfilter.model.entity;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import patchfilter.model.util.FileIO;

@Slf4j
//@Builder
@Data


public class Method {
	
	private String methodNameString;
	private String filePathString;
	private Integer starLineInteger;
	private Integer endLineInteger;
	private String contentString;
	
	public Method() {
		
	}
	
	
	public Method(String methodnameString, String fileString) {
		methodNameString = methodnameString;
		filePathString = fileString;
	}
	
	public void updateMethodContent() {
		if(! filePathString.equals("") && endLineInteger!=null && starLineInteger != null) {
			String fileCnt[] = FileIO.readFileToString(filePathString).split("\n");
			StringBuilder reStringBuilder  = new StringBuilder();
			for(int i = starLineInteger -1 ; i <= endLineInteger -1; i++) {
				reStringBuilder.append(fileCnt[i]).append("\n");
			}
			contentString = reStringBuilder.toString();
		}
		
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String toString() {
		return "Method [methodNameString=" + methodNameString + ", StarLine="
				+ starLineInteger + ", endLineInteger=" + endLineInteger + ", contentString=" + contentString + "]";
	}

}
