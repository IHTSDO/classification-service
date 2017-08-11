package org.snomed.otf.reasoner.server.service.classification;

import java.util.Set;

public class ReasonerTaxonomyEntry {

	private final long sourceId;
	private final Set<Long> parentIds;

	public ReasonerTaxonomyEntry(long sourceId, Set<Long> parents) {
		this.sourceId = sourceId;
		this.parentIds = parents;
	}

	public long getSourceId() {
		return sourceId;
	}

	public Set<Long> getParentIds() {
		return parentIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReasonerTaxonomyEntry that = (ReasonerTaxonomyEntry) o;

		if (sourceId != that.sourceId) return false;
		return parentIds != null ? parentIds.equals(that.parentIds) : that.parentIds == null;
	}

	@Override
	public int hashCode() {
		int result = (int) (sourceId ^ (sourceId >>> 32));
		result = 31 * result + (parentIds != null ? parentIds.hashCode() : 0);
		return result;
	}
}
