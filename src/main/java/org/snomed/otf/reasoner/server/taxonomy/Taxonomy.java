package org.snomed.otf.reasoner.server.taxonomy;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.snomed.otf.reasoner.server.constants.Concepts;
import org.snomed.otf.reasoner.server.data.ConcreteDomainFragment;
import org.snomed.otf.reasoner.server.data.StatementFragment;

import java.util.*;
import java.util.stream.Collectors;

public class Taxonomy {

	private Set<Long> allConceptIds = new LongOpenHashSet();
	private Set<Long> fullyDefinedConceptIds = new LongOpenHashSet();
	private Map<Long, Set<StatementFragment>> statementFragmentMap = new Long2ObjectOpenHashMap<>();
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

	public void addStatementFragment(long conceptId, StatementFragment statementFragment) {
		statementFragmentMap.computeIfAbsent(conceptId, k -> new HashSet<>()).add(statementFragment);
		if (statementFragment.getTypeId() == Concepts.IS_A_LONG) {
			subTypesMap.computeIfAbsent(statementFragment.getDestinationId(), k -> new HashSet<>()).add(conceptId);
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
		if (conceptId == Concepts.ROOT_LONG) {
			return false;
		}

		// Check all ancestors for the attribute concept
		for (StatementFragment statementFragment : statementFragmentMap.get(conceptId)) {
			if (statementFragment.getTypeId() == Concepts.IS_A_LONG) {
				return statementFragment.getDestinationId() == ancestor || conceptHasAncestor(statementFragment.getDestinationId(), ancestor);
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

	public Collection<Object> getNonIsAFragments(Long conceptid) {
		return statementFragmentMap.get(conceptid).stream().filter(f -> f.getTypeId() != Concepts.IS_A_LONG).collect(Collectors.toList());
	}

	public Set<Long> getSubTypeIds(long conceptId) {
		return subTypesMap.get(conceptId);
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
}
