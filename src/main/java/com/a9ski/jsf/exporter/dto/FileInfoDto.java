package com.a9ski.jsf.exporter.dto;

import java.io.Serializable;

import javax.el.ValueExpression;
import javax.faces.view.facelets.TagAttribute;

public class FileInfoDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5407190444968339112L;

	private final ValueExpression fileName;
	private final ValueExpression fileType;
	
	public FileInfoDto(ValueExpression fileName, ValueExpression fileType) {
		super();
		this.fileName = fileName;
		this.fileType = fileType;
	}

	public ValueExpression getFileName() {
		return fileName;
	}

	public ValueExpression getFileType() {
		return fileType;
	}	
}
