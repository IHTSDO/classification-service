package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.service.SnomedReasonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootApplication
public class Application {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Uncomment for manual classification testing
	@PostConstruct
	private void testClassification() {
		// Path to a SNOMED release on local disk
		String snomedRf2SnapshotArchivePath = "testing/SnomedCT_Release_INT.zip";
//		String snomedRf2SnapshotArchivePath = "release/SnomedCT_InternationalRF2_Production_20170131.zip";

		// Name of Reasoner factory to use on classpath
		String reasonerFactoryClassName = "org.semanticweb.elk.owlapi.ElkReasonerFactory";

		try {
			InputStream resourceAsStream = new FileInputStream(snomedRf2SnapshotArchivePath);
			File results = snomedReasonerService.classify(resourceAsStream, reasonerFactoryClassName);
			logger.info("Results file path {}", results.getAbsolutePath());
		} catch (ReleaseImportException | OWLOntologyCreationException e) {
			logger.error("Classification error", e);
		} catch (FileNotFoundException e) {
			logger.error("Could not open archive", e);
		}
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

}
