package sg.edu.nus.iss.edgp.notification.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationServiceExceptionTest {

	@Test
	void testConstructorWithMessage() {
		String message = "Email service error";
		EmailNotificationServiceException exception = new EmailNotificationServiceException(message);

		assertEquals(message, exception.getMessage());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String message = "Email service failure";
		Throwable cause = new RuntimeException("SMTP server not reachable");
		EmailNotificationServiceException exception = new EmailNotificationServiceException(message, cause);

		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
