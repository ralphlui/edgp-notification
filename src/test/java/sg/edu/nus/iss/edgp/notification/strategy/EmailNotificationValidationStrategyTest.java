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
		request.setUserName("testUser");
		request.setTemporaryPassword("tempPass123");
		request.setUserEmail("test@example.com");

		ValidationResult result = validationStrategy.validateObject(request);

		assertTrue(result.isValid());
		assertNull(result.getMessage());
		assertNull(result.getStatus());
	}

	@Test
	public void testMissingUserName() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserName("");
		request.setTemporaryPassword("tempPass123");
		request.setUserEmail("test@example.com");

		ValidationResult result = validationStrategy.validateObject(request);

		assertFalse(result.isValid());
		assertEquals("Username is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	public void testMissingAllFields() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserName("");
		request.setTemporaryPassword("");
		request.setUserEmail("");

		ValidationResult result = validationStrategy.validateObject(request);

		assertFalse(result.isValid());
		assertEquals("Username and Temporary Password and User email is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}

	@Test
	public void testMissingTemporaryPasswordAndEmail() {
		EmailNotificationRequest request = new EmailNotificationRequest();
		request.setUserName("testUser");
		request.setTemporaryPassword("");
		request.setUserEmail("");

		ValidationResult result = validationStrategy.validateObject(request);

		assertFalse(result.isValid());
		assertEquals("Temporary Password and User email is required", result.getMessage());
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
	}
}
