package sg.edu.nus.iss.edgp.notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.notification.dto.APIResponse;
import sg.edu.nus.iss.edgp.notification.dto.AuditDTO;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.service.impl.AuditService;
import sg.edu.nus.iss.edgp.notification.service.impl.EmailNotificationService;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class EmailNotificationController {

	private final EmailNotificationValidationStrategy emailNotificationValidationStrategy;
	private final EmailNotificationService emailNotificationService;
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";
	private final AuditService auditService;
	
	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationController.class);

	@PostMapping(value = "/invitation-user", produces = "application/json")
	public  ResponseEntity<APIResponse<NotificationDTO>> sendUserInvitation(@RequestHeader("Authorization") String authorizationHeader,
			@RequestBody EmailNotificationRequest emailNotiReq) {

		logger.info("Calling email notification initial password set request API ...");
		String message = "";
		String activityType = "Sending initial password set request";
		String endpoint = "/api/notifications/invitation-user";
		String httpMethod = HttpMethod.POST.name();
		
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);
		
		try {
			ValidationResult validationResult = emailNotificationValidationStrategy.validateObject(emailNotiReq,false);
			if (!validationResult.isValid()) {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO, validationResult.getStatus().value(), message, authorizationHeader);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}

			NotificationDTO notiDTO = emailNotificationService.sendUserInvitation(emailNotiReq);
			boolean isSent = notiDTO.isSent();
			message = isSent ? "Email is sent successfully." : "Email is not sent successfully.";
			HttpStatus status = isSent ? HttpStatus.OK : validationResult.getStatus();
			auditService.logAudit(auditDTO, status.value(), message, authorizationHeader);
			return isSent ? ResponseEntity.status(status).body(APIResponse.success(notiDTO, message))
					: ResponseEntity.status(status).body(APIResponse.error(message));

		} catch (Exception e) {
			message = e instanceof EmailNotificationServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	@PostMapping(value = "/send-email-with-attachment")
	public ResponseEntity<APIResponse<NotificationDTO>> sendAttachmentEmail(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestPart("EmailRequest") EmailNotificationRequest emailNotiReq,
			@RequestParam("file") MultipartFile file) {
		
		logger.info("Calling email notification with attachement API ...");
		String message = "";
		String activityType = "Sending notification with attachement";
		String endpoint = "/api/notifications/send-email-with-attachment";
		String httpMethod = HttpMethod.POST.name();
		
		AuditDTO auditDTO = auditService.createAuditDTO(activityType, endpoint, httpMethod);

		
	    try {
	    	 // Validate attachment presence
	        if (file == null || file.isEmpty()) {
	        	message = "CSV attachment is required to send the email.";
	        	auditService.logAudit(auditDTO, 400, message, authorizationHeader);
	        	return ResponseEntity.status(400).body(APIResponse.error(message));
	        }

	        byte[] fileBytes = file.getBytes();
	        String fileName = file.getOriginalFilename();
	        
	        ValidationResult validationResult = emailNotificationValidationStrategy.validateObject(emailNotiReq,true);
			if (!validationResult.isValid()) {
				message = validationResult.getMessage();
				logger.error(message);
				auditService.logAudit(auditDTO, validationResult.getStatus().value(), message, authorizationHeader);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}


	        NotificationDTO notiDTO = emailNotificationService.sendEmailWithAttachment(emailNotiReq, fileBytes, fileName);

	        boolean isSent = notiDTO.isSent();
			message = isSent ? "Email is sent successfully." : "Email is not sent successfully.";
			HttpStatus status = isSent ? HttpStatus.OK : validationResult.getStatus();
			auditService.logAudit(auditDTO, status.value(), message, authorizationHeader);
			return isSent ? ResponseEntity.status(status).body(APIResponse.success(notiDTO, message))
					: ResponseEntity.status(status).body(APIResponse.error(message));

	    } catch (Exception e) {
	        logger.error("Failed to send email with attachment", e);
	        throw new EmailNotificationServiceException("Failed to send email", e);
	    }
	}


}
