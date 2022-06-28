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

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ISCGuard;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;
import fr.loria.dedale.bsimu.Util;

public class BSimuGuard {

	private BSimuEvent bSimuEvent;
	private ISCGuard iSCGuard;
	private String label;
	private String jebId;
	private String predicateString;
	private boolean success = true;
	private StringBuilder result = new StringBuilder();
	private StringBuilder userTodo = new StringBuilder();
	private Predicate predicate;
	private int order;
	private int tag;

	public BSimuGuard(BSimuEvent bSimuEvent, ISCGuard iSCGuard)
			throws RodinDBException {
		this.bSimuEvent = bSimuEvent;
		this.iSCGuard = iSCGuard;
		try {
			this.predicate = iSCGuard.getPredicate(bSimuEvent.getTypeEnvironment());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		// this.iGuard = bSimuEvent.getIEvent().getGuard(
		// iSCGuard.getSource().getElementName());
		this.label = iSCGuard.getLabel();
		this.order = BSimuGlobal.getAutoId2();
		this.tag = IBSimu.NORMAL_GUARD;
		this.jebId = bSimuEvent.getJebId() + ".guard.g" + order;
		this.predicateString = Util.escapeHtml(predicate.toString());
	}

	public ISCGuard getISCGuard() {
		return iSCGuard;
	}

	public String getLabel() {
		return label;
	}

	public String getJebId() {
		return jebId;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public String getPredicateString() {
		return predicateString;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getResult() {
		return result.toString();
	}

	public String getUserTodo() {
		return userTodo.toString();
	}

	public int getOrder() {
		return this.order;
	}

	public void translate() {
		// debug
		Util.debugPredicate(predicate, IBSimu.MACHINE);

		FreeIdentifier[] freeIdentifiers = predicate.getFreeIdentifiers();
		for (FreeIdentifier freeIdentifier : freeIdentifiers) {
			String key = freeIdentifier.toString();
			if (BSimuGlobal.getTag(key) == IBSimu.UNDEFINE) {
				this.tag = IBSimu.PARAMETERIZED_GUARD;
				break;
			}
		}

		userTodo.append("// TODO Auto-generated function stub: guard [" + label
				+ "]" + IBSimu.NEWLINE);
		userTodo.append(jebId + ".predicate = function( $arg ) {"
				+ IBSimu.NEWLINE);
		userTodo.append(IBSimu.TAB + "return true;" + IBSimu.NEWLINE);
		userTodo.append("};" + IBSimu.NEWLINE);

		result.append(jebId + " = new jeb.lang.Guard( '" + jebId + "', '"
				+ label + "', " + bSimuEvent.getJebId() + ", " + tag + " );"
				+ IBSimu.NEWLINE);
		result.append(jebId + ".predicate = function( $arg ) {"
				+ IBSimu.NEWLINE);

		try {
			BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.MACHINE);

			String parseResult = bSimuFormula.parsePredicate(predicate);
			if (bSimuFormula.isSuccess()) {
				result.append(IBSimu.TAB + "return " + parseResult + ";"
						+ IBSimu.NEWLINE);
			} else {
				success = false;
			}

		} catch (Exception e) {
			success = false;
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		result.append("};" + IBSimu.NEWLINE);
		result.append(IBSimu.NEWLINE);
	}

}