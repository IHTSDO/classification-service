package org.snomed.otf.reasoner.server.service.taxonomy;

import org.ihtsdo.otf.snomedboot.factory.ImpotentComponentFactory;
import org.snomed.otf.reasoner.server.service.constants.Concepts;
import org.snomed.otf.reasoner.server.service.data.StatementFragment;
import org.snomed.otf.reasoner.server.service.model.SnomedOntologyUtils;

import static java.lang.Long.parseLong;
import static org.snomed.otf.reasoner.server.service.constants.Concepts.STATED_RELATIONSHIP;
import static org.snomed.otf.reasoner.server.service.constants.Concepts.UNIVERSAL_RESTRICTION_MODIFIER;

public class ExistingTaxonomyLoader extends ImpotentComponentFactory {

	private ExistingTaxonomy existingTaxonomy = new ExistingTaxonomy();
	private static final String ACTIVE = "1";
	private boolean loadingDelta;

	@Override
	public void newConceptState(String conceptId, String effectiveTime, String active, String moduleId, String definitionStatusId) {

		if (ACTIVE.equals(active)) {
			long id = parseLong(conceptId);
			existingTaxonomy.getAllConceptIds().add(id);
			if (Concepts.FULLY_DEFINED.equals(definitionStatusId)) {
				existingTaxonomy.getFullyDefinedConceptIds().add(id);
			} else {
				existingTaxonomy.getFullyDefinedConceptIds().remove(id);
			}
		} else if (loadingDelta) {
			// Inactive concepts in the delta should be removed from the snapshot view
			long id = parseLong(conceptId);
			existingTaxonomy.getAllConceptIds().remove(id);
			existingTaxonomy.getFullyDefinedConceptIds().remove(id);
		}
	}

	@Override
	public void newRelationshipState(String id, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, String relationshipGroup, String typeId, String characteristicTypeId, String modifierId) {

		// Deal with mutable changes in the delta (groupId)

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

			boolean stated = STATED_RELATIONSHIP.equals(characteristicTypeId);

			existingTaxonomy.addStatementFragment(
					stated,
					parseLong(sourceId),
					new StatementFragment(
							parseLong(id),
							parseLong(typeId),
							parseLong(destinationId),
							destinationNegated,
							Integer.parseInt(relationshipGroup),
							unionGroup,
							universal,
							SnomedOntologyUtils.translateCharacteristicType(characteristicTypeId)
							)
			);
		} else if (loadingDelta) {
			// Inactive relationships in the delta should be removed from the snapshot view
			long fragmentId = parseLong(id);
			existingTaxonomy.getStatementFragments(parseLong(sourceId)).removeIf(fragment -> fragmentId == fragment.getStatementId());
		}
	}

	public ExistingTaxonomy getExistingTaxonomy() {
		return existingTaxonomy;
	}

	public void startLoadingDelta() {
		loadingDelta = true;
	}
}
