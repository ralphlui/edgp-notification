package sg.edu.nus.iss.edgp.notification.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtConfigTest {

    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
    }

    @Test
    void getJWTPubliceKey_trimsWhitespace() {
         
        String base64 = "QUJDREVGR0g=";
        String withWhitespace = "  \n\t" + base64 + " \n\t  ";

        ReflectionTestUtils.setField(jwtConfig, "jwtPublicKey", withWhitespace);

         
        String actual = jwtConfig.getJWTPubliceKey();

        
        assertEquals(base64, actual, "Whitespace should be removed from jwt.public.key");
    }

    @Test
    void loadPublicKey_success_returnsSameKey() throws Exception {
       
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey originalPub = (RSAPublicKey) kp.getPublic();

        byte[] x509Encoded = originalPub.getEncoded(); // X.509 encoding
        String base64 = Base64.getEncoder().encodeToString(x509Encoded);

        ReflectionTestUtils.setField(jwtConfig, "jwtPublicKey", base64);

       
        RSAPublicKey loaded = jwtConfig.loadPublicKey();

        
        assertEquals(originalPub.getModulus(), loaded.getModulus(), "Modulus should match");
        assertEquals(originalPub.getPublicExponent(), loaded.getPublicExponent(), "Exponent should match");

        
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey reconstructed = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(x509Encoded));
        assertEquals(reconstructed, loaded, "Loaded key should equal the reconstructed key");
    }

    @Test
    void loadPublicKey_invalidBase64_throwsException() {
        
        ReflectionTestUtils.setField(jwtConfig, "jwtPublicKey", "not-a-valid-base64-key$$$");

         
        assertThrows(Exception.class, () -> jwtConfig.loadPublicKey(),
                "Invalid base64 should cause loadPublicKey to throw");
    }

    @Test
    void loadPublicKey_validBase64ButNotKey_throwsException() {
        
        String base64 = Base64.getEncoder().encodeToString("just-bytes-not-a-key".getBytes());
        ReflectionTestUtils.setField(jwtConfig, "jwtPublicKey", base64);

        assertThrows(Exception.class, () -> jwtConfig.loadPublicKey(),
                "Non-key bytes should cause loadPublicKey to throw");
    }
}
