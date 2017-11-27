package org.snomed.otf.reasoner.server.service.normalform.internal;

import com.google.common.collect.ImmutableList;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipNormalFormGenerator;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class UnionGroup implements SemanticComparable<UnionGroup> {

	private final List<RelationshipFragment> fragments;

	private int unionGroupNumber = RelationshipNormalFormGenerator.NUMBER_NOT_PRESERVED;

	/**
	 * Creates a new union group instance with the specified parameters,
	 * preserving the union group number for later reference.
	 *
	 * @param fragments
	 *            the relationship fragments to associate with this union
	 *            group (may not be <code>null</code>)
	 */
	public UnionGroup(final Iterable<RelationshipFragment> fragments) {
		checkArgument(fragments != null, "fragments is null.");
		this.fragments = ImmutableList.copyOf(fragments);
	}

	public List<RelationshipFragment> getRelationshipFragments() {
		return fragments;
	}

	public int getUnionGroupNumber() {
		return unionGroupNumber;
	}

	public void setUnionGroupNumber(final int unionGroupNumber) {
		checkArgument(unionGroupNumber > RelationshipNormalFormGenerator.NUMBER_NOT_PRESERVED, "Illegal union group number '%s'.", unionGroupNumber);
		this.unionGroupNumber = unionGroupNumber;
	}

	@Override
	public boolean isSameOrStrongerThan(final UnionGroup other) {

		/*
		 * Things same or stronger than A OR B OR C:
		 *
		 * - A' OR B OR C, where A' is a subclass of A
		 * - B
		 *
		 * So we'll have to check for all of our fragments to see if a less
		 * expressive fragment exists in the "other" union group. Points are
		 * awarded if we manage to get away with less fragments than the
		 * "other" union group.
		 */
		for (final RelationshipFragment ourFragment : fragments) {

			boolean found = false;

			for (final RelationshipFragment otherFragment : other.fragments) {

				if (ourFragment.isSameOrStrongerThan(otherFragment)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return 31 + fragments.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof UnionGroup)) {
			return false;
		}

		final UnionGroup other = (UnionGroup) obj;

		if (fragments.size() != other.fragments.size()) {
			return false;
		}

		// containsAll should be symmetric in this case
		return fragments.containsAll(other.fragments);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("UnionGroup [fragments=");
		builder.append(fragments);
		builder.append("]");
		return builder.toString();
	}
}
