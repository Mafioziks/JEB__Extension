/******************************************************************************
 * @author  : Faqing Yang
 * @date    : 2013/11/29
 * @version : 0.6.5
 *
 * Copyright (c) 2013 Faqing Yang
 * Licensed under the MIT license.
 * 
 ******************************************************************************/

package fr.loria.dedale.bsimu.jeb;

import java.util.Comparator;
import java.util.HashMap;

import fr.loria.dedale.bsimu.IBSimu;

public class BSimuGlobal {

	private static HashMap<String, BSimuIdentifier> constantIdentifiers = new HashMap<String, BSimuIdentifier>();
	private static HashMap<String, BSimuIdentifier> variableIdentifiers = new HashMap<String, BSimuIdentifier>();
	private static int autoId = 1;
	private static int autoId2 = 1;
	private static int autoAxiomId = 1;

	static int INTEGER_VERSION = 2; // 0: NUMBER, 1: STRING, 2: BIGINTEGER

	static int sets = 0;
	static int constants = 0;
	static int parameters = 0;
	static Comparator<BSimuParameter> comararator = new Comparator<BSimuParameter>() {
		public int compare(BSimuParameter a, BSimuParameter b) {
			return a.order - b.order;
		}
	};

	public static void initProject() {
		BSimuGlobal.INTEGER_VERSION = 2;
		BSimuGlobal.constantIdentifiers.clear();
		BSimuGlobal.autoId = 1;
		BSimuGlobal.autoId2 = 1;
		BSimuGlobal.sets = 0;
		BSimuGlobal.constants = 0;
		BSimuGlobal.parameters = 0;
	}

	public static void initMachine() {
		BSimuGlobal.variableIdentifiers.clear();
	}

	public static int getAutoId() {
		return autoId++;
	}

	public static void setAutoId(int autoId) {
		BSimuGlobal.autoId = autoId;
	}

	public static int getAutoId2() {
		return autoId2++;
	}

	public static void setAutoAxiomId(int autoAxiomId) {
		BSimuGlobal.autoAxiomId = autoAxiomId;
	}

	public static int getAutoAxiomId() {
		return autoAxiomId++;
	}

	public static void setAutoId2(int autoId) {
		BSimuGlobal.autoId2 = autoId;
	}

	// add identifier by key without prefix
	public static void addIdentifier(String key, BSimuIdentifier identifier) {
		if (identifier.tag == IBSimu.CONSTANT) {
			if (!constantIdentifiers.containsKey(key)) {
				constantIdentifiers.put(key, identifier);
			}
		} else {
			if (!variableIdentifiers.containsKey(key)) {
				variableIdentifiers.put(key, identifier);
			}
		}
	}

	public static int getTag(String key) {
		BSimuIdentifier bSimuIdentifier = constantIdentifiers.get(key);
		if (bSimuIdentifier == null) {
			bSimuIdentifier = variableIdentifiers.get(key);
		}
		if (bSimuIdentifier != null) {
			return bSimuIdentifier.tag;
		}
		return IBSimu.UNDEFINE;
	}
}
