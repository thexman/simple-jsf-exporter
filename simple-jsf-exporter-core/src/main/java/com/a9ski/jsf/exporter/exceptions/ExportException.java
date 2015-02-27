package com.a9ski.jsf.exporter.exceptions;

public class ExportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8590854586675804745L;

	public ExportException() {
		super();
	}

	public ExportException(String message) {
		super(message);
	}

	public ExportException(Throwable cause) {
		super(cause);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}

}
