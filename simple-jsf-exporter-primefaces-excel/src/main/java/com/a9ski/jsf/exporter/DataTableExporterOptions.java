package com.a9ski.jsf.exporter;

import java.io.InputStream;
import java.io.Serializable;

public class DataTableExporterOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7572667248366824067L;

	private SelectionType selectionType = SelectionType.ALL;
	
	public SelectionType getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(final SelectionType selectionType) {
		this.selectionType = selectionType;
	}
	
	public InputStream getTemplateStream() {
		return null;
	}

}
