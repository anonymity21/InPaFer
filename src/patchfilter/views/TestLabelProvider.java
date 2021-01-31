package patchfilter.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


import patchfilter.util.TestLine;
//import patchfilter.model.entity.LineInfo;

public class TestLabelProvider implements ITableLabelProvider{

	@Override
	public void addListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Image getColumnImage(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int index) {
		// TODO Auto-generated method stub
		if(element instanceof TestLine){
			TestLine testLine = (TestLine)element;
			switch(index){
			case 0: 
				String line = testLine.getTestCaseString();
				return line;
			/*case 1: 
				double score = testLine.getScore();
				return score+"";*/
			case 1:
				int patchNum = testLine.getRemainPatchNum();
				return patchNum + "";
			}
		}
		return null;
	}

}
