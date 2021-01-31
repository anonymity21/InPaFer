package patchfilter.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

import com.sun.corba.se.spi.activation.Activator;

public class LineLabel extends LineBreakpoint{

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return  null;
	}
	
	public LineLabel(IResource resource, int lineNumber) throws CoreException {
	     IMarker marker = resource.createMarker(
	         "org.eclipse.debug.examples.core.pda.lineBreakpoint.marker");
	     setMarker(marker);
	     setEnabled(true);
	     ensureMarker().setAttribute(IMarker.LINE_NUMBER, lineNumber);
	  }
}

