/*
 * #%L
 * Simple JSF Exporter Primefaces Excel
 * %%
 * Copyright (C) 2015 A9SKI
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.a9ski.jsf.exporter;

import java.io.Serializable;

import javax.faces.component.UIComponent;

/**
 * Export value
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class ExportValue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1740880443659869984L;
	
	private final String stringValue;
	
	private final Object objectValue;
	
	private final UIComponent component;
	
	public ExportValue(final String stringValue, final Object objectValue, final UIComponent component) {
		super();
		this.stringValue = stringValue;
		this.objectValue = objectValue;
		this.component = component;
	}

	public Object getObjectValue() {
		return objectValue;
	}
	
	public String getStringValue() {
		return stringValue;
	}
	
	public UIComponent getComponent() {
		return component;
	}
}
