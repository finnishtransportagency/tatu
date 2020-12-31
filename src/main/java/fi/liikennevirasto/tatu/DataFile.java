package fi.liikennevirasto.tatu;

import java.util.ArrayList;
import java.util.List;

public class DataFile {

	private ArrayList<String> columnNames;
	private ArrayList<DataColumn> colmunData;
	
	public DataFile(List<String> columnNames) {
		this.columnNames = new ArrayList<String>(columnNames);
		this.colmunData = new ArrayList<DataColumn>();
	}

	/**
	 * Add row data to columns.
	 * @param rowData
	 * @throws TatuException
	 */
	public void addRow(ArrayList<Object> rowData) throws TatuException {
		if (rowData.size() != columnNames.size())
			throw new TatuException("Illegal size of data");

		for (int i = 0; i < rowData.size(); i++) {
			this.colmunData.get(i).addData(rowData.get(i));
		}
		
	}

	public ArrayList<String> getColumnNames() {
		return columnNames;
	}

}
