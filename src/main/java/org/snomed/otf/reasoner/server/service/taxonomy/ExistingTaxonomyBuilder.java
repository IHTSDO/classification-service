package org.snomed.otf.reasoner.server.service.taxonomy;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.ihtsdo.otf.snomedboot.ReleaseImporter;
import org.ihtsdo.otf.snomedboot.factory.LoadingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.InputStream;

public class ExistingTaxonomyBuilder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public ExistingTaxonomy build(InputStream snomedRf2SnapshotArchive) throws ReleaseImportException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ExistingTaxonomyLoader existingTaxonomyLoader = new ExistingTaxonomyLoader();
		new ReleaseImporter().loadSnapshotReleaseFiles(
				snomedRf2SnapshotArchive,
				new LoadingProfile().withStatedRelationships().withFullRelationshipObjects(),
				existingTaxonomyLoader);

		stopWatch.stop();
		logger.info("ExistingTaxonomy loaded in {} seconds", stopWatch.getTotalTimeSeconds());
		ExistingTaxonomy existingTaxonomy = existingTaxonomyLoader.getExistingTaxonomy();
		logger.info("{} concepts loaded", existingTaxonomy.getAllConceptIds().size());
		return existingTaxonomy;
	}
}
