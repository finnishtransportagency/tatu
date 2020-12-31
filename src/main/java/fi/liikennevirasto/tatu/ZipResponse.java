package fi.liikennevirasto.tatu;

import java.io.File;

public class ZipResponse extends ReaderResponse {

	public enum ZipStatus {
		OK,
		NOT_READY,
		ERROR,
		NOT_FOUND;
	}
	
	private ZipStatus zipStatus;
	private File zipFile;
	private String errorMessage;
	
	public ZipResponse(ZipStatus zipStatus, File zipFile, int totalRows, int errorRows) {
		super(totalRows, errorRows);
		this.zipStatus = zipStatus;
		this.zipFile = zipFile;
	}
	
	public ZipResponse(String errorMessage) {
		super(0, 0);
		this.zipStatus = ZipStatus.ERROR;
		this.errorMessage = errorMessage;
	}

	public ZipStatus getZipStatus() {
		return zipStatus;
	}

	public File getZipFile() {
		return zipFile;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	
}
