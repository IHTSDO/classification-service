package org.snomed.otf.reasoner.server.taxonomy;

import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.ihtsdo.otf.snomedboot.ReleaseImporter;
import org.ihtsdo.otf.snomedboot.factory.LoadingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.ontology.DelegateOntology;
import org.springframework.util.StopWatch;

/**
 * Class for building the bare minimum representation of the state of the SNOMED&nbsp;CT ontology before processing changes.
 * <p>
 * This class should be used to compare the ontology state with the outcome of the classification process.
 */
public class ReasonerTaxonomyBuilder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void build() throws ReleaseImportException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TaxonomyLoader taxonomyLoader = new TaxonomyLoader();
		new ReleaseImporter().loadSnapshotReleaseFiles(
				"release/SnomedCT_InternationalRF2_Production_20170131",
				new LoadingProfile().withStatedRelationships().withFullRelationshipObjects(),
				taxonomyLoader);

		stopWatch.stop();
		logger.info("Taxonomy loaded in {} seconds", stopWatch.getTotalTimeSeconds());
		Taxonomy taxonomy = taxonomyLoader.getTaxonomy();
		logger.info("{} concepts loaded", taxonomy.getConceptIdToStatements().size());

		new DelegateOntology(taxonomy);
	}
}
