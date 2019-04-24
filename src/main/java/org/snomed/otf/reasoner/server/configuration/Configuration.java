package org.snomed.otf.reasoner.server.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@SpringBootApplication
@EnableJms
public abstract class Configuration {

	@Bean
	public SnomedReasonerService getSnomedReasonerService() {
		return new SnomedReasonerService();
	}

	@Bean // Serialize message content to json using TextMessage
	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	@Bean
	// The default queue prefetch size is 1,000. That prevents scaling this service.
	// Setting the queue prefetch size to 1 allows other instances in the cluster to take the next job on the queue.
	public ActiveMQConnectionFactoryPrefetchCustomizer queuePrefetchCustomizer(@Value("${spring.activemq.queuePrefetch:1}") int queuePrefetch) {
		return new ActiveMQConnectionFactoryPrefetchCustomizer(queuePrefetch);
	}

	@Bean
	public MessagingHelper messagingHelper() {
		return new MessagingHelper();
	}

}
