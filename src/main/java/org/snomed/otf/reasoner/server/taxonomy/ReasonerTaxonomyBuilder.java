package org.snomed.otf.reasoner.server.taxonomy;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.ihtsdo.otf.snomedboot.ReleaseImporter;
import org.ihtsdo.otf.snomedboot.factory.LoadingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class ReasonerTaxonomyBuilder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public Taxonomy build(String releaseDirectoryPath) throws ReleaseImportException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TaxonomyLoader taxonomyLoader = new TaxonomyLoader();
		new ReleaseImporter().loadSnapshotReleaseFiles(
				releaseDirectoryPath,
				new LoadingProfile().withStatedRelationships().withFullRelationshipObjects(),
				taxonomyLoader);

		stopWatch.stop();
		logger.info("Taxonomy loaded in {} seconds", stopWatch.getTotalTimeSeconds());
		Taxonomy taxonomy = taxonomyLoader.getTaxonomy();
		logger.info("{} concepts loaded", taxonomy.getAllConceptIds().size());
		return taxonomy;
	}
}
