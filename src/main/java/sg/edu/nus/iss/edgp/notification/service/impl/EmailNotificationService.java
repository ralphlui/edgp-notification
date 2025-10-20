package sg.edu.nus.iss.edgp.notification.service.impl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import sg.edu.nus.iss.edgp.notification.configuration.AWSConfig;
import sg.edu.nus.iss.edgp.notification.configuration.EDGPNotificationConfig;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.service.IEmailNotificationService;
import sg.edu.nus.iss.edgp.notification.utility.AmazonSES;
import sg.edu.nus.iss.edgp.notification.utility.DTOMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

@Service
@RequiredArgsConstructor
public class EmailNotificationService implements IEmailNotificationService {

	private final AWSConfig awsConfig;
	private final SesClient sesClient;
	private final EDGPNotificationConfig edgpNotificationConfig;
	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

	@Override
	public NotificationDTO sendUserInvitation(EmailNotificationRequest emailNotiRequest) {

		try {
			 
			String from = awsConfig.getEmailFrom().trim();
			String invitationURL = edgpNotificationConfig.getClientInvitationUserURL() + "/" + emailNotiRequest.getToken();

			String subject = "Invitation to Join Our System";
			String body = "Welcome to our system.<br> Your account has been successfully created.<br>"
					+ " Username: " + emailNotiRequest.getUserEmail() + "<br>" 
					+ "For security reasons, you are required to setup your profile immediately upon first use.<br>"
					+ " Please visit this page here: " + invitationURL + "<br><br>"
					+ "This is an auto-generated email, please do not reply.";

			boolean isSent = AmazonSES.sendEmail(sesClient, from, Arrays.asList(emailNotiRequest.getUserEmail()), subject,
					body);
			return DTOMapper.toNotificationDTO(emailNotiRequest.getUserEmail(), isSent);
		} catch (Exception ex) {
			logger.error("Exception occurred while inviting a new user", ex);
			throw new EmailNotificationServiceException("An error occured while inviting a new user",
					ex);
		}

	}
	
	@Override
	public NotificationDTO sendEmailWithAttachment(EmailNotificationRequest req, byte[] attachment, String fileName) {
	    try {
	        SesClient client = awsConfig.sesClient();

	        boolean isSent = this.sendEmailWithAttachment(
	            client,
	            awsConfig.getEmailFrom().trim(),
	            List.of(req.getUserEmail()),
	            req.getSubject(),
	            req.getBody(),
	            attachment,
	            "text/csv",
	            fileName
	        );

	        return DTOMapper.toNotificationDTO(req.getUserEmail(), isSent);
	    } catch (Exception e) {
	        logger.info("Error sending email with attachment", e);
	        throw new EmailNotificationServiceException("Error sending email with attachment", e);
	    }
	}
	
	@Override
	public boolean sendEmailWithAttachment(
	        SesClient sesClient,
	        String from,
	        List<String> to,
	        String subject,
	        String htmlBody,
	        byte[] csvBytes,
	        String fileType,
	        String fileName
	) {
	    try {
	        
	        Session session = Session.getInstance(new Properties());
	        MimeMessage message = new MimeMessage(session);

	        message.setFrom(new InternetAddress(from));
	        message.setRecipients(Message.RecipientType.TO, to.stream().map(addr -> {
	            try {
	                return new InternetAddress(addr);
	            } catch (AddressException e) {
	                throw new RuntimeException("Invalid recipient address: " + addr, e);
	            }
	        }).toArray(InternetAddress[]::new));
	        message.setSubject(subject, StandardCharsets.UTF_8.name());

	        MimeBodyPart htmlPart = new MimeBodyPart();
	        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

	        MimeMultipart alt = new MimeMultipart("alternative");
	        alt.addBodyPart(htmlPart);
	        MimeBodyPart altWrapper = new MimeBodyPart();
	        altWrapper.setContent(alt);

	        MimeMultipart mixed = new MimeMultipart("mixed");
	        mixed.addBodyPart(altWrapper);

	        // Attachment
	        MimeBodyPart attachment = new MimeBodyPart();
	        DataSource dataSource = new ByteArrayDataSource(csvBytes, fileType);
	        attachment.setDataHandler(new DataHandler(dataSource));
	        attachment.setFileName(fileName);
	        mixed.addBodyPart(attachment);

	        message.setContent(mixed);
	        message.saveChanges();

	        byte[] rawBytes;
	        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
	            message.writeTo(outputStream);
	            rawBytes = outputStream.toByteArray();
	        }

	        RawMessage rawMessage = RawMessage.builder()
	                .data(SdkBytes.fromByteArray(rawBytes))
	                .build();

	        SendRawEmailRequest request = SendRawEmailRequest.builder()
	                .rawMessage(rawMessage)
	                .build();

	        SendRawEmailResponse response = sesClient.sendRawEmail(request);

	        boolean httpOk = response.sdkHttpResponse() != null && response.sdkHttpResponse().isSuccessful();
	        String messageId = response.messageId();

	        if (httpOk && messageId != null && !messageId.isBlank()) {
	            logger.info("SES send succeeded. MessageId=" + messageId);
	            return true;
	        } else {
	        	logger.error("SES send may have failed or is indeterminate. "
	                    + "HTTP ok: " + httpOk + ", MessageId: " + messageId);
	            return false;
	        }

	    } catch (SesException e) {
	        
	    	logger.info("SES error: " + e.awsErrorDetails().errorMessage());
	        return false;
	    } catch (Exception e) { 
	    	logger.info("Failed to send email with attachment: " + e.getMessage());
	        return false;
	    }
	}

}
