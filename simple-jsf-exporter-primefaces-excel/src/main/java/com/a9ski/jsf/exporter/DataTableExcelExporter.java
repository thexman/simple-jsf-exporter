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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectMany;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.component.api.DynamicColumn;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.celleditor.CellEditor;
import org.primefaces.component.datatable.DataTable;

import com.a9ski.jsf.exporter.exceptions.ExportException;

/**
 * Exporter for primefaces DataTable components
 * 
 * @author Kiril Arabadzhiyski
 *
 */
public class DataTableExcelExporter implements DataExporter<DataTable, DataTableExporterOptions> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 130970127138675488L;

	protected Workbook workbook;
	protected CreationHelper creationHelper;
	protected ExcelFileType type;
	protected DataTableExporterOptions options;

	@Override
	public DataTableExporterOptions getDefaultOptions() {
		return new DataTableExporterOptions();
	}
	
	public DataTableExporterOptions getOptions() {
		return options;
	}

	@Override
	public void init(final DataTable dataTable, final DataTableExporterOptions options, final String fileType, final String fileName, final FacesContext facesContext) throws ExportException {
		final ExcelFileType type = ExcelFileType.parse(fileType);
		if (type == null) {
			throw new IllegalArgumentException("Invalid file type");
		}
		this.type = type;
		switch (type) {
		case XLS:
			workbook = createHSSFWorkbook(options);
			creationHelper = workbook.getCreationHelper();
			break;
		case XLXS:
			workbook = createXSSFWorkbook(options);
			creationHelper = workbook.getCreationHelper();
			break;
		default:
			throw new IllegalArgumentException("Unknown file type " + type.name());
		}

		this.options = options;
	}

	protected XSSFWorkbook createXSSFWorkbook(final DataTableExporterOptions options) throws ExportException {
		final File templateFile = options.getTemplateFile();
		final XSSFWorkbook wb;
		if (templateFile != null && templateFile.exists()) {
			try {
				wb = new XSSFWorkbook(templateFile);
			} catch (final IOException ex) {
				throw new ExportException(ex);
			} catch (final InvalidFormatException ex) {
				throw new ExportException(ex);
			}
		} else {
			wb = new XSSFWorkbook();
		}
		return wb;
	}

	protected HSSFWorkbook createHSSFWorkbook(final DataTableExporterOptions options) throws ExportException {
		final File templateFile = options.getTemplateFile();
		final HSSFWorkbook wb;
		if (templateFile != null && templateFile.exists()) {
			try {
				final InputStream is = new FileInputStream(templateFile);
				try {
					wb = new HSSFWorkbook(is);
					is.close();
				} finally {
					IOUtils.closeQuietly(is);
				}
			} catch (final IOException ex) {
				throw new ExportException(ex);
			}
		} else {
			wb = new HSSFWorkbook();
		}
		return wb;
	}

	@Override
	public void close(final DataTable table, final DataTableExporterOptions options, final String fileType, final String fileName, final FacesContext context) throws ExportException {
		// do nothing
	}

	@Override
	public void export(final DataTable table, DataTableExporterOptions options, final String fileType, final String fileName, final FacesContext context) throws ExportException {
		final Sheet sheet = workbook.createSheet();
		export(table, options, context, sheet);

		table.setRowIndex(-1);
	}

	protected void export(final DataTable table, DataTableExporterOptions options, final FacesContext context, final Sheet sheet) throws ExportException {
		options = getOptions();
		
		addColumnFacets(table, options, sheet, ColumnType.HEADER);

		switch (options.getSelectionType()) {
		case PAGE_ONLY:
			exportPageOnly(context, table, sheet, options);
			break;
		case SELECTION_ONLY:
			exportSelectionOnly(context, table, sheet, options);
			break;
		default:
			exportAll(context, table, sheet, options);
			break;
		}

		if (table.hasFooterColumn()) {
			addColumnFacets(table, options, sheet, ColumnType.FOOTER);
		}
	}

	protected void exportPageOnly(final FacesContext context, final DataTable table, final Sheet sheet, DataTableExporterOptions options) throws ExportException  {
		final int first = table.getFirst();
		int rows = table.getRows();
		if (rows == 0) {
			rows = table.getRowCount();
		}

		final int rowsToExport = first + rows;

		for (int rowIndex = first; rowIndex < rowsToExport; rowIndex++) {
			exportRow(table, sheet, rowIndex);
		}
	}

	protected void exportAll(final FacesContext context, final DataTable table, final Sheet sheet, DataTableExporterOptions options) throws ExportException {
		final int first = table.getFirst();
		final int rowCount = table.getRowCount();
		final int rows = table.getRows();
		final boolean lazy = table.isLazy();

		if (rowCount > 0) {
			if (lazy) {
				final int chunksCount;
				final int chunkSize;
				
				if (options.getChunkSize() > 0) {
					chunkSize = options.getChunkSize();
					chunksCount = rowCount / chunkSize + (rowCount % chunkSize >  0 ? 1 : 0);
				} else {
					chunksCount = 1;
					chunkSize = rowCount;
				}
				
				for(int chunk = 0; chunk < chunksCount; chunk++) {					
					final int firstRow = chunk * chunkSize;  
					table.setFirst(firstRow);
					table.setRows(chunkSize);
					table.clearLazyCache();
					table.loadLazyData();
				
					for (int rowIndex = 0; rowIndex < chunkSize; rowIndex++) {
						exportRow(table, sheet, rowIndex);
					}
				}
	
				// restore
				table.setFirst(first);
				table.setRows(rows);
				table.setRowIndex(-1);
				table.clearLazyCache();
				table.loadLazyData();
			} else {
				for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
					exportRow(table, sheet, rowIndex);
				}
	
				// restore
				table.setFirst(first);
			}
		}
	}

	protected void exportRow(final DataTable table, final Sheet sheet, final int rowIndex) throws ExportException {
		table.setRowIndex(rowIndex);
		if (!table.isRowAvailable()) {
			return;
		}

		exportCells(table, sheet);
	}

	protected void exportCells(final DataTable table, final Sheet sheet) throws ExportException {
		final int sheetRowIndex = sheet.getLastRowNum() + 1;
		final Row row = sheet.createRow(sheetRowIndex);

		for (final UIColumn col : getTableColumns(table)) {
			if (col instanceof DynamicColumn) {
				((DynamicColumn) col).applyStatelessModel();
			}

			if (col.isRendered() && col.isExportable()) {
				addColumnValue(row, col.getChildren(), col, table);
			}
		}
	}

	protected List<UIColumn> getTableColumns(final DataTable table) {
		return table.getColumns();
	}

	protected void exportSelectionOnly(final FacesContext context, final DataTable table, final Sheet sheet, DataTableExporterOptions options2) throws ExportException {
		final Object selection = table.getSelection();
		final String var = table.getVar();

		if (selection != null) {
			final Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

			if (selection.getClass().isArray()) {
				final int size = Array.getLength(selection);

				for (int i = 0; i < size; i++) {
					requestMap.put(var, Array.get(selection, i));
					exportRow(table, sheet, i);
				}
			} else if (List.class.isAssignableFrom(selection.getClass())) {
				final List<?> list = (List<?>) selection;

				for (int i = 0; i < list.size(); i++) {
					requestMap.put(var, list.get(i));
					exportRow(table, sheet, i);
				}
			} else {
				requestMap.put(var, selection);
				exportCells(table, sheet);
			}
		}
	}

	@Override
	public String getContentType() {
		switch (type) {
		case XLS:
			return "application/vnd.ms-excel";
		case XLXS:
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}
		throw new IllegalArgumentException("Unknown file type " + type.name());
	}

	@Override
	public Object getPostProcessorParam() {
		return workbook;
	}

	@Override
	public Object getPreProcessorParam() {
		return workbook;
	}

	@Override
	public void writeExport(final OutputStream os) throws ExportException {
		try {
			workbook.write(os);
		} catch (final IOException ex) {
			throw new ExportException(ex);
		}
	}

	protected void addColumnFacets(final DataTable table, DataTableExporterOptions options, final Sheet sheet, final ColumnType columnType) throws ExportException {
		final int sheetRowIndex = columnType.equals(ColumnType.HEADER) ? getFirstHeaderRow(options) : (sheet.getLastRowNum() + 1);
		final Row rowHeader = sheet.createRow(sheetRowIndex);

		for (final UIColumn col : getTableColumns(table)) {
			if (col instanceof DynamicColumn) {
				((DynamicColumn) col).applyStatelessModel();
			}

			if (col.isRendered() && col.isExportable()) {
				final UIComponent facet = col.getFacet(columnType.facet());
				if (facet != null) {
					addColumnValue(rowHeader, facet, col);
				} else {
					String textValue;
					switch (columnType) {
					case HEADER:
						textValue = col.getHeaderText();
						break;

					case FOOTER:
						textValue = col.getFooterText();
						break;

					default:
						textValue = "";
						break;
					}

					addColumnValue(rowHeader, new ExportValue(textValue, textValue, table), col);
				}
			}
		}
	}

	protected int getFirstHeaderRow(DataTableExporterOptions options) {
		return options.getFirstHeaderRow();
	}

	protected Cell addColumnValue(final Row row, final UIComponent component, final UIColumn tableCol) throws ExportException {
		final ExportValue value = component == null ? new ExportValue("", null, component) : exportValue(FacesContext.getCurrentInstance(), component);
		return addColumnValue(row, value, tableCol);
	}

	protected Cell addColumnValue(final Row row, final ExportValue value, final UIColumn tableCol) throws ExportException {
		final int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		final Cell cell = row.createCell(cellIndex);

		addColumnValue(cell, value, tableCol);
		
		return cell;
	}

	protected void addColumnValue(final Cell cell, final ExportValue value, final UIColumn tableColumn) throws ExportException {
		final String textValue = (value != null ? value.getStringValue() : "");
		cell.setCellValue(creationHelper.createRichTextString(textValue));
	}

	protected void addColumnValue(final Row row, final List<UIComponent> components, final UIColumn tableCol, final DataTable table) throws ExportException {
		final int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		final Cell cell = row.createCell(cellIndex);
		final StringBuilder builder = new StringBuilder();
		final FacesContext context = FacesContext.getCurrentInstance();

		for (final UIComponent component : components) {
			if (component.isRendered()) {
				final String value = exportValue(context, component).getStringValue();

				if (value != null) {
					builder.append(value);
				}
			}
		}

		final String textValue = builder.toString();
		addColumnValue(cell, new ExportValue(textValue, textValue, table), tableCol);
	}

	@SuppressWarnings("rawtypes")
	protected ExportValue exportValue(final FacesContext context, final UIComponent component) throws ExportException {
		if (component instanceof HtmlCommandLink) { // support for PrimeFaces
			// and standard
			// HtmlCommandLink
			final HtmlCommandLink link = (HtmlCommandLink) component;
			final Object value = link.getValue();

			if (value != null) {
				return new ExportValue(String.valueOf(value), value, component);
			} else {
				// export first value holder
				for (final UIComponent child : link.getChildren()) {
					if (child instanceof ValueHolder) {
						return exportValue(context, child);
					}
				}

				return new ExportValue("", null, component);
			}
		} else if (component instanceof ValueHolder) {

			if (component instanceof EditableValueHolder) {
				final Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
				if (submittedValue != null) {
					return new ExportValue(submittedValue.toString(), submittedValue, component);
				}
			}

			final ValueHolder valueHolder = (ValueHolder) component;
			final Object value = valueHolder.getValue();
			if (value == null) {
				return new ExportValue("", value, component);
			}

			Converter converter = valueHolder.getConverter();
			if (converter == null) {
				final Class valueType = value.getClass();
				converter = context.getApplication().createConverter(valueType);
			}

			if (converter != null) {
				if (component instanceof UISelectMany) {
					final StringBuilder builder = new StringBuilder();
					List collection = null;

					if (value instanceof List) {
						collection = (List) value;
					} else if (value.getClass().isArray()) {
						collection = Arrays.asList(value);
					} else {
						throw new FacesException("Value of " + component.getClientId(context) + " must be a List or an Array.");
					}

					final int collectionSize = collection.size();
					for (int i = 0; i < collectionSize; i++) {
						final Object object = collection.get(i);
						builder.append(converter.getAsString(context, component, object));

						if (i < (collectionSize - 1)) {
							builder.append(",");
						}
					}

					final String valuesAsString = builder.toString();
					builder.setLength(0);

					return new ExportValue(valuesAsString, collection, component);
				} else {
					return new ExportValue(converter.getAsString(context, component, value), value, component);
				}
			} else {
				return new ExportValue(value.toString(), value, component);
			}
		} else if (component instanceof CellEditor) {
			return exportValue(context, ((CellEditor) component).getFacet("output"));
		} else if (component instanceof HtmlGraphicImage) {
			return new ExportValue((String) component.getAttributes().get("alt"), component, component);
		} else {
			// This would get the plain texts on UIInstructions when using Facelets
			final String value = component.toString();

			if (value != null) {
				return new ExportValue(value.trim(), component, component);
			} else {
				return new ExportValue("", component, component);
			}
		}
	}

}
