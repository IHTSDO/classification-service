package org.snomed.otf.reasoner.server.configuration;

import org.ihtsdo.otf.dao.resources.ResourceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("classification.job.storage")
public class ClassificationJobResourceConfiguration extends ResourceConfiguration {
}
