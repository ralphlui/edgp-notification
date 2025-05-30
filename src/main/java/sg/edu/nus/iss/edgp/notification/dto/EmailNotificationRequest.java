package sg.edu.nus.iss.edgp.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotificationRequest {

	private String userName;
	private String temporaryPassword;
	private String userEmail;
}
