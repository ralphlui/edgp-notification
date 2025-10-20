package sg.edu.nus.iss.edgp.notification.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.edgp.notification.dto.AuditDTO;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.exception.EmailNotificationServiceException;
import sg.edu.nus.iss.edgp.notification.service.impl.AuditService;
import sg.edu.nus.iss.edgp.notification.service.impl.EmailNotificationService;
import sg.edu.nus.iss.edgp.notification.service.impl.JwtService;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;

@WebMvcTest(EmailNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailNotificationService emailNotificationService;

    @MockitoBean
    private EmailNotificationValidationStrategy emailNotificationValidationStrategy;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EmailNotificationRequest validRequest;
    private NotificationDTO notificationDTO;
    private AuditDTO auditDTO;

    @BeforeEach
    void setUp() {
        validRequest = new EmailNotificationRequest();
        notificationDTO = new NotificationDTO();
        notificationDTO.setSent(true);

        auditDTO = new AuditDTO();
    }

    // -------- /invitation-user --------

    @Test
    void sendUserInvitation_success_returns200WithSuccessBody() throws Exception {
        ValidationResult validResult = new ValidationResult();
        validResult.setValid(true);
        validResult.setMessage("");
        validResult.setStatus(HttpStatus.OK);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(false)))
                .thenReturn(validResult);
        when(emailNotificationService.sendUserInvitation(any(EmailNotificationRequest.class)))
                .thenReturn(notificationDTO);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/notifications/invitation-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        // IMPORTANT: send the actual request object, not the ValidationResult
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email is sent successfully."));
    }

    @Test
    void sendUserInvitation_validationError_returns400() throws Exception {
        ValidationResult invalid = new ValidationResult();
        invalid.setValid(false);
        invalid.setMessage("Invalid request");
        invalid.setStatus(HttpStatus.BAD_REQUEST);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(false)))
                .thenReturn(invalid);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/notifications/invitation-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    @Test
    void sendUserInvitation_notSent_returns200WithFailureBody() throws Exception {
        ValidationResult valid = new ValidationResult();
        valid.setValid(true);
        valid.setMessage("");
        valid.setStatus(HttpStatus.OK);

        notificationDTO.setSent(false);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(false)))
                .thenReturn(valid);
        when(emailNotificationService.sendUserInvitation(any(EmailNotificationRequest.class)))
                .thenReturn(notificationDTO);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/notifications/invitation-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(validRequest)))
                // Controller uses HttpStatus.OK even when not sent
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is not sent successfully."));
    }

    @Test
    void sendUserInvitation_unexpectedException_returns500WithGenericMessage() throws Exception {
        ValidationResult valid = new ValidationResult();
        valid.setValid(true);
        valid.setMessage("");
        valid.setStatus(HttpStatus.OK);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(false)))
                .thenReturn(valid);
        when(emailNotificationService.sendUserInvitation(any(EmailNotificationRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/notifications/invitation-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "An error occurred while processing your request. Please try again later."));
    }

    // -------- /send-email-with-attachment --------

    @Test
    void sendAttachmentEmail_success_returns200WithSuccessBody() throws Exception {
        ValidationResult valid = new ValidationResult();
        valid.setValid(true);
        valid.setMessage("");
        valid.setStatus(HttpStatus.OK);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(true)))
                .thenReturn(valid);

        NotificationDTO dto = new NotificationDTO();
        dto.setSent(true);
        when(emailNotificationService.sendEmailWithAttachment(any(EmailNotificationRequest.class), any(byte[].class), anyString()))
                .thenReturn(dto);

        // EmailRequest JSON part
        MockMultipartFile emailPart = new MockMultipartFile(
                "EmailRequest",
                "EmailRequest",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(validRequest));

        // File part
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "recipients.csv",
                "text/csv",
                "email\nuser@example.com".getBytes());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/notifications/send-email-with-attachment")
                        .file(emailPart)
                        .file(filePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email is sent successfully."));
    }

    @Test
    void sendAttachmentEmail_missingFile_returns400AndErrorBody() throws Exception {
        when(auditService.createAuditDTO(anyString(), anyString(), anyString()))
                .thenReturn(auditDTO);

        MockMultipartFile emailPart = new MockMultipartFile(
                "EmailRequest",
                "EmailRequest",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(validRequest));

         
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "recipients.csv",
                "text/csv",
                new byte[0]
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/notifications/send-email-with-attachment")
                        .file(emailPart)
                        .file(emptyFile)
                        .header("Authorization", "Bearer test-token")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("CSV attachment is required to send the email."));
    }


    @Test
    void sendAttachmentEmail_validationError_returnsConfiguredStatus() throws Exception {
        ValidationResult invalid = new ValidationResult();
        invalid.setValid(false);
        invalid.setMessage("Invalid EmailRequest");
        invalid.setStatus(HttpStatus.BAD_REQUEST);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(true)))
                .thenReturn(invalid);

        MockMultipartFile emailPart = new MockMultipartFile(
                "EmailRequest",
                "EmailRequest",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(validRequest));
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "recipients.csv",
                "text/csv",
                "email\nuser@example.com".getBytes());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/notifications/send-email-with-attachment")
                        .file(emailPart)
                        .file(filePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid EmailRequest"));
    }

    @Test
    void sendAttachmentEmail_serviceThrows_wrapsAndPropagatesEmailNotificationServiceException() throws Exception {
        ValidationResult valid = new ValidationResult();
        valid.setValid(true);
        valid.setMessage("");
        valid.setStatus(HttpStatus.OK);

        when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
        when(emailNotificationValidationStrategy.validateObject(any(EmailNotificationRequest.class), eq(true)))
                .thenReturn(valid);

        when(emailNotificationService.sendEmailWithAttachment(any(EmailNotificationRequest.class), any(byte[].class), anyString()))
                .thenThrow(new RuntimeException("downstream boom"));

        MockMultipartFile emailPart = new MockMultipartFile(
                "EmailRequest",
                "EmailRequest",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(validRequest));
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "recipients.csv",
                "text/csv",
                "email\nuser@example.com".getBytes());

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/notifications/send-email-with-attachment")
                        .file(emailPart)
                        .file(filePart)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(result -> {
                    // Controller rethrows EmailNotificationServiceException -> framework maps to 500
                    Throwable ex = result.getResolvedException();
                    if (!(ex instanceof EmailNotificationServiceException)) {
                        throw new AssertionError("Expected EmailNotificationServiceException but was: " + ex);
                    }
                });
    }
}
