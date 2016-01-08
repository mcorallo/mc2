package it.mcsquared.engine.manager;

import static org.junit.Assert.*;
import it.mcsquared.engine.EngineTest;
import it.mcsquared.engine.manager.email.Email;
import it.mcsquared.engine.test.PrivateMethodDetails;

import java.util.Arrays;

import org.junit.Test;
import org.subethamail.wiser.WiserMessage;

public class MailManagerTest extends EngineTest {

	@Test
	public void sendEmailTest() throws Exception {
		startMailServer();

		EmailManager mailManager = engine.getMailManager();
		Email email = new Email();
		email.setTos(Arrays.asList("you"));
		email.setFrom("me");
		email.setSubject("subject");
		email.setBody("body");

		PrivateMethodDetails details = new PrivateMethodDetails();
		details.setName("send");
		details.setClazz(EmailManager.class);
		details.addParam(Email.class, email);
		details.addParam(Boolean.class, false);

		boolean sent = invokePrivateMethod(mailManager, details);
		assertTrue(sent);

		Thread.sleep(2000);

		int receivedSize = mailServer.getMessages().size();
		assertEquals(1, receivedSize);
		WiserMessage message = mailServer.getMessages().get(0);
		assertTrue(message.toString().contains("From: me"));
		assertTrue(message.toString().contains("To: you"));
		assertTrue(message.toString().contains("Subject: subject"));
		assertTrue(message.toString().contains("body"));
	}

	//TODO [mc2] attachment test

}
