package sg.edu.nus.iss.edgp.notification.service;

import sg.edu.nus.iss.edgp.notification.dto.AuditDTO;

public interface IAuditService {
	void sendMessage(AuditDTO autAuditDTO, String authorizationHeader);
}
