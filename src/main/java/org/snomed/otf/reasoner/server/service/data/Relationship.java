package org.snomed.otf.reasoner.server.service.data;

import org.snomed.otf.reasoner.server.service.constants.SnomedConstants;

public class Relationship {

	private final long relationshipId;
	private int effectiveTime;
	private final long moduleId;
	private final long typeId;
	private final long destinationId;
	private int group;
	private final int unionGroup;
	private final boolean universal;
	private final boolean destinationNegated;
	private SnomedConstants.CharacteristicType characteristicType;

	public Relationship(final long typeId, final long destinationId) {
		this(-1, -1, -1, typeId, destinationId, false, 0, 0, false, null);
	}

	public Relationship(long relationshipId,
						int effectiveTime,
						long moduleId,
						long typeId,
						long destinationId,
						boolean destinationNegated,
						int group,
						int unionGroup,
						boolean universal,
						SnomedConstants.CharacteristicType characteristicType) {
		this.relationshipId = relationshipId;
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

	public long getRelationshipId() {
		return relationshipId;
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

	public SnomedConstants.CharacteristicType getCharacteristicType() {
		return characteristicType;
	}

	public void setCharacteristicType(SnomedConstants.CharacteristicType characteristicType) {
		this.characteristicType = characteristicType;
	}

	public long getModuleId() {
		return moduleId;
	}

	public int getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(int effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public void setGroup(int group) {
		this.group = group;
	}

}
