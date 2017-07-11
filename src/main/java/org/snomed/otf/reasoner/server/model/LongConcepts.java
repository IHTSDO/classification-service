/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snomed.otf.reasoner.server.model;

import com.google.common.collect.Sets;
import org.snomed.otf.reasoner.server.constants.Concepts;

import java.util.Collections;
import java.util.Set;

/**
 * A SNOMED CT concept ID collection which holds constants in primitive long form.
 */
public interface LongConcepts {

	long HAS_DOSE_FORM_ID = Long.parseLong(Concepts.HAS_DOSE_FORM);
	long LATERALITY_ID = Long.parseLong(Concepts.LATERALITY);
	long PART_OF_ID = Long.parseLong(Concepts.PART_OF);

	long NOT_APPLICABLE_ID = -1L;
	long IS_A_ID = Long.parseLong(Concepts.IS_A);
	long CONCEPT_MODEL_ATTRIBUTE_ID = Long.parseLong(Concepts.CONCEPT_MODEL_ATTRIBUTE);
	long HAS_ACTIVE_INGREDIENT_ID = Long.parseLong(Concepts.HAS_ACTIVE_INGREDIENT);
	long EXISTENTIAL_RESTRICTION_MODIFIER_ID = Long.parseLong(Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
	long UNIVERSAL_RESTRICTION_MODIFIER_ID = Long.parseLong(Concepts.UNIVERSAL_RESTRICTION_MODIFIER);
	Set<Long> NEVER_GROUPED_ROLE_IDS = Sets.newHashSet(PART_OF_ID, LATERALITY_ID, HAS_DOSE_FORM_ID, HAS_ACTIVE_INGREDIENT_ID);

}
