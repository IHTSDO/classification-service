/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package org.snomed.otf.reasoner.server.service.model;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.constants.SnomedConstants;

import com.google.common.collect.Iterables;

/**
 * Utility class that holds OWL object identifier prefixes as well as methods for creating class expressions for object intersections and unions.
 */
public abstract class SnomedOntologyUtils implements SnomedConstants {

	public static final String BASE_NAMESPACE = "http://snomed.org/";
	public static final IRI BASE_IRI = IRI.create(BASE_NAMESPACE);
	public static final URI BASE_URI = BASE_IRI.toURI();
	public static final String PREFIX_SNOMED = "snomed:";
	public static final String PREFIX_LABEL = PREFIX_SNOMED + "label_";
	public static final String PREFIX_CONCEPT = PREFIX_SNOMED + "concept_";
	public static final String PREFIX_ROLE = PREFIX_SNOMED + "role_";
	public static final String PREFIX_ROLE_HAS_MEASUREMENT = PREFIX_SNOMED + "roleHasMeasurement";
	public static final String PREFIX_ROLE_GROUP = PREFIX_SNOMED + "roleGroup";
	public static final String PREFIX_CONCEPT_UNIT_NOT_APPLICABLE = PREFIX_SNOMED + "conceptUnitNotApplicable";
	public static final String PREFIX_DATA_HAS_VALUE = PREFIX_SNOMED + "dataHasValue";
	public static final String PREFIX_HAS_UNIT = PREFIX_SNOMED + "roleHasUnit";

	/**
	 * Creates a {@link DefaultPrefixManager} instance for the specified ontology. The prefix {{@code snomed:} will be registered for the ontology's
	 * IRI.
	 * @param ontology the ontology to use (may not be {@code null})
	 * @return the created prefix manager
	 */
	public static DefaultPrefixManager createPrefixManager(final OWLOntology ontology) {
		return createPrefixManager(ontology.getOntologyID().getOntologyIRI());
	}

	/**
	 * Creates a {@link DefaultPrefixManager} instance for the specified IRI. The prefix {{@code snomed:} will be registered for the argument.
	 * @param ontologyIRI the IRI to register for the {@code snomed:} prefix (may not be {@code null})
	 * @return the created prefix manager
	 */
	public static DefaultPrefixManager createPrefixManager(final IRI ontologyIRI) {
		final DefaultPrefixManager prefixManager = new DefaultPrefixManager();
		prefixManager.setPrefix(PREFIX_SNOMED, ontologyIRI.toString() + "#");
		return prefixManager;
	}

	/**
	 * Creates an {@link OWLObjectIntersectionOf} expression for the passed in class expression set, or returns a single expression if the set has
	 * only one element.
	 * @param df the {@link OWLDataFactory} to use for creating OWL objects
	 * @param terms the set of terms to convert
	 * @return the converted class expression
	 */
	public static OWLClassExpression simplifyIntersectionOf(final OWLDataFactory df, final Set<OWLClassExpression> terms) {
		if (terms.size() > 1) {
			return df.getOWLObjectIntersectionOf(terms);
		} else {
			return Iterables.getOnlyElement(terms); // Also handles case when terms is empty
		}
	}

	/**
	 * Creates an {@link OWLObjectUnionOf} expression for the passed in class expression set, or returns a single expression if the set has only one
	 * element.
	 * @param df the {@link OWLDataFactory} to use for creating OWL objects
	 * @param terms the set of terms to convert
	 * @return the converted class expression
	 */
	public static OWLClassExpression simplifyUnionOf(final OWLDataFactory df, final Set<OWLClassExpression> terms) {
		if (terms.size() > 1) {
			return df.getOWLObjectUnionOf(terms);
		} else {
			return Iterables.getOnlyElement(terms); // Also handles case when terms is empty
		}
	}
	
	public static CharacteristicType translateCharacteristicType (String charTypeSctId) {
		switch (charTypeSctId) {
			case Concepts.INFERRED_RELATIONSHIP : return CharacteristicType.INFERRED;
			case Concepts.STATED_RELATIONSHIP : return CharacteristicType.STATED;
			case Concepts.ADDITIONAL_RELATIONSHIP : return CharacteristicType.ADDITIONAL;
		}
		throw new IllegalArgumentException ( "Encountered unexpected characterstic type id " + charTypeSctId);
	}

	private SnomedOntologyUtils() {
		// Prevent instantiation
	}
}