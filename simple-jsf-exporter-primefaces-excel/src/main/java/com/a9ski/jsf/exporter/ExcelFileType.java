package com.a9ski.jsf.exporter;

public enum ExcelFileType {
	XLS, XLXS;

	public static ExcelFileType parse(final String s) {
		for(ExcelFileType t : values()) {
			if (t.name().equalsIgnoreCase(s)) {
				return t;
			}
		}
		throw new IllegalArgumentException(String.format("No enum constant %s.%s. It must be one of the values: %s", ExcelFileType.class.getName(), s, values()));
	}
}
