package sg.edu.nus.iss.edgp.notification.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;

import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationValidationStrategyTest {

    private EmailNotificationValidationStrategy validationStrategy;

    @BeforeEach
    void setup() {
        validationStrategy = new EmailNotificationValidationStrategy();
    }

    // -------------------- isWithAtt = false (no attachment) --------------------

    @Test
    void valid_noAttachment_returnsValid() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setToken("AAAAAA");

        ValidationResult result = validationStrategy.validateObject(request, false);

        assertTrue(result.isValid());
        assertNull(result.getMessage());
        assertNull(result.getStatus());
    }

    @Test
    void missingUserEmail_noAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("");
        request.setToken("AAAAAA");

        ValidationResult result = validationStrategy.validateObject(request, false);

        assertFalse(result.isValid());
        assertEquals("User email is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }

    @Test
    void missingAllFields_noAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("");
        request.setToken("");

        ValidationResult result = validationStrategy.validateObject(request, false);

        assertFalse(result.isValid());
        assertEquals("User email and User's token is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }

    // -------------------- isWithAtt = true (with attachment) --------------------

    @Test
    void valid_withAttachment_tokenNotRequired_returnsValid() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setToken(null);                 // token not required when isWithAtt = true
        request.setSubject("Hello");
        request.setBody("Body text");

        ValidationResult result = validationStrategy.validateObject(request, true);

        assertTrue(result.isValid());
        assertNull(result.getMessage());
        assertNull(result.getStatus());
    }

    @Test
    void missingBody_withAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setSubject("Subject only");
        request.setBody("");                    // missing body

        ValidationResult result = validationStrategy.validateObject(request, true);

        assertFalse(result.isValid());
        assertEquals("Email body is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }

    @Test
    void missingSubject_withAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setSubject("");                 // missing subject
        request.setBody("Some body");

        ValidationResult result = validationStrategy.validateObject(request, true);

        assertFalse(result.isValid());
        assertEquals("Email subject is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }

    @Test
    void missingBodyAndSubject_withAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setSubject("");                 // missing
        request.setBody("");                    // missing

        ValidationResult result = validationStrategy.validateObject(request, true);

        assertFalse(result.isValid());
        assertEquals("Email body and Email subject is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }

    @Test
    void missingEmailBodySubject_withAttachment_returnsBadRequest() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("");               // missing email
        request.setSubject("");                 // missing subject
        request.setBody("");                    // missing body

        ValidationResult result = validationStrategy.validateObject(request, true);

        // Order follows the strategy: User email, Email body, Email subject
        assertFalse(result.isValid());
        assertEquals("User email and Email body and Email subject is required", result.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }
}
