package sg.edu.nus.iss.edgp.notification.connector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class AuthAPICallTest {

	private AuthAPICall authAPICall;

	@Mock
	private HttpClient httpClient;

	@Mock
	private HttpResponse<String> httpResponse;

	@Mock
	private HttpClient httpClientMock;
	
	@Mock
	private HttpResponse<String> httpResponseMock;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		authAPICall = new AuthAPICall();

		try {
			java.lang.reflect.Field field = AuthAPICall.class.getDeclaredField("authURL");
			field.setAccessible(true);
			field.set(authAPICall, "http://fake-auth-url.com/");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testValidateActiveUser_ExceptionHandling() throws Exception {
		when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
				.thenThrow(new RuntimeException("Connection error"));

		String result = authAPICall.validateActiveUser("user123", "Bearer xyz");

		assertEquals("", result);
	}

	@Test
	void testHttpClientSendReturnsExpectedResponse() throws Exception {
		// Arrange
		String expectedResponseBody = "{\"status\":\"active\"}";

		// Simulate the response body
		when(httpResponseMock.body()).thenReturn(expectedResponseBody);

		when(httpClientMock.send(any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
				.thenReturn(httpResponseMock);

		// Create dummy request (you can customize this)
		HttpRequest request = HttpRequest.newBuilder().uri(new java.net.URI("http://example.com")).GET().build();

		// Act
		HttpResponse<String> response = httpClientMock.send(request, HttpResponse.BodyHandlers.ofString());
		String actualBody = response.body();

		// Assert
		assertEquals(expectedResponseBody, actualBody);
	}
}
