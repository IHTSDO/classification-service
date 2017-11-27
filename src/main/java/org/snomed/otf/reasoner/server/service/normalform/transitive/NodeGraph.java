package org.snomed.otf.reasoner.server.service.normalform.transitive;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NodeGraph {

	private Map<Long, Node> nodeMap = new Long2ObjectOpenHashMap<>();

	public void addParent(long conceptId, long parentId) {
		if (conceptId == parentId) return;
		Node concept = nodeMap.computeIfAbsent(conceptId, Node::new);
		Node parent = nodeMap.computeIfAbsent(parentId, Node::new);
		concept.getParents().add(parent);
	}

	public Set<Long> getAncestors(long conceptId) {
		Node node = nodeMap.get(conceptId);
		if (node == null) {
			return Collections.emptySet();
		}
		return node.getAncestorIds();
	}
}
