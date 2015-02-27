package com.a9ski.jsf.exporter;

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

public class DataTableExcelExporter implements DataExporter<DataTable, DataTableExporterOptions> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 130970127138675488L;

	private Workbook workbook;
	private CreationHelper creationHelper;
	private ExcelFileType type;

	@Override
	public DataTableExporterOptions getDefaultOptions() {
		return new DataTableExporterOptions();
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
	}

	protected XSSFWorkbook createXSSFWorkbook(final DataTableExporterOptions options) throws ExportException {
		final InputStream is = options.getTemplateStream();
		final XSSFWorkbook wb;
		if (is != null) {
			try {
				wb = new XSSFWorkbook(is);
				is.close();
			} catch (IOException ex) {
				throw new ExportException(ex);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			wb = new XSSFWorkbook();
		}
		return wb;
	}

	protected HSSFWorkbook createHSSFWorkbook(final DataTableExporterOptions options) throws ExportException {
		final InputStream is = options.getTemplateStream();
		final HSSFWorkbook wb;
		if (is != null) {
			try {
				wb = new HSSFWorkbook(is);
				is.close();
			} catch (IOException ex) {
				throw new ExportException(ex);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} else {
			wb = new HSSFWorkbook();
		}
		return wb;
	}

	@Override
	public void close(final DataTable table, final DataTableExporterOptions options, final String fileType, final String fileName, final FacesContext context) {
		// do nothing
	}

	@Override
	public void export(final DataTable table, DataTableExporterOptions options, final String fileType, final String fileName, final FacesContext context) {
		final Sheet sheet = workbook.createSheet();
		addColumnFacets(table, sheet, ColumnType.HEADER);

		if (options == null) {
			options = getDefaultOptions();
		}

		switch (options.getSelectionType()) {
		case PAGE_ONLY:
			exportPageOnly(context, table, sheet);
			break;
		case SELECTION_ONLY:
			exportSelectionOnly(context, table, sheet);
			break;
		default:
			exportAll(context, table, sheet);
			break;
		}

		if (table.hasFooterColumn()) {
			addColumnFacets(table, sheet, ColumnType.FOOTER);
		}

		table.setRowIndex(-1);
	}

	protected void exportPageOnly(final FacesContext context, final DataTable table, final Sheet sheet) {
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

	protected void exportAll(final FacesContext context, final DataTable table, final Sheet sheet) {
		final int first = table.getFirst();
		final int rowCount = table.getRowCount();
		final int rows = table.getRows();
		final boolean lazy = table.isLazy();

		if (lazy) {
			if (rowCount > 0) {
				table.setFirst(0);
				table.setRows(rowCount);
				table.clearLazyCache();
				table.loadLazyData();
			}

			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				exportRow(table, sheet, rowIndex);
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

	protected void exportRow(final DataTable table, final Sheet sheet, final int rowIndex) {
		table.setRowIndex(rowIndex);
		if (!table.isRowAvailable()) {
			return;
		}

		exportCells(table, sheet);
	}

	protected void exportCells(final DataTable table, final Sheet sheet) {
		final int sheetRowIndex = sheet.getLastRowNum() + 1;
		final Row row = sheet.createRow(sheetRowIndex);

		for (final UIColumn col : table.getColumns()) {
			if (col instanceof DynamicColumn) {
				((DynamicColumn) col).applyStatelessModel();
			}

			if (col.isRendered() && col.isExportable()) {
				addColumnValue(row, col.getChildren(), col);
			}
		}
	}

	protected void exportSelectionOnly(final FacesContext context, final DataTable table, final Sheet sheet) {
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

	protected void addColumnFacets(final DataTable table, final Sheet sheet, final ColumnType columnType) {
		final int sheetRowIndex = columnType.equals(ColumnType.HEADER) ? 0 : (sheet.getLastRowNum() + 1);
		final Row rowHeader = sheet.createRow(sheetRowIndex);

		for (final UIColumn col : table.getColumns()) {
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

					addColumnValue(rowHeader, textValue, col);
				}
			}
		}
	}

	protected void addColumnValue(final Row row, final UIComponent component, final UIColumn tableCol) {
		final String value = component == null ? "" : exportValue(FacesContext.getCurrentInstance(), component);
		addColumnValue(row, value, tableCol);
	}

	protected void addColumnValue(final Row row, final String value, final UIColumn tableCol) {
		final int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		final Cell cell = row.createCell(cellIndex);

		addColumnValue(cell, value, tableCol);
	}

	protected void addColumnValue(final Cell cell, final String value, final UIColumn tableColumn) {
		cell.setCellValue(creationHelper.createRichTextString(value));
	}

	protected void addColumnValue(final Row row, final List<UIComponent> components, final UIColumn tableCol) {
		final int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		final Cell cell = row.createCell(cellIndex);
		final StringBuilder builder = new StringBuilder();
		final FacesContext context = FacesContext.getCurrentInstance();

		for (final UIComponent component : components) {
			if (component.isRendered()) {
				final String value = exportValue(context, component);

				if (value != null) {
					builder.append(value);
				}
			}
		}

		addColumnValue(cell, builder.toString(), tableCol);
	}

	protected String exportValue(final FacesContext context, final UIComponent component) {

		if (component instanceof HtmlCommandLink) { // support for PrimeFaces
			// and standard
			// HtmlCommandLink
			final HtmlCommandLink link = (HtmlCommandLink) component;
			final Object value = link.getValue();

			if (value != null) {
				return String.valueOf(value);
			} else {
				// export first value holder
				for (final UIComponent child : link.getChildren()) {
					if (child instanceof ValueHolder) {
						return exportValue(context, child);
					}
				}

				return "";
			}
		} else if (component instanceof ValueHolder) {

			if (component instanceof EditableValueHolder) {
				final Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
				if (submittedValue != null) {
					return submittedValue.toString();
				}
			}

			final ValueHolder valueHolder = (ValueHolder) component;
			final Object value = valueHolder.getValue();
			if (value == null) {
				return "";
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

					return valuesAsString;
				} else {
					return converter.getAsString(context, component, value);
				}
			} else {
				return value.toString();
			}
		} else if (component instanceof CellEditor) {
			return exportValue(context, ((CellEditor) component).getFacet("output"));
		} else if (component instanceof HtmlGraphicImage) {
			return (String) component.getAttributes().get("alt");
		} else {
			// This would get the plain texts on UIInstructions when using
			// Facelets
			final String value = component.toString();

			if (value != null) {
				return value.trim();
			} else {
				return "";
			}
		}
	}

}
