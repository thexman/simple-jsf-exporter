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
package com.a9ski.jsf.exporter;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.a9ski.jsf.exporter.dto.CallbackDto;
import com.a9ski.jsf.exporter.dto.ClassInfoDto;
import com.a9ski.jsf.exporter.dto.FileInfoDto;
import com.a9ski.jsf.exporter.exceptions.ExportException;

/**
 * ActionListener that performs data exporting
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class DataExporterAction implements ActionListener, StateHolder {

	private ValueExpression sourceExpr;
	private ValueExpression optionsExpr;
	private FileInfoDto fileInfo;
	private ClassInfoDto classInfo;
	private CallbackDto callback;

	public DataExporterAction() {
		super();
	}

	public DataExporterAction(final ValueExpression source, final ValueExpression options, final FileInfoDto fileInfo, final ClassInfoDto classInfo, final CallbackDto callback) {
		this.sourceExpr = source;
		this.optionsExpr = options;
		this.fileInfo = fileInfo;
		this.classInfo = classInfo;
		this.callback = callback;
	}

	@Override
	public Object saveState(final FacesContext context) {
		final Object[] values = new Object[5];
		values[0] = sourceExpr;
		values[1] = optionsExpr;
		values[2] = fileInfo;
		values[3] = classInfo;
		values[4] = callback;
		return values;
	}

	@Override
	public void restoreState(final FacesContext context, final Object state) {
		final Object[] values = (Object[]) state;
		sourceExpr = (ValueExpression) values[0];
		optionsExpr = (ValueExpression) values[1];
		fileInfo = (FileInfoDto) values[2];
		classInfo = (ClassInfoDto) values[3];
		callback = (CallbackDto) values[4];
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public void setTransient(final boolean newTransientValue) {
		// do nothing
	}

	@Override
	public void processAction(final ActionEvent event) throws AbortProcessingException {
		try {
			export(event);
		} catch (final Exception e) {
			throw new FacesException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <C extends UIComponent, O extends Serializable> DataExporter<C, O> createExporter(final ELContext elContext) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		final Class<? extends DataExporter<C, O>> clazz;
		if (classInfo.getClassValue() == null) {
			final ClassLoader cl;
			if (classInfo.getClassLoader() != null) {
				cl = (ClassLoader) classInfo.getClassLoader().getValue(elContext);
			} else {
				if (Thread.currentThread().getContextClassLoader() != null) {
					cl = Thread.currentThread().getContextClassLoader();
				} else {
					cl = getClass().getClassLoader();
				}
			}
			final String className;
			if (classInfo.getClassName() != null) {
				className = (String) classInfo.getClassName().getValue(elContext);
			} else {
				throw new IllegalArgumentException("Requires either exporterClassName or exporterClass");
			}

			clazz = (Class<? extends DataExporter<C, O>>) cl.loadClass(className);
		} else {
			clazz = (Class<? extends DataExporter<C, O>>) classInfo.getClassValue().getValue(elContext);
		}

		if (clazz != null) {
			return clazz.newInstance();
		}
		throw new IllegalArgumentException("Cannot instantiate importer class");
	}

	@SuppressWarnings("unchecked")
	protected <C extends UIComponent, O extends Serializable> void export(final ActionEvent event) throws Exception {
		final FacesContext facesContext = FacesContext.getCurrentInstance();
		final ExternalContext externalContext = facesContext.getExternalContext();
		final ELContext elContext = facesContext.getELContext();

		// component to be exported
		final String componentId = (String) sourceExpr.getValue(elContext);

		final C sourceComponent = (C) event.getComponent().findComponent(componentId);
		if (sourceComponent == null) {
			throw new FacesException("Could not find component \"" + componentId + "\" in view");
		}

		final String fileType = (String) fileInfo.getFileType().getValue(elContext);
		final String fileName = (String) fileInfo.getFileName().getValue(elContext);

		// create exporter
		final DataExporter<C, O> exporter = createExporter(elContext);

		// retrieve exporter options
		final O options;
		if (optionsExpr == null) {
			options = exporter.getDefaultOptions();
		} else {
			final O o = (O) optionsExpr.getValue(elContext);
			if (o == null) {
				options = exporter.getDefaultOptions();
			} else {
				options = o;
			}
		}

		try {
			// initialize exporter
			exporter.init(sourceComponent, options, fileType, fileName, facesContext);

			// invoke the pre-processor
			if (callback.getPreProcessor() != null) {
				callback.getPreProcessor().invoke(elContext, new Object[] { exporter.getPreProcessorParam() });
			}

			// generate the export
			exporter.export(sourceComponent, options, fileType, fileName, facesContext);

			// invoke the post-processor if there is one
			if (callback.getPostProcessor() != null) {
				callback.getPostProcessor().invoke(elContext, new Object[] { exporter.getPostProcessorParam() });
			}

			// write exporter response
			writeExport(facesContext, externalContext, elContext, exporter);
		} finally {
			exporter.close(sourceComponent, options, fileType, fileName, facesContext);
		}
	}

	private <C extends UIComponent, O extends Serializable> void writeExport(final FacesContext facesContext, final ExternalContext externalContext, final ELContext elContext, final DataExporter<C, O> exporter) throws ExportException, UnsupportedEncodingException, IOException {

		externalContext.setResponseContentType(exporter.getContentType());
		externalContext.setResponseHeader("Expires", "0");
		externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		externalContext.setResponseHeader("Pragma", "public");

		final String encodedFileName = URLEncoder.encode((String) fileInfo.getFileName().getValue(elContext), "UTF-8");
		externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF8''" + encodedFileName);

		exporter.writeExport(externalContext.getResponseOutputStream());

		// write the response and signal JSF that we're done
		facesContext.responseComplete();
	}

}
