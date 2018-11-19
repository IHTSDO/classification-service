package org.snomed.otf.reasoner.server.configuration;

import org.ihtsdo.otf.jms.MessagingHelper;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@SpringBootApplication
@EnableJms
public class Configuration {

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
	public MessagingHelper messagingHelper() {
		return new MessagingHelper();
	}

}
