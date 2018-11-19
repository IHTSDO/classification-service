package org.snomed.otf.reasoner.server.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ihtsdo.otf.jms.MessagingHelper;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;

@Configuration
public class TestConfiguration extends org.snomed.otf.reasoner.server.configuration.Configuration {

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

}
