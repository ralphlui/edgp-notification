package sg.edu.nus.iss.edgp.notification.service;

import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;

public interface IEmailNotificationService {

	boolean sendChaingDefaultPassword(EmailNotificationRequest emailNotiRequest);
}
