/******************************************************************************
 * @author  : Faqing Yang
 * @author  : Arnaud Dieumegard // Contribution to Error handling
 * @date    : 2013/11/29
 * @version : 0.6.5
 *
 * Copyright (c) 2013 Faqing Yang
 * Copyright (c) 2016 Arnaud Dieumegard
 * Licensed under the MIT license.
 * 
 ******************************************************************************/

package bsimu.jeb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ISCMachineRoot;
import org.eventb.core.IVariable;
import org.eventb.core.ast.ITypeEnvironment;
import org.rodinp.core.RodinDBException;

import bsimu.ExceptionHandler;
import bsimu.IBSimu;
import bsimu.Util;

public class BSimuMachine {

	private String machineName;
	private boolean abstractMachine = false;
	private String refinement;
	private ArrayList<BSimuVariable> bSimuVariables;
	private ArrayList<BSimuInvariant> bSimuInvariants;
	private ArrayList<BSimuEvent> bSimuEvents;
	private ArrayList<BSimuEvent> bSimuEventsWithInit;

	private IMachineRoot iMachineRoot;
	private ISCMachineRoot iSCMachineRoot;
	private ITypeEnvironment iTypeEnvironment;

	protected PrintWriter jsOut;
	private PrintWriter htmlOut;
	private PrintWriter userOut;
	private String jsFileName;
	private String htmlFileName;
	private String userFileName;

	public BSimuMachine(BSimuProject bSimuProject, IMachineRoot iMachineRoot)
			throws RodinDBException {

		this.machineName = iMachineRoot.getRodinFile().getBareName();
		this.iMachineRoot = iMachineRoot;
		this.iSCMachineRoot = iMachineRoot.getSCMachineRoot();
		try {
			this.iTypeEnvironment = iSCMachineRoot
					.getTypeEnvironment();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		if (iMachineRoot.getRefinesClauses().length == 0) {
			this.abstractMachine = true;
		}

		// variables list
		this.bSimuVariables = new ArrayList<BSimuVariable>();
		for (IVariable iVariable : iMachineRoot.getVariables()) {
			BSimuVariable bSimuVariable = new BSimuVariable(this, iVariable);
			this.bSimuVariables.add(bSimuVariable);
		}

		// invariant list
		BSimuGlobal.setAutoId(1);
		this.bSimuInvariants = new ArrayList<BSimuInvariant>();
		for (ISCInvariant iSCInvariant : iSCMachineRoot.getSCInvariants()) {
			if (!iSCInvariant.isTheorem()) {
				BSimuInvariant bSimuInvariant = new BSimuInvariant(this,
						iSCInvariant);
				if (bSimuInvariant.isKept()) {
					this.bSimuInvariants.add(bSimuInvariant);
				}
			}
		}

		// events list
		BSimuGlobal.setAutoId(1);
		this.bSimuEvents = new ArrayList<BSimuEvent>();
		this.bSimuEventsWithInit = new ArrayList<BSimuEvent>();
		for (ISCEvent iSCEvent : iSCMachineRoot.getSCEvents()) {
			BSimuEvent bSimuEvent = new BSimuEvent(this, iSCEvent);
			if (bSimuEvent.isInitialisation()) {
				this.bSimuEventsWithInit.add(bSimuEvent);
			} else {
				this.bSimuEventsWithInit.add(bSimuEvent);
				this.bSimuEvents.add(bSimuEvent);
			}
		}

		this.jsFileName = bSimuProject.getProjectPath() + File.separator
				+ machineName + ".js";
		this.htmlFileName = bSimuProject.getProjectPath() + File.separator
				+ machineName + "." + IBSimu.PAGE_EXT;
		this.userFileName = bSimuProject.getProjectPath() + File.separator
				+ machineName + "_user.js";
	}

	public String getMachineName() {
		return machineName;
	}

	public IMachineRoot getIMachineRoot() {
		return iMachineRoot;
	}

	public boolean isAbstractMachine() {
		return abstractMachine;
	}

	public String getRefinement() {
		return refinement;
	}

	public ISCMachineRoot getISCMachineRoot() {
		return iSCMachineRoot;
	}

	public ITypeEnvironment getTypeEnvironment() {
		return iTypeEnvironment;
	}

	public String getVariableViewHtml() throws RodinDBException {
		StringBuilder html = new StringBuilder();
		html.append("<table width='100%' cellpadding='3' cellspacing='3'>"
				+ IBSimu.NEWLINE);
		html.append("<tr style='background-color:#f0f0f0'>" + IBSimu.NEWLINE);
		html.append("<td name='variableCheckbox' style='display:none' width='1%'><input type='checkbox' id='jeb.ui.VARIABLES_CHECKED' onclick='jeb.ui.update(this.id)'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td width='10%' align='center'><b>Variable</b></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><b>Value</b></td>" + IBSimu.NEWLINE);
		html.append("</tr>");

		for (BSimuVariable bSimuVariable : bSimuVariables) {
			html.append("<tr>" + IBSimu.NEWLINE);
			html.append("<td name='variableCheckbox' style='display:none'><input type='checkbox'></td>"
					+ IBSimu.NEWLINE);
			html.append("<td class='labelDotted'>"
					+ bSimuVariable.identifierString + "</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula'><div id='" + bSimuVariable.jebId
					+ "'></div></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		html.append("<tr>" + IBSimu.NEWLINE);
		html.append("<td name='variableCheckbox' style='display:none'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><input class='small' type='button' id='jeb.ui.variableCheckbox' value='Filter' onclick='jeb.ui.filter(this.id)'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		html.append("</table>" + IBSimu.NEWLINE);
		return html.toString();
	}

	public String getInvariantViewHtml() throws RodinDBException {
		StringBuilder html = new StringBuilder();
		html.append("<table width='100%' cellpadding='3' cellspacing='3'>"
				+ IBSimu.NEWLINE);
		html.append("<tr style='background-color:#f0f0f0'>" + IBSimu.NEWLINE);
		html.append("<td name='invariantCheckbox' style='display:none' width='1%'><input type='checkbox' id='jeb.ui.INVARIANTS_CHECKED' onclick='jeb.ui.update(this.id)'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td width='10%' align='center'><b>Invariant</b></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><b>Predicate</b></td>" + IBSimu.NEWLINE);
		html.append("<td align='center'><b>Value</b></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		String previousMachine = "";
		boolean first = true;
		if (bSimuInvariants.size() > 0) {
			previousMachine = bSimuInvariants.get(0).getMachine();
		}

		for (BSimuInvariant bSimuInvariant : bSimuInvariants) {
			String currentMachine = bSimuInvariant.getMachine();
			if (!currentMachine.equals(previousMachine)) {
				previousMachine = currentMachine;
				first = true;
			}
			if (first) {
				html.append("<tr>" + IBSimu.NEWLINE);
				html.append("<td name='invariantCheckbox' style='display:none'><input type='checkbox'></td>"
						+ IBSimu.NEWLINE);
				html.append("<td class='labelDotted'><b><i>"
						+ bSimuInvariant.getMachine() + "</i></b></td>"
						+ IBSimu.NEWLINE);
				html.append("<td class='bottomDotted'></td>" + IBSimu.NEWLINE);
				html.append("<td class='bottomDotted'></td>" + IBSimu.NEWLINE);
				html.append("</tr>" + IBSimu.NEWLINE);
				first = false;
			}
			html.append("<tr>" + IBSimu.NEWLINE);
			html.append("<td name='invariantCheckbox' style='display:none'><input type='checkbox'></td>"
					+ IBSimu.NEWLINE);
			html.append("<td class='labelDotted'>" + bSimuInvariant.getLabel()
					+ "</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula' onmouseover='jeb.ui.showTip(event, "
					+ bSimuInvariant.getJebId()
					+ ")' onmouseout='jeb.ui.hideTip(event)'>"
					+ bSimuInvariant.getPredicateString()
					+ "</td>"
					+ IBSimu.NEWLINE);
			html.append("<td class='formula' id='" + bSimuInvariant.getJebId()
					+ "' align='center'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);

		}

		html.append("<tr>" + IBSimu.NEWLINE);
		html.append("<td name='invariantCheckbox' style='display:none'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><input class='small' type='button' id='jeb.ui.invariantCheckbox' value='Filter' onclick='jeb.ui.filter(this.id)'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td></td>" + IBSimu.NEWLINE);
		html.append("<td></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		html.append("</table>" + IBSimu.NEWLINE);
		return html.toString();
	}

	public String getEventViewHtml() throws RodinDBException {
		StringBuilder html = new StringBuilder();
		html.append("<table border='0' width='100%'>" + IBSimu.NEWLINE);
		html.append("<tr>" + IBSimu.NEWLINE);
		html.append("<td width='10%'></td>" + IBSimu.NEWLINE);
		html.append("<td><input class='middle' type='button' id='jeb.scheduler.testAllGuards' value='Test All Guards' onclick='jeb.scheduler.testAllGuards()'>"
				+ IBSimu.NEWLINE);
		html.append("<input class='small' type='button' id='jeb.scheduler.autoRun' value='Auto Run' onclick='jeb.scheduler.autoRun()'>"
				+ IBSimu.NEWLINE);
		html.append("<input class='small' type='button' id='jeb.scheduler.stop' value='Stop' onclick='jeb.scheduler.stop()'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td width='10%'></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		for (BSimuEvent bSimuEvent : bSimuEventsWithInit) {
			String id = bSimuEvent.getJebId();
			String label = bSimuEvent.getLabel();
			String parameterDisabled = bSimuEvent.hasParameters() ? ""
					: "disabled";
			String guardDisabled = bSimuEvent.hasGuards() ? "" : "disabled";
			String actionDisabled = bSimuEvent.hasActions() ? "" : "disabled";

			html.append("<tr>" + IBSimu.NEWLINE);
			if (bSimuEvent.isInitialisation()) {
				html.append("<td><input class='large' id='" + id
						+ "' type='button' value='" + label
						+ "' onclick='jeb.scheduler.init()'></td>"
						+ IBSimu.NEWLINE);
			} else {
				html.append("<td><input class='large' id='" + id
						+ "' type='button' value='" + label
						+ "' onclick='jeb.scheduler.onEventClick(" + id
						+ ")'></td>" + IBSimu.NEWLINE);
			}
			html.append("<td>" + IBSimu.NEWLINE);
			html.append("<input type='checkbox' "
					+ parameterDisabled
					+ " id='"
					+ id
					+ ".parameter' name='parameterCheckbox' onclick='jeb.ui.updateEvent(this.id)'> Parameters |"
					+ IBSimu.NEWLINE);
			html.append("<input type='checkbox' "
					+ guardDisabled
					+ " id='"
					+ id
					+ ".guard' name='guardCheckbox' onclick='jeb.ui.updateEvent(this.id)'> Guards |"
					+ IBSimu.NEWLINE);
			html.append("<input type='checkbox' "
					+ actionDisabled
					+ " id='"
					+ id
					+ ".action' name='actionCheckbox' onclick='jeb.ui.updateEvent(this.id)'> Actions "
					+ IBSimu.NEWLINE);
			//Champ pour gestion du choix des actions auto
			//Field for automatic action scheduler management
			html.append("<label name='probability' > | <input class='text' id='"
					+ id
					+ ".probability.input' value = '1' onkeypress='"
					+ id
					+ ".probability=parseFloat(this.value)' onblur='"
					+ id
					+ ".probability=parseFloat(this.value)'> Probability </label>"
					+ IBSimu.NEWLINE);
			html.append("<!--TODO ADD PROBA HERE-->" + IBSimu.NEWLINE);
			html.append("</td>" + IBSimu.NEWLINE);
			html.append("<td></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);

			// append parameters, guards, actions
			html.append(bSimuEvent.getParametersHtml());
			html.append(bSimuEvent.getGuardsHtml());
			html.append(bSimuEvent.getActionsHtml());
		}

		html.append("</table>" + IBSimu.NEWLINE);
		return html.toString();
	}

	private void outputJebHtml() throws RodinDBException {

		if (IBSimu.PAGE_EXT.equals("php")) {
			htmlOut.println("<?php header('Content-Type: text/html; charset=utf-8');?>");
		}

		htmlOut.println("<html>");

		// start head
		htmlOut.println("<head>");
		htmlOut.println("<meta charset='utf-8'>");
		htmlOut.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
		htmlOut.println("<title>" + machineName + "</title>");

		// style
		htmlOut.println("<link rel='stylesheet' type='text/css' href='jeb.css'>");
		htmlOut.println("</head>");

		// start body
		htmlOut.println("<body onload='jeb.ui.initMachinePage()'>");

		// animator view
		// htmlOut.println("<center><canvas id='jeb.ui.animator' style='display:none'></canvas></center>");
		htmlOut.println("<div style='position:relative'><center><span style='position:relative' id='zoneanimation'><canvas id='jeb.ui.animator' style='display:none'></canvas></span></center></div>");

		// tool-bar view
		htmlOut.println("<table width='100%' style='background-color:#f0f0f0;'>");
		htmlOut.println("<tr>");
		htmlOut.println("<td>");
		htmlOut.println("<input type='checkbox' id='jeb.ui.CODE_TIP_DISPLAY' onclick='jeb.ui.update(this.id)'> Show code tip |");
		htmlOut.println("<input type='checkbox' id='jeb.ui.PARAMETERS_DISPLAY' onclick='jeb.ui.update(this.id)'> Show parameters |");
		htmlOut.println("<input type='checkbox' id='jeb.ui.GUARDS_DISPLAY' onclick='jeb.ui.update(this.id)'> Show guards |");
		htmlOut.println("<input type='checkbox' id='jeb.ui.ACTIONS_DISPLAY' onclick='jeb.ui.update(this.id)'> Show actions |");
		htmlOut.println("<input type='checkbox' id='jeb.ui.SCENARIO_ENABLED' onclick='jeb.ui.update(this.id)'> Enable scenario |");
		//enable probability
		htmlOut.println("<input type='checkbox' id='jeb.ui.PROBABILITY_ENABLED' onclick='jeb.ui.update(this.id)'> Enable Probability |");
		htmlOut.println("Timer interval: <input type='text' style='width:60px' id='jeb.ui.TIMER_INTERVAL' value='100' onblur='jeb.ui.update(this.id)' onkeypress='event.keyCode==13&&jeb.ui.update(this.id)'> |");
		htmlOut.println("Max try arguments: <input type='text' style='width:60px' id='jeb.ui.MAX_TRY_ARGUMENTS' value='10' onblur='jeb.ui.update(this.id)'  onkeypress='event.keyCode==13&&jeb.ui.update(this.id)'>");
		htmlOut.println("</td>");
		htmlOut.println("<td><a href='index.html'>Home</a>");
		htmlOut.println("</td>");
		htmlOut.println("</tr>");
		htmlOut.println("</table>");

		// start view
		htmlOut.println("<table border='0' width='100%'>");
		htmlOut.println("<tr>");

		htmlOut.println("<td width='30%' valign='top'>");
		// Variable view
		htmlOut.println(getVariableViewHtml());
		htmlOut.println("<br>");
		// Invariant view
		htmlOut.println(getInvariantViewHtml());
		htmlOut.println("</td>");

		htmlOut.println("<td width='55%' valign='top'>");
		// Event view
		htmlOut.println(getEventViewHtml());
		htmlOut.println("</td>");

		// Console
		htmlOut.println("<td valign='top'>");
		htmlOut.println("<table width='100%' cellpadding='0' cellspacing='0'>");
		htmlOut.println("<tr>");
		htmlOut.println("<td align='center' style='background-color:#f0f0f0;height:25px'><b>Scenario</b></td>");
		htmlOut.println("</tr>");
		htmlOut.println("<tr>");
		htmlOut.println("<td valign='top'>");
		htmlOut.println("<div id='jeb.ui.console' style='height:450px;overflow:auto;background-color:#f7f7f7'></div>");
		htmlOut.println("</td>");
		htmlOut.println("</tr>");
		htmlOut.println("</table>");
		htmlOut.println("</td>");

		// end view
		htmlOut.println("</tr>");
		htmlOut.println("</table>");

		// seen contexts
		htmlOut.println("<hr>");
		htmlOut.println("Seen contexts : ");
		for (ISCInternalContext context : iSCMachineRoot.getSCSeenContexts()) {
			htmlOut.println("&nbsp;<a href='" + context.getElementName()
					+ ".html'>" + context.getElementName() + "</a> ");
		}

		// tip div
		htmlOut.println("<div id='jeb.ui.tip' style='background-color:#ffffcc;position:absolute;visibility:hidden;padding:5px'></div>");

		// defer='defer' starts here
		// include *.js
		htmlOut.println("<script type='text/javascript' src='biginteger.js'></script>");
		htmlOut.println("<script type='text/javascript' src='jeb.js'></script>");
		htmlOut.println("<script type='text/javascript' src='set.js'></script>");

		// include all seen contexts
		ISCInternalContext[] contexts = iSCMachineRoot.getSCSeenContexts();
		for (ISCInternalContext context : contexts) {
			htmlOut.println("<script type='text/javascript' src='"
					+ context.getElementName() + ".js'></script>");
		}

		// include machine.js
		htmlOut.println("<script type='text/javascript' src='"
				+ machineName + ".js'></script>");

		// include *.user.js
		htmlOut.println("<script type='text/javascript' src='jeb_user.js'></script>");
		htmlOut.println("<script type='text/javascript' src='"
				+ machineName + "_user.js'></script>");
		// defer='defer' ends here
		
		// end body
		htmlOut.println("</body>");
		htmlOut.println("</html>");
	}

	public HashMap<String, StringBuilder> getAllParametersMap()
			throws RodinDBException {
		HashMap<String, StringBuilder> parametersMap = new HashMap<String, StringBuilder>();

		for (BSimuEvent bSimuEvent : bSimuEvents) {
			for (BSimuParameter bSimuParameter : bSimuEvent.getParameters()) {
				String id = bSimuParameter.identifierString;
				if (!parametersMap.containsKey(id)) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(bSimuParameter.parsedResult);
					parametersMap.put(id, stringBuilder);
				} else {
					parametersMap.get(id).append(bSimuParameter.parsedResult);
				}
			}
		}
		return parametersMap;
	}

	public void translate() throws RodinDBException {

		// Open output file
		try {
			jsOut = new PrintWriter(jsFileName, "UTF-8");
			htmlOut = new PrintWriter(htmlFileName, "UTF-8");
			userOut = new PrintWriter(userFileName, "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open output file.");
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		// output machine html
		outputJebHtml();

		try {
			// output machine javascript
			jsOut.println("/******************************************************************************");
			jsOut.println(" * MACHINE [" + machineName + "]");
			jsOut.println(" * Generated at " + Util.now());
			jsOut.println(" * " + IBSimu.VERSION);
			jsOut.println(" ******************************************************************************/");
			jsOut.println("");

			// System.out.println("\nTranslating machine [" + machineName +
			// "]\n");

			// translate variables
			jsOut.println("/* Variables */");
			for (BSimuVariable bSimuVariable : bSimuVariables) {
				bSimuVariable.translate();
				jsOut.println(bSimuVariable.getResult());
			}
			jsOut.println("");

			// translate invariants
			jsOut.println("/* Invariants */");
			for (BSimuInvariant bSimuInvariant : bSimuInvariants) {
				bSimuInvariant.translate();
				jsOut.println(bSimuInvariant.getResult());
				if (!bSimuInvariant.isSuccess()) {
					userOut.println(bSimuInvariant.getUserTodo());
				}
			}

			// translate events
			for (BSimuEvent bSimuEvent : bSimuEventsWithInit) {
				bSimuEvent.translate();
				jsOut.println(bSimuEvent.getResult());
				if (!bSimuEvent.isSuccess()) {
					userOut.println(bSimuEvent.getUserTodo());
				}
			}

			// arguments generator
			HashMap<String, StringBuilder> parametersMap = getAllParametersMap();
			for (String parameter : parametersMap.keySet()) {
				userOut.println("// Auto-generated function: argument generator");
				String body = parametersMap.get(parameter).toString();
				if (body.contains("@TODO")) {
					userOut.println("/*");
					userOut.println("var get_" + parameter
							+ " = function( eventId ) {");
					userOut.print(parametersMap.get(parameter).toString());
					userOut.println("};");
					userOut.println("*/");
				} else {
					userOut.println("var get_" + parameter
							+ " = function( eventId ) {");
					userOut.print(parametersMap.get(parameter).toString());
					userOut.println("};");
				}
				userOut.println("");
			}
			// animator canvas
			userOut.println("jeb.animator.init = function() {");
			userOut.println("$anim.canvas.width = 1000;");
			userOut.println("$anim.canvas.height = 500;");
			userOut.println("$anim.canvas.style.display = '';");
			userOut.println("}");
			userOut.println("");
			userOut.println("jeb.animator.draw = function() {");
			userOut.println("//ADD ANIMATION HERE");
			userOut.println("}");
			userOut.println("");

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		// Close the output file
		jsOut.close();
		htmlOut.close();
		userOut.close();

	}

}