package com.a9ski.jsf.exporter;

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

public class DataExporterAction implements ActionListener, StateHolder {

	private ValueExpression sourceExpr;
	private ValueExpression optionsExpr;
	private FileInfoDto fileInfo;
	private ClassInfoDto classInfo;
	private CallbackDto callback;
	
	public DataExporterAction() {
		super();
	}
	
	public DataExporterAction(ValueExpression source, ValueExpression options, FileInfoDto fileInfo, ClassInfoDto classInfo, CallbackDto callback) {
		this.sourceExpr = source;
		this.optionsExpr = options;
		this.fileInfo = fileInfo;
		this.classInfo = classInfo;
		this.callback = callback;
	}

	@Override
	public Object saveState(FacesContext context) {
		Object[] values = new Object[5];
		values[0] = sourceExpr;
		values[1] = optionsExpr;
		values[2] = fileInfo;
		values[3] = classInfo;
		values[4] = callback;		
		return values;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] values = (Object[]) state;
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
	public void setTransient(boolean newTransientValue) {
		// do nothing
	}

	@Override
	public void processAction(ActionEvent event) throws AbortProcessingException {
		try {
			export(event);
		} catch (Exception e) {
			throw new FacesException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <C extends UIComponent, O> DataExporter<C,O> createExporter(C sourceComponent, O options, String fileType, FacesContext facesContext, ELContext elContext) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		final Class<? extends DataExporter<C, O>> clazz;
		if (classInfo.getClassValue() != null) {
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
				className = null;
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
	protected <C extends UIComponent, O> void export(ActionEvent event) throws Exception {
		final FacesContext facesContext = FacesContext.getCurrentInstance();
		final ExternalContext externalContext = facesContext.getExternalContext();
		final ELContext elContext = facesContext.getELContext();

		// component to be exported
		final String componentId = (String) sourceExpr.getValue(elContext);		
		
		@SuppressWarnings("unchecked")
		final C sourceComponent = (C) event.getComponent().findComponent(componentId);
		if (sourceComponent == null) {
			throw new FacesException("Could not find component \"" + componentId + "\" in view");
		}
		
	
		final String fileType = (String) fileInfo.getFileType().getValue(elContext);

		// retrieve exporter options
		final O options;
		if (optionsExpr == null) { 
			options = null;
		} else {
			options = (O) optionsExpr.getValue(elContext);
		}

		final DataExporter<C,O> exporter = createExporter(sourceComponent, options, fileType, facesContext, elContext);		
		
		// invoke the pre-processor 
		if (callback.getPreProcessor() != null) {
			callback.getPreProcessor().invoke(elContext, new Object[]{ exporter.getPreProcessorParam() });
		}
		
		// generate the export		
		exporter.export(sourceComponent, options, fileType, facesContext);		
		
		// invoke the post-processor if there is one
		if (callback.getPostProcessor() != null) {
			callback.getPostProcessor().invoke(elContext, new Object[]{ exporter.getPostProcessorParam() });
		}
		
		// configure response meta-data
		externalContext.setResponseContentType(exporter.getContentType());
		externalContext.setResponseHeader("Expires", "0");
		externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		externalContext.setResponseHeader("Pragma", "public");
		
		String encodedFileName = URLEncoder.encode((String)fileInfo.getFileName().getValue(elContext), "UTF-8");
		externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF8''" + encodedFileName);
		
		// write the response and signal JSF that we're done
		facesContext.responseComplete();
		exporter.writeExport(externalContext.getResponseOutputStream());
	}

}
