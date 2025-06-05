package sg.edu.nus.iss.edgp.notification.service.impl;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.notification.configuration.AWSConfig;
import sg.edu.nus.iss.edgp.notification.configuration.EDGPNotificationConfig;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.utility.AmazonSES;
import sg.edu.nus.iss.edgp.notification.utility.DTOMapper;
import software.amazon.awssdk.services.ses.SesClient;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

	private final AWSConfig awsConfig;
	private final EDGPNotificationConfig edgpNotificationConfig;
	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

	public NotificationDTO sendInitialPasswordSetRequest(EmailNotificationRequest emailNotiRequest) {

		try {
			SesClient client = awsConfig.sesClient();
			String from = awsConfig.getEmailFrom().trim();

			String subject = "Action Required: Change Your Default Password";
			String body = "Welcome to our system.<br> Your account has been successfully created.<br>"
					+ " Username: " + emailNotiRequest.getUserEmail() + "<br>" 
					+ "For security reasons, you are required to set your password immediately upon first use.<br>"
					+ " Please visit the login page here: " + edgpNotificationConfig.getClientLoginURL() + "<br><br>"
					+ "This is an auto-generated email, please do not reply.";

			boolean isSent = AmazonSES.sendEmail(client, from, Arrays.asList(emailNotiRequest.getUserEmail()), subject,
					body);
			return DTOMapper.toNotificationDTO(emailNotiRequest.getUserEmail(), isSent);
		} catch (Exception ex) {
			logger.error("Exception occurred while sending to change default password", ex);
			throw new EmailNotificationServiceException("An error occured while sending to change default password",
					ex);
		}

	}
}
