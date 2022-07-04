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
import org.eventb.core.ISCAction;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.ITypeEnvironment;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;
import fr.loria.dedale.bsimu.Util;

public class BSimuAction {

	private BSimuEvent bSimuEvent;
	private String label;
	private String jebId;
	private Assignment assignment;
	private String assignmentString;
	private ITypeEnvironment iTypeEnvironment;
	private boolean success = true;
	private StringBuilder result = new StringBuilder();
	private StringBuilder userTodo = new StringBuilder();

	public BSimuAction(BSimuEvent bSimuEvent, ISCAction iSCAction)
			throws RodinDBException {
		this.bSimuEvent = bSimuEvent;
		// this.iAction = bSimuEvent.getIEvent().getAction(
		// iSCAction.getSource().getElementName());
		this.iTypeEnvironment = bSimuEvent.getTypeEnvironment();
		this.label = iSCAction.getLabel();
		this.jebId = bSimuEvent.getJebId() + ".action.a"
				+ BSimuGlobal.getAutoId2();
		try {
			this.assignment = iSCAction.getAssignment(iTypeEnvironment);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		this.assignmentString = Util.escapeHtml(assignment.toString());
	}

	public String getJebId() {
		return jebId;
	}

	public String getLabel() {
		return label;
	}

	public String getAssignmentString() {
		return assignmentString;
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
		Util.debugAssignment(assignment);

		userTodo.append("// TODO Auto-generated function stub: action ["
				+ label + "]" + IBSimu.NEWLINE);
		userTodo.append(jebId + ".assignment = function( $arg ) {"
				+ IBSimu.NEWLINE);
		userTodo.append("};" + IBSimu.NEWLINE);

		result.append(jebId + " = new jeb.lang.Action( '" + jebId + "', '"
				+ label + "', " + bSimuEvent.getJebId() + " );"
				+ IBSimu.NEWLINE);
		// result.append(jebId + ".assignmentString = \""
		// +assignmentString+"\""+ IBSimu.NEWLINE);
		result.append(jebId + ".assignment = function( $arg ) {"
				+ IBSimu.NEWLINE);

		// if (iAction.hasComment()) {
		// if (iAction.getComment().contains("@ignore")) {
		// result.append(IBSimu.TAB + "/* ignore */" + IBSimu.NEWLINE);
		// result.append("};" + IBSimu.NEWLINE);
		// result.append(IBSimu.NEWLINE);
		// return result.toString();
		// }
		// }

		try {
			BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.MACHINE);
			String parseResult = bSimuFormula.parseAssignment(assignment);
			if (bSimuFormula.isSuccess()) {
				result.append(parseResult + IBSimu.NEWLINE);
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