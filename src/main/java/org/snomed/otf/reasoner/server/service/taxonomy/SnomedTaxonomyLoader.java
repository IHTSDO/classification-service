package org.snomed.otf.reasoner.server.service.taxonomy;

import com.google.common.base.Strings;
import org.ihtsdo.otf.snomedboot.ReleaseImportException;
import org.ihtsdo.otf.snomedboot.factory.ImpotentComponentFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.constants.SnomedConstants;
import org.snomed.otf.reasoner.server.service.data.Relationship;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static java.lang.Long.parseLong;
import static org.snomed.otf.reasoner.server.service.constants.Concepts.STATED_RELATIONSHIP;
import static org.snomed.otf.reasoner.server.service.constants.Concepts.UNIVERSAL_RESTRICTION_MODIFIER;

public class SnomedTaxonomyLoader extends ImpotentComponentFactory {

	private SnomedTaxonomy snomedTaxonomy = new SnomedTaxonomy();
	private static final String ACTIVE = "1";
	private static final String ontologyDocStart = "Prefix(:=<http://snomed.info/id/>) Ontology(";
	private static final String ontologyDocEnd = ")";

	private boolean loadingDelta;
	private int effectiveTimeNow = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
	private final OWLOntologyManager owlOntologyManager;

	private boolean stop = false;
	private Exception owlParsingExceptionThrown;
	private String owlParsingExceptionMemberId;

	public SnomedTaxonomyLoader() {
		owlOntologyManager = OWLManager.createOWLOntologyManager();
	}

	@Override
	public void newConceptState(String conceptId, String effectiveTime, String active, String moduleId, String definitionStatusId) {
		if (ACTIVE.equals(active)) {
			long id = parseLong(conceptId);
			snomedTaxonomy.getAllConceptIds().add(id);
			if (Concepts.FULLY_DEFINED.equals(definitionStatusId)) {
				snomedTaxonomy.getFullyDefinedConceptIds().add(id);
			} else {
				snomedTaxonomy.getFullyDefinedConceptIds().remove(id);
			}
		} else if (loadingDelta) {
			// Inactive concepts in the delta should be removed from the snapshot view
			long id = parseLong(conceptId);
			snomedTaxonomy.getAllConceptIds().remove(id);
			snomedTaxonomy.getFullyDefinedConceptIds().remove(id);
		}
	}

	@Override
	public void newRelationshipState(String id, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, String relationshipGroup, String typeId, String characteristicTypeId, String modifierId) {
		boolean stated = STATED_RELATIONSHIP.equals(characteristicTypeId);

		if (ACTIVE.equals(active)) {

			boolean universal = UNIVERSAL_RESTRICTION_MODIFIER.equals(modifierId);
			int unionGroup = 0;

			// TODO: is this correct? Is there a better way?
			// From Snow Owl import logic:
			// Universal "has active ingredient" relationships should be put into a union group
			if (Concepts.HAS_ACTIVE_INGREDIENT.equals(typeId) && universal) {
				unionGroup = 1;
			}

			// TODO: Destination negated is always false?
			boolean destinationNegated = false;
			int effectiveTimeInt = !Strings.isNullOrEmpty(effectiveTime) ? Integer.parseInt(effectiveTime) : effectiveTimeNow;
			snomedTaxonomy.addOrModifyRelationship(
					stated,
					parseLong(sourceId),
					new Relationship(
							parseLong(id),
							effectiveTimeInt,
							parseLong(moduleId),
							parseLong(typeId),
							parseLong(destinationId),
							destinationNegated,
							Integer.parseInt(relationshipGroup),
							unionGroup,
							universal,
							SnomedConstants.CharacteristicType.fromConceptId(characteristicTypeId)
					)
			);
		} else if (loadingDelta) {
			// Inactive relationships in the delta should be removed from the snapshot view
			snomedTaxonomy.removeRelationship(stated, sourceId, id);
		}
	}

	@Override
	public void newReferenceSetMemberState(String[] fieldNames, String id, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String... otherValues) {
		if (refsetId.equals(Concepts.OWL_AXIOM_REFERENCE_SET) && owlParsingExceptionThrown == null) {
			if (ACTIVE.equals(active)) {
				try {
					String owlExpressionString = otherValues[0];
					OWLAxiom owlAxiom = deserialiseAxiom(owlExpressionString, id);
					snomedTaxonomy.addAxiom(referencedComponentId, id, owlAxiom);
				} catch (OWLException | OWLRuntimeException e) {
					owlParsingExceptionThrown = e;
					owlParsingExceptionMemberId = id;
				}
			} else {
				// Remove the axiom from our active set
				// Match by id rather than a deserialised representation because the equals method may fail.
				snomedTaxonomy.removeAxiom(referencedComponentId, id);
			}
		}
	}

	void reportErrors() throws ReleaseImportException {
		if (owlParsingExceptionThrown != null) {
			throw new ReleaseImportException("Failed to parse OWL Axiom in reference set member '" + owlParsingExceptionMemberId + "'",
					owlParsingExceptionThrown);
		}
	}

	private OWLAxiom deserialiseAxiom(String axiomString, String memberId) throws OWLOntologyCreationException {
		OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(
				new StringDocumentSource(ontologyDocStart + axiomString + ontologyDocEnd));
		Set<OWLAxiom> axioms = owlOntology.getAxioms();
		if (axioms.size() != 1) {
			throw new IllegalArgumentException("OWL Axiom reference set member should contain a single Axiom, " +
					"found " + axioms.size() + " for member id " + memberId);
		}
		return axioms.iterator().next();
	}

	public SnomedTaxonomy getSnomedTaxonomy() {
		return snomedTaxonomy;
	}

	public void startLoadingDelta() {
		loadingDelta = true;
	}
}
