package org.snomed.otf.reasoner.server.service.normalform;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.snomed.otf.reasoner.server.service.constants.SnomedConstants;
import org.snomed.otf.reasoner.server.service.data.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelationshipChangeCollector extends OntologyChangeProcessor<Relationship> implements SnomedConstants{

	private final Map<Long, Set<Relationship>> addedStatements;
	private final Map<Long, Set<Relationship>> removedStatements;
	private Long addedCount;
	private Long removedCount;

	public RelationshipChangeCollector() {
		addedCount = 0L;
		removedCount = 0L;
		addedStatements = new Long2ObjectOpenHashMap<>();
		removedStatements = new Long2ObjectOpenHashMap<>();
	}

	@Override
	protected void handleAddedSubject(long conceptId, Relationship addedSubject) {
		addedStatements.computeIfAbsent(conceptId, k -> new HashSet<>()).add(addedSubject);
		addedCount++;
	}

	@Override
	protected void handleRemovedSubject(long conceptId, Relationship removedSubject) {
		//We will preserve any "Additional" characteristic types eg PartOf relationships
		if (removedSubject.getCharacteristicType() == null || removedSubject.getCharacteristicType() != CharacteristicType.ADDITIONAL) {
			removedStatements.computeIfAbsent(conceptId, k -> new HashSet<>()).add(removedSubject);
			removedCount++;
		}
	}

	public Long getAddedCount() {
		return addedCount;
	}

	public Long getRemovedCount() {
		return removedCount;
	}

	public Map<Long, Set<Relationship>> getAddedStatements() {
		return addedStatements;
	}

	public Map<Long, Set<Relationship>> getRemovedStatements() {
		return removedStatements;
	}
}
