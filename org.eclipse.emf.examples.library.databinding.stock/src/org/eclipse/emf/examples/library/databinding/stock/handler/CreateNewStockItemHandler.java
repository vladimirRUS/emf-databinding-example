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
package org.eclipse.emf.examples.library.databinding.stock.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.example.library.service.ILibraryPersistenceService;
import org.eclipse.emf.examples.extlibrary.EXTLibraryFactory;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Item;
import org.eclipse.emf.examples.extlibrary.Library;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;

public class CreateNewStockItemHandler extends AbstractHandler {
	public static final String commandId = "org.eclipse.emf.examples.library.databinding.stock.new";
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEvaluationContext ctx = (IEvaluationContext) event.getApplicationContext();
		IEditorPart part = (IEditorPart) ctx.getVariable(ISources.ACTIVE_EDITOR_NAME);
		Library parent = (Library) ((IStructuredSelection)ctx.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME)).getFirstElement();
		
		if( parent != null ) {
			ILibraryPersistenceService service = (ILibraryPersistenceService) part.getEditorInput().getAdapter(ILibraryPersistenceService.class);
			
			if( service != null ) {
				Item item;
				String type = event.getParameter("org.eclipse.emf.examples.library.databinding.stock.type");
				if( type.equals("book") ) {
					item = EXTLibraryFactory.eINSTANCE.createBook();	
				} else if( type.equals("tapebook") ) {
					item = EXTLibraryFactory.eINSTANCE.createBookOnTape();
				} else if( type.equals("video") ) {
					item = EXTLibraryFactory.eINSTANCE.createVideoCassette();
				} else if( type.equals("periodical") ) {
					item = EXTLibraryFactory.eINSTANCE.createPeriodical();
				} else {
					throw new ExecutionException("Unknow stock item type '"+type+"'.");
				}
				
				Command cmd = AddCommand.create(service.getEditingDomain(), parent, EXTLibraryPackage.Literals.LIBRARY__STOCK, item);
				if( cmd.canExecute() ) {
					service.getEditingDomain().getCommandStack().execute(cmd);
					return item;
				} else {
					throw new ExecutionException("Could not execute add stock item command.");
				}
			} else {
				throw new ExecutionException("No library service available");
			}
		} else {
			throw new ExecutionException("No library selected!");
		}
	}
}