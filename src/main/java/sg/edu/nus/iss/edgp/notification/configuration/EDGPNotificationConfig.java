package sg.edu.nus.iss.edgp.notification.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EDGPNotificationConfig {

	@Value("${client.login.url}")
	private String clientLoginURL;

	@Bean
	public String getClientLoginURL() {
		return clientLoginURL;
	}
}
