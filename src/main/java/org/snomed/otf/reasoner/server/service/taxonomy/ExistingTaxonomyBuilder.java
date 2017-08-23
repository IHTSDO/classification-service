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
	private static final LoadingProfile LOADING_PROFILE = new LoadingProfile()
			.withStatedRelationships()
			.withFullRelationshipObjects()
			.withoutFullDescriptionObjects();

	public ExistingTaxonomy build(InputStream snomedRf2SnapshotArchive, InputStream currentReleaseRf2DeltaArchive) throws ReleaseImportException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		ExistingTaxonomyLoader existingTaxonomyLoader = new ExistingTaxonomyLoader();

		ReleaseImporter releaseImporter = new ReleaseImporter();
		releaseImporter.loadSnapshotReleaseFiles(
				snomedRf2SnapshotArchive,
				LOADING_PROFILE,
				existingTaxonomyLoader);
		logger.info("Loaded previous release snapshot");

		existingTaxonomyLoader.startLoadingDelta();

		releaseImporter.loadDeltaReleaseFiles(
				currentReleaseRf2DeltaArchive,
				LOADING_PROFILE,
				existingTaxonomyLoader);
		logger.info("Loaded current release delta");

		stopWatch.stop();
		logger.info("ExistingTaxonomy loaded in {} seconds", stopWatch.getTotalTimeSeconds());

		ExistingTaxonomy existingTaxonomy = existingTaxonomyLoader.getExistingTaxonomy();
		logger.info("{} concepts loaded", existingTaxonomy.getAllConceptIds().size());
		return existingTaxonomy;
	}
}
