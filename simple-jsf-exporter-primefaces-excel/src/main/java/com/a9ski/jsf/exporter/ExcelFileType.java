package com.a9ski.jsf.exporter;

public enum ExcelFileType {
	XLS, XLXS;

	public static ExcelFileType parse(final String s) {
		return ExcelFileType.valueOf(s);
	}
}
