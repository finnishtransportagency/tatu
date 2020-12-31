package fi.liikennevirasto.tatu.service;

import fi.liikennevirasto.tatu.model.Process;

public interface EmailService {
	public void sendMail(Process paramProcess);
}
