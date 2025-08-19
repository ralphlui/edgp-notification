package sg.edu.nus.iss.edgp.notification.utility;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
 
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*; 
import software.amazon.awssdk.services.ses.model.SesException; 

@Component
public class AmazonSES {

	private static final Logger logger = LoggerFactory.getLogger(AmazonSES.class);

	public static boolean sendEmail(SesClient client, String from, Collection<String> recipientsTo, String subject,
			String body) {
		boolean isSent = false;

		try {
			Destination destination = Destination.builder().toAddresses(recipientsTo).build();

			Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();

			Content bodyContent = Content.builder().data(body).charset("UTF-8").build();

			Body emailBody = Body.builder().html(bodyContent).build();

			Message message = Message.builder().subject(subjectContent).body(emailBody).build();

			SendEmailRequest request = SendEmailRequest.builder().destination(destination).message(message).source(from)
					.build();

			client.sendEmail(request);
			isSent = true;
			logger.info("Email sent successfully.");

		} catch (SesException e) {
			logger.error("sendEmail exception...", e);
			isSent = false;
		}

		return isSent;
	}

}
