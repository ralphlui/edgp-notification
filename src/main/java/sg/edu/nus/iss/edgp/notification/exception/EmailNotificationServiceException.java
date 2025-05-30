package sg.edu.nus.iss.edgp.notification.exception;

public class EmailNotificationServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailNotificationServiceException(String message) {
		super(message);
	}
	
	public EmailNotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
