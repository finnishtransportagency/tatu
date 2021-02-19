package fi.liikennevirasto.tatu.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tatu_sessions_sovt")
//@Table(name = "tatu_sessions")
public class Process implements Serializable {

	private static final long serialVersionUID = 8722525151509394998L;

	@Id
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

	@Column(name = "email", unique = false, nullable = true)
    private String email;

	@Column(name = "filetype", unique = false, nullable = true)
    private String filetype;

	@Column(name = "datafile", unique = false, nullable = true)
    private String datafile;
	
	@Column(name = "kuvfile", unique = false, nullable = true)
    private String kuvfile;
	
	@Column(name = "filename", unique = false, nullable = true)
    private String filename;

	@Column(name = "timestamp", unique = false, nullable = true)
    private Date timestamp;

	@Column(name = "status", unique = false, nullable = false)
    private int status;

	@Column(name = "lines_total", unique = false, nullable = false)
    private int linesTotal;

	@Column(name = "lines_errors", unique = false, nullable = false)
    private int linesErrors;

	@Column(name = "error", unique = false, nullable = true)
    private String error;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public String getDatafile() {
		return datafile;
	}

	public void setDatafile(String datafile) {
		this.datafile = datafile;
	}

	public String getKuvfile() {
		return kuvfile;
	}

	public void setKuvfile(String kuvfile) {
		this.kuvfile = kuvfile;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getLinesTotal() {
		return linesTotal;
	}

	public void setLinesTotal(int linesTotal) {
		this.linesTotal = linesTotal;
	}

	public int getLinesErrors() {
		return linesErrors;
	}

	public void setLinesErrors(int linesErrors) {
		this.linesErrors = linesErrors;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
