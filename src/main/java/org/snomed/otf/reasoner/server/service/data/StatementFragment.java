package org.snomed.otf.reasoner.server.service.data;

public class StatementFragment {

	private final long statementId;
	private final long typeId;
	private final long destinationId;
	private final int group;
	private final int unionGroup;
	private final boolean universal;
	private final boolean destinationNegated;

	public StatementFragment(final long typeId, final long destinationId) {
		this(-1, typeId, destinationId, false, 0, 0, false);
	}

	public StatementFragment(long statementId,
							 long typeId,
							 long destinationId,
							 boolean destinationNegated,
							 int group,
							 int unionGroup,
							 boolean universal) {
		this.statementId = statementId;
		this.typeId = typeId;
		this.destinationId = destinationId;
		this.destinationNegated = destinationNegated;
		this.group = group;
		this.unionGroup = unionGroup;
		this.universal = universal;
	}

	public long getStatementId() {
		return statementId;
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

	public int getUnionGroup() {
		return unionGroup;
	}

	public boolean isUniversal() {
		return universal;
	}

	public boolean isDestinationNegated() {
		return destinationNegated;
	}

}
