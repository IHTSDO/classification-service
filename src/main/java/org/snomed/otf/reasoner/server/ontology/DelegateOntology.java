/*
 * Based on code from B2i:
 *  Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package org.snomed.otf.reasoner.server.ontology;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.snomed.otf.reasoner.server.constants.Concepts;
import org.snomed.otf.reasoner.server.data.ConcreteDomainFragment;
import org.snomed.otf.reasoner.server.data.StatementFragment;
import org.snomed.otf.reasoner.server.model.*;
import org.snomed.otf.reasoner.server.taxonomy.Taxonomy;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectImpl;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static org.snomed.otf.reasoner.server.model.SnomedOntologyUtils.PREFIX_CONCEPT;
import static org.snomed.otf.reasoner.server.model.SnomedOntologyUtils.PREFIX_ROLE;

public class DelegateOntology extends OWLObjectImpl implements OWLMutableOntology {

	private Taxonomy taxonomy;
	private final OWLOntologyManager manager;
	private final OWLOntologyID ontologyID;
	private final DefaultPrefixManager prefixManager;

	DelegateOntology(final OWLOntologyManager manager, final OWLOntologyID ontologyID) {
		this.manager = manager;
		this.ontologyID = ontologyID;
		this.prefixManager = SnomedOntologyUtils.createPrefixManager(this);
	}

	@Override
	public OWLOntologyManager getOWLOntologyManager() {
		return manager;
	}

	@Override
	public OWLOntologyID getOntologyID() {
		return ontologyID;
	}

	@Override
	public boolean isAnonymous() {
		return ontologyID.isAnonymous();
	}

	@Override
	public Set<OWLAnnotation> getAnnotations() {
		// No direct annotations are added to the ontology
		return WritableEmptySet.create();
	}

	@Override
	public Set<IRI> getDirectImportsDocuments() throws UnknownOWLOntologyException {
		// No direct import IRIs will be present
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLOntology> getDirectImports() throws UnknownOWLOntologyException {
		// No directly imported ontologies will be added
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLOntology> getImports() throws UnknownOWLOntologyException {
		// No transitive imports will be present, as no direct imports are present
		return null;
	}

	@Override
	public Set<OWLOntology> getImportsClosure() throws UnknownOWLOntologyException {
		// No imports closure needs to be calculated, as no direct imports are present
		return Sets.newHashSet(this);
	}

	@Override
	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		// No imports declarations are present
		return WritableEmptySet.create();
	}

	@Override
	public boolean isEmpty() {
		return 0 == getAxiomCount();
	}

	@Override
	public Set<OWLAxiom> getAxioms() {
		// All axioms are logical axioms
		return new HashSet<>(getLogicalAxioms());
	}

	@Override
	public int getAxiomCount() {
		// All axioms are logical axioms
		return getLogicalAxiomCount();
	}

	@Override
	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		final Set<OWLLogicalAxiom> results = new HashSet<>();
		results.addAll(getAxioms(AxiomType.SUBCLASS_OF));
		results.addAll(getAxioms(AxiomType.EQUIVALENT_CLASSES));
		results.addAll(getAxioms(AxiomType.SUB_OBJECT_PROPERTY));
		results.addAll(getAxioms(AxiomType.DISJOINT_UNION));
		return results;
	}

	@Override
	public int getLogicalAxiomCount() {
		int totalCount = 0;
		totalCount += getAxiomCount(AxiomType.SUBCLASS_OF);
		totalCount += getAxiomCount(AxiomType.EQUIVALENT_CLASSES);
		totalCount += getAxiomCount(AxiomType.SUB_OBJECT_PROPERTY);
		totalCount += getAxiomCount(AxiomType.DISJOINT_UNION);
		return totalCount;
	}

	@Override
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
		if (AxiomType.SUBCLASS_OF.equals(axiomType)) {
			return (Set<T>) getSubClassAxioms();
		} else if (AxiomType.EQUIVALENT_CLASSES.equals(axiomType)) {
			return (Set<T>) getEquivalentClassesAxioms();
		} else if (AxiomType.SUB_OBJECT_PROPERTY.equals(axiomType)) {
			return (Set<T>) getObjectSubPropertyAxioms();
		} else if (AxiomType.DISJOINT_UNION.equals(axiomType)) {
			return (Set<T>) getDisjointUnionAxioms();
		} else {
			return new HashSet<>();
		}
	}

	private Set<OWLSubClassOfAxiom> getSubClassAxioms() {
		// All 'Primitive' concepts are sub classes of the stated parents
		final Set<OWLSubClassOfAxiom> result = new HashSet<>();

		final Set<Long> conceptIdSet = getReasonerTaxonomyBuilder().getConceptIdSet();
		for (Long conceptId : conceptIdSet) {
			if (getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
				collectSubClassAxiomsForConceptId(conceptId, result);
			}
		}

//		result.addAll(plusOntology.getAxioms(AxiomType.SUBCLASS_OF));
		return result;
	}

	private void collectSubClassAxiomsForConceptId(Long conceptId, Set<OWLSubClassOfAxiom> result) {
		final List<OWLAxiom> rawAxioms = createRawAxioms(conceptId, true);
		// TODO: calling createRawAxioms seems inefficient, attempt to optimise later

		for (final OWLAxiom axiom : rawAxioms) {
			if (AxiomType.SUBCLASS_OF.equals(axiom.getAxiomType())) {
				result.add((OWLSubClassOfAxiom) axiom);
				break;
			}
		}
	}

	private Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms() {
		// All 'Fully Defined' concepts are equivalent to the stated parents
		final Set<OWLEquivalentClassesAxiom> result = new HashSet<>();

		Set<Long> fullyDefinedConceptIds = getReasonerTaxonomyBuilder().getFullyDefinedConceptIds();
		for (Long conceptId : fullyDefinedConceptIds) {
			collectEquivalentClassesAxiomForLHS(conceptId, result);
		}

//		result.addAll(plusOntology.getAxioms(AxiomType.EQUIVALENT_CLASSES));
		return result;
	}

	private List<OWLAxiom> createRawAxioms(Long conceptId, boolean primitive) {

		final Collection<ConcreteDomainFragment> conceptDomainFragments = getReasonerTaxonomyBuilder().getConceptConcreteDomainFragments(conceptId);
		final Set<ConcreteDomainDefinition> conceptDomainDefinitions = new HashSet<>();

		for (final ConcreteDomainFragment conceptFragment : conceptDomainFragments) {
			conceptDomainDefinitions.add(new ConcreteDomainDefinition(conceptFragment));
		}

		final ConceptDefinition definition = new ConceptDefinition(conceptDomainDefinitions, conceptId, primitive, null);
		final Collection<StatementFragment> statementFragments = getReasonerTaxonomyBuilder().getStatementFragments(conceptId);

		for (final StatementFragment statementFragment : statementFragments) {
			final long statementId = statementFragment.getStatementId();
			final Collection<ConcreteDomainFragment> relationshipDomainFragments =
					getReasonerTaxonomyBuilder().getStatementConcreteDomainFragments(statementId);
			final Set<ConcreteDomainDefinition> relationshipDomainDefinitions = new HashSet<>();

			for (final ConcreteDomainFragment relationshipDomainFragment : relationshipDomainFragments) {
				relationshipDomainDefinitions.add(new ConcreteDomainDefinition(relationshipDomainFragment));
			}

			final RelationshipDefinition relationshipDefinition = new RelationshipDefinition(relationshipDomainDefinitions,
					statementFragment.getTypeId(), statementFragment.getDestinationId(), statementFragment.isDestinationNegated(),
					statementFragment.isUniversal());

			if (Concepts.IS_A_LONG == statementFragment.getTypeId()) {
				definition.addIsaDefinition(relationshipDefinition);
			} else if (LongConcepts.NEVER_GROUPED_ROLE_IDS.contains(statementFragment.getTypeId()) && 0 == statementFragment.getGroup()) {
				definition.addNeverGroupedDefinition(relationshipDefinition, statementFragment.getGroup(), statementFragment.getUnionGroup());
			} else {
				definition.addGroupDefinition(relationshipDefinition, statementFragment.getGroup(), statementFragment.getUnionGroup());
			}
		}

		final List<OWLAxiom> axioms = new ArrayList<>();
		definition.collect(manager.getOWLDataFactory(), prefixManager, axioms, new HashSet<>());
		return axioms;
	}

	private Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxioms() {
		// Attributes which are not direct descendants of the 'Concept model attribute' concept are sub-properties
		final Set<OWLSubObjectPropertyOfAxiom> result = new HashSet<>();
		for (Long attributeConceptId : getReasonerTaxonomyBuilder().getAttributeConceptIds()) {
			collectSubPropertyAxiomsForConceptId(attributeConceptId, result);
		}

//		result.addAll(plusOntology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY));
		return result;
	}

	private void collectSubPropertyAxiomsForConceptId(final long attributeConceptId, final Set<OWLSubObjectPropertyOfAxiom> result) {
		Set<Long> superTypeIds = getReasonerTaxonomyBuilder().getSuperTypeIds(attributeConceptId);
		for (Long superTypeId : superTypeIds) {
			if (getReasonerTaxonomyBuilder().conceptHasAncestor(superTypeId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG)) {
				final OWLObjectProperty subProperty = manager.getOWLDataFactory().getOWLObjectProperty(PREFIX_ROLE + attributeConceptId, prefixManager);
				final OWLObjectProperty superProperty = manager.getOWLDataFactory().getOWLObjectProperty(PREFIX_ROLE + superTypeId, prefixManager);
				final OWLSubObjectPropertyOfAxiom propertyAxiom = manager.getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
				result.add(propertyAxiom);
			}
		}
	}

	private Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms() {
		// TODO: Ask is there are ever any non-exhaustive concepts. According to Snow Owl a concept can be exhaustive depending on it's subclass definition status.
		return WritableEmptySet.create();
	}

	@Override
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, boolean includeImportsClosure) {
		// No need to consider imports closure
		return getAxioms(axiomType);
	}

	@Override
	public Set<OWLAxiom> getTBoxAxioms(boolean includeImportsClosure) {
		final Set<OWLAxiom> tboxAxioms = new HashSet<>();
		tboxAxioms.addAll(getAxioms(AxiomType.SUBCLASS_OF));
		tboxAxioms.addAll(getAxioms(AxiomType.EQUIVALENT_CLASSES));
		tboxAxioms.addAll(getAxioms(AxiomType.DISJOINT_UNION));
		return tboxAxioms;
	}

	@Override
	public Set<OWLAxiom> getABoxAxioms(boolean includeImportsClosure) {
		// Not handling individuals
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAxiom> getRBoxAxioms(boolean includeImportsClosure) {
		final Set<OWLAxiom> rboxAxioms = new HashSet<>();
		rboxAxioms.addAll(getAxioms(AxiomType.SUB_OBJECT_PROPERTY));
		return rboxAxioms;
	}

	@Override
	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
		/*
		 * TODO more optimal solution exists:
		 *
		 * - subclassof --> number of primitive classes
		 * - equivalentclasses --> number of defined classes
		 * - subobjectpropertyof --> number of role inclusions
		 * - disjoint union --> number of exhaustive classes
		 */
		return getAxioms(axiomType).size(); // + plusOntology.getAxiomCount(axiomType);
	}

	@Override
	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType, boolean includeImportsClosure) {
		// No need to consider imports closure
		return getAxiomCount(axiomType);
	}

	@Override
	public boolean containsAxiom(OWLAxiom axiom) {
		return baseContainsAxiom(axiom);// || plusOntology.containsAxiom(axiom);
	}

	private boolean baseContainsAxiom(final OWLAxiom axiom) {
		final AxiomType<?> axiomType = axiom.getAxiomType();
		if (AxiomType.SUBCLASS_OF.equals(axiomType)) {
			return containsSubClassAxiom((OWLSubClassOfAxiom) axiom);
		} else if (AxiomType.EQUIVALENT_CLASSES.equals(axiomType)) {
			return containsEquivalentClassesAxiom((OWLEquivalentClassesAxiom) axiom);
		} else if (AxiomType.SUB_OBJECT_PROPERTY.equals(axiomType)) {
			return containsObjectSubPropertyAxiom((OWLSubObjectPropertyOfAxiom) axiom);
		} else if (AxiomType.DISJOINT_UNION.equals(axiomType)) {
			return containsDisjointUnionAxiom((OWLDisjointUnionAxiom) axiom);
		} else {
			return false;
		}
	}

	private boolean containsSubClassAxiom(final OWLSubClassOfAxiom axiom) {
		final OWLClassExpression subClass = axiom.getSubClass();
		if (subClass.isAnonymous()) {
			return false;
		}

		final OWLClass namedSubClass = subClass.asOWLClass();
		if (!isConceptClass(namedSubClass)) {
			return false;
		}

		final long conceptId = getConceptId(namedSubClass);

		if (!getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
			return false;
		}

		return createRawAxioms(conceptId, true).contains(axiom);
	}

	private boolean containsEquivalentClassesAxiom(final OWLEquivalentClassesAxiom axiom) {
		for (final OWLClassExpression expression : axiom.getClassExpressions()) {
			if (expression.isAnonymous()) {
				continue;
			}

			final OWLClass namedSubClass = expression.asOWLClass();
			if (!isConceptClass(namedSubClass)) {
				continue;
			}

			final long conceptId = getConceptId(namedSubClass);
			if (getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
				continue;
			}

			if (createRawAxioms(conceptId, false).contains(axiom)) {
				return true;
			}
		}

		return false;
	}

	private boolean containsObjectSubPropertyAxiom(final OWLSubObjectPropertyOfAxiom axiom) {
		final OWLObjectPropertyExpression subProperty = axiom.getSubProperty();
		final OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
		return isConceptModelAttribute(subProperty) && isConceptModelAttribute(superProperty);
	}

	private boolean isConceptModelAttribute(final OWLObjectPropertyExpression propertyExpression) {
		if (propertyExpression.isAnonymous()) {
			return false;
		}

		final OWLObjectProperty namedSubProperty = propertyExpression.asOWLObjectProperty();
		if (!isRole(namedSubProperty)) {
			return false;
		}

		final long conceptId = getConceptId(namedSubProperty);
		return getReasonerTaxonomyBuilder().conceptHasAncestor(conceptId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG);
	}

	private boolean containsDisjointUnionAxiom(final OWLDisjointUnionAxiom axiom) {
		return false; // Same as Snow Owl
	}

	private boolean isConceptClass(final OWLClass owlClass) {
		return hasPrefix(owlClass, PREFIX_CONCEPT);
	}

	private boolean isRole(final OWLObjectProperty objectProperty) {
		return hasPrefix(objectProperty, SnomedOntologyUtils.PREFIX_ROLE);
	}

	private boolean hasPrefix(final OWLEntity entity, final String prefix) {
		return prefixManager.getShortForm(entity.getIRI()).startsWith(prefix);
	}

	private long getConceptId(final OWLEntity entity) {
		final String strippedShortForm = prefixManager.getShortForm(entity.getIRI()).substring(SnomedOntologyUtils.PREFIX_SNOMED.length());
		return Long.parseLong(strippedShortForm.split("_")[1]);
	}

	@Override
	public boolean containsAxiom(OWLAxiom axiom, boolean includeImportsClosure) {
		// No need to consider imports closure
		return containsAxiom(axiom);
	}

	@Override
	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom) {
		// Strip the annotation from the incoming axiom and check
		return containsAxiom(axiom.getAxiomWithoutAnnotations());
	}

	@Override
	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
		return containsAxiomIgnoreAnnotations(axiom);
	}

	@Override
	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom axiom, boolean includeImportsClosure) {
		return getAxiomsIgnoreAnnotations(axiom);
	}

	@Override
	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		// No GCIs are present in the ontology
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLEntity> getSignature() {
		final Set<OWLEntity> results = new HashSet<>();
		results.addAll(getClassesInSignature());
		results.addAll(getObjectPropertiesInSignature());
		return results;
	}

	@Override
	public Set<OWLEntity> getSignature(boolean includeImportsClosure) {
		return getSignature();
	}

	@Override
	public Set<OWLClass> getClassesInSignature() {
		final Set<OWLClass> result = new HashSet<>();
		if (isEmpty()) {
			return result;
		}

		final OWLDataFactory df = manager.getOWLDataFactory();
		// TODO: Remove this?
		result.add(df.getOWLClass(SnomedOntologyUtils.PREFIX_CONCEPT_UNIT_NOT_APPLICABLE, prefixManager));

		final Set<Long> conceptIdSet = getReasonerTaxonomyBuilder().getConceptIdSet();
		for (Long conceptId : conceptIdSet) {
			result.add(df.getOWLClass(PREFIX_CONCEPT + conceptId, prefixManager));
		}

//		result.addAll(plusOntology.getClassesInSignature());
		return result;
	}

	@Override
	public Set<OWLClass> getClassesInSignature(boolean includeImportsClosure) {
		return getClassesInSignature();
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		final Set<OWLObjectProperty> result = new HashSet<>();
		if (isEmpty()) {
			return result;
		}

		final OWLDataFactory df = manager.getOWLDataFactory();
		// TODO: Will these be replaced by properties from the OWL Reference Set?
		result.add(df.getOWLObjectProperty(SnomedOntologyUtils.PREFIX_HAS_UNIT, prefixManager));
		result.add(df.getOWLObjectProperty(SnomedOntologyUtils.PREFIX_ROLE_GROUP, prefixManager));
		result.add(df.getOWLObjectProperty(SnomedOntologyUtils.PREFIX_ROLE_HAS_MEASUREMENT, prefixManager));
		result.add(df.getOWLObjectProperty(SnomedOntologyUtils.PREFIX_DATA_HAS_VALUE, prefixManager));

		for (Long conceptId : getReasonerTaxonomyBuilder().getConceptIdSet()) {
			if (getReasonerTaxonomyBuilder().conceptHasAncestor(conceptId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG)) {
				result.add(df.getOWLObjectProperty(PREFIX_CONCEPT + conceptId, prefixManager));
			}
		}

//		result.addAll(plusOntology.getObjectPropertiesInSignature());
		return result;
	}
	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean includeImportsClosure) {
		return getObjectPropertiesInSignature();
	}

	@Override public Set<OWLDataProperty> getDataPropertiesInSignature() {
		// TODO Consider switching to proper data properties (dataHasValue should be here)
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature(boolean includeImportsClosure) {
		return getDataPropertiesInSignature();
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		// No individuals are present in the ontology
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature(boolean includeImportsClosure) {
		return getIndividualsInSignature();
	}

	@Override
	public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals() {
		// No anonymous individuals are present in the ontology
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature() {
		// Consider switching to proper datatypes (all used OWL datatypes should be returned here)
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature(boolean includeImportsClosure) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
		// No annotations
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity, boolean includeImportsClosure) {
		return getReferencingAxioms(owlEntity);
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual owlAnonymousIndividual) {
		// No anonymous individuals
		return WritableEmptySet.create();
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity owlEntity) {
		// There is no "punning" (overlap between entity IRIs) in the ontology
		return containsEntityInSignature(owlEntity.getIRI());
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity owlEntity, boolean includeImportsClosure) {
		return containsEntityInSignature(owlEntity);
	}

	@Override
	public boolean containsEntityInSignature(IRI entityIRI) {
		return containsClassInSignature(entityIRI)
				|| containsObjectPropertyInSignature(entityIRI)
				|| containsAnnotationPropertyInSignature(entityIRI);
	}

	@Override
	public boolean containsEntityInSignature(IRI entityIRI, boolean includeImportsClosure) {
		return containsEntityInSignature(entityIRI);
	}

	@Override
	public boolean isDeclared(OWLEntity owlEntity) {
		return false;
	}

	@Override
	public boolean isDeclared(OWLEntity owlEntity, boolean includeImportsClosure) {
		return isDeclared(owlEntity);
	}

	@Override
	public boolean containsClassInSignature(IRI owlClassIRI) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public boolean containsClassInSignature(IRI owlClassIRI, boolean includeImportsClosure) {
		return containsClassInSignature(owlClassIRI);
	}

	@Override
	public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public boolean containsObjectPropertyInSignature(IRI owlObjectPropertyIRI, boolean includeImportsClosure) {
		return containsObjectPropertyInSignature(owlObjectPropertyIRI);
	}

	@Override
	public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI) {
		return false;
	}

	@Override
	public boolean containsDataPropertyInSignature(IRI owlDataPropertyIRI, boolean includeImportsClosure) {
		return containsDataPropertyInSignature(owlDataPropertyIRI);
	}

	@Override
	public boolean containsAnnotationPropertyInSignature(IRI owlAnnotationPropertyIRI) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public boolean containsAnnotationPropertyInSignature(IRI owlAnnotationPropertyIRI, boolean includeImportsClosure) {
		return containsAnnotationPropertyInSignature(owlAnnotationPropertyIRI);
	}

	@Override
	public boolean containsIndividualInSignature(IRI owlIndividualIRI) {
		// No individuals in the ontology
		return false;
	}

	@Override
	public boolean containsIndividualInSignature(IRI owlIndividualIRI, boolean includeImportsClosure) {
		return containsIndividualInSignature(owlIndividualIRI);
	}

	@Override
	public boolean containsDatatypeInSignature(IRI owlDatatypeIRI) {
		return false;
	}

	@Override
	public boolean containsDatatypeInSignature(IRI owlDatatypeIRI, boolean includeImportsClosure) {
		return containsDatatypeInSignature(owlDatatypeIRI);
	}

	@Override
	public Set<OWLEntity> getEntitiesInSignature(IRI iri) {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public Set<OWLEntity> getEntitiesInSignature(IRI iri, boolean includeImportsClosure) {
		return getEntitiesInSignature(iri);
	}

	@Override
	public Set<OWLClassAxiom> getAxioms(OWLClass owlClass) {
		final Set<OWLClassAxiom> results = newHashSet();
		results.addAll(getSubClassAxiomsForSubClass(owlClass));
		results.addAll(getEquivalentClassesAxioms(owlClass));
		results.addAll(getDisjointUnionAxioms(owlClass));
		return results;
	}

	@Override
	public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		// The only definition axiom an object property can participate in is the SubObjectPropertyOfAxiom
		// TODO - is the above still true?
		final Set<OWLObjectPropertyAxiom> results = newHashSet();
		results.addAll(getObjectSubPropertyAxiomsForSubProperty(owlObjectPropertyExpression));
		return results;
	}

	@Override
	public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLIndividualAxiom> getAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype owlDatatype) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity owlEntity) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(OWLAnnotationSubject owlAnnotationSubject) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass owlClass) {
		if (!isConceptClass(owlClass)) {
			return WritableEmptySet.create();
		}

		final long conceptId = getConceptId(owlClass);

		if (!getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
			return WritableEmptySet.create();
		}

		final Set<OWLSubClassOfAxiom> result = new HashSet<>();
		collectSubClassAxiomsForConceptId(conceptId, result);

//		result.addAll(plusOntology.getSubClassAxiomsForSubClass(owlClass));
		return result;
	}

	@Override
	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass owlClass) {
		if (!isConceptClass(owlClass)) {
			return WritableEmptySet.create();
		}

		final long conceptId = getConceptId(owlClass);

		if (!getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
			return WritableEmptySet.create();
		}

		final Set<Long> subTypeIds = getReasonerTaxonomyBuilder().getSubTypeIds(conceptId);
		final Set<OWLSubClassOfAxiom> result = newHashSet();

		for (Long subTypeId : subTypeIds) {
			// Subtype must be _primitive_, if not, continue
			if (!getReasonerTaxonomyBuilder().isPrimitive(subTypeId)) {
				continue;
			}

			// The only supertype should be the current concept
			if (getReasonerTaxonomyBuilder().getSuperTypeIds(subTypeId).size() > 1) {
				continue;
			}

			// No non-ISA relationships can be present on the subtype
			if (getReasonerTaxonomyBuilder().getNonIsAFragments(subTypeId).size() > 0) {
				continue;
			}

			collectSubClassAxiomsForConceptId(subTypeId, result);
		}

//		result.addAll(plusOntology.getSubClassAxiomsForSuperClass(owlClass));
		return result;
	}

	@Override
	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass owlClass) {
		if (!isConceptClass(owlClass)) {
			return WritableEmptySet.create();
		}
		final long conceptId = getConceptId(owlClass);
		final Set<OWLEquivalentClassesAxiom> result = newHashSet();

		collectEquivalentClassesAxiomForRHS(conceptId, result);

		if (!getReasonerTaxonomyBuilder().isPrimitive(conceptId)) {
			collectEquivalentClassesAxiomForLHS(conceptId, result);
		}

//		result.addAll(plusOntology.getEquivalentClassesAxioms(owlClass));
		return result;
	}

	private void collectEquivalentClassesAxiomForLHS(final long conceptId, final Set<OWLEquivalentClassesAxiom> result) {
		final List<OWLAxiom> rawAxioms = createRawAxioms(conceptId, false);
		// TODO: calling createRawAxioms seems inefficient, attempt to optimise later

		for (final OWLAxiom axiom : rawAxioms) {
			if (AxiomType.EQUIVALENT_CLASSES.equals(axiom.getAxiomType())) {
				result.add((OWLEquivalentClassesAxiom) axiom);
				break;
			}
		}
	}

	private void collectEquivalentClassesAxiomForRHS(final long conceptId, final Set<OWLEquivalentClassesAxiom> result) {
		final Set<Long> subTypeIds = getReasonerTaxonomyBuilder().getSubTypeIds(conceptId);

		for (Long subTypeId : subTypeIds) {
			// Subtype must be _defined_, if not, continue
			if (getReasonerTaxonomyBuilder().isPrimitive(subTypeId)) {
				continue;
			}

			// The only supertype should be the current concept
			if (getReasonerTaxonomyBuilder().getSuperTypeIds(subTypeId).size() > 1) {
				continue;
			}

			// No non-ISA relationships can be present on the subtype
			if (getReasonerTaxonomyBuilder().getNonIsAFragments(subTypeId).size() > 0) {
				continue;
			}

			collectEquivalentClassesAxiomForLHS(subTypeId, result);
		}
	}
	@Override
	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass owlClass) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass) {

		if (!isConceptClass(owlClass)) {
			return WritableEmptySet.create();
		}

		final long conceptId = getConceptId(owlClass);

		if (!getReasonerTaxonomyBuilder().isExhaustive(conceptId)) {
			return WritableEmptySet.create();
		}

		final Set<OWLDisjointUnionAxiom> result = newHashSet();
		collectDisjointUnionAxiomForConceptId(conceptId, result);

//		result.addAll(plusOntology.getDisjointUnionAxioms(owlClass));
		return result;
	}

	private void collectDisjointUnionAxiomForConceptId(final long conceptId, final Set<OWLDisjointUnionAxiom> result) {
		final OWLClass conceptClass = manager.getOWLDataFactory().getOWLClass(PREFIX_CONCEPT + conceptId, prefixManager);
		final Set<OWLClass> disjointUnionClasses = new HashSet<>();

		final Set<Long> subTypeIds = getReasonerTaxonomyBuilder().getSubTypeIds(conceptId);

		for (Long subTypeId : subTypeIds) {
			final OWLClass disjointUnionMember = manager.getOWLDataFactory().getOWLClass(PREFIX_CONCEPT + subTypeId, prefixManager);
			disjointUnionClasses.add(disjointUnionMember);
		}

		final OWLDisjointUnionAxiom disjointUnionAxiom = manager.getOWLDataFactory().getOWLDisjointUnionAxiom(conceptClass, disjointUnionClasses);
		result.add(disjointUnionAxiom);
	}

	@Override
	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass owlClass) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(OWLObjectPropertyExpression subProperty) {

		if (subProperty.isAnonymous()) {
			return WritableEmptySet.create();
		}

		final OWLObjectProperty objectProperty = subProperty.asOWLObjectProperty();

		if (!isRole(objectProperty)) {
			return WritableEmptySet.create();
		}

		final long conceptId = getConceptId(objectProperty);

		if (!getReasonerTaxonomyBuilder().conceptHasAncestor(conceptId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG)) {
			return WritableEmptySet.create();
		}

		final Set<OWLSubObjectPropertyOfAxiom> result = newHashSet();
		collectSubPropertyAxiomsForConceptId(conceptId, result);

//		result.addAll(plusOntology.getObjectSubPropertyAxiomsForSubProperty(owlObjectPropertyExpression));
		return result;
	}

	@Override
	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(OWLObjectPropertyExpression superProperty) {
		if (superProperty.isAnonymous()) {
			return WritableEmptySet.create();
		}

		final OWLObjectProperty objectProperty = superProperty.asOWLObjectProperty();

		if (!isRole(objectProperty)) {
			return WritableEmptySet.create();
		}

		final long conceptId = getConceptId(objectProperty);

		if (!getReasonerTaxonomyBuilder().conceptHasAncestor(conceptId, Concepts.CONCEPT_MODEL_ATTRIBUTE_LONG)) {
			return WritableEmptySet.create();
		}

		final Set<Long> subTypeIds = getReasonerTaxonomyBuilder().getSubTypeIds(conceptId);
		final Set<OWLSubObjectPropertyOfAxiom> result = WritableEmptySet.create();

		for (Long subTypeId : subTypeIds) {
			collectSubPropertyAxiomsForConceptId(subTypeId, result);
		}

//		result.addAll(plusOntology.getObjectSubPropertyAxiomsForSuperProperty(superProperty));
		return result;
	}

	@Override
	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(OWLDataPropertyExpression owlDataPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(OWLDataProperty owlDataProperty) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(OWLDataPropertyExpression owlDataPropertyExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression owlClassExpression) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual owlIndividual) {
		return WritableEmptySet.create();
	}

	@Override
	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype owlDatatype) {
		return WritableEmptySet.create();
	}

	@Override
	public void accept(OWLObjectVisitor owlObjectVisitor) {
		owlObjectVisitor.visit(this);
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> owlObjectVisitorEx) {
		return owlObjectVisitorEx.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject owlObject) {
		if (owlObject == this) {
			return 0;
		}
		final OWLOntology other = (OWLOntology) owlObject;
		return ontologyID.compareTo(other.getOntologyID());
	}

	@Override
	public List<OWLOntologyChange> applyChange(final OWLOntologyChange change) throws OWLOntologyChangeException {
		return applyChanges(ImmutableList.of(change));
	}

	@Override
	public List<OWLOntologyChange> applyChanges(final List<OWLOntologyChange> changes) throws OWLOntologyChangeException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	private Taxonomy getReasonerTaxonomyBuilder() {
		return taxonomy;
	}

	public void dispose() {
//		manager.removeOntology(plusOntology);
		manager.removeOntology(this);
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
}
