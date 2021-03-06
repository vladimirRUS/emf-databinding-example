/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Tom Schindl<tom.schindl@bestsolution.at> - Port to EMF
 ******************************************************************************/
package org.eclipse.emf.databinding.edit.properties.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

/**
 * <p>
 * <b>PROVISIONAL This API is subject to arbitrary change, including renaming or
 * removal.</b>
 * </p>
 * @since 1.1
 */
public class EMFEditListProperty extends SimpleListProperty {
	
	private class ListVisitorImpl extends ListDiffVisitor {
		private EditingDomain domain;
		private EObject eObj;
		private EStructuralFeature feature;
		
		private ListVisitorImpl(EditingDomain domain, EObject eObj, EStructuralFeature feature) {
			this.domain = domain;
			this.eObj = eObj;
			this.feature = feature;
		}
		
		@Override
		public void handleAdd(int index, Object element) {
			execute(AddCommand.create(domain, eObj, feature, element, index));
		}

		@Override
		public void handleRemove(int index, Object element) {
			execute(RemoveCommand.create(domain, eObj, feature, element));
		}
		
		private  boolean execute(Command command)
		  {
		    if (command.canExecute())
		    {
		      domain.getCommandStack().execute(command);
		      return true;
		    }
		    else
		    {
		      return false;
		    }
		  }
	}
	
	private EditingDomain domain;
	private final EStructuralFeature feature;

	/**
	 * @param feature
	 */
	public EMFEditListProperty(EditingDomain domain, EStructuralFeature feature) {
		this.feature = feature;
		this.domain = domain;
	}
	
	protected EStructuralFeature getFeature() {
		return feature;
	}

	public Object getElementType() {
		return feature;
	}

	@Override
	protected List<?> doGetList(Object source) {
		EObject eObj = (EObject) source;
		return (List<?>) eObj.eGet(feature);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSetList(Object source, List list, ListDiff diff) {
		diff.accept(new ListVisitorImpl(domain,(EObject) source,getFeature()));
	}

	@Override
	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new Listener(listener);
	}

	private class Listener extends AdapterImpl implements
			INativePropertyListener {
		private final ISimplePropertyListener listener;

		private Listener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		@Override
		public void notifyChanged(Notification msg) {
			if (feature == msg.getFeature() && !msg.isTouch()) {
				final ListDiff diff;
				switch (msg.getEventType()) {
				case Notification.ADD: {
					diff = Diffs.createListDiff(Diffs.createListDiffEntry(msg
							.getPosition(), true, msg.getNewValue()));
					break;
				}
				case Notification.ADD_MANY: {
					Collection<?> newValues = (Collection<?>) msg.getNewValue();
					ListDiffEntry[] listDiffEntries = new ListDiffEntry[newValues
							.size()];
					int position = msg.getPosition();
					int index = 0;
					for (Object newValue : newValues) {
						listDiffEntries[index++] = Diffs.createListDiffEntry(
								position++, true, newValue);
					}
					diff = Diffs.createListDiff(listDiffEntries);
					break;
				}
				case Notification.REMOVE: {
					diff = Diffs.createListDiff(Diffs.createListDiffEntry(msg
							.getPosition(), false, msg.getOldValue()));
					break;
				}
				case Notification.REMOVE_MANY: {
					Collection<?> oldValues = (Collection<?>) msg.getOldValue();
					ListDiffEntry[] listDiffEntries = new ListDiffEntry[oldValues
							.size()];
					int position = msg.getPosition();
					int index = 0;
					for (Object oldValue : oldValues) {
						listDiffEntries[index++] = Diffs.createListDiffEntry(
								position++, false, oldValue);
					}
					diff = Diffs.createListDiff(listDiffEntries);
					break;
				}
				case Notification.SET:
				case Notification.RESOLVE: {
					ListDiffEntry[] listDiffEntries = new ListDiffEntry[2];
					listDiffEntries[0] = Diffs.createListDiffEntry(msg
							.getPosition(), false, msg.getOldValue());
					listDiffEntries[1] = Diffs.createListDiffEntry(msg
							.getPosition(), true, msg.getNewValue());
					diff = Diffs.createListDiff(listDiffEntries);
					break;
				}
				case Notification.MOVE: {
					Object movedValue = msg.getNewValue();
					ListDiffEntry[] listDiffEntries = new ListDiffEntry[2];
					listDiffEntries[0] = Diffs.createListDiffEntry(
							(Integer) msg.getOldValue(), false, movedValue);
					listDiffEntries[1] = Diffs.createListDiffEntry(msg
							.getPosition(), true, movedValue);
					diff = Diffs.createListDiff(listDiffEntries);
					break;
				}
				case Notification.UNSET: {
					// This just represents going back to the unset state, but
					// that doesn't affect the contents of the list.
					//
					return;
				}
				default: {
					throw new RuntimeException("unhandled case");
				}
				}
				listener.handlePropertyChange(new SimplePropertyEvent(msg
						.getNotifier(), EMFEditListProperty.this, diff));
			}
		}
	}

	@Override
	protected void doAddListener(Object source, INativePropertyListener listener) {
		EObject eObj = (EObject) source;
		eObj.eAdapters().add((Adapter) listener);
	}

	@Override
	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
		EObject eObj = (EObject) source;
		eObj.eAdapters().remove((Adapter) listener);
	}
}
