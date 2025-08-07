package org.snomed.otf.reasoner.server.configuration;

import org.snomed.module.storage.ModuleStorageCoordinator;
import org.snomed.otf.reasoner.server.Application;
import org.snomed.otf.reasoner.server.service.ClassificationJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(classes = {Application.class})
@PropertySource("classpath:/application-test.properties")
public abstract class TestConfiguration {
	@Autowired
	protected ClassificationJobManager classificationJobManager;

	@MockBean
	protected ModuleStorageCoordinator moduleStorageCoordinator;
}
