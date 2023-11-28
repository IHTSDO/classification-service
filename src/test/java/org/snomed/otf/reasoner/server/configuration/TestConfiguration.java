package org.snomed.otf.reasoner.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

@Configuration
public class TestConfiguration extends org.snomed.otf.reasoner.server.configuration.Configuration {


	private final static Logger LOGGER = LoggerFactory.getLogger(TestConfiguration.class);
	private static final String ACTIVEMQ_IMAGE = "symptoma/activemq:5.18.3";
	private static final int ACTIVEMQ_PORT = 61616;

	@SuppressWarnings("rawtypes")
	@Container
	private static final GenericContainer activeMqContainer = new GenericContainer(ACTIVEMQ_IMAGE).withExposedPorts(ACTIVEMQ_PORT);
	static {
		activeMqContainer.start();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		String brokerUrlFormat = "tcp://%s:%d";
		String brokerUrl = String.format(brokerUrlFormat, activeMqContainer.getHost(), activeMqContainer.getFirstMappedPort());
		LOGGER.info("ActiveMQ test broker URL: {}", brokerUrl);
		return new ActiveMQConnectionFactory(brokerUrl);
	}
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
		factory.setConnectionFactory(connectionFactory());
		factory.setSessionTransacted(true);
		factory.setPubSubDomain(true);
		return factory;
	}
}
