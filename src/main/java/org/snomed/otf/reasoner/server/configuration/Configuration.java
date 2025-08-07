package org.snomed.otf.reasoner.server.configuration;

import org.ihtsdo.otf.jms.MessagingHelper;
import org.ihtsdo.otf.resourcemanager.ResourceManager;
import org.snomed.module.storage.ModuleStorageCoordinator;
import org.snomed.module.storage.RF2Service;
import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	@Bean
	public ExecutorService cachedTaskExecutors() {
		return Executors.newCachedThreadPool();
	}

	@Bean
	public ModuleStorageCoordinator moduleStorageCoordinator(@Value("${environment}") String environment, @Autowired @Qualifier("newReleaseResourceManager") ResourceManager resourceManager) {
		if (environment == null) {
			return null;
		}

		return switch (environment.split("-")[0]) {
			case "prod" -> ModuleStorageCoordinator.initProd(resourceManager);
			case "uat" -> ModuleStorageCoordinator.initUat(resourceManager);
			default -> ModuleStorageCoordinator.initDev(resourceManager);
		};
	}

	@Bean
	@Qualifier("newReleaseResourceManager")
	public ResourceManager newResourceManager(@Autowired ModuleStorageResourceConfig resourceConfiguration, @Autowired ResourceLoader cloudResourceLoader) {
		return new ResourceManager(resourceConfiguration, cloudResourceLoader);
	}

	@Bean
	@Qualifier("legacyReleaseResourceManager")
	public ResourceManager legacyResourceManager(@Autowired SnomedReleaseResourceConfiguration snomedReleaseResourceConfiguration, @Autowired ResourceLoader cloudResourceLoader) {
		return new ResourceManager(snomedReleaseResourceConfiguration, cloudResourceLoader);
	}

	@Bean
	public RF2Service rf2Service() {
		return new RF2Service();
	}
}
