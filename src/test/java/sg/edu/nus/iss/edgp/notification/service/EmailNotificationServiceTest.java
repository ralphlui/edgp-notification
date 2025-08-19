package sg.edu.nus.iss.edgp.notification.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension; 

import sg.edu.nus.iss.edgp.notification.configuration.AWSConfig;
import sg.edu.nus.iss.edgp.notification.configuration.EDGPNotificationConfig;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.service.impl.EmailNotificationService;
import sg.edu.nus.iss.edgp.notification.utility.AmazonSES;
import sg.edu.nus.iss.edgp.notification.utility.DTOMapper;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;

@ExtendWith(MockitoExtension.class)
public class EmailNotificationServiceTest {

    @Mock
    private AWSConfig awsConfig;

    @Mock
    private EDGPNotificationConfig edgpNotificationConfig;

    @Mock
    private SesClient sesClient;

    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationService(awsConfig,sesClient, edgpNotificationConfig);
    }

    @Test
    void testSendUserInvitation_success() {
        // Arrange
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setToken("TOKEN123");

        when(awsConfig.getEmailFrom()).thenReturn("noreply@example.com");
        when(edgpNotificationConfig.getClientInvitationUserURL())
                .thenReturn("http://example.com/invitation");

        NotificationDTO expectedDto = new NotificationDTO();
        expectedDto.setUserEmail("test@example.com");
        expectedDto.setSent(true);

        try (MockedStatic<AmazonSES> amazonSES = Mockito.mockStatic(AmazonSES.class);
             MockedStatic<DTOMapper> dtoMapper = Mockito.mockStatic(DTOMapper.class)) {

            amazonSES.when(() -> AmazonSES.sendEmail(
                    eq(sesClient),
                    eq("noreply@example.com"),
                    eq(Arrays.asList("test@example.com")),
                    eq("Invitation to Join Our System"),
                    argThat(body -> body.contains("http://example.com/invitation/TOKEN123"))
            )).thenReturn(true);

            dtoMapper.when(() -> DTOMapper.toNotificationDTO("test@example.com", true))
                     .thenReturn(expectedDto);
  
            NotificationDTO result = emailNotificationService.sendUserInvitation(request);
 
            assertNotNull(result);
            assertTrue(result.isSent());
            assertEquals("test@example.com", result.getUserEmail());

            amazonSES.verify(() -> AmazonSES.sendEmail(
                    any(SesClient.class), anyString(), anyList(), anyString(), anyString()
            ));
            dtoMapper.verify(() -> DTOMapper.toNotificationDTO("test@example.com", true));
        }
    }

    @Test
    void testSendUserInvitation_failure() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
        request.setToken("TOKEN123");

        when(awsConfig.getEmailFrom()).thenReturn("no-reply@example.com");
        when(edgpNotificationConfig.getClientInvitationUserURL())
                .thenReturn("https://app.example.com/invite");

        try (MockedStatic<AmazonSES> amazonSES = Mockito.mockStatic(AmazonSES.class)) {
            amazonSES.when(() -> AmazonSES.sendEmail(
                    any(SesClient.class),
                    eq("no-reply@example.com"),
                    eq(List.of("test@example.com")),
                    eq("Invitation to Join Our System"),
                    anyString()
            )).thenThrow(new RuntimeException("SES client error"));

            EmailNotificationServiceException thrown = assertThrows(
                    EmailNotificationServiceException.class,
                    () -> emailNotificationService.sendUserInvitation(request)
            );
            assertTrue(thrown.getMessage().toLowerCase().contains("inviting"));
        }
    }
    
    @Test
    void testSendEmailWithAttachment_requestVariant_success() {
        
        EmailNotificationRequest req = new EmailNotificationRequest();
        req.setUserEmail("test@example.com");
        req.setSubject("Monthly Report");
        req.setBody("<b>Attached</b>");
 
        when(awsConfig.getEmailFrom()).thenReturn("no-reply@example.com");
        when(awsConfig.sesClient()).thenReturn(sesClient);
 
        SendRawEmailResponse sesResponse = mock(SendRawEmailResponse.class);
        SdkHttpResponse httpResp = mock(SdkHttpResponse.class);
        when(httpResp.isSuccessful()).thenReturn(true);
        when(sesResponse.sdkHttpResponse()).thenReturn(httpResp);
        when(sesResponse.messageId()).thenReturn("msg-123");
        when(sesClient.sendRawEmail(any(SendRawEmailRequest.class))).thenReturn(sesResponse);
 
        NotificationDTO expectedDto = mock(NotificationDTO.class);
        try (MockedStatic<DTOMapper> mapper = Mockito.mockStatic(DTOMapper.class)) {
            mapper.when(() -> DTOMapper.toNotificationDTO("test@example.com", true))
                  .thenReturn(expectedDto);
 
            NotificationDTO actual = emailNotificationService
                    .sendEmailWithAttachment(req, "a,b,c\n1,2,3\n".getBytes(StandardCharsets.UTF_8), "report.csv");
 
            assertSame(expectedDto, actual);
            verify(awsConfig, times(1)).sesClient();
            verify(sesClient, times(1)).sendRawEmail(any(SendRawEmailRequest.class));
            mapper.verify(() -> DTOMapper.toNotificationDTO("test@example.com", true));
        }
    }


    
    @Test
    void testSendEmailWithAttachment_requestVariant_failure_onSesClientFactory() {
        EmailNotificationRequest req = new EmailNotificationRequest();
        req.setUserEmail("test@example.com");
        req.setSubject("S");
        req.setBody("B");

        when(awsConfig.sesClient()).thenThrow(new RuntimeException("SES client error"));

        EmailNotificationServiceException ex = assertThrows(
                EmailNotificationServiceException.class,
                () -> emailNotificationService.sendEmailWithAttachment(req, "x".getBytes(), "f.csv")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("attachment"));
    }


}

