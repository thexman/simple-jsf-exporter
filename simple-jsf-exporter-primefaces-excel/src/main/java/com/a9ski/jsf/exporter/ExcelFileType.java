package com.a9ski.jsf.exporter;

public enum ExcelFileType {
	XLS, XLXS;
	
	public static ExcelFileType parse(String s) {
		 return ExcelFileType.valueOf(s);
	}
}
