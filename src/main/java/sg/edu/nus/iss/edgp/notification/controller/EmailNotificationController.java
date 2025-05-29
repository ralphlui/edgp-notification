package sg.edu.nus.iss.edgp.notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.notification.dto.APIResponse;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.service.impl.EmailNotificationService;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class EmailNotificationController {

	private final EmailNotificationValidationStrategy emailNotificationValidationStrategy;
	private final EmailNotificationService emailNotificationService;
	private String genericErrorMessage = "An error occurred while processing your request. Please try again later.";

	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationController.class);

	@PostMapping(value = "/sendEmail", produces = "application/json")
	public <T> ResponseEntity<APIResponse<T>> sendChaingDefaultPassword(
			@RequestBody EmailNotificationRequest emailNotiReq) {

		logger.info("Calling change default password API ...");
		String message = "";

		try {
			ValidationResult validationResult = emailNotificationValidationStrategy.validateObject(emailNotiReq);
			if (!validationResult.isValid()) {
				message = validationResult.getMessage();
				logger.error(message);
				return ResponseEntity.status(validationResult.getStatus()).body(APIResponse.error(message));
			}

			boolean isSent = emailNotificationService.sendChaingDefaultPassword(emailNotiReq);
			message = isSent ? "Email is sent successfully." : "Email is not sent successfully.";
			HttpStatus status = isSent ? HttpStatus.OK : validationResult.getStatus();

			return isSent ? ResponseEntity.status(status).body(APIResponse.success(null, message))
					: ResponseEntity.status(status).body(APIResponse.error(message));

		} catch (Exception e) {
			message = e instanceof EmailNotificationServiceException ? e.getMessage() : genericErrorMessage;
			logger.error(message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}

}
