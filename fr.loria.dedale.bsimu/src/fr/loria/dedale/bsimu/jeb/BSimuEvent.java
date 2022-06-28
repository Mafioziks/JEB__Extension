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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IEvent;
import org.eventb.core.ISCAction;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCParameter;
import org.eventb.core.ast.ITypeEnvironment;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;

public class BSimuEvent {

	private BSimuMachine bSimuMachine;
	private IEvent iEvent;
	private ISCEvent iSCEvent;
	private String label;
	private String jebId;
	private boolean isInitialisation;
	private ITypeEnvironment iTypeEnvironment;
	private ArrayList<BSimuParameter> bSimuParameters;
	private ArrayList<BSimuParameter> bSimuLocalVariables;
	private ArrayList<BSimuGuard> bSimuGuards;
	private ArrayList<BSimuAction> bSimuActions;
	private boolean success = true;
	private StringBuilder result = new StringBuilder();
	private StringBuilder userTodo = new StringBuilder();

	public BSimuEvent(BSimuMachine bSimuMachine, ISCEvent iSCEvent)
			throws RodinDBException {
		this.bSimuMachine = bSimuMachine;
		this.iSCEvent = iSCEvent;
		this.iEvent = bSimuMachine.getIMachineRoot().getEvent(
				iSCEvent.getSource().getElementName());
		try {
			this.iTypeEnvironment = iSCEvent.getTypeEnvironment(
					bSimuMachine.getTypeEnvironment());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		this.label = iSCEvent.getLabel();

		if ("INITIALISATION".equals(label)) {
			this.jebId = IBSimu.EVENT_PREFIX + "init";
			this.isInitialisation = true;
		} else {
			this.jebId = IBSimu.EVENT_PREFIX + "e" + BSimuGlobal.getAutoId();
			this.isInitialisation = false;
		}

		// guards list
		BSimuGlobal.setAutoId2(1);
		this.bSimuGuards = new ArrayList<BSimuGuard>();
		for (ISCGuard iSCGuard : iSCEvent.getSCGuards()) {
			BSimuGuard bSimuGuard = new BSimuGuard(this, iSCGuard);
			this.bSimuGuards.add(bSimuGuard);
		}

		// actions list
		BSimuGlobal.setAutoId2(1);
		this.bSimuActions = new ArrayList<BSimuAction>();
		for (ISCAction iSCAction : iSCEvent.getSCActions()) {
			BSimuAction bSimuAction = new BSimuAction(this, iSCAction);
			this.bSimuActions.add(bSimuAction);
		}

		// parameters list
		ISCParameter[] iSCParameters = iSCEvent.getSCParameters();
		this.bSimuParameters = new ArrayList<BSimuParameter>();
		this.bSimuLocalVariables = new ArrayList<BSimuParameter>();
		for (ISCParameter iSCParameter : iSCParameters) {
			BSimuParameter bSimuParameter = new BSimuParameter(this,
					iSCParameter);
			if (bSimuParameter.tag == IBSimu.PARAMETER) {
				bSimuParameters.add(bSimuParameter);
				BSimuGlobal.parameters++;
			} else {
				bSimuLocalVariables.add(bSimuParameter);
			}
		}

		Collections.sort((List<BSimuParameter>) bSimuParameters,
				BSimuGlobal.comararator);
		Collections.sort((List<BSimuParameter>) bSimuLocalVariables,
				BSimuGlobal.comararator);

	}

	public BSimuMachine getBSimuMachine() {
		return bSimuMachine;
	}

	public ArrayList<BSimuGuard> getBSimuGuards() {
		return bSimuGuards;
	}

	public IEvent getIEvent() {
		return iEvent;
	}

	public ISCEvent getISCEvent() {
		return iSCEvent;
	}

	public String getLabel() {
		return label;
	}

	public String getJebId() {
		return jebId;
	}

	public ITypeEnvironment getTypeEnvironment() {
		return iTypeEnvironment;
	}

	public boolean isInitialisation() {
		return isInitialisation;
	}

	public ArrayList<BSimuParameter> getParameters() {
		return bSimuParameters;
	}

	public boolean hasParameters() {
		return (bSimuParameters.size() > 0 || bSimuLocalVariables.size() > 0) ? true
				: false;
	}

	public boolean hasGuards() {
		return bSimuGuards.size() > 0;
	}

	public boolean hasActions() {
		return bSimuActions.size() > 0;
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

	public String getParametersHtml() throws RodinDBException {
		StringBuilder html = new StringBuilder();

		for (BSimuParameter bSimuParameter : bSimuParameters) {
			String parLabel = bSimuParameter.identifierString;
			html.append("<tr eventId='" + jebId + "' name='parameter'>"
					+ IBSimu.NEWLINE);
			html.append("<td class='label'>" + parLabel + " :</td>"
					+ IBSimu.NEWLINE);
			html.append("<td><input type='text' id='" + bSimuParameter.jebId
					+ ".input' style='width:100%' value = 'get_" + parLabel
					+ "(" + this.jebId + ")' onblur='" + jebId
					+ ".testGuards()'  onkeypress='event.keyCode==13&&" + jebId
					+ ".testGuards()'></td>" + IBSimu.NEWLINE);
			html.append("<td id='" + bSimuParameter.jebId
					+ "' class='formula'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}
		for (BSimuParameter bSimuParameter : bSimuLocalVariables) {
			String parLabel = bSimuParameter.identifierString;
			html.append("<tr eventId='" + jebId + "' name='parameter'>"
					+ IBSimu.NEWLINE);
			html.append("<td class='label'>" + parLabel + " :</td>"
					+ IBSimu.NEWLINE);
			html.append("<td style='background-color:#f7f7f7'>"
					+ bSimuParameter.predicateString + "</td>" + IBSimu.NEWLINE);
			html.append("<td id='" + bSimuParameter.jebId
					+ "' class='formula'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		return html.toString();
	}

	public String getGuardsHtml() {
		StringBuilder html = new StringBuilder();

		for (BSimuGuard bSimuGuard : bSimuGuards) {
			html.append("<tr eventId='" + jebId + "' name='guard'>"
					+ IBSimu.NEWLINE);
			html.append("<td class='label'>" + bSimuGuard.getLabel()
					+ " :</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula' "
					+ "onmouseover='jeb.ui.showTip(event, "
					+ bSimuGuard.getJebId()
					+ ")' onmouseout='jeb.ui.hideTip(event)'>"
					+ bSimuGuard.getPredicateString() + "</td>"
					+ IBSimu.NEWLINE);
			html.append("<td id='" + bSimuGuard.getJebId()
					+ "' class='formula'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		return html.toString();
	}

	public String getActionsHtml() {
		StringBuilder html = new StringBuilder();

		for (BSimuAction bSimuAction : bSimuActions) {
			html.append("<tr eventId='" + jebId + "' name='action'>"
					+ IBSimu.NEWLINE);
			html.append("<td class='label'>" + bSimuAction.getLabel()
					+ " :</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula' "
					+ "onmouseover='jeb.ui.showTip(event, "
					+ bSimuAction.getJebId()
					+ ")' onmouseout='jeb.ui.hideTip(event)'>"
					+ bSimuAction.getAssignmentString() + "</td>"
					+ IBSimu.NEWLINE);
			html.append("<td id='" + bSimuAction.getJebId()
					+ "' align='center'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		return html.toString();
	}

	public void translate() {

		result.append("/* Event [" + label + "] */" + IBSimu.NEWLINE);
		result.append(jebId + " = new jeb.lang.Event( '" + jebId + "', '"
				+ label + "' );" + IBSimu.NEWLINE);
		result.append(IBSimu.NEWLINE);

		if (hasParameters()) {
			// cache parameter
			for (BSimuParameter bSimuParameter : bSimuParameters) {
				result.append(bSimuParameter.jebId
						+ " = new jeb.lang.Parameter( '" + bSimuParameter.jebId
						+ "', " + jebId + ", 0 );" + IBSimu.NEWLINE);
			}
			for (BSimuParameter bSimuParameter : bSimuLocalVariables) {
				result.append(bSimuParameter.jebId
						+ " = new jeb.lang.Parameter( '" + bSimuParameter.jebId
						+ "', " + jebId + ", 1 );" + IBSimu.NEWLINE);
			}

			// bindArguments
			result.append(jebId + ".bindArguments = function( $arg ) {"
					+ IBSimu.NEWLINE);
			// parameters
			for (BSimuParameter bSimuParameter : bSimuParameters) {
				result.append(IBSimu.TAB + bSimuParameter.prefixId
						+ ".value = jeb.ui.parseArgument("
						+ bSimuParameter.prefixId + ".domNodeInput.value);"
						+ IBSimu.NEWLINE);
			}
			// local variables
			for (BSimuParameter bSimuParameter : bSimuLocalVariables) {
				result.append(IBSimu.TAB + bSimuParameter.prefixId
						+ ".value = " + bSimuParameter.parsedResult + ";"
						+ IBSimu.NEWLINE);
			}
			result.append("};" + IBSimu.NEWLINE);
			result.append(IBSimu.NEWLINE);
		}

		// translate guards
		for (BSimuGuard bSimuGuard : bSimuGuards) {
			bSimuGuard.translate();
			result.append(bSimuGuard.getResult());
			if (!bSimuGuard.isSuccess()) {
				success = false;
				userTodo.append(bSimuGuard.getUserTodo() + IBSimu.NEWLINE);
			}
		}

		// translate actions
		for (BSimuAction bSimuAction : bSimuActions) {
			bSimuAction.translate();
			result.append(bSimuAction.getResult());
			if (!bSimuAction.isSuccess()) {
				success = false;
				userTodo.append(bSimuAction.getUserTodo() + IBSimu.NEWLINE);
			}
		}
	}

}