package org.snomed.otf.reasoner.server.service.taxonomy;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.ihtsdo.otf.snomedboot.ReleaseImporter;
import org.ihtsdo.otf.snomedboot.factory.LoadingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.InputStream;

import static org.snomed.otf.reasoner.server.service.constants.Concepts.MRCM_ATTRIBUTE_DOMAIN_INTERNATIONAL_REFERENCE_SET;
import static org.snomed.otf.reasoner.server.service.constants.Concepts.OWL_AXIOM_REFERENCE_SET;

public class SnomedTaxonomyBuilder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final LoadingProfile SNAPSHOT_LOADING_PROFILE = new LoadingProfile()
			.withStatedRelationships()
			.withFullRelationshipObjects()
			.withRefset(OWL_AXIOM_REFERENCE_SET)
			.withRefset(MRCM_ATTRIBUTE_DOMAIN_INTERNATIONAL_REFERENCE_SET)
			.withFullRefsetMemberObjects()
			.withoutDescriptions();

	private static final LoadingProfile DELTA_LOADING_PROFILE = SNAPSHOT_LOADING_PROFILE
			.withInactiveConcepts() // The delta needs to be able to inactivate previously active components
			.withInactiveRelationships()
			.withInactiveRefsetMembers();

	public SnomedTaxonomy build(InputStream snomedRf2SnapshotArchive, InputStream currentReleaseRf2DeltaArchive) throws ReleaseImportException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		SnomedTaxonomyLoader snomedTaxonomyLoader = new SnomedTaxonomyLoader();

		ReleaseImporter releaseImporter = new ReleaseImporter();
		releaseImporter.loadSnapshotReleaseFiles(
				snomedRf2SnapshotArchive,
				SNAPSHOT_LOADING_PROFILE,
				snomedTaxonomyLoader);
		snomedTaxonomyLoader.reportErrors();
		logger.info("Loaded previous release snapshot");

		snomedTaxonomyLoader.startLoadingDelta();

		releaseImporter.loadDeltaReleaseFiles(
				currentReleaseRf2DeltaArchive,
				DELTA_LOADING_PROFILE,
				snomedTaxonomyLoader);
		snomedTaxonomyLoader.reportErrors();
		logger.info("Loaded current release delta");

		stopWatch.stop();
		logger.info("SnomedTaxonomy loaded in {} seconds", stopWatch.getTotalTimeSeconds());

		SnomedTaxonomy snomedTaxonomy = snomedTaxonomyLoader.getSnomedTaxonomy();
		logger.info("{} concepts loaded", snomedTaxonomy.getAllConceptIds().size());
		return snomedTaxonomy;
	}
}
