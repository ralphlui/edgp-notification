package sg.edu.nus.iss.edgp.notification.authentication;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import sg.edu.nus.iss.edgp.notification.dto.AuditDTO;
import sg.edu.nus.iss.edgp.notification.service.impl.AuditService;
import sg.edu.nus.iss.edgp.notification.service.impl.JwtService;

class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter(jwtService, auditService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void validToken_setsAuthentication_andContinuesChain() throws Exception {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/test");
        request.setMethod("GET");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/test", "GET")).thenReturn(auditDTO);

        when(jwtService.getUserDetail("Bearer " + token, token)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class,
                SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        // No error response written
        assertEquals(200, response.getStatus()); // default since filterChain invoked
    }

    @Test
    void missingAuthorizationHeader_returns401_andAudits() throws ServletException, IOException {
        request.setRequestURI("/test");
        request.setMethod("GET");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/test", "GET")).thenReturn(auditDTO);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Authorization header is missing or invalid."),
                isNull());
        verifyNoInteractions(jwtService);
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void invalidAuthorizationHeaderPrefix_returns401_andAudits() throws Exception {
        request.addHeader("Authorization", "Token abc"); // not "Bearer "
        request.setRequestURI("/test");
        request.setMethod("GET");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/test", "GET")).thenReturn(auditDTO);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Authorization header is missing or invalid."),
                eq("Token abc"));
        verifyNoInteractions(jwtService);
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void invalidToken_validateReturnsFalse_returns401_andAudits() throws Exception {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/test");
        request.setMethod("POST");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/test", "POST")).thenReturn(auditDTO);

        when(jwtService.getUserDetail("Bearer " + token, token)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Invalid or expired JWT token"),
                eq("Bearer " + token));
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void expiredToken_throwsExpiredJwtException_returns401_andAudits() throws Exception {
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/secure");
        request.setMethod("GET");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/secure", "GET")).thenReturn(auditDTO);

        // getUserDetail throws ExpiredJwtException
        when(jwtService.getUserDetail("Bearer " + token, token))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("JWT token is expired"),
                eq("Bearer " + token));
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void malformedToken_throwsMalformedJwtException_returns401_andAudits() throws Exception {
        String token = "malformed.token";
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/secure");
        request.setMethod("GET");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/secure", "GET")).thenReturn(auditDTO);

        when(jwtService.getUserDetail("Bearer " + token, token))
                .thenThrow(new MalformedJwtException("bad"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Invalid JWT token"),
                eq("Bearer " + token));
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void genericException_returns401_withMessage_andAudits() throws Exception {
        String token = "any.token";
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/secure");
        request.setMethod("PUT");

        AuditDTO auditDTO = new AuditDTO();
        when(auditService.createAuditDTO("", "/secure", "PUT")).thenReturn(auditDTO);

        when(jwtService.getUserDetail("Bearer " + token, token))
                .thenThrow(new Exception("Something went wrong"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(auditService).logAudit(eq(auditDTO),
                eq(HttpServletResponse.SC_UNAUTHORIZED),
                eq("Something went wrong"),
                eq("Bearer " + token));
        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}
