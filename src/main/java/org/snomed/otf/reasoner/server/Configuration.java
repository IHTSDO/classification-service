package org.snomed.otf.reasoner.server;

import org.snomed.otf.owltoolkit.service.SnomedReasonerService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Configuration {

	@Bean
	public SnomedReasonerService getSnomedReasonerService() {
		return new SnomedReasonerService();
	}

}
