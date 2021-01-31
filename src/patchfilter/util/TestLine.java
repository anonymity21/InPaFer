package patchfilter.util;

public class TestLine {
	private String testCaseString;
	private double score;
	private int lineNum;
	private int remainPatchNum;
	public TestLine(String testCaseString, double score, int patchNum) {
		this.testCaseString = testCaseString;
		this.score = score;
		this.remainPatchNum = patchNum;
	}
	public String getTestCaseString() {
		return testCaseString;
	}
	public void setTestCaseString(String testCaseString) {
		this.testCaseString = testCaseString;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public int getRemainPatchNum() {
		return remainPatchNum;
	}
	public void setRemainPatchNum(int remainPatchNum) {
		this.remainPatchNum = remainPatchNum;
	}
	@Override
	public String toString() {
		return "TestLine [testCaseString=" + testCaseString + ", score=" + score + "]";
	}


}
