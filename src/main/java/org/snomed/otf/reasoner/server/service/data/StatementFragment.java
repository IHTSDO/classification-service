package org.snomed.otf.reasoner.server.service.data;

import org.snomed.otf.reasoner.server.service.constants.SnomedConstants;

public class StatementFragment implements SnomedConstants {

	private final long statementId;
	private long effectiveTime;
	private final long moduleId;
	private final long typeId;
	private final long destinationId;
	private int group;
	private final int unionGroup;
	private final boolean universal;
	private final boolean destinationNegated;
	private CharacteristicType characteristicType;

	public StatementFragment(final long typeId, final long destinationId) {
		this(-1, -1, -1, typeId, destinationId, false, 0, 0, false, null);
	}

	public StatementFragment(long statementId,
							 long effectiveTime,
							 long moduleId,
							 long typeId,
							 long destinationId,
							 boolean destinationNegated,
							 int group,
							 int unionGroup,
							 boolean universal,
							 CharacteristicType characteristicType) {
		this.statementId = statementId;
		this.effectiveTime = effectiveTime;
		this.moduleId = moduleId;
		this.typeId = typeId;
		this.destinationId = destinationId;
		this.destinationNegated = destinationNegated;
		this.group = group;
		this.unionGroup = unionGroup;
		this.universal = universal;
		this.characteristicType = characteristicType;
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

	public CharacteristicType getCharacteristicType() {
		return characteristicType;
	}

	public void setCharacteristicType(CharacteristicType characteristicType) {
		this.characteristicType = characteristicType;
	}

	public long getModuleId() {
		return moduleId;
	}

	public long getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(long effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public void setGroup(int group) {
		this.group = group;
	}

}
