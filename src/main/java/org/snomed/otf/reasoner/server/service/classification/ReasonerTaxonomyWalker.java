/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package org.snomed.otf.reasoner.server.service.classification;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.reasoner.server.service.ontology.OntologyService;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

public class ReasonerTaxonomyWalker {

	private static final NodeSet<OWLClass> EMPTY_NODE_SET = new OWLClassNodeSet();

	private static final Logger LOGGER = LoggerFactory.getLogger(ReasonerTaxonomyWalker.class);

	private final OWLReasoner reasoner;

	private final ReasonerTaxonomy taxonomy;

	private Set<Long> processedConceptIds;

	private final DefaultPrefixManager prefixManager;

	private boolean nothingProcessed;

	public ReasonerTaxonomyWalker(final OWLReasoner reasoner, final ReasonerTaxonomy changeSet, DefaultPrefixManager prefixManager) {
		this.reasoner = reasoner;
		this.taxonomy = changeSet;
		this.prefixManager = prefixManager;
		this.processedConceptIds = new LongOpenHashSet(600000);
	}

	public ReasonerTaxonomy walk() {
		LOGGER.info(">>> ExistingTaxonomy extraction");

		final Deque<Node<OWLClass>> nodesToProcess = new LinkedList<>();
		nodesToProcess.add(reasoner.getTopClassNode());

		// Breadth-first walk through the class hierarchy
		while (!nodesToProcess.isEmpty()) {

			final Node<OWLClass> currentNode = nodesToProcess.removeFirst();
			final NodeSet<OWLClass> nextNodeSet = walk(currentNode);

			if (!EMPTY_NODE_SET.equals(nextNodeSet)) {
				nodesToProcess.addAll(nextNodeSet.getNodes());
			}

		}

		processedConceptIds.clear();
		processedConceptIds = null;

		LOGGER.info("<<< taxonomy extraction");
		return taxonomy;
	}

	private NodeSet<OWLClass> walk(final Node<OWLClass> node) {

		if (isNodeProcessed(node)) {
			return node.isTopNode() ? reasoner.getSubClasses(node.getRepresentativeElement(), true) : EMPTY_NODE_SET;
		}

		// Check first if we are at the bottom node, as all OWL classes are superclasses of Nothing
		final boolean unsatisfiable = node.isBottomNode();
		final Set<Long> conceptIds = new LongOpenHashSet();
		final long representativeConceptId = getConceptIds(node, conceptIds);

		if (unsatisfiable) {
			registerEquivalentConceptIds(conceptIds, true);
			processedConceptIds.addAll(conceptIds);
			return EMPTY_NODE_SET;
		}

		// Check if all parents have already been visited earlier
		final NodeSet<OWLClass> parentNodeSet = reasoner.getSuperClasses(node.getRepresentativeElement(), true);

		for (final Node<OWLClass> parentNode : parentNodeSet) {

			if (!isNodeProcessed(parentNode)) {
				return EMPTY_NODE_SET;
			}
		}

		if (conceptIds.size() > 1) {
			registerEquivalentConceptIds(conceptIds, false);
		}

		final Set<Long> parentConceptIds = new LongOpenHashSet();

		for (final Node<OWLClass> parentNode : parentNodeSet) {

			// No parents if we found the Top node
			if (parentNode.isTopNode()) {
				break;
			}

			final long parentConceptId = getConceptIds(parentNode, new LongOpenHashSet());
			parentConceptIds.add(parentConceptId);
		}

		registerParentConceptIds(representativeConceptId, parentConceptIds);

		processedConceptIds.addAll(conceptIds);

		conceptIds.remove(representativeConceptId);
		parentConceptIds.clear();
		parentConceptIds.add(representativeConceptId);

		for (Long conceptId : conceptIds) {
			registerParentConceptIds(conceptId, parentConceptIds);
		}

		return computeNextNodeSet(node);
	}

	private NodeSet<OWLClass> computeNextNodeSet(final Node<OWLClass> node) {
		final NodeSet<OWLClass> subClasses = reasoner.getSubClasses(node.getRepresentativeElement(), true);

		if (!subClasses.isBottomSingleton()) {
			return subClasses;
		}

		if (nothingProcessed) {
			return EMPTY_NODE_SET;
		} else {
			nothingProcessed = true;
			return subClasses;
		}
	}

	private void registerParentConceptIds(final long child, final Set<Long> parents) {
		taxonomy.addEntry(new ReasonerTaxonomyEntry(child, parents));
	}

	private boolean isNodeProcessed(final Node<OWLClass> node) {
		for (final OWLClass owlClass : node) {
			if (!isConceptClass(owlClass)) {
				continue;
			}

			final long storageKey = getConceptId(owlClass);
			if (!processedConceptIds.contains(storageKey)) {
				return false;
			}
		}

		return true;
	}

	private long getConceptIds(final Node<OWLClass> node, final Set<Long> conceptIds) {
		for (final OWLClass owlClass : node) {
			if (!isConceptClass(owlClass)) {
				continue;
			}

			final long conceptId = getConceptId(owlClass);
			conceptIds.add(conceptId);
		}

		return conceptIds.iterator().next();
	}

	private void registerEquivalentConceptIds(final Set<Long> conceptIds, final boolean unsatisfiable) {
		if (unsatisfiable) {
			taxonomy.getUnsatisfiableConceptIds().addAll(conceptIds);
		} else {
			taxonomy.addEquivalentConceptIds(conceptIds);
		}
	}

	private boolean isConceptClass(final OWLClass owlClass) {
		return hasPrefix(owlClass, OntologyService.SNOMED_CONCEPT);
	}

	private boolean hasPrefix(final OWLClass owlClass, final String prefix) {
		return prefixManager.getShortForm(owlClass.getIRI()).startsWith(prefix);
	}

	private long getConceptId(final OWLClass owlClass) {
		final String strippedShortForm = prefixManager.getShortForm(owlClass.getIRI()).substring(OntologyService.SNOMED.length());
		return Long.parseLong(strippedShortForm.split("_")[1]);
	}
}
