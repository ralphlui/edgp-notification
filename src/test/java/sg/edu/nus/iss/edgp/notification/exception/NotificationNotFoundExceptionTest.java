package sg.edu.nus.iss.edgp.notification.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationNotFoundExceptionTest {

	@Test
	void testConstructorWithMessage() {
		String errorMessage = "Notification not found";
		NotificationNotFoundException exception = new NotificationNotFoundException(errorMessage);

		assertEquals(errorMessage, exception.getMessage());
	}

	@Test
	void testConstructorWithMessageAndCause() {
		String errorMessage = "Notification not found";
		Throwable cause = new RuntimeException("Underlying cause");
		NotificationNotFoundException exception = new NotificationNotFoundException(errorMessage, cause);

		assertEquals(errorMessage, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
