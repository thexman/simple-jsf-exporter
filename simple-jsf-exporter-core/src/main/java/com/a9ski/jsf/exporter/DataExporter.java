/*
 * #%L
 * Simple JSF Exporter Core
 * %%
 * Copyright (C) 2015 Kiril Arabadzhiyski
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
/**
 *  
 * Copyright 2015 Kiril Arabadzhiyski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.a9ski.jsf.exporter;

import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.a9ski.jsf.exporter.exceptions.ExportException;

/**
 * All exporter must implement this interface.
 * 
 * The exporter lifecycle is:
 * <ol>
 * <li>Instantiating exporter class</li>
 * <li>Calling
 * {@link DataExporter#init(UIComponent, Serializable, String, String, FacesContext)}
 * to initialize the exporter</li>
 * <li>Calling preProcessor({@link DataExporter#getPreProcessorParam()}). The
 * preProcessor method is specified in the JSF tag</li>
 * <li>Calling
 * {@link DataExporter#export(UIComponent, Serializable, String, String, FacesContext)}
 * to perform actual data export</li>
 * <li>Calling postProcessor({@link DataExporter#getPostProcessorParam()}). The
 * postProcessor method is specified in the JSF tag</li>
 * <li>Calling {@link DataExporter#writeExport(OutputStream)} to write the
 * content of the export to the HTTP output stream</li>
 * <li>Calling
 * {@link DataExporter#close(UIComponent, Serializable, String, String, FacesContext)}
 * so the exporter could free any underlying resources</li>
 * <li>The exporter class left to be garbage collected</li>
 * </ol>
 * 
 * @author Kiril Arabadzhiyski
 *
 * @param <C>
 *            JSF component being exported (e.g. DataTable)
 * @param <O>
 *            class containing export options
 */
public interface DataExporter<C extends UIComponent, O extends Serializable> extends Serializable {

	/**
	 * Object being passed to the preProcessor method
	 * 
	 * @return Object being passed to the preProcessor method
	 * @throws ExportException
	 */
	public Object getPreProcessorParam() throws ExportException;

	/**
	 * Object being passed to the postProcessor method
	 * 
	 * @return Object being passed to the postProcessor method
	 * @throws ExportException
	 */
	public Object getPostProcessorParam() throws ExportException;

	/**
	 * Initializes the exporter.
	 * <p>
	 * This method is the first method being called after the exporter is
	 * instantiated
	 * </p>
	 * 
	 * @param component
	 *            the JSF component being exported
	 * @param options
	 *            the exporter's option.
	 * @param fileType
	 *            the export file type (from the tag value)
	 * @param fileName
	 *            the export file name (from the tag value)
	 * @param facesContext
	 *            faces context
	 * @throws ExportException
	 */
	public void init(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	/**
	 * Exports the data from the component.
	 * 
	 * @param component
	 *            the JSF component being exported
	 * @param options
	 *            the exporter's option.
	 * @param fileType
	 *            the export file type (from the tag value)
	 * @param fileName
	 *            the export file name (from the tag value)
	 * @param facesContext
	 *            faces context
	 * @throws ExportException
	 */
	public void export(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	/**
	 * The content type that is returned to the browser. e.g. "image/jpeg"
	 * 
	 * @return the content type that is returned to the browser
	 * @throws ExportException
	 */
	public String getContentType() throws ExportException;

	/**
	 * Writes the export data to the Http response output stream
	 * 
	 * @param responseOutputStream
	 *            HTTP response output stream
	 * @throws ExportException
	 */
	public void writeExport(OutputStream responseOutputStream) throws ExportException;

	/**
	 * Closes any underlying resources.
	 * <p>
	 * This is the last method called, before the exporter is left to be garbage
	 * collected
	 * </p>
	 * 
	 * @param component
	 *            the JSF component being exported
	 * @param options
	 *            the exporter's option.
	 * @param fileType
	 *            the export file type (from the tag value)
	 * @param fileName
	 *            the export file name (from the tag value)
	 * @param facesContext
	 *            faces context
	 * @throws ExportException
	 */
	public void close(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	/**
	 * Returns default exporter's options (in case no options are specified via
	 * the JSF tag)
	 * 
	 * @return default exporter's options
	 */
	public O getDefaultOptions();
}
