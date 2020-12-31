package fi.liikennevirasto.tatu;

public class ReaderResponse {

	private int totalRows;
	private int errorRows;
	
	public ReaderResponse(int totalRows, int errorRows) {
		this.totalRows = totalRows;
		this.errorRows = errorRows;
	}
	
	public void addToTotalCount() {
		totalRows++;
	}
	
	public void addToErrorCount() {
		errorRows++;
	}
	
	public int getTotalRows() {
		return totalRows;
	}
	public int getErrorRows() {
		return errorRows;
	}
	
	
}
