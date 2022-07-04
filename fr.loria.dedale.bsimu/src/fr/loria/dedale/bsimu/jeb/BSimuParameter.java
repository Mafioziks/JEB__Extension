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

import org.eventb.core.ISCParameter;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.RelationalPredicate;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.IBSimu;

public class BSimuParameter extends BSimuIdentifier {

	String inputId;
	String prefixId;
	String valueString;
	String parsedResult;
	String predicateString;
	Boolean generatorSupported;
	int order;

	public BSimuParameter(BSimuEvent bSimuEvent, ISCParameter iSCParameter)
			throws RodinDBException {
		this.identifierString = iSCParameter.getIdentifierString();
		this.jebId = bSimuEvent.getJebId() + ".parameter." + identifierString;
		this.inputId = jebId + ".input";
		this.prefixId = IBSimu.ARGUMENT_PREFIX + identifierString;
		this.valueString = prefixId + ".value";
		this.parsedResult = "";
		this.tag = IBSimu.PARAMETER;
		this.order = 0;
		BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.MACHINE);
		this.generatorSupported = false;
		Boolean matched = false;
		StringBuilder result = new StringBuilder();

		for (BSimuGuard bSimuGuard : bSimuEvent.getBSimuGuards()) {
			Predicate predicate = bSimuGuard.getPredicate();

			if (!(predicate instanceof RelationalPredicate)) {
				continue;
			}

			RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;
			String left = bSimuFormula.parseExpression(relationalPredicate
					.getLeft());
			String right = bSimuFormula.parseExpression(relationalPredicate
					.getRight());
			int rightTag = relationalPredicate.getRight().getTag();
			int tag = predicate.getTag();

			switch (tag) {
			case Formula.IN:
				if (valueString.equals(left)) {
					matched = true;
					this.order = bSimuGuard.getOrder();
					result.append(IBSimu.TAB + "if (eventId == "
							+ bSimuEvent.getJebId() + ") {" + IBSimu.NEWLINE);

					switch (rightTag) {
					case Formula.BOOL:
						generatorSupported = true;
						result.append(IBSimu.TAB2
								+ "return $B.BOOL.anyMember();"
								+ IBSimu.NEWLINE);
						break;

					case Formula.UPTO:
					case Formula.FREE_IDENT:
						generatorSupported = true;
						result.append(IBSimu.TAB2 + "return " + right
								+ ".anyMember();" + IBSimu.NEWLINE);
						break;

					case Formula.INTEGER:
						result.append(IBSimu.TAB2 + "// @TODO" + IBSimu.NEWLINE);
						result.append(IBSimu.TAB2
								+ "return $B.INTEGER.anyMember();"
								+ IBSimu.NEWLINE);
						break;

					case Formula.NATURAL:
						result.append(IBSimu.TAB2 + "// @TODO" + IBSimu.NEWLINE);
						result.append(IBSimu.TAB2
								+ "return $B.NATURAL.anyMember();"
								+ IBSimu.NEWLINE);
						break;

					case Formula.NATURAL1:
						result.append(IBSimu.TAB2 + "// @TODO" + IBSimu.NEWLINE);
						result.append(IBSimu.TAB2
								+ "return $B.NATURAL1.anyMember();"
								+ IBSimu.NEWLINE);
						break;

					default:
						generatorSupported = true;
						// result.append(IBSimu.TAB2 + "// @TODO" +
						// IBSimu.NEWLINE);
						result.append(IBSimu.TAB2 + "return " + right
								+ ".anyMember();" + IBSimu.NEWLINE);
						break;
					}
					result.append(IBSimu.TAB + "}" + IBSimu.NEWLINE);
					this.parsedResult = result.toString();
				}
				break;

			case Formula.NOTIN:
				if (valueString.equals(left)) {
					this.order = bSimuGuard.getOrder();
					matched = true;
					result.append(IBSimu.TAB + "if (eventId == "
							+ bSimuEvent.getJebId() + ") {" + IBSimu.NEWLINE);
					result.append(IBSimu.TAB2 + "// @TODO" + IBSimu.NEWLINE);
					result.append(IBSimu.TAB + "}" + IBSimu.NEWLINE);
					this.parsedResult = result.toString();
				}
				break;

			case Formula.EQUAL:
				// try left match
				if (valueString.equals(left)) {
					matched = true;
					this.order = bSimuGuard.getOrder();
					this.tag = IBSimu.LOCAL_VARIABLE;
					this.parsedResult = right;
					String str = bSimuGuard.getPredicateString();
					this.predicateString = str.substring(str.indexOf("=") + 1);
				} else if (valueString.equals(right)) {
					// try right match
					matched = true;
					this.order = bSimuGuard.getOrder();
					this.tag = IBSimu.LOCAL_VARIABLE;
					this.parsedResult = left;
					String str = bSimuGuard.getPredicateString();
					this.predicateString = str.substring(0,
							str.lastIndexOf("="));
				}
				break;
			}

			if (matched) {
				break;
			}
		}

		// find implicit type or other situation
		if (!matched && this.tag == IBSimu.PARAMETER) {
			this.order = 999; // set to the last position
			result.append(IBSimu.TAB + "if (eventId == "
					+ bSimuEvent.getJebId() + ") {" + IBSimu.NEWLINE);
			result.append(IBSimu.TAB2 + "// @TODO" + IBSimu.NEWLINE);
			result.append(IBSimu.TAB + "}" + IBSimu.NEWLINE);
			this.parsedResult = result.toString();
		}

	}

}