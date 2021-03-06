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
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;

/**
 * @since 3.3
 * 
 */
public class BeanSetProperty extends SimpleSetProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	public BeanSetProperty(PropertyDescriptor propertyDescriptor,
			Class elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = elementType == null ? BeanPropertyHelper
				.getCollectionPropertyElementType(propertyDescriptor)
				: elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected Set doGetSet(Object source) {
		return asSet(BeanPropertyHelper
				.readProperty(source, propertyDescriptor));
	}

	private Set asSet(Object propertyValue) {
		if (propertyValue == null)
			return Collections.EMPTY_SET;
		if (propertyDescriptor.getPropertyType().isArray())
			return new HashSet(Arrays.asList((Object[]) propertyValue));
		return (Set) propertyValue;
	}

	protected void doSetSet(Object source, Set set, SetDiff diff) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor,
				convertSetToBeanPropertyType(set));
	}

	private Object convertSetToBeanPropertyType(Set set) {
		Object propertyValue = set;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class componentType = propertyDescriptor.getPropertyType()
					.getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, set
					.size());
			propertyValue = set.toArray(array);
		}
		return propertyValue;
	}

	public INativePropertyListener adaptListener(
			final ISimplePropertyListener listener) {
		return new Listener(listener);
	}

	private class Listener implements INativePropertyListener,
			PropertyChangeListener {
		private final ISimplePropertyListener listener;

		private Listener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			SetDiff diff;
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			if (oldValue != null && newValue != null) {
				diff = Diffs.computeSetDiff(asSet(oldValue), asSet(newValue));
			} else {
				diff = null;
			}
			if (propertyDescriptor.getName().equals(evt.getPropertyName())) {
				listener.handlePropertyChange(new SimplePropertyEvent(evt
						.getSource(), BeanSetProperty.this, diff));
			}
		}
	}

	public void doAddListener(Object source, INativePropertyListener listener) {
		BeanPropertyListenerSupport.hookListener(source, propertyDescriptor
				.getName(), (PropertyChangeListener) listener);
	}

	public void doRemoveListener(Object source, INativePropertyListener listener) {
		BeanPropertyListenerSupport.unhookListener(source, propertyDescriptor
				.getName(), (PropertyChangeListener) listener);
	}

	public String toString() {
		Class beanClass = propertyDescriptor.getReadMethod()
				.getDeclaringClass();
		String propertyName = propertyDescriptor.getName();
		String s = beanClass.getName() + "." + propertyName + "{}"; //$NON-NLS-1$ //$NON-NLS-2$

		if (elementType != null)
			s += " <" + elementType.getName() + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
