/******************************************************************************
 * @author  : Faqing Yang
 * @date    : 2013/11/29
 * @version : 0.6.5
 *
 * Copyright (c) 2013 Faqing Yang
 * Licensed under the MIT license.
 * 
 ******************************************************************************/

package bsimu.jeb;

import org.eventb.core.ISCVariable;
import org.eventb.core.IVariable;
import org.rodinp.core.RodinDBException;

import bsimu.IBSimu;

public class BSimuVariable extends BSimuIdentifier {

	private IVariable iVariable;
	private ISCVariable iSCVariable;
	private StringBuilder result = new StringBuilder();

	public BSimuVariable(BSimuMachine bSimuMachine, IVariable iVariable)
			throws RodinDBException {
		this.iVariable = iVariable;
		this.iSCVariable = bSimuMachine.getISCMachineRoot().getSCVariable(
				iVariable.getIdentifierString());
		this.identifierString = iVariable.getIdentifierString();
		this.jebId = IBSimu.VARIABLE_PREFIX + identifierString;
		this.tag = IBSimu.GLOBAL_VARIABLE;
		BSimuGlobal.addIdentifier(identifierString, this);
	}

	public IVariable getIVariable() {
		return iVariable;
	}

	public ISCVariable getISCVariable() {
		return iSCVariable;
	}

	public String getResult() {
		return result.toString();
	}

	public void translate() {
		result.append(jebId + " = new jeb.lang.Variable( '" + jebId + "' );");
	}
}