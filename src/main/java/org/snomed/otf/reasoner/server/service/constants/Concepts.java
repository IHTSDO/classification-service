package org.snomed.otf.reasoner.server.service.constants;

import static java.lang.Long.parseLong;

public class Concepts {

	public static final String ROOT = "138875005";
	public static final Long ROOT_LONG = parseLong(ROOT);
	public static final String IS_A = "116680003";
	public static final Long IS_A_LONG = parseLong(IS_A);

	public static final String FULLY_DEFINED = "900000000000073002";
	public static final String EXISTENTIAL_RESTRICTION_MODIFIER = "900000000000451002";
	public static final String UNIVERSAL_RESTRICTION_MODIFIER = "900000000000452009";

	public static final String INFERRED_RELATIONSHIP = "900000000000011006";
	public static final String STATED_RELATIONSHIP = "900000000000010007";
	public static final String ADDITIONAL_RELATIONSHIP = "900000000000227009";
	public static final String DEFINING_RELATIONSHIP = "900000000000006009";

	// Concepts that require special care when classifying
	public static final String CONCEPT_MODEL_ATTRIBUTE = "410662002";
	public static final Long CONCEPT_MODEL_ATTRIBUTE_LONG = parseLong(CONCEPT_MODEL_ATTRIBUTE);
	public static final String PART_OF = "123005000";
	public static final String LATERALITY = "272741003";
	public static final String HAS_ACTIVE_INGREDIENT = "127489000";
	public static final Long HAS_ACTIVE_INGREDIENT_LONG = parseLong(HAS_ACTIVE_INGREDIENT);
	public static final String HAS_DOSE_FORM = "411116001";

}
