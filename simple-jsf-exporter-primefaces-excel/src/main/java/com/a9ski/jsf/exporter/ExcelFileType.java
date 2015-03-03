/*
 * #%L
 * Simple JSF Exporter Primefaces Excel
 * %%
 * Copyright (C) 2015 A9SKI
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

/**
 * Format of the excel file (Excel 97-2003 format OR Excel 2007 format)
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public enum ExcelFileType {
	XLS, XLXS;

	public static ExcelFileType parse(final String s) {
		for (final ExcelFileType t : values()) {
			if (t.name().equalsIgnoreCase(s)) {
				return t;
			}
		}
		throw new IllegalArgumentException(String.format("No enum constant %s.%s. It must be one of the values: %s", ExcelFileType.class.getName(), s, values()));
	}
}
