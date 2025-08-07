package org.snomed.otf.reasoner.server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationProperties {
	@Value("${legacy-dependency-management:true}")
	private String legacyDependencyManagement;

	public boolean isLegacyDependencyManagement() {
		if (legacyDependencyManagement == null) {
			return true;
		}

		return "true".equalsIgnoreCase(legacyDependencyManagement);
	}
}
