package org.snomed.otf.reasoner.server.service.ontology;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import java.util.HashSet;
import java.util.Set;

public class ExpressionGroup {

	private Set<OWLClassExpression> members;
	private OWLObjectSomeValuesFrom hasActiveIngredientClassExpression;

	public ExpressionGroup() {
		members = new HashSet<>();
	}

	void addMember(OWLClassExpression owlClassExpression) {
		members.add(owlClassExpression);
	}

	public Set<OWLClassExpression> getMembers() {
		return members;
	}

	public void setHasActiveIngredientClassExpression(OWLObjectSomeValuesFrom hasActiveIngredientClassExpression) {
		this.hasActiveIngredientClassExpression = hasActiveIngredientClassExpression;
	}

	public OWLObjectSomeValuesFrom getHasActiveIngredientClassExpression() {
		return hasActiveIngredientClassExpression;
	}
}
