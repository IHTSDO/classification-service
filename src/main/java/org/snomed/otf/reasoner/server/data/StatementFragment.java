package org.snomed.otf.reasoner.server.data;

public class StatementFragment {

	private final long typeId;
	private final long destinationId;
	private final int group;

	public StatementFragment(long typeId, long destinationId, int group) {
		this.typeId = typeId;
		this.destinationId = destinationId;
		this.group = group;
	}

	public long getTypeId() {
		return typeId;
	}

	public long getDestinationId() {
		return destinationId;
	}

	public int getGroup() {
		return group;
	}
}
