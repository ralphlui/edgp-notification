package sg.edu.nus.iss.edgp.notification.service.impl;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.notification.configuration.AWSConfig;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.utility.AmazonSES;
import software.amazon.awssdk.services.ses.SesClient;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {
	
	private final AWSConfig awsConfig;	
	private static final Logger logger = LoggerFactory.getLogger(AmazonSES.class);


	public boolean sendChaingDefaultPassword(EmailNotificationRequest emailNotiRequest) {
		
		boolean isSent = false;
		try {
			SesClient client = awsConfig.sesClient();
			String from = awsConfig.getEmailFrom().trim();

			String subject = "Action Required: Change Your Default Password";
			String body = "Welcome to our system.<br> Your account has been successfully created. Please find your temporary login credentials below:<br>"
					+ " Username: " + emailNotiRequest.getUserName() + "<br>"
					+ " Temporary Password: " + emailNotiRequest.getTemporaryPassword() + "<br>"
					+ "For security reasons, you are required to log in and change your password immediately upon first use.<br>"
					+ " Please visit the login page here: " + emailNotiRequest.getLoginUrl() + "<br><br>"
					+ "This is an auto-generated email, please do not reply.";

			isSent = AmazonSES.sendEmail(client, from, Arrays.asList(emailNotiRequest.getUserEmail()), subject, body);
		} catch (Exception ex) {
			logger.error("Exception occurred while sending to change default password", ex);
			throw new EmailNotificationServiceException("An error occured while sending to change default password", ex);	
		}

		return isSent;
		
	}
}
