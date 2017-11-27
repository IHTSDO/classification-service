package org.snomed.otf.reasoner.server.service.normalform.internal;

/**
 * Represents any item in an ontology which can be compared for
 * expressiveness.
 *
 * @param <T>
 *            the implementing type
 */
public interface SemanticComparable<T> {

	/**
	 * Checks if the specified item can be regarded as redundant when
	 * compared to the current item. An item is redundant with respect to
	 * another if it less specific, i.e. it describes a broader range of
	 * individuals.
	 *
	 * @param other
	 *            the item to compare against
	 *
	 * @return <code>true</code> if this item contains an equal or more
	 *         specific description when compared to the other item,
	 *         <code>false</code> otherwise
	 */
	boolean isSameOrStrongerThan(T other);
}
