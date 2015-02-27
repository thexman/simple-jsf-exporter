package com.a9ski.jsf.exporter;

import java.io.OutputStream;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class DataExporter<C extends UIComponent, O> {
	public DataExporter() {
		super();
	}
	
	public Object getPreProcessorParam() {
		return null;
	}
	
	public Object getPostProcessorParam() {
		return null;
	}
	
	public void export(C component, O options, String fileType, FacesContext facesContext) {
		
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeExport(OutputStream responseOutputStream) {
		
	}
}
