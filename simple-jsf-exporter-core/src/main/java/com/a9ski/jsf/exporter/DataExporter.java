package com.a9ski.jsf.exporter;

import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.a9ski.jsf.exporter.exceptions.ExportException;

public interface DataExporter<C extends UIComponent, O extends Serializable> extends Serializable {

	public Object getPreProcessorParam() throws ExportException;

	public Object getPostProcessorParam() throws ExportException;

	public void init(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	public void export(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	public String getContentType() throws ExportException;

	public void writeExport(OutputStream responseOutputStream) throws ExportException;

	public void close(C component, O options, String fileType, String fileName, FacesContext facesContext) throws ExportException;

	public O getDefaultOptions();
}
