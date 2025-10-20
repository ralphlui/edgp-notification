package sg.edu.nus.iss.edgp.notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class EmailNotificationRequest {

	private String userEmail;
	private String token;
	private String subject;
	private String body;
}
