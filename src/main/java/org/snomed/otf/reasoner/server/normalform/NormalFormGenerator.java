/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snomed.otf.reasoner.server.normalform;

import com.google.common.collect.Ordering;
import org.snomed.otf.reasoner.server.classification.ReasonerTaxonomy;
import org.snomed.otf.reasoner.server.taxonomy.ExistingTaxonomy;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base class for different implementations, which generate a set of components in normal form, based on a subsumption
 * hierarchy encapsulated in a reasoner.
 * 
 * @param <T> the generated component type
 * 
 */
public abstract class NormalFormGenerator<T> {

	protected final ReasonerTaxonomy reasonerTaxonomy;
	
	protected final ExistingTaxonomy existingTaxonomy;

	public NormalFormGenerator(final ReasonerTaxonomy reasonerTaxonomy, ExistingTaxonomy existingTaxonomy) {
		this.reasonerTaxonomy = reasonerTaxonomy;
		this.existingTaxonomy = existingTaxonomy;
	}
	
	/**
	 * Computes and returns all changes as a result of normal form computation.
	 * 
	 * @param processor the change processor to route changes to
	 * @param ordering an ordering defined over existing and generated components, used for detecting changes
	 * @return the total number of generated components
	 */
	public final int collectNormalFormChanges(final OntologyChangeProcessor<T> processor, final Ordering<T> ordering) {
		final List<Long> entries = reasonerTaxonomy.getConceptIds();
		int generatedComponentCount = 0;
		
		for (Long conceptId : entries) {
			final Collection<T> existingComponents = getExistingComponents(conceptId);
			final Collection<T> generatedComponents = getGeneratedComponents(conceptId);
			processor.apply(conceptId, existingComponents, generatedComponents, ordering);
			generatedComponentCount += generatedComponents.size();
		}

		return generatedComponentCount; 
	}
	
	public abstract Collection<T> getExistingComponents(final long conceptId);

	/**
	 * Computes and returns a set of components in normal form for the specified concept.
	 * @param conceptId the concept for which components should be generated
	 * @return the generated components of the specified concept in normal form
	 */
	public abstract Collection<T> getGeneratedComponents(final long conceptId);
}
