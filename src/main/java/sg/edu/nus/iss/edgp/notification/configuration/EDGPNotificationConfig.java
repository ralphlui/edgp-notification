package sg.edu.nus.iss.edgp.notification.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EDGPNotificationConfig {

	@Value("${client.initialise.password.url}")
	private String clientInitialisePasswordURL;

	@Bean
	public String getClientInitialisePasswordURL() {
		return clientInitialisePasswordURL;
	}
}
