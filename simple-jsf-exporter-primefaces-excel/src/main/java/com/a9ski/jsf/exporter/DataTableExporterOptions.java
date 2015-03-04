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

import java.io.File;
import java.io.Serializable;

/**
 * Exporter options for {@link DataTableExcelExporter}
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class DataTableExporterOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7572667248366824067L;

	private SelectionType selectionType = SelectionType.ALL;
	private File templateFile = null;
	private int firstHeaderRow = 0;

	public SelectionType getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(final SelectionType selectionType) {
		this.selectionType = selectionType;
	}

	/**
	 * Gets the template file used to construct the export excel.
	 * @return the template file used to construct the export excel.
	 */
	public File getTemplateFile() {
		return this.templateFile;
	}

	/**
	 * Sets the template file used to construct the export excel.
	 * @param templateFile the template file used to construct the export excel.
	 */
	public void setTemplateFile(final File templateFile) {
		this.templateFile = templateFile;
	}
	
	/**
	 * Gets the first header row (zero based). Default value is 0
	 * @return the first header row (zero based)
	 */
	public int getFirstHeaderRow() {
		return firstHeaderRow;
	}
	
	/**
	 * Sets the first header row (zero based)
	 * @param firstHeaderRow the first header row
	 */
	public void setFirstHeaderRow(int firstHeaderRow) {
		this.firstHeaderRow = firstHeaderRow;
	}
}
