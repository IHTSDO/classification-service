package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

//	Uncomment for manual classification testing
//	@PostConstruct
	private void testClassification() {
		// Path to a SNOMED release on local disk
		String releaseDirectoryPath = "release/SnomedCT_InternationalRF2_Production_20170131";

		// Name of Reasoner factory to use on classpath
		String reasonerFactoryClassName = "org.semanticweb.elk.owlapi.ElkReasonerFactory";

		try {
			snomedReasonerService.classify(releaseDirectoryPath, reasonerFactoryClassName);
		} catch (ReleaseImportException | OWLOntologyCreationException e) {
			logger.error("Classification error", e);
		}
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

}
