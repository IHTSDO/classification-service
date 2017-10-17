package org.snomed.otf.reasoner.server;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.service.ReasonerServiceException;
import org.snomed.otf.reasoner.server.service.SnomedReasonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.regex;

@SpringBootApplication
@EnableSwagger2
public class Application {

	@Autowired
	private SnomedReasonerService snomedReasonerService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String DEFAULT_REASONER_FACTORY = "org.semanticweb.elk.owlapi.ElkReasonerFactory";

	public static void main(String[] args) throws OWLOntologyCreationException {
		SpringApplication.run(Application.class, args);
	}

	// Uncomment for manual classification testing
//	@PostConstruct
	private void testClassification() {
		// Path to a SNOMED release on local disk
		String snomedRf2SnapshotArchivePath = "store/releases/SnomedCT_InternationalRF2_Production_20170131T120000WithoutRT.zip";
		String snomedRf2DeltaArchivePath = "release/SnomedCT-RF2-20170731.zip";
//		String snomedRf2DeltaArchivePath = "testing/SnomedCT_Release_INT.zip";
//		String snomedRf2SnapshotArchivePath = "release/SnomedCT_InternationalRF2_Production_20170131.zip";
		try {
			InputStream snomedRf2SnapshotArchive = new FileInputStream(snomedRf2SnapshotArchivePath);
			InputStream snomedRf2DeltaArchive = new FileInputStream(snomedRf2DeltaArchivePath);
			File results = snomedReasonerService.classify(snomedRf2SnapshotArchive, snomedRf2DeltaArchive, DEFAULT_REASONER_FACTORY);
			logger.info("Results file path {}", results.getAbsolutePath());
		} catch (ReleaseImportException | OWLOntologyCreationException e) {
			logger.error("Classification error", e);
		} catch (FileNotFoundException e) {
			logger.error("Could not open archive", e);
		} catch (ReasonerServiceException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(not(regex("/error")))
				.build();
	}

}
