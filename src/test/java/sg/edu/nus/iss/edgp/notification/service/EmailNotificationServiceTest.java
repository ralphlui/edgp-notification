package sg.edu.nus.iss.edgp.notification.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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
import software.amazon.awssdk.services.ses.SesClient;

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
        emailNotificationService = new EmailNotificationService(awsConfig, edgpNotificationConfig);
    }

    @Test
    void testSendInitialPasswordSetRequest_success() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");

        NotificationDTO expectedDto = new NotificationDTO();
        expectedDto.setUserEmail(request.getUserEmail());
        expectedDto.setSent(true);

        Mockito.when(awsConfig.sesClient()).thenReturn(sesClient);
        Mockito.when(awsConfig.getEmailFrom()).thenReturn("noreply@example.com");
        Mockito.when(edgpNotificationConfig.getClientInvitationUserURL()).thenReturn("http://example.com/login");

        try (MockedStatic<AmazonSES> amazonSES = Mockito.mockStatic(AmazonSES.class);
             MockedStatic<DTOMapper> dtoMapper = Mockito.mockStatic(DTOMapper.class)) {

            amazonSES.when(() -> AmazonSES.sendEmail(Mockito.eq(sesClient),
                    Mockito.eq("noreply@example.com"),
                    Mockito.eq(Arrays.asList("test@example.com")),
                    Mockito.anyString(),
                    Mockito.anyString())).thenReturn(true);

            dtoMapper.when(() -> DTOMapper.toNotificationDTO("test@example.com", true)).thenReturn(expectedDto);

            NotificationDTO result = emailNotificationService.sendInitialPasswordSetRequest(request);

            assertNotNull(result);
            assertTrue(result.isSent());
            assertEquals("test@example.com", result.getUserEmail());
        }
    }

    @Test
    void testSendInitialPasswordSetRequest_failure() {
        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setUserEmail("test@example.com");
      
        Mockito.when(awsConfig.sesClient()).thenThrow(new RuntimeException("SES client error"));

        EmailNotificationServiceException thrown = assertThrows(
                EmailNotificationServiceException.class,
                () -> emailNotificationService.sendInitialPasswordSetRequest(request)
        );

        assertTrue(thrown.getMessage().contains("An error occured"));
    }
}

