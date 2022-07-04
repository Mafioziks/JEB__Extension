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

import org.eventb.core.IConstant;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.IBSimu;

public class BSimuConstant extends BSimuIdentifier {
	/*
	 * private BSimuContext bSimuContext; private IConstant iConstant; private
	 * ISCConstant iSCConstant;
	 */
	private StringBuilder result = new StringBuilder();

	public BSimuConstant(BSimuContext bSimuContext, IConstant iConstant)
			throws RodinDBException {
		this.identifierString = iConstant.getIdentifierString();
		this.jebId = IBSimu.CONSTANT_PREFIX + identifierString;
		this.tag = IBSimu.CONSTANT;
		BSimuGlobal.addIdentifier(identifierString, this);
	}

	public String getResult() {
		return result.toString();
	}

	public void translate() throws RodinDBException {
		result.append(jebId + ";" + IBSimu.NEWLINE);
	}
}
