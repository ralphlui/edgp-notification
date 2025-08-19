package sg.edu.nus.iss.edgp.notification.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.strategy.IAPIHelperValidationStrategy;

@Component
public class EmailNotificationValidationStrategy implements IAPIHelperValidationStrategy<EmailNotificationRequest> {

	@Override
	public ValidationResult validateObject(EmailNotificationRequest emailNotiReq, boolean isWithAtt) {
		ValidationResult validationResult = new ValidationResult();
		String userEmail = emailNotiReq.getUserEmail();
		String token = emailNotiReq.getToken();

		List<String> missingFields = new ArrayList<>();
		if (userEmail == null || userEmail.isEmpty())
			missingFields.add("User email");
		if (!isWithAtt) {
		if (token == null || token.isEmpty())
			missingFields.add("User's token");
		}

		if (isWithAtt) {
			if (emailNotiReq.getBody() == null || emailNotiReq.getBody().isEmpty()) {
				missingFields.add("Email body");
			}
			if (emailNotiReq.getSubject() == null || emailNotiReq.getSubject().isEmpty()) {
				missingFields.add("Email subject");
			}

		}

		if (!missingFields.isEmpty()) {
			validationResult.setMessage(String.join(" and ", missingFields) + " is required");
			validationResult.setStatus(HttpStatus.BAD_REQUEST);
			validationResult.setValid(false);
			return validationResult;
		}

		validationResult.setValid(true);
		return validationResult;
	}

}
