package org.snomed.otf.reasoner.server.taxonomy;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.ihtsdo.otf.snomedboot.factory.ImpotentComponentFactory;
import org.snomed.otf.reasoner.server.constants.Concepts;
import org.snomed.otf.reasoner.server.data.StatementFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class TaxonomyLoader extends ImpotentComponentFactory {

	private Taxonomy taxonomy = new Taxonomy();

	@Override
	public void createConcept(String conceptId, String effectiveTime, String active, String moduleId, String definitionStatusId) {
		if (Concepts.FULLY_DEFINED.equals(definitionStatusId)) {
			taxonomy.getFullyDefinedConceptIds().add(parseLong(conceptId));
		}
	}

	@Override
	public void addRelationship(String id, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, String relationshipGroup, String typeId, String characteristicTypeId, String modifierId) {
		long source = parseLong(sourceId);
		Collection<StatementFragment> statementFragments = taxonomy.getConceptIdToStatements().computeIfAbsent(source, k -> new ArrayList<>());
		statementFragments.add(new StatementFragment(parseLong(typeId), parseLong(destinationId), parseInt(relationshipGroup)));
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
}
