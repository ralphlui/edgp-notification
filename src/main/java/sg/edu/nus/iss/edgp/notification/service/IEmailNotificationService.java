package sg.edu.nus.iss.edgp.notification.service;

import java.util.List;

import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import software.amazon.awssdk.services.ses.SesClient;

public interface IEmailNotificationService {

	NotificationDTO sendUserInvitation(EmailNotificationRequest emailNotiRequest);
	
	NotificationDTO sendEmailWithAttachment(EmailNotificationRequest req, byte[] attachment, String fileName);
	
	boolean sendEmailWithAttachment(
	        SesClient sesClient,
	        String from,
	        List<String> to,
	        String subject,
	        String htmlBody,
	        byte[] csvBytes,
	        String fileType,
	        String fileName
	);
}
