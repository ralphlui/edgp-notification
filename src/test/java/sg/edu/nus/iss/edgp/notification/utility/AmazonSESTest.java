package sg.edu.nus.iss.edgp.notification.utility;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AmazonSESTest {

	@Test
	void testSendEmail_successful() {
		// Arrange
		SesClient mockClient = mock(SesClient.class);
		String from = "sender@example.com";
		List<String> recipients = List.of("recipient@example.com");
		String subject = "Test Subject";
		String body = "Test Body";

		// Act
		boolean result = AmazonSES.sendEmail(mockClient, from, recipients, subject, body);

		// Assert
		assertTrue(result);
		verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
	}

	@Test
	void testSendEmail_failure_sesException() {
		// Arrange
		SesClient mockClient = mock(SesClient.class);
		String from = "sender@example.com";
		List<String> recipients = List.of("recipient@example.com");
		String subject = "Test Subject";
		String body = "Test Body";

		doThrow(SesException.class).when(mockClient).sendEmail(any(SendEmailRequest.class));

		// Act
		boolean result = AmazonSES.sendEmail(mockClient, from, recipients, subject, body);

		// Assert
		assertFalse(result);
		verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
	}
}
