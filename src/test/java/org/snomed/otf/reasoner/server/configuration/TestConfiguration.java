package org.snomed.otf.reasoner.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

@Configuration
public class TestConfiguration extends org.snomed.otf.reasoner.server.configuration.Configuration {

	@Autowired
	private ConnectionFactory connectionFactory;

	@Bean
	public ClassificationJobManager classificationJobManager(
			@Autowired SnomedReleaseResourceConfiguration snomedReleaseResourceConfiguration,
			@Autowired ClassificationJobResourceConfiguration classificationJobResourceConfiguration,
			@Autowired MessagingHelper messagingHelper,
			@Autowired ObjectMapper objectMapper) {

		return new ClassificationJobManager(
				snomedReleaseResourceConfiguration,
				classificationJobResourceConfiguration,
				new FileSystemResourceLoader(),
				getSnomedReasonerService(),
				messagingHelper,
				objectMapper
		);
	}

	@Bean(name = "topicJmsListenerContainerFactory")
	public DefaultJmsListenerContainerFactory getTopicFactory() {
		DefaultJmsListenerContainerFactory factory = new  DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setSessionTransacted(true);
		factory.setPubSubDomain(true);
		return factory;
	}

}
