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
import org.eventb.core.ISCAxiom;
import org.eventb.core.ast.Predicate;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;
import fr.loria.dedale.bsimu.Util;

public class BSimuAxiom {

	private String predicateString;
	private Predicate predicate;
	private String label;
	private String jebId;
	private boolean success = true;
	private StringBuilder result = new StringBuilder();
	private StringBuilder userTodo = new StringBuilder();

	public BSimuAxiom(BSimuContext bSimuContext, ISCAxiom iSCAxiom)
			throws RodinDBException {
		try {
			this.predicate = iSCAxiom.getPredicate(bSimuContext.getTypeEnvironment());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		this.predicateString = Util.escapeHtml(predicate.toString());
		this.label = iSCAxiom.getLabel();
		this.jebId = IBSimu.AXIOM_PREFIX + "a" + BSimuGlobal.getAutoAxiomId();
	}

	public String getPredicateString() {
		return predicateString;
	}

	public String getLabel() {
		return label;
	}

	public String getJebId() {
		return jebId;
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

	public void translate() {
		// debug
		Util.debugPredicate(predicate, IBSimu.CONTEXT);

		result.append(jebId + " = new jeb.lang.Axiom( '" + jebId + "', '"
				+ label + "' );" + IBSimu.NEWLINE);
		result.append(jebId + ".predicate = function() {" + IBSimu.NEWLINE);
		try {
			BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.CONTEXT);
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

		// userTodo.append("// Todo Auto-generated function stub: axiom [" +
		// contextName + "/" + label
		// + "]" + IBSimu.NEWLINE);
		// userTodo.append(jebId + ".predicate = function() {" +
		// IBSimu.NEWLINE);
		// userTodo.append(IBSimu.TAB + "return true;" + IBSimu.NEWLINE);
		// userTodo.append("};" + IBSimu.NEWLINE);

	}

}
