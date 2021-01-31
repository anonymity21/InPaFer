package patchfilter.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import patchfilter.model.entity.LineInfo;
import patchfilter.model.util.StateType;


//import patchfilter.util.StateType;
//import patchfilter.util.TraceLine;

public class TraceLabelProvider implements ITableLabelProvider {

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
		if(element instanceof LineInfo){
			LineInfo traceLine = (LineInfo)element;
			switch(index){
			case 2: 
				String line = traceLine.getLineName();
				return line;
			case 3: 
				StateType state = traceLine.getStateType();
				return state.toString();
			case 4:
				String patchNum = traceLine.getPatchList().size()+"";
				return patchNum;
			}
		}
		return null;
	}

}
