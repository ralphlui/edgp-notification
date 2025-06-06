package sg.edu.nus.iss.edgp.notification.service;

import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;

public interface IEmailNotificationService {

	NotificationDTO sendUserInvitation(EmailNotificationRequest emailNotiRequest);
}
