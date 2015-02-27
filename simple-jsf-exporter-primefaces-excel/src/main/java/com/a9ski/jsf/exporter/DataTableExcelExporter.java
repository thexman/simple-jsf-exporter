package com.a9ski.jsf.exporter;

import java.io.IOException;
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
	public void init(DataTable dataTable, DataTableExporterOptions options,
			String fileType, String fileName, FacesContext facesContext) {
		final ExcelFileType type = ExcelFileType.parse(fileType);
		if (type == null) {
			throw new IllegalArgumentException("Invalid file type");
		}
		this.type = type;
		switch (type) {
		case XLS:
			workbook = new HSSFWorkbook();
			creationHelper = workbook.getCreationHelper();
			break;
		case XLXS:
			workbook = new XSSFWorkbook();
			creationHelper = workbook.getCreationHelper();
			break;
		default:
			throw new IllegalArgumentException("Unknown file type "
					+ type.name());
		}
	}

	@Override
	public void close(DataTable table, DataTableExporterOptions options, String fileType, String fileName, FacesContext context) {
		// do nothing
	}

	
	@Override
	public void export(DataTable table, DataTableExporterOptions options, String fileType, String fileName, FacesContext context) {
		final Sheet sheet = workbook.createSheet();
		addColumnFacets(table, sheet, ColumnType.HEADER);

		if (options == null) {
			options = getDefaultOptions();
		}
		
		switch (options.getSelectionType()) {
			case PAGE_ONLY: exportPageOnly(context, table, sheet); break;
			case SELECTION_ONLY: exportSelectionOnly(context, table, sheet); break;
			default: exportAll(context, table, sheet); break;
		}
		
		if (table.hasFooterColumn()) {
			addColumnFacets(table, sheet, ColumnType.FOOTER);
		}

		table.setRowIndex(-1);
	}
	
	protected void exportPageOnly(FacesContext context, DataTable table, Sheet sheet) {        
        int first = table.getFirst();
        int rows = table.getRows();
        if(rows == 0) {
            rows = table.getRowCount();
        }
        
    	int rowsToExport = first + rows;
        
        for(int rowIndex = first; rowIndex < rowsToExport; rowIndex++) {                
            exportRow(table, sheet, rowIndex);
        }
    }
    

	protected void exportAll(FacesContext context, DataTable table,	Sheet sheet) {
		int first = table.getFirst();
		int rowCount = table.getRowCount();
		int rows = table.getRows();
		boolean lazy = table.isLazy();

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

	protected void exportRow(DataTable table, Sheet sheet, int rowIndex) {
		table.setRowIndex(rowIndex);
		if (!table.isRowAvailable()) {
			return;
		}

		exportCells(table, sheet);
	}
	
    protected void exportCells(DataTable table, Sheet sheet) {
        int sheetRowIndex = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(sheetRowIndex);
        
        for (UIColumn col : table.getColumns()) {
            if (col instanceof DynamicColumn) {
                ((DynamicColumn) col).applyStatelessModel();
            }
                        
            if (col.isRendered() && col.isExportable()) {
                addColumnValue(row, col.getChildren());
            }
        }
    }
    

	protected void exportSelectionOnly(FacesContext context, DataTable table, Sheet sheet) {
		Object selection = table.getSelection();
		String var = table.getVar();

		if (selection != null) {
			Map<String, Object> requestMap = context.getExternalContext()
					.getRequestMap();

			if (selection.getClass().isArray()) {
				int size = Array.getLength(selection);

				for (int i = 0; i < size; i++) {
					requestMap.put(var, Array.get(selection, i));
					exportRow(table, sheet, i);
				}
			} else if (List.class.isAssignableFrom(selection.getClass())) {
				List<?> list = (List<?>) selection;

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
	public void writeExport(OutputStream os) throws ExportException {
		try {
			workbook.write(os);
		} catch (final IOException ex) {
			throw new ExportException(ex);
		}
	}

	protected void addColumnFacets(DataTable table, Sheet sheet, ColumnType columnType) {
		int sheetRowIndex = columnType.equals(ColumnType.HEADER) ? 0 : (sheet.getLastRowNum() + 1);
		Row rowHeader = sheet.createRow(sheetRowIndex);

		for (UIColumn col : table.getColumns()) {
			if (col instanceof DynamicColumn) {
				((DynamicColumn) col).applyStatelessModel();
			}

			if (col.isRendered() && col.isExportable()) {
				UIComponent facet = col.getFacet(columnType.facet());
				if (facet != null) {
					addColumnValue(rowHeader, facet);
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

					addColumnValue(rowHeader, textValue);
				}
			}
		}
	}

	protected void addColumnValue(Row row, UIComponent component) {
		String value = component == null ? "" : exportValue(FacesContext.getCurrentInstance(), component);
		addColumnValue(row, value);
	}

	protected void addColumnValue(Row row, String value) {
		int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		Cell cell = row.createCell(cellIndex);

		
		cell.setCellValue(creationHelper.createRichTextString(value));
	}

	protected void addColumnValue(Row row, List<UIComponent> components) {
		int cellIndex = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
		Cell cell = row.createCell(cellIndex);
		StringBuilder builder = new StringBuilder();
		FacesContext context = FacesContext.getCurrentInstance();

		for (UIComponent component : components) {
			if (component.isRendered()) {
				String value = exportValue(context, component);

				if (value != null)
					builder.append(value);
			}
		}

		cell.setCellValue(creationHelper.createRichTextString(builder.toString()));
	}

	protected String exportValue(FacesContext context, UIComponent component) {

		if (component instanceof HtmlCommandLink) { // support for PrimeFaces
													// and standard
													// HtmlCommandLink
			HtmlCommandLink link = (HtmlCommandLink) component;
			Object value = link.getValue();

			if (value != null) {
				return String.valueOf(value);
			} else {
				// export first value holder
				for (UIComponent child : link.getChildren()) {
					if (child instanceof ValueHolder) {
						return exportValue(context, child);
					}
				}

				return "";
			}
		} else if (component instanceof ValueHolder) {

			if (component instanceof EditableValueHolder) {
				Object submittedValue = ((EditableValueHolder) component)
						.getSubmittedValue();
				if (submittedValue != null) {
					return submittedValue.toString();
				}
			}

			ValueHolder valueHolder = (ValueHolder) component;
			Object value = valueHolder.getValue();
			if (value == null) {
				return "";
			}

			Converter converter = valueHolder.getConverter();
			if (converter == null) {
				Class valueType = value.getClass();
				converter = context.getApplication().createConverter(valueType);
			}

			if (converter != null) {
				if (component instanceof UISelectMany) {
					StringBuilder builder = new StringBuilder();
					List collection = null;

					if (value instanceof List) {
						collection = (List) value;
					} else if (value.getClass().isArray()) {
						collection = Arrays.asList(value);
					} else {
						throw new FacesException("Value of "
								+ component.getClientId(context)
								+ " must be a List or an Array.");
					}

					int collectionSize = collection.size();
					for (int i = 0; i < collectionSize; i++) {
						Object object = collection.get(i);
						builder.append(converter.getAsString(context,
								component, object));

						if (i < (collectionSize - 1)) {
							builder.append(",");
						}
					}

					String valuesAsString = builder.toString();
					builder.setLength(0);

					return valuesAsString;
				} else {
					return converter.getAsString(context, component, value);
				}
			} else {
				return value.toString();
			}
		} else if (component instanceof CellEditor) {
			return exportValue(context,
					((CellEditor) component).getFacet("output"));
		} else if (component instanceof HtmlGraphicImage) {
			return (String) component.getAttributes().get("alt");
		} else {
			// This would get the plain texts on UIInstructions when using
			// Facelets
			String value = component.toString();

			if (value != null)
				return value.trim();
			else
				return "";
		}
	}


}
