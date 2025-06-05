package sg.edu.nus.iss.edgp.notification.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class EmailNotificationValidationStrategyTest {

	private EmailNotificationValidationStrategy validationStrategy;

	@BeforeEach
	public void setup() {
		validationStrategy = new EmailNotificationValidationStrategy();
	}

	@Test
	public void testValidEmailNotificationRequest() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserEmail("test@example.com");

		ValidationResult result = validationStrategy.validateObject(request);

		assertTrue(result.isValid());
		assertNull(result.getMessage());
		assertNull(result.getStatus());
	}

	@Test
	public void testMissingUserEmail() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserEmail("");

		ValidationResult result = validationStrategy.validateObject(request);

		assertFalse(result.isValid());
		assertEquals("User email is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	public void testMissingAllFields() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserEmail("");

		ValidationResult result = validationStrategy.validateObject(request);

		assertFalse(result.isValid());
		assertEquals("User email is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}
}
