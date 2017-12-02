package org.snomed.otf.reasoner.server.service.taxonomy;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.snomed.otf.reasoner.server.service.ReasonerServiceException;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.data.Relationship;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

public class SnomedTaxonomy {

	private Set<Long> allConceptIds = new LongOpenHashSet();
	private Set<Long> fullyDefinedConceptIds = new LongOpenHashSet();
	private Map<Long, Relationship> statedRelationshipsById = new HashMap<>();
	private Map<Long, Relationship> inferredRelationshipsById = new HashMap<>();
	private Map<String, OWLAxiom> axiomsById = new HashMap<>();
	private Map<Long, Set<Relationship>> conceptStatedRelationshipMap = new Long2ObjectOpenHashMap<>();
	private Map<Long, Set<Relationship>> conceptInferredRelationshipMap = new Long2ObjectOpenHashMap<>();
	private Map<Long, Set<OWLAxiom>> conceptAxiomMap = new Long2ObjectOpenHashMap<>();
	private Map<Long, Set<Long>> statedSubTypesMap = new Long2ObjectOpenHashMap<>();

	public boolean isPrimitive(Long conceptId) {
		return !fullyDefinedConceptIds.contains(conceptId);
	}

	/**
	 * Returns with all the active source relationships of a concept given by its unique ID.
	 * @param conceptId the ID of the SNOMED&nbsp;CT concept.
	 * @return the active source relationships.
	 */
	public Collection<Relationship> getStatedRelationships(Long conceptId) {
		return conceptStatedRelationshipMap.getOrDefault(conceptId, Collections.emptySet());
	}
	
	public Collection<Relationship> getInferredRelationships(Long conceptId) {
		return conceptInferredRelationshipMap.getOrDefault(conceptId, Collections.emptySet());
	}

	public void addOrModifyRelationship(boolean stated, long conceptId, Relationship relationship) {
		// Have we seen this relationship before ie we need to modify it?
		Relationship existingRelationship = stated ? statedRelationshipsById.get(relationship.getRelationshipId())
				: inferredRelationshipsById.get(relationship.getRelationshipId());

		if (existingRelationship != null) {
			// Only effectiveTime and groupId are mutable
			existingRelationship.setEffectiveTime(relationship.getEffectiveTime());
			existingRelationship.setGroup(relationship.getGroup());
		} else {
			// add relationship
			if (stated) {
				conceptStatedRelationshipMap.computeIfAbsent(conceptId, k -> new HashSet<>()).add(relationship);
				if (relationship.getTypeId() == Concepts.IS_A_LONG) {
					statedSubTypesMap.computeIfAbsent(relationship.getDestinationId(), k -> new HashSet<>()).add(conceptId);
				}
				statedRelationshipsById.put(relationship.getRelationshipId(), relationship);
			} else {
				conceptInferredRelationshipMap.computeIfAbsent(conceptId, k -> new HashSet<>()).add(relationship);
				inferredRelationshipsById.put(relationship.getRelationshipId(), relationship);
			}
		}
	}

	// TODO: Replace this - just collect the attributes used in active relationships
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
		for (Relationship relationship : conceptStatedRelationshipMap.getOrDefault(conceptId, Collections.emptySet())) {
			if (relationship.getTypeId() == Concepts.IS_A_LONG) {
				return relationship.getDestinationId() == ancestor || conceptHasAncestor(relationship.getDestinationId(), ancestor, ++depth);
			}
		}
		return false;
	}

	public Set<Long> getSuperTypeIds(long conceptId) {
		if (conceptId == Concepts.ROOT_LONG) {
			return Collections.emptySet();
		}

		Set<Long> superTypes = new HashSet<>();
		for (Relationship relationship : conceptStatedRelationshipMap.get(conceptId)) {
			if (relationship.getTypeId() == Concepts.IS_A_LONG) {
				superTypes.add(relationship.getDestinationId());
			}
		}
		return superTypes;
	}

	public Collection<Relationship> getNonIsARelationships(Long conceptId) {
		return conceptStatedRelationshipMap.getOrDefault(conceptId, Collections.emptySet()).stream().filter(f -> f.getTypeId() != Concepts.IS_A_LONG).collect(Collectors.toList());
	}

	public Set<Long> getSubTypeIds(long conceptId) {
		Set<Long> longs = statedSubTypesMap.get(conceptId);
		return longs != null ? longs : Collections.emptySet();
	}

	public Set<Long> getAncestorIds(long conceptId) {
		return getAncestorIds(conceptId, new LongOpenHashSet());
	}

	private Set<Long> getAncestorIds(long conceptId, Set<Long> ids) {
		Set<Long> subTypeIds = getSubTypeIds(conceptId);
		ids.addAll(subTypeIds);
		for (Long subTypeId : subTypeIds) {
			getAncestorIds(subTypeId, ids);
		}
		return ids;
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

	public Collection<Relationship> getInferredRelationships(long conceptId) {
		return conceptInferredRelationshipMap.getOrDefault(conceptId, Collections.emptySet());
	}

	public void removeRelationship(boolean stated, String sourceId, String relationshipIdStr) {
		long relationshipId = parseLong(relationshipIdStr);
		if (stated) {
			getStatedRelationships(parseLong(sourceId)).removeIf(relationship -> relationshipId == relationship.getRelationshipId());
			statedRelationshipsById.remove(relationshipId);
		} else {
			getInferredRelationships(parseLong(sourceId)).removeIf(relationship -> relationshipId == relationship.getRelationshipId());
			inferredRelationshipsById.remove(relationshipId);
		}
	}

	public void addAxiom(String referencedComponentId, String axiomId, OWLAxiom owlAxiom) {
		conceptAxiomMap.computeIfAbsent(parseLong(referencedComponentId), id -> new HashSet<>()).add(owlAxiom);
		axiomsById.put(axiomId, owlAxiom);
	}

	public void removeAxiom(String referencedComponentId, String id) {
		// Find the previously loaded axiom by id so that it can be removed from the set of axioms on the concept
		OWLAxiom owlAxiomToRemove = axiomsById.remove(id);
		if (owlAxiomToRemove != null) {
			conceptAxiomMap.get(parseLong(referencedComponentId)).remove(owlAxiomToRemove);
		}
	}

	public Map<Long, Set<OWLAxiom>> getConceptAxiomMap() {
		return conceptAxiomMap;
	}

	public void debugDumpToDisk(File tempDir, String effectiveDate) throws ReasonerServiceException {
		try {
			File outputFile = new File(tempDir,  "sct2_StatedRelationship_Snapshot_INT_" + effectiveDate + ".txt");
			saveRelationshipsToDisk(conceptStatedRelationshipMap, outputFile);

			outputFile = new File(tempDir,  "sct2_Relationship_Snapshot_INT_" + effectiveDate + ".txt");
			saveRelationshipsToDisk (conceptInferredRelationshipMap, outputFile);
		} catch (IOException e) {
			throw new ReasonerServiceException("Unable to save existing taxonomy to disk",e);
		}
	}

	private void saveRelationshipsToDisk(Map<Long, Set<Relationship>> relationshipMap, File outputFile) throws IOException {
		String TSV_FIELD_DELIMITER = "\t";
		String LINE_DELIMITER = "\r\n";
		try(	OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputFile, true), StandardCharsets.UTF_8);
				BufferedWriter bw = new BufferedWriter(osw);
				PrintWriter out = new PrintWriter(bw))
		{
			String header = "id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId";
			out.print(header + LINE_DELIMITER);

			StringBuilder line = new StringBuilder();
			for (Map.Entry<Long, Set<Relationship>> fragSetEntry : relationshipMap.entrySet()) {

				for (Relationship relationship : fragSetEntry.getValue()) {
					line.setLength(0);
					line.append(relationship.getRelationshipId()).append(TSV_FIELD_DELIMITER)
							.append(relationship.getEffectiveTime()).append(TSV_FIELD_DELIMITER)
							.append("1").append(TSV_FIELD_DELIMITER)
							.append(relationship.getModuleId()).append(TSV_FIELD_DELIMITER)
							.append(fragSetEntry.getKey()).append(TSV_FIELD_DELIMITER)
							.append(relationship.getDestinationId()).append(TSV_FIELD_DELIMITER)
							.append(relationship.getGroup()).append(TSV_FIELD_DELIMITER)
							.append(relationship.getTypeId()).append(TSV_FIELD_DELIMITER)
							.append(relationship.getCharacteristicType()).append(TSV_FIELD_DELIMITER)
							.append(Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
					out.print(line.toString() + LINE_DELIMITER);
				}
			}
		}
	}

}
