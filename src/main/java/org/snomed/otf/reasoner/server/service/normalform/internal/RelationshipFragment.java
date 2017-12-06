package org.snomed.otf.reasoner.server.service.normalform.internal;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.snomed.otf.reasoner.server.service.data.Relationship;
import org.snomed.otf.reasoner.server.service.normalform.RelationshipNormalFormGenerator;

import java.text.MessageFormat;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents concept attribute-value pairs, used when relationships
 * originating from different sources are being processed.
 */
public final class RelationshipFragment implements SemanticComparable<RelationshipFragment> {

	private RelationshipNormalFormGenerator relationshipNormalFormGenerator;
	private final Relationship fragment;

	/**
	 * Creates a new relationship fragment from the specified relationship.
	 *
	 * @param fragment
	 *            the relationship to extract attribute and value from (may
	 *            not be <code>null</code>)
	 *
	 * @throws NullPointerException
	 *             if the given relationship is <code>null</code>
	 */
	public RelationshipFragment(RelationshipNormalFormGenerator relationshipNormalFormGenerator, final Relationship fragment) {
		this.relationshipNormalFormGenerator = relationshipNormalFormGenerator;
		this.fragment = checkNotNull(fragment, "fragment");
	}

	public boolean isDestinationNegated() {
		return fragment.isDestinationNegated();
	}


	public boolean isUniversal() {
		return fragment.isUniversal();
	}

	public long getTypeId() {
		return fragment.getTypeId();
	}


	public long getDestinationId() {
		return fragment.getDestinationId();
	}


	public long getStatementId() {
		return fragment.getRelationshipId();
	}


	@Override
	public boolean isSameOrStrongerThan(final RelationshipFragment other) {

		if (this.equals(other)) {
			return true;
		}

		if (isUniversal() != other.isUniversal()) {
			return false;
		}

		if (this.getTypeId() == 116680003L && other.getTypeId() == 116680003L) {
			System.out.println("Two IS As compared");
		}

		if (!isDestinationNegated() && !other.isDestinationNegated()) {

			/*
			 * Things same or stronger than (some/all) rA:
			 *
			 * - (some/all) r'A, where r' is equal to r or is a descendant of r
			 * - (some/all) rA', where A' is equal to A or is a descendant of A
			 * - (some/all) r'A', where both of the above applies
			 */
			final Set<Long> attributeClosure = getConceptAndAllSuperTypes(getTypeId());
			final Set<Long> valueClosure = getValueClosure(getDestinationId(), getTypeId());

			return attributeClosure.contains(other.getTypeId()) && valueClosure.contains(other.getDestinationId());

		} else if (isDestinationNegated() && !other.isDestinationNegated()) {

			final Set<Long> otherAttributeClosure = getConceptAndAllSuperTypes(other.getTypeId());
			final Set<Long> superTypes = getValueClosure(getDestinationId(), getTypeId());
			superTypes.remove(getDestinationId());

			/*
			 * Note that "other" itself may be exhaustive in this case --
			 * the negation will work entirely within the confines of
			 * "other", so it is still going to be more expressive than
			 * "other".
			 *
			 * Supertypes of the negated value can only appear above the
			 * "layers" of exhaustive concepts, because any other case
			 * should be unsatisfiable.
			 */
			return otherAttributeClosure.contains(getTypeId()) && (hasCommonExhaustiveSuperType(other) || isDestinationExhaustive()) && superTypes.contains(other.getDestinationId());

		} else if (!isDestinationNegated() && other.isDestinationNegated()) {

			final Set<Long> attributeClosure = getConceptAndAllSuperTypes(getTypeId());

			/*
			 * Any contradictions should be filtered out by the reasoner beforehand, so we just check if the two concepts
			 * have a common exhaustive ancestor.
			 */
			return attributeClosure.contains(other.getTypeId()) && hasCommonExhaustiveSuperType(other);

		} else /* if (destinationNegated && other.destinationNegated) */ {

			/*
			 * Note that the comparison is the exact opposite of the first case - if both fragments are negated,
			 * the one which negates a more loose definition is the one that is more strict in the end.
			 */
			final Set<Long> otherAttributeClosure = getConceptAndAllSuperTypes(other.getTypeId());
			final Set<Long> otherValueClosure = getValueClosure(other.getDestinationId(), other.getTypeId());

			return otherAttributeClosure.contains(getTypeId()) && otherValueClosure.contains(getDestinationId());
		}
	}

	private boolean isDestinationExhaustive() {
		return isExhaustive(getDestinationId());
	}

	private boolean hasCommonExhaustiveSuperType(final RelationshipFragment other) {

		final Set<Long> valueAncestors = relationshipNormalFormGenerator.getReasonerTaxonomy().getAncestors(getDestinationId());
		final Set<Long> otherValueAncestors = relationshipNormalFormGenerator.getReasonerTaxonomy().getAncestors(other.getDestinationId());
		final Set<Long> commonAncestors = Sets.intersection(valueAncestors, otherValueAncestors);

		for (Long commonAncestor : commonAncestors) {
			if (isExhaustive(commonAncestor)) {
				return true;
			}
		}

		return false;
	}

	private boolean isExhaustive(final long conceptId) {
		return relationshipNormalFormGenerator.getSnomedTaxonomy().isExhaustive(conceptId);
	}

	/**
	 * Collects all parent concepts reachable from the specified concept. The
	 * returned set also includes the starting concept.
	 *
	 * @param conceptId
	 *            the concept to start from
	 *
	 * @return a set containing the starting concept and all reachable
	 *         supertypes
	 */
	private Set<Long> getConceptAndAllSuperTypes(final long conceptId) {
		final Set<Long> ancestors = relationshipNormalFormGenerator.getReasonerTaxonomy().getAncestors(conceptId);
		final Set<Long> conceptAndAncestors = new LongOpenHashSet(ancestors);
		conceptAndAncestors.add(conceptId);
		return conceptAndAncestors;
	}

	private Set<Long> getValueClosure(final long conceptId, final long typeId) {
		Set<Long> closure = getConceptAndAllSuperTypes(conceptId);
		if (relationshipNormalFormGenerator.getAllTransitiveProperties().contains(typeId)) {
			closure.addAll(relationshipNormalFormGenerator.getTransitiveNodeGraphs().get(typeId).getAncestors(conceptId));
			for (Long transitiveProperty : relationshipNormalFormGenerator.getSnomedTaxonomy().getSubTypeIds(typeId)) {
				closure.addAll(relationshipNormalFormGenerator.getTransitiveNodeGraphs().get(transitiveProperty).getAncestors(conceptId));
			}
		}
		return closure;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof RelationshipFragment)) {
			return false;
		}

		final RelationshipFragment other = (RelationshipFragment) obj;

		return (isUniversal() == other.isUniversal()) &&
				(isDestinationNegated() == other.isDestinationNegated()) &&
				(getTypeId() == other.getTypeId()) &&
				(getDestinationId() == other.getDestinationId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isUniversal(), isDestinationNegated(), getTypeId(), getDestinationId());
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0,number,#} : {1}{2,number,#} ({3})", getTypeId(), (isDestinationNegated() ? "NOT" : ""), getDestinationId(), isUniversal());
	}
}
