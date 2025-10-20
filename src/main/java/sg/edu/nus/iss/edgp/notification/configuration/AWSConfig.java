package sg.edu.nus.iss.edgp.notification.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AWSConfig {

	@Value("${spring.cloud.aws.region.static}")
	private String awsRegion;

	@Value("${spring.cloud.aws.credentials.access-key}")
	private String awsAccessKey;

	@Value("${spring.cloud.aws.credentials.secret-key}")
	private String awsSecretKey;

	@Value("${aws.ses.from}")
	private String emailFrom;

	@Value("${aws.sqs.queue.audit.url}")
	private String sqsURL;

	@Bean
	public String getEmailFrom() {
		return emailFrom;
	}

	@Bean
	public SesClient sesClient() {
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKey,awsSecretKey);
		return SesClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(Region.of(awsRegion)).build();
	}
	
	  @Bean
	    public SqsClient sqsClient() {
	        return SqsClient.builder()
	                .region(Region.AP_SOUTHEAST_1)
	                .build();
	    }

}
