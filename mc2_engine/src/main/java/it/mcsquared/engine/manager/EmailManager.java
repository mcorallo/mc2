package it.mcsquared.engine.manager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.mcsquared.engine.Mc2Engine;
import it.mcsquared.engine.manager.email.Email;

/**
 * 
 * @author mcorallo
 */
public class EmailManager {
	private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);

	private Mc2Engine engine;

	public EmailManager(Mc2Engine engine) throws Exception {
		this.engine = engine;
	}

	public boolean send(Email email) {
		return send(email, engine.isLocalEnv());
	}

	private boolean send(Email email, Boolean test) {
		boolean sent;
		try {
			String subject = email.getSubject();
			Address[] tos = getAdresses(email.getTos());

			if (test) {
				logger.debug("Sending email: " + email);
				sent = true;
			} else {
				logger.debug("Sending email: " + email);
				sent = false;
				Session session = getSession();

				MimeMessage msg = new MimeMessage(session);

				msg.setFrom(new InternetAddress(email.getFrom()));
				msg.addRecipients(Message.RecipientType.TO, tos);
				msg.addRecipients(Message.RecipientType.CC, getAdresses(email.getCcs()));
				msg.addRecipients(Message.RecipientType.BCC, getAdresses(email.getBccs()));
				msg.setSubject(subject);

				List<File> attachments = email.getAttachments();
				if (attachments == null || attachments.size() == 0) {
					if (email.isHtml()) {
						msg.setContent(email.getBody(), "text/html");
					} else {
						msg.setText(email.getBody());
					}
				} else {
					Multipart multipart = new MimeMultipart();
					// text
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setText(email.getBody());
					multipart.addBodyPart(messageBodyPart);
					// attachments
					for (File file : attachments) {
						logger.debug("Attachment: " + file.getName() + " size=" + file.length() + " bytes");
						messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(file);
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName(file.getName());
						multipart.addBodyPart(messageBodyPart);
					}

					// Put parts in message
					if (email.isHtml()) {
						msg.setContent(multipart, "text/html");
					} else {
						msg.setContent(multipart);
					}
				}

				msg.saveChanges();
				logger.debug("Sending");
				try {
					Transport.send(msg);
					sent = true;
				} catch (Exception e) {
					logger.error("", e);
					sent = false;
				}
				logger.info("Sent email: " + subject + ", to " + Arrays.toString(tos));
			}
			return sent;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}
	}

	private static Address[] getAdresses(List<String> adresses) throws AddressException {
		Address[] arrayAdresses;
		if (adresses == null || adresses.size() == 0) {
			arrayAdresses = new Address[0];
		} else {
			arrayAdresses = new Address[adresses.size()];
			int i = 0;
			for (String address : adresses) {
				arrayAdresses[i++] = new InternetAddress(address);
			}
		}
		return arrayAdresses;

	}

	private Session getSession() {
		Properties p = new Properties();
		String[] keys = {
				"mail.transport.protocol",
				"mail.smtp.host",
				"mail.smtp.starttls.enable",
				"mail.smtp.auth",
				"mail.user",
				"mail.password",
				"mail.smtp.port"
		};
		for (String k : keys) {
			String property = engine.getSystemProperty(k);
			if (property == null) {
				continue;
			}
			p.setProperty(k, property);
		}

		String auth = engine.getSystemProperty("mail.smtp.auth");
		Authenticator authenticator = null;
		if (Boolean.parseBoolean(auth)) {
			String user = engine.getSystemProperty("mail.user");
			String pwd = engine.getSystemProperty("mail.password");
			authenticator = new Authenticator(user, pwd);
			p.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
		}
		return Session.getInstance(p, authenticator);
	}
}

class Authenticator extends javax.mail.Authenticator {
	private PasswordAuthentication authentication;

	public Authenticator(String username, String password) {
		authentication = new PasswordAuthentication(username, password);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return authentication;
	}
}
