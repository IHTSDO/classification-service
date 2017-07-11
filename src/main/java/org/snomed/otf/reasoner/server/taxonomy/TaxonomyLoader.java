package org.snomed.otf.reasoner.server.taxonomy;

import org.ihtsdo.otf.snomedboot.factory.ImpotentComponentFactory;
import org.snomed.otf.reasoner.server.constants.Concepts;
import org.snomed.otf.reasoner.server.data.StatementFragment;

import static java.lang.Long.parseLong;
import static org.snomed.otf.reasoner.server.constants.Concepts.STATED_RELATIONSHIP;
import static org.snomed.otf.reasoner.server.constants.Concepts.UNIVERSAL_RESTRICTION_MODIFIER;

public class TaxonomyLoader extends ImpotentComponentFactory {

	private Taxonomy taxonomy = new Taxonomy();
	private static final String ACTIVE = "1";

	@Override
	public void newConceptState(String conceptId, String effectiveTime, String active, String moduleId, String definitionStatusId) {
		if (ACTIVE.equals(active)) {
			long id = parseLong(conceptId);
			taxonomy.getAllConceptIds().add(id);
			if (Concepts.FULLY_DEFINED.equals(definitionStatusId)) {
				taxonomy.getFullyDefinedConceptIds().add(id);
			}
		}
	}

	@Override
	public void newRelationshipState(String id, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, String relationshipGroup, String typeId, String characteristicTypeId, String modifierId) {
		if (ACTIVE.equals(active) && STATED_RELATIONSHIP.equals(characteristicTypeId)) {

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

			taxonomy.addStatementFragment(
					parseLong(sourceId),
					new StatementFragment(
							parseLong(id),
							parseLong(typeId),
							parseLong(destinationId),
							destinationNegated,
							Integer.parseInt(relationshipGroup),
							unionGroup,
							universal)
			);
		}
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

}
