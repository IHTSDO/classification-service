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
package org.snomed.otf.reasoner.server.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of a data type.
 * @model
 * @generated
 */
public enum DataType {
	/**
	 * Integer.
	 * @see #INTEGER_VALUE
	 * @generated
	 * @ordered
	 */
	INTEGER(0, "INTEGER", "Integer"),

	/**
	 * Decimal.
	 * @see #DECIMAL_VALUE
	 * @generated
	 * @ordered
	 */
	DECIMAL(1, "DECIMAL", "Decimal"),

	/**
	 * Boolean.
	 * @see #BOOLEAN_VALUE
	 * @generated
	 * @ordered
	 */
	BOOLEAN(2, "BOOLEAN", "Boolean"),

	/**
	 * Date.
	 * @see #DATE_VALUE
	 * @generated
	 * @ordered
	 */
	DATE(3, "DATE", "Datetime"),

	/**
	 * String.
	 * @see #STRING_VALUE
	 * @generated
	 * @ordered
	 */
	STRING(4, "STRING", "String");

	/**
	 * The '<em><b>INTEGER</b></em>' literal value.
	 * @see #INTEGER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INTEGER_VALUE = 0;

	/**
	 * The '<em><b>DECIMAL</b></em>' literal value.
	 * @see #DECIMAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DECIMAL_VALUE = 1;

	/**
	 * The '<em><b>BOOLEAN</b></em>' literal value.
	 * @see #BOOLEAN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BOOLEAN_VALUE = 2;

	/**
	 * The '<em><b>DATE</b></em>' literal value.
	 * @see #DATE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DATE_VALUE = 3;

	/**
	 * The '<em><b>STRING</b></em>' literal value.
	 * @see #STRING
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int STRING_VALUE = 4;

	/**
	 * An array of all the '<em><b>Data Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DataType[] VALUES_ARRAY =
		new DataType[] {
			INTEGER,
			DECIMAL,
			BOOLEAN,
			DATE,
			STRING,
		};

	/**
	 * A public read-only list of all the '<em><b>Data Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<DataType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Data Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DataType get(String literal) {
		for (DataType result : VALUES_ARRAY) {
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Data Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DataType getByName(String name) {
		for (DataType result : VALUES_ARRAY) {
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Data Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DataType get(int value) {
		switch (value) {
			case INTEGER_VALUE: return INTEGER;
			case DECIMAL_VALUE: return DECIMAL;
			case BOOLEAN_VALUE: return BOOLEAN;
			case DATE_VALUE: return DATE;
			case STRING_VALUE: return STRING;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DataType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * Returns with the value of the data type.
	 * @generated
	 */
	public int getValue() {
	  return value;
	}

	/**
	 * Returns with the name of the data type.
	 * @generated
	 */
	public String getName() {
	  return name;
	}

	/**
	 * Returns with the human readable name of the data type.
	 * @generated
	 */
	public String getLiteral() {
	  return literal;
	}

	/**
	 * See {@link #getLiteral()}.
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //DataType