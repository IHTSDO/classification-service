package org.snomed.otf.reasoner.server.service.normalform.transitive;

import java.util.HashSet;
import java.util.Set;

public class Node {

	private final Long id;
	private Set<Node> parents;

	public Node(Long id) {
		this.id = id;
		parents = new HashSet<>();
	}

	public Set<Long> getAncestorIds() {
		HashSet<Long> ids = new HashSet<>();
		getAncestorIds(ids);
		ids.remove(id);
		return ids;
	}

	private void getAncestorIds(Set<Long> ids) {
		ids.add(id);
		for (Node parent : parents) {
			parent.getAncestorIds(ids);
		}
	}

	public Set<Node> getParents() {
		return parents;
	}

	public Long getId() {
		return id;
	}
}
