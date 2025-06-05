package sg.edu.nus.iss.edgp.notification.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;

@Component
public class DTOMapper {

	public static NotificationDTO toNotificationDTO(String userName, boolean isSent) {
		NotificationDTO notiDTO = new NotificationDTO();
		notiDTO.setUserEmail(userName);
		notiDTO.setSent(isSent);
		return notiDTO;
	}
}
