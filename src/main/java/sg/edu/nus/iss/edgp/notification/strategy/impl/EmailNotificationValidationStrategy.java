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
	public ValidationResult validateObject(EmailNotificationRequest emailNotiReq) {
		ValidationResult validationResult = new ValidationResult();

		List<String> missingFields = new ArrayList<>();
		if (emailNotiReq.getUserName().isEmpty())
			missingFields.add("Username");
		if (emailNotiReq.getTemporaryPassword().isEmpty())
			missingFields.add("Temporary Password");
		if (emailNotiReq.getUserEmail().isEmpty())
			missingFields.add("User email");

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

