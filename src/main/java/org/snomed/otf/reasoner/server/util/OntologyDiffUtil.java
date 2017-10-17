package org.snomed.otf.reasoner.server.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class OntologyDiffUtil {

	public void diffOntologyFiles(File file1, File file2) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology1 = manager.loadOntologyFromOntologyDocument(file1);
		OWLOntology owlOntology2 = manager.loadOntologyFromOntologyDocument(file2);

		Set<OWLAxiom> axioms1 = owlOntology1.getAxioms();
		Set<OWLAxiom> axioms2 = owlOntology2.getAxioms();

		Map<AxiomType, AtomicInteger> foundTypeCount = new HashMap<>();
		Map<AxiomType, AtomicInteger> missingTypeCount = new HashMap<>();
		for (OWLAxiom owlAxiom1 : axioms1) {
			if (axioms2.contains(owlAxiom1)) {
				foundTypeCount.computeIfAbsent(owlAxiom1.getAxiomType(), t -> new AtomicInteger()).incrementAndGet();
			} else {
				int i = missingTypeCount.computeIfAbsent(owlAxiom1.getAxiomType(), t -> new AtomicInteger()).incrementAndGet();
				if (i < 100) {
					System.out.println("Not found - " + owlAxiom1);
				}
			}
		}
		System.out.println("Found " + foundTypeCount);
		System.out.println("Missing " + missingTypeCount);
	}
}
