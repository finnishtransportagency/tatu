package fi.liikennevirasto.tatu.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import fi.liikennevirasto.tatu.model.Process;

@Service("emailService")
public class EmailServiceImpl implements EmailService {

	private static final Logger logger = Logger.getLogger(EmailServiceImpl.class);
	
	@Autowired
	private MailSender mailSender;
	
	@Override
	public void sendMail(Process process) {
		// TODO: Not implemented.
		if (process != null) {
			String address = process.getEmail();
			
			logger.debug("address =" + address);
		}
	}
	

	private void sendSimpleMailMessage(String address) {
		SimpleMailMessage message = new SimpleMailMessage();
		
		message.setTo(address);
		message.setText("Test");
		
		
	}
}
