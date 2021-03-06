/*******************************************************************************
 * Copyright (c) 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.examples.library.databinding;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class AbstractForm {
	private String id;
	private TabItem item;
	private IWorkbenchPartSite site;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	protected TabItem getItem() {
		return item;
	}
	
	protected IWorkbenchPartSite getSite() {
		return site;
	}
	
	public void createForm(IWorkbenchPartSite site, TabFolder folder, EditingDomain domain, DataBindingContext context, IObservableValue master) {
		this.site = site;
		item = doCreateForm(folder, domain, context, master);
	}
	
	public abstract TabItem doCreateForm(TabFolder folder, EditingDomain domain, DataBindingContext context, IObservableValue master);
	public abstract void postExecuteFailure(String commandId,ExecutionException exception);
	public abstract void postExecuteSuccess(String commandId, Object returnValue);
	protected abstract void doDispose();
	
	public void dispose() {
		doDispose();
		item.getControl().dispose();
		item.dispose();
	}
}
