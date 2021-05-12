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

import org.eventb.core.ICarrierSet;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.rodinp.core.RodinDBException;

import bsimu.ExceptionHandler;
import bsimu.IBSimu;

public class BSimuSet extends BSimuIdentifier {

	private BSimuContext bSimuContext;
	private StringBuilder result = new StringBuilder();

	public BSimuSet(BSimuContext bSimuContext, ICarrierSet iCarrierSet)
			throws RodinDBException {
		this.bSimuContext = bSimuContext;
		this.identifierString = iCarrierSet.getIdentifierString();
		this.jebId = IBSimu.CONSTANT_PREFIX + identifierString;
		this.tag = IBSimu.CONSTANT;
		BSimuGlobal.addIdentifier(identifierString, this);
	}

	public String translate() {
		try {
			ISCAxiom[] scAxioms = bSimuContext.getSCAxioms();

			for (ISCAxiom scAxiom : scAxioms) {
				Predicate predicate = scAxiom.getPredicate(
						bSimuContext.getTypeEnvironment());
				if (predicate.getTag() == Formula.EQUAL) {
					RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;
					Expression left = relationalPredicate.getLeft();
					Expression right = relationalPredicate.getRight();
					BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.CONTEXT);

					if (jebId.equals(bSimuFormula.parseExpression(left))
							&& right.getTag() == Formula.SETEXT) {
						result.append(" = "
								+ bSimuFormula.parseExpression(right));
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		return this.jebId + result.toString() + ";" + IBSimu.NEWLINE;
	}

}