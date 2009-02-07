package org.eclipse.emf.example.library.service.xmi;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.example.library.service.BaseLibraryPersistenceService;
import org.eclipse.emf.example.library.service.xmi.internal.Activator;

public abstract class XMILibraryPersistenceService extends BaseLibraryPersistenceService {
	private Resource resource;
	
	@Override
	protected Resource doGetResource() {
		if( resource == null ) {
			File f = doGetFile();
			
			if( f.exists() ) {
				resource = getResourceSet().getResource(URI.createFileURI(f.getAbsolutePath()), true);
			} else {
				resource = getResourceSet().createResource(URI.createURI("http:///org/eclipse/emf/examples/library/extlibrary.ecore/1.0.0"));
				resource.setURI(URI.createFileURI(f.getAbsolutePath()));
			}	
		}
		return resource;		
	}
	
	public abstract File doGetFile();
	
	public String getCategory() {
		return "XMI";
	}
	
	@Override
	protected IStatus doSave() {
		try {
			resource.save(null);
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Saveing failed",e);
		}
	}
}