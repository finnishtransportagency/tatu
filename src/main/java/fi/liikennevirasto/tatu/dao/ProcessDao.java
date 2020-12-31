package fi.liikennevirasto.tatu.dao;

import java.util.List;

import fi.liikennevirasto.tatu.model.Process;

public interface ProcessDao {

	public void createProcess(Process process);
	public void updateProcess(Process process);
	public Process getProcess(String uuid);
	public List<Process> getProcessesWithStatus(int status);
	public Process getOldestProcess();
	public void deleteProcess(Process process);
}
