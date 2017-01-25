package org.snomed.otf.reasoner.server.taxonomy;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.snomed.otf.reasoner.server.data.StatementFragment;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Taxonomy {

	private Set<Long> fullyDefinedConceptIds = new LongOpenHashSet();

	/** Mapping between concept IDs and the associated active outbound relationships. */
	private Map<Long, Collection<StatementFragment>> conceptIdToStatements = new Long2ObjectOpenHashMap<>();

	public Set<Long> getFullyDefinedConceptIds() {
		return fullyDefinedConceptIds;
	}

	public Map<Long, Collection<StatementFragment>> getConceptIdToStatements() {
		return conceptIdToStatements;
	}
}
