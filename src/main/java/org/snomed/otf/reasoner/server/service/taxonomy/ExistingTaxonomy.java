package org.snomed.otf.reasoner.server.service.taxonomy;

import static java.lang.Long.parseLong;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import org.snomed.otf.reasoner.server.service.ReasonerServiceException;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.data.ConcreteDomainFragment;
import org.snomed.otf.reasoner.server.service.data.StatementFragment;
import org.snomed.otf.reasoner.server.service.model.SnomedOntologyUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ExistingTaxonomy {

	private Set<Long> allConceptIds = new LongOpenHashSet();
	private Set<Long> fullyDefinedConceptIds = new LongOpenHashSet();
	private Map<Long, StatementFragment> statementFragmentsById = new HashMap<Long, StatementFragment>();
	private Map<Long, Set<StatementFragment>> statementFragmentMap = new Long2ObjectOpenHashMap<>();
	private Map<Long, Set<StatementFragment>> inferredStatementMap = new Long2ObjectOpenHashMap<>();
	private Map<Long, Set<Long>> subTypesMap = new Long2ObjectOpenHashMap<>();

	public boolean isPrimitive(Long conceptId) {
		return !fullyDefinedConceptIds.contains(conceptId);
	}

	public Collection<ConcreteDomainFragment> getConceptConcreteDomainFragments(Long conceptId) {
		// Later: Collect concrete domain members
		return Collections.emptySet();
	}

	/**
	 * Returns with all the active source relationships of a concept given by its unique ID.
	 * @param conceptId the ID of the SNOMED&nbsp;CT concept.
	 * @return the active source relationships.
	 */
	public Collection<StatementFragment> getStatementFragments(Long conceptId) {
		return statementFragmentMap.getOrDefault(conceptId, Collections.emptySet());
	}
	
	public Collection<StatementFragment> getInferredFragments(Long conceptId) {
		return inferredStatementMap.getOrDefault(conceptId, Collections.emptySet());
	}

	public void addOrModifyStatementFragment(boolean stated, long conceptId, StatementFragment statementFragment) {
		//Have we seen this relationship before ie we need to modify it?
		if (statementFragmentsById.containsKey(statementFragment.getStatementId())) {
			StatementFragment existingFragment = statementFragmentsById.get(statementFragment.getStatementId());
			//Only effectivetime and groupId are mutable
			existingFragment.setEffectiveTime(statementFragment.getEffectiveTime());
			existingFragment.setGroup(statementFragment.getGroup());
		} else {  //add fragment
			if (stated) {
				statementFragmentMap.computeIfAbsent(conceptId, k -> new HashSet<>()).add(statementFragment);
				if (statementFragment.getTypeId() == Concepts.IS_A_LONG) {
					subTypesMap.computeIfAbsent(statementFragment.getDestinationId(), k -> new HashSet<>()).add(conceptId);
				}
			} else {
				inferredStatementMap.computeIfAbsent(conceptId, k -> new HashSet<>()).add(statementFragment);
			}
			statementFragmentsById.put(statementFragment.getStatementId(), statementFragment);
		}
	}

	public Collection<ConcreteDomainFragment> getStatementConcreteDomainFragments(long statementId) {
		// Later: Collect relationship concrete domain members
		return Collections.emptySet();
	}

	public Set<Long> getAttributeConceptIds() {
		Set<Long> attributeIds = new HashSet<>();
		for (Long conceptId : allConceptIds) {
			if (conceptHasAncestor(conceptId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG)) {
				attributeIds.add(conceptId);
			}
		}
		return attributeIds;
	}
	
	public boolean conceptHasAncestor(long conceptId, long ancestor) {
		return conceptHasAncestor(conceptId, ancestor, 0);
	}

	public boolean conceptHasAncestor(long conceptId, long ancestor, long depth) {
		if (conceptId == Concepts.ROOT_LONG) {
			return false;
		}
		
		//TODO Temporary code to find out why we're seeing a recursive hierarchy
		if (depth > 30) {
			throw new RuntimeException("Depth limit exceeded searching for potential ancestor " + ancestor + " of concept " + conceptId ); 
		}

		// Check all ancestors for the attribute concept
		for (StatementFragment statementFragment : statementFragmentMap.get(conceptId)) {
			if (statementFragment.getTypeId() == Concepts.IS_A_LONG) {
				return statementFragment.getDestinationId() == ancestor || conceptHasAncestor(statementFragment.getDestinationId(), ancestor, ++depth);
			}
		}
		return false;
	}

	public Set<Long> getSuperTypeIds(long conceptId) {
		if (conceptId == Concepts.ROOT_LONG) {
			return Collections.emptySet();
		}

		Set<Long> superTypes = new HashSet<>();
		for (StatementFragment statementFragment : statementFragmentMap.get(conceptId)) {
			if (statementFragment.getTypeId() == Concepts.IS_A_LONG) {
				superTypes.add(statementFragment.getDestinationId());
			}
		}
		return superTypes;
	}

	public Collection<StatementFragment> getNonIsAFragments(Long conceptId) {
		return statementFragmentMap.getOrDefault(conceptId, Collections.emptySet()).stream().filter(f -> f.getTypeId() != Concepts.IS_A_LONG).collect(Collectors.toList());
	}

	public Set<Long> getSubTypeIds(long conceptId) {
		Set<Long> longs = subTypesMap.get(conceptId);
		return longs != null ? longs : Collections.emptySet();
	}

	public boolean isExhaustive(long conceptId) {
		// TODO: is this always false?
		return false;
	}

	public Set<Long> getAllConceptIds() {
		return allConceptIds;
	}

	public Set<Long> getFullyDefinedConceptIds() {
		return fullyDefinedConceptIds;
	}

	public Set<Long> getConceptIdSet() {
		return allConceptIds;
	}

	public Collection<StatementFragment> getInferredStatementFragments(long conceptId) {
		return inferredStatementMap.getOrDefault(conceptId, Collections.emptySet());
	}

	public void saveToDisk(File tempDir, String effectiveDate) throws ReasonerServiceException {
		try {
			File outputFile = new File(tempDir,  "sct2_StatedRelationship_Snapshot_INT_" + effectiveDate + ".txt");
			saveFragmentsToDisk(statementFragmentMap, outputFile);
			
			outputFile = new File(tempDir,  "sct2_Relationship_Snapshot_INT_" + effectiveDate + ".txt");
			saveFragmentsToDisk (inferredStatementMap, outputFile);
		} catch (IOException e) {
			throw new ReasonerServiceException("Unable to save existing taxonomy to disk",e);
		}
	}

	private void saveFragmentsToDisk( Map<Long, Set<StatementFragment>> fragmentMap, File outputFile) throws FileNotFoundException, IOException {
		String TSV_FIELD_DELIMITER = "\t";
		String LINE_DELIMITER = "\r\n";
		try(	OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile, true), StandardCharsets.UTF_8);
				BufferedWriter bw = new BufferedWriter(osw);
				PrintWriter out = new PrintWriter(bw))
		{
			String header = "id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId";
			out.print(header + LINE_DELIMITER);
			
			StringBuffer line = new StringBuffer();
			for (Map.Entry<Long, Set<StatementFragment>> fragSetEntry : fragmentMap.entrySet()) {
				
				for (StatementFragment frag : fragSetEntry.getValue()) {
					line.setLength(0);
					line.append(frag.getStatementId()).append(TSV_FIELD_DELIMITER)
						.append(frag.getEffectiveTime()).append(TSV_FIELD_DELIMITER)
						.append("1").append(TSV_FIELD_DELIMITER)
						.append(frag.getModuleId()).append(TSV_FIELD_DELIMITER)
						.append(fragSetEntry.getKey()).append(TSV_FIELD_DELIMITER)
						.append(frag.getDestinationId()).append(TSV_FIELD_DELIMITER)
						.append(frag.getGroup()).append(TSV_FIELD_DELIMITER)
						.append(frag.getTypeId()).append(TSV_FIELD_DELIMITER)
						.append(SnomedOntologyUtils.translateCharacteristicType(frag.getCharacteristicType())).append(TSV_FIELD_DELIMITER)
						.append(Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
					out.print(line.toString() + LINE_DELIMITER);
				}
			}
		} 
	}

	public void removeStatementFragment(String sourceId, String fragmentIdStr) {
		long fragmentId = parseLong(fragmentIdStr);
		getStatementFragments(parseLong(sourceId)).removeIf(fragment -> fragmentId == fragment.getStatementId());
		getInferredFragments(parseLong(sourceId)).removeIf(fragment -> fragmentId == fragment.getStatementId());
		statementFragmentsById.remove(fragmentId);
	}
}
