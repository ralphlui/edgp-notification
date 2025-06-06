package sg.edu.nus.iss.edgp.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test; 
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.edgp.notification.dto.AuditDTO;
import sg.edu.nus.iss.edgp.notification.dto.EmailNotificationRequest;
import sg.edu.nus.iss.edgp.notification.dto.NotificationDTO;
import sg.edu.nus.iss.edgp.notification.dto.ValidationResult;
import sg.edu.nus.iss.edgp.notification.service.impl.AuditService;
import sg.edu.nus.iss.edgp.notification.service.impl.EmailNotificationService;
import sg.edu.nus.iss.edgp.notification.service.impl.JwtService;
import sg.edu.nus.iss.edgp.notification.strategy.impl.EmailNotificationValidationStrategy;

@WebMvcTest(EmailNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmailNotificationControllerTest {

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
		validRequest = new EmailNotificationRequest(); // populate fields if needed
		notificationDTO = new NotificationDTO();
		notificationDTO.setSent(true);

		auditDTO = new AuditDTO(); // populate fields if needed
	}

	@Test
	void testSendUserInvitation_Success() throws Exception {
		// Mock behavior
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);
		validResult.setMessage("");
		validResult.setStatus(HttpStatus.OK);

		when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
		when(emailNotificationValidationStrategy.validateObject(any())).thenReturn(validResult);
		when(emailNotificationService.sendUserInvitation(any())).thenReturn(notificationDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications/invitation-user")
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer test-token")
				.content(objectMapper.writeValueAsString(validResult)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Email is sent successfully."))
				.andDo(print());
	}

	@Test
	void testSendUserInvitationt_ValidationError() throws Exception {
		ValidationResult invalidResult = new ValidationResult();
		invalidResult.setValid(false);
		invalidResult.setMessage("Invalid request");
		invalidResult.setStatus(HttpStatus.BAD_REQUEST);

		when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
		when(emailNotificationValidationStrategy.validateObject(any())).thenReturn(invalidResult);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications/invitation-user")
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer test-token")
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Invalid request"));
	}

	@Test
	void testSendUserInvitation_EmailNotSent() throws Exception {
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);
		validResult.setMessage("");
		validResult.setStatus(HttpStatus.OK);
		notificationDTO.setSent(false);

		when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
		when(emailNotificationValidationStrategy.validateObject(any())).thenReturn(validResult);
		when(emailNotificationService.sendUserInvitation(any())).thenReturn(notificationDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications/invitation-user")
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer test-token")
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(MockMvcResultMatchers.status().isOk()) // Adjust if a different status is returned for
																	// failure
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Email is not sent successfully."));
	}

	@Test
	void testSendUserInvitation_InternalServerError() throws Exception {
		ValidationResult validResult = new ValidationResult();
		validResult.setValid(true);
		validResult.setMessage("");
		validResult.setStatus(HttpStatus.OK);

		when(auditService.createAuditDTO(anyString(), anyString(), anyString())).thenReturn(auditDTO);
		when(emailNotificationValidationStrategy.validateObject(any())).thenReturn(validResult);
		when(emailNotificationService.sendUserInvitation(any()))
				.thenThrow(new RuntimeException("Unexpected error"));

		mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications/invitation-user")
				.contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer test-token")
				.content(objectMapper.writeValueAsString(validRequest)))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("An error occurred while processing your request. Please try again later."));
	}
}
