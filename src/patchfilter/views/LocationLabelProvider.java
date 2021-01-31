package patchfilter.views;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import patchfilter.util.LocationLine;
import patchfilter.util.TestLine;

public class LocationLabelProvider implements ITableLabelProvider {

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
		if(element instanceof LocationLine){
			LocationLine locationLine = (LocationLine)element;
			switch(index){
			case 2: 
				String line = locationLine.getModifyMethod();
				return line;
			case 3:
				int patchNum = locationLine.getPatchList().size();
				return patchNum+"";
			}
		}
		return null;
	}



}
