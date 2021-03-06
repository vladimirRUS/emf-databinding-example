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
import org.eclipse.emf.example.library.service.ILibraryPersistenceService;
import org.eclipse.emf.examples.extlibrary.Book;
import org.eclipse.emf.examples.extlibrary.BookOnTape;
import org.eclipse.emf.examples.extlibrary.Item;
import org.eclipse.emf.examples.extlibrary.Library;
import org.eclipse.emf.examples.extlibrary.Periodical;
import org.eclipse.emf.examples.extlibrary.VideoCassette;
import org.eclipse.emf.examples.library.databinding.stock.dialog.BookDialog;
import org.eclipse.emf.examples.library.databinding.stock.dialog.BookOnTapeDialog;
import org.eclipse.emf.examples.library.databinding.stock.dialog.PeriodicalDialog;
import org.eclipse.emf.examples.library.databinding.stock.dialog.VideoDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;

public class EditStockItemHandler extends AbstractHandler {
	public static final String commandId = "org.eclipse.emf.examples.library.databinding.stock.edititem";
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEvaluationContext ctx = (IEvaluationContext) event.getApplicationContext();
		IEditorPart part = (IEditorPart) ctx.getVariable(ISources.ACTIVE_EDITOR_NAME);
		ILibraryPersistenceService service = (ILibraryPersistenceService) part.getEditorInput().getAdapter(ILibraryPersistenceService.class);
		Library parent = (Library) ((IStructuredSelection)ctx.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME)).getFirstElement();
		
		Item item = (Item) ((IStructuredSelection) ctx.getVariable(org.eclipse.emf.examples.library.databinding.ISources.SELECTED_STOCK_ITEM_NAME)).getFirstElement();
		
		if( item instanceof Book ) {
			BookDialog dialog = new BookDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),service.getEditingDomain(),parent,(Book)item);
			dialog.open();
		} else if( item instanceof BookOnTape ) {
			BookOnTapeDialog dialog = new BookOnTapeDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),service.getEditingDomain(),parent,(BookOnTape)item);
			dialog.open();
		} else if( item instanceof VideoCassette ) {
			VideoDialog dialog = new VideoDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),service.getEditingDomain(),parent,(VideoCassette)item);
			dialog.open();
		} else if( item instanceof Periodical ) {
			PeriodicalDialog dialog = new PeriodicalDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),service.getEditingDomain(),(Periodical)item);
			dialog.open();
		}
		
		return null;
	}

}
