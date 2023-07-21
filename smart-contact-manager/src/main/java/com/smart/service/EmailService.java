package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) 
	{
		boolean f = false;
		//rest of the code
		
		String from ="ramkrishnakushwah82@gmial.com";
		
		// variable for gmail host
		String host = "smtp.gmail.com";
		
		//get the system properties
		Properties properties = System.getProperties();
		
		System.out.println("PROPERTIES :"+properties);
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", 465);
		properties.put("mail.smtp.ssl.enable", "true");//security socket layer enable
		properties.put("mail.smtp.auth", "true");
		
		//STEP : 1 to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				//psw-fwzlgthtqzmjectb
				return new PasswordAuthentication("ramkrishnakushwah82@gmail.com","fwzlgthtqzmjectb");
			}			
		});
		
		session.setDebug(true);
		
		//STEP : 2 Compose the message [text, multi media]
		MimeMessage mimeMessage = new MimeMessage(session);
		
		try {
			//from email
			mimeMessage.setFrom(from);
			
			//adding recipient to message
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			mimeMessage.setSubject(subject);
			
			//adding text to message
			//mimeMessage.setText(message);
			mimeMessage.setContent(message,"text/html");
			
			//send mail
			//STEP : 3 send the message using Transport class
			Transport.send(mimeMessage);
			
			System.out.println("Sent success....................");
			
			f=true;
			
		} catch (Exception e) {
			System.out.println("Error.....................................");
			e.printStackTrace();
		}
		
		return f;
	}
}
