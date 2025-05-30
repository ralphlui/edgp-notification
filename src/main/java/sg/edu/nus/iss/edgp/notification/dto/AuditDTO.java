package sg.edu.nus.iss.edgp.notification.dto;

import lombok.Getter;
import lombok.Setter;
import sg.edu.nus.iss.edgp.notification.enums.AuditResponseStatus;

@Getter
@Setter
public class AuditDTO {

	private int statusCode = 0;
	private String userId = "";
	private String username = "";
	private String activityType = "";
	private String activityDescription = "";
	private String requestActionEndpoint = "";
	private AuditResponseStatus responseStatus;
	private String requestHTTPVerb;
	private String remarks = "";

}
