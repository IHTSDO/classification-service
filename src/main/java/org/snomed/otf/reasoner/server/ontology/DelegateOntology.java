package org.snomed.otf.reasoner.server.ontology;

import org.semanticweb.owlapi.model.*;
import org.snomed.otf.reasoner.server.taxonomy.Taxonomy;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DelegateOntology extends OWLObjectImpl implements OWLMutableOntology {

	private final Taxonomy taxonomy;
	private final OWLOntologyManager manager;
	private final OWLOntologyID ontologyID;

	public DelegateOntology(Taxonomy taxonomy, final OWLOntologyManager manager, final OWLOntologyID ontologyID) {
		this.manager = manager;
		this.ontologyID = ontologyID;
		this.taxonomy = taxonomy;
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
	public List<OWLOntologyChange> applyChange(OWLOntologyChange owlOntologyChange) throws OWLOntologyChangeException {
		return applyChanges(Collections.singletonList(owlOntologyChange));
	}

	@Override
	public List<OWLOntologyChange> applyChanges(List<OWLOntologyChange> list) throws OWLOntologyChangeException {
		return null;
	}

	@Override
	public Set<OWLAnnotation> getAnnotations() {
		return Collections.emptySet();
	}

	@Override
	public Set<IRI> getDirectImportsDocuments() throws UnknownOWLOntologyException {
		return Collections.emptySet();
	}

	@Override
	public Set<OWLOntology> getDirectImports() throws UnknownOWLOntologyException {
		return Collections.emptySet();
	}

	@Override
	public Set<OWLOntology> getImports() throws UnknownOWLOntologyException {
		return null;
	}

	@Override
	public Set<OWLOntology> getImportsClosure() throws UnknownOWLOntologyException {
		return Collections.emptySet();
	}

	@Override
	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		return Collections.emptySet();
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
			return new HashSet<T>();
		}
	}

	private Set<OWLSubClassOfAxiom> getSubClassAxioms() {
		return null;
	}

	private Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms() {
		return null;
	}

	private Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxioms() {
		return null;
	}

	@Override
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, boolean b) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getTBoxAxioms(boolean b) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getABoxAxioms(boolean b) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getRBoxAxioms(boolean b) {
		return null;
	}

	@Override
	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
		return 0;
	}

	@Override
	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType, boolean b) {
		return 0;
	}

	@Override
	public boolean containsAxiom(OWLAxiom owlAxiom) {
		return false;
	}

	@Override
	public boolean containsAxiom(OWLAxiom owlAxiom, boolean b) {
		return false;
	}

	@Override
	public boolean containsAxiomIgnoreAnnotations(OWLAxiom owlAxiom) {
		return false;
	}

	@Override
	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom owlAxiom) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom owlAxiom, boolean b) {
		return null;
	}

	@Override
	public boolean containsAxiomIgnoreAnnotations(OWLAxiom owlAxiom, boolean b) {
		return false;
	}

	@Override
	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		return null;
	}

	@Override
	public Set<OWLEntity> getSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLClass> getClassesInSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals() {
		return null;
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature(boolean b) {
		return null;
	}

	@Override
	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
		return null;
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity, boolean b) {
		return null;
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual owlAnonymousIndividual) {
		return null;
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity owlEntity) {
		return false;
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity owlEntity, boolean b) {
		return false;
	}

	@Override
	public boolean containsEntityInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsEntityInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean isDeclared(OWLEntity owlEntity) {
		return false;
	}

	@Override
	public boolean isDeclared(OWLEntity owlEntity, boolean b) {
		return false;
	}

	@Override
	public boolean containsClassInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsClassInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean containsObjectPropertyInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsObjectPropertyInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean containsDataPropertyInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsDataPropertyInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean containsAnnotationPropertyInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsAnnotationPropertyInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean containsIndividualInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsIndividualInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public boolean containsDatatypeInSignature(IRI iri) {
		return false;
	}

	@Override
	public boolean containsDatatypeInSignature(IRI iri, boolean b) {
		return false;
	}

	@Override
	public Set<OWLEntity> getEntitiesInSignature(IRI iri) {
		return null;
	}

	@Override
	public Set<OWLEntity> getEntitiesInSignature(IRI iri, boolean b) {
		return null;
	}

	@Override
	public Set<OWLClassAxiom> getAxioms(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLObjectPropertyAxiom> getAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLIndividualAxiom> getAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return null;
	}

	@Override
	public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype owlDatatype) {
		return null;
	}

	@Override
	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return null;
	}

	@Override
	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return null;
	}

	@Override
	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(OWLAnnotationProperty owlAnnotationProperty) {
		return null;
	}

	@Override
	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity owlEntity) {
		return null;
	}

	@Override
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(OWLAnnotationSubject owlAnnotationSubject) {
		return null;
	}

	@Override
	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass owlClass) {
		return null;
	}

	@Override
	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(OWLObjectPropertyExpression owlObjectPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(OWLDataPropertyExpression owlDataPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(OWLDataProperty owlDataProperty) {
		return null;
	}

	@Override
	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(OWLDataPropertyExpression owlDataPropertyExpression) {
		return null;
	}

	@Override
	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression owlClassExpression) {
		return null;
	}

	@Override
	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual owlIndividual) {
		return null;
	}

	@Override
	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype owlDatatype) {
		return null;
	}

	@Override
	protected int compareObjectOfSameType(OWLObject owlObject) {
		return 0;
	}

	@Override
	public void accept(OWLObjectVisitor owlObjectVisitor) {

	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> owlObjectVisitorEx) {
		return null;
	}
}
