package org.snomed.otf.reasoner.server.service.constants;

public interface SnomedConstants {

	enum CharacteristicType {

		STATED, INFERRED, ADDITIONAL;

		public static CharacteristicType fromConceptId(String conceptId) {
			switch (conceptId) {
				case Concepts.STATED_RELATIONSHIP:
					return STATED;
				case Concepts.INFERRED_RELATIONSHIP:
					return INFERRED;
				default:
					return ADDITIONAL;
			}
		}
	}
	
}
