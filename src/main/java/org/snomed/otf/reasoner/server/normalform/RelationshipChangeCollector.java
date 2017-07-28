package org.snomed.otf.reasoner.server.normalform;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.snomed.otf.reasoner.server.data.StatementFragment;
import org.snomed.otf.reasoner.server.taxonomy.ExistingTaxonomy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RelationshipChangeCollector extends OntologyChangeProcessor<StatementFragment> {

	private final Map<Long, Set<StatementFragment>> addedStatements;
	private final Map<Long, Set<StatementFragment>> removedStatements;
	private Long addedCount;
	private Long removedCount;

	public RelationshipChangeCollector() {
		addedCount = 0L;
		removedCount = 0L;
		addedStatements = new Long2ObjectOpenHashMap<>();
		removedStatements = new Long2ObjectOpenHashMap<>();
	}

	@Override
	protected void handleAddedSubject(long conceptId, StatementFragment addedSubject) {
		addedStatements.getOrDefault(conceptId, new HashSet<>()).add(addedSubject);
		addedCount++;
	}

	@Override
	protected void handleRemovedSubject(long conceptId, StatementFragment removedSubject) {
		removedStatements.getOrDefault(conceptId, new HashSet<>()).add(removedSubject);
		removedCount++;
	}

	public Long getAddedCount() {
		return addedCount;
	}

	public Long getRemovedCount() {
		return removedCount;
	}
}
