package org.snomed.otf.reasoner.server.service;

import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.data.Relationship;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipChangeCollector;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
class ClassificationResultsService {

	private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private static final String RELATIONSHIPS_HEADER = "id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId";
	private static final String EQUIVALENT_REFSET_HEADER = "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\tmapTarget";
	private static final String TAB = "\t";

	File createResultsRf2Archive(RelationshipChangeCollector changeCollector, List<Set<Long>> equivalentConceptIdSets, Date startDate) throws ReasonerServiceException {
		File resultsDirectory = getOutputDirectoryFile();
		File outputFile = new File(resultsDirectory, "classification-results-rf2-" + new Date().getTime() + ".zip");
		try {
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile), UTF_8_CHARSET);
				 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zipOutputStream))) {

				String formattedDate = DATE_FORMAT.format(startDate);
				zipOutputStream.putNextEntry(new ZipEntry(String.format("RF2/sct2_Relationship_Delta_Classification_%s.txt", formattedDate)));
				writeRelationshipChanges(writer, changeCollector.getAddedStatements(), changeCollector.getRemovedStatements());

				zipOutputStream.putNextEntry(new ZipEntry(String.format("RF2/der2_sRefset_EquivalentConceptSimpleMapDelta_Classification_%s.txt", formattedDate)));
				writeEquivalentConcepts(writer, equivalentConceptIdSets);
			}
		} catch (IOException e) {
			throw new ReasonerServiceException("Failed to write out results archive.", e);
		}
		return outputFile;
	}

	private void writeRelationshipChanges(BufferedWriter writer, Map<Long, Set<Relationship>> addedStatements, Map<Long, Set<Relationship>> removedStatements) throws IOException {
		// Write header
		writer.write(RELATIONSHIPS_HEADER);
		writer.newLine();

		// Write newly inferred relationships
		for (Long sourceId : addedStatements.keySet()) {
			String relationshipId = "";
			String active = "1";
			for (Relationship relationship : addedStatements.get(sourceId)) {
				writeRelationship(writer,
						relationshipId,
						active,
						sourceId,
						relationship.getDestinationId(),
						relationship.getGroup(),
						relationship.getTypeId(),
						Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
			}
		}

		// Write redundant relationships
		for (Long sourceId : removedStatements.keySet()) {
			String active = "0";
			for (Relationship relationship : removedStatements.get(sourceId)) {
				writeRelationship(writer,
						relationship.getRelationshipId() + "",
						active,
						sourceId,
						relationship.getDestinationId(),
						relationship.getGroup(),
						relationship.getTypeId(),
						Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
			}
		}

		writer.flush();
	}

	private void writeEquivalentConcepts(BufferedWriter writer, List<Set<Long>> equivalentConceptIdSets) throws IOException {
		// Write header
		writer.write(EQUIVALENT_REFSET_HEADER);
		writer.newLine();

		// Write sets of equivalentConcepts
		for (Set<Long> equivalentConceptIdSet : equivalentConceptIdSets) {
			String setId = UUID.randomUUID().toString();

			for (Long conceptId : equivalentConceptIdSet) {
				// random member id
				writer.write(UUID.randomUUID().toString());
				writer.write(TAB);

				// no effectiveTime
				writer.write(TAB);

				// active
				writer.write("1");
				writer.write(TAB);

				// no moduleId
				writer.write(TAB);

				// no refsetId
				writer.write(TAB);

				// referencedComponentId is one of the concepts in the set
				writer.write(conceptId.toString());
				writer.write(TAB);

				// mapTarget is the unique id for the set
				writer.write(setId);
				writer.write(TAB);

				writer.newLine();
			}
		}

		writer.flush();
	}

	private void writeRelationship(BufferedWriter writer, String relationshipId, String active, Long sourceId, Long destinationId, Integer group, Long typeId, String existentialRestrictionModifier) throws IOException {
		writer.write(relationshipId);
		writer.write(TAB);

		// No effectiveTime
		writer.write(TAB);

		// active
		writer.write(active);
		writer.write(TAB);

		// No module
		writer.write(TAB);

		// sourceId
		writer.write(sourceId.toString());
		writer.write(TAB);

		// destinationId
		writer.write(destinationId.toString());
		writer.write(TAB);

		// relationshipGroup
		writer.write(group.toString());
		writer.write(TAB);

		// typeId
		writer.write(typeId.toString());
		writer.write(TAB);

		// characteristicTypeId
		writer.write(Concepts.INFERRED_RELATIONSHIP);
		writer.write(TAB);

		// modifierId always existential at this time
		writer.write(existentialRestrictionModifier);
		writer.write(TAB);

		writer.newLine();
	}

	private File getOutputDirectoryFile() throws ReasonerServiceException {
		File resultsDirectory = new File("classification-results");
		if (!resultsDirectory.isDirectory()) {
			if (!resultsDirectory.mkdirs()) {
				throw new ReasonerServiceException("Failed to create results directory.");
			}
		}
		return resultsDirectory;
	}

}
