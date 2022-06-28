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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IConstant;
import org.eventb.core.IContextRoot;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCContextRoot;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ast.ITypeEnvironment;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;
import fr.loria.dedale.bsimu.Util;

public class BSimuContext {

	private String contextName;
	private PrintWriter jsOut;
	private PrintWriter htmlOut;
	private String jsFileName;
	private String htmlFileName;

	private ISCContextRoot iSCContextRoot;
	private ArrayList<BSimuSet> bSimuSets;
	private ArrayList<BSimuConstant> bSimuConstants;
	private ArrayList<BSimuAxiom> bSimuAxioms;
	private ISCAxiom[] iSCAxioms;
	private ITypeEnvironment iTypeEnvironment;
	private boolean abstractContext = false;

	public BSimuContext(String bSimuProjectPath, IContextRoot contextRoot)
			throws RodinDBException {
		this.contextName = contextRoot.getRodinFile().getBareName();
		this.jsFileName = bSimuProjectPath + File.separator + contextName
				+ ".js";
		this.htmlFileName = bSimuProjectPath + File.separator + contextName
				+ "." + IBSimu.PAGE_EXT;
		this.iSCContextRoot = contextRoot.getSCContextRoot();
		try {
			this.iTypeEnvironment = iSCContextRoot
					.getTypeEnvironment();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		
		if (iSCContextRoot.getAbstractSCContexts().length == 0) {
			abstractContext = true;
		}

		// set list
		this.bSimuSets = new ArrayList<BSimuSet>();
		for (ICarrierSet iCarrierSet : contextRoot.getCarrierSets()) {
			BSimuSet bSimuSet = new BSimuSet(this, iCarrierSet);
			this.bSimuSets.add(bSimuSet);
			BSimuGlobal.sets++;
		}

		// constant list
		this.bSimuConstants = new ArrayList<BSimuConstant>();
		for (IConstant iConstant : contextRoot.getConstants()) {
			BSimuConstant bSimuConstant = new BSimuConstant(this, iConstant);
			this.bSimuConstants.add(bSimuConstant);
			BSimuGlobal.constants++;
		}

		// axiom list
		this.iSCAxioms = iSCContextRoot.getSCAxioms();
		this.bSimuAxioms = new ArrayList<BSimuAxiom>();
		for (ISCAxiom iSCAxiom : iSCAxioms) {
			BSimuAxiom bSimuAxiom = new BSimuAxiom(this, iSCAxiom);
			this.bSimuAxioms.add(bSimuAxiom);
		}
	}

	public boolean isAbstractContext() {
		return abstractContext;
	}

	public String getContextName() {
		return contextName;
	}

	public ISCContextRoot getSCContextRoot() {
		return iSCContextRoot;
	}

	public ITypeEnvironment getTypeEnvironment() {
		return iTypeEnvironment;
	}

	public ISCAxiom[] getSCAxioms() {
		return iSCAxioms;
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
		htmlOut.println("<title>" + contextName + "</title>");

		// style
		htmlOut.println("<link rel='stylesheet' type='text/css' href='jeb.css'>");
		htmlOut.println("</head>");

		// start body
		htmlOut.println("<body onload='jeb.ui.initContextPage()'>");

		htmlOut.println("<center><font size='5'>Context: <i>" + contextName
				+ "</i></font></center>");

		// extends contexts
		htmlOut.println("<table width='100%'><tr>");
		htmlOut.println("<td width='5%'><b>Extends</b>:</td>");
		htmlOut.println("<td>");
		ISCInternalContext[] contexts = iSCContextRoot.getAbstractSCContexts();
		for (ISCInternalContext context : contexts) {
			htmlOut.println("&nbsp;<a href='" + context.getElementName()
					+ ".html'>" + context.getElementName() + "</a> ");
		}
		htmlOut.println("</td>");
		htmlOut.println("<td width='15%'><input type='checkbox' id='jeb.ui.CODE_TIP_DISPLAY' onclick='jeb.ui.update(this.id)'> Show code tip </td>");
		htmlOut.println("<td width='5%'><a href='index.html'>Home</a></td>");
		htmlOut.println("</tr></table>");

		StringBuilder html = new StringBuilder();

		// Set view
		if (bSimuSets.size() > 0) {
			html.append("<table width='100%' cellpadding='3' cellspacing='3'>"
					+ IBSimu.NEWLINE);

			html.append("<tr style='background-color:#f0f0f0'>"
					+ IBSimu.NEWLINE);
			html.append("<td width='1%'></td>" + IBSimu.NEWLINE);
			html.append("<td width='10%' align='center'><b>Set</b></td>"
					+ IBSimu.NEWLINE);
			html.append("<td align='center'><b>Value</b></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);

			for (BSimuSet bSimuSet : bSimuSets) {
				html.append("<tr>" + IBSimu.NEWLINE);
				html.append("<td></td>" + IBSimu.NEWLINE);
				html.append("<td class='labelDotted'>"
						+ bSimuSet.identifierString + "</td>" + IBSimu.NEWLINE);
				html.append("<td class='formula' id='" + bSimuSet.jebId
						+ "'></td>" + IBSimu.NEWLINE);
				html.append("</tr>" + IBSimu.NEWLINE);
			}

			html.append("</table>" + IBSimu.NEWLINE);
		}

		// Constant view
		html.append("<table width='100%' cellpadding='3' cellspacing='3'>"
				+ IBSimu.NEWLINE);

		html.append("<tr style='background-color:#f0f0f0'>" + IBSimu.NEWLINE);
		html.append("<td width='1%'></td>" + IBSimu.NEWLINE);
		html.append("<td width='10%' align='center'><b>Constant</b></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><b>Value</b></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		for (BSimuConstant bSimuConstant : bSimuConstants) {
			html.append("<tr>" + IBSimu.NEWLINE);
			html.append("<td></td>" + IBSimu.NEWLINE);
			html.append("<td class='labelDotted'>"
					+ bSimuConstant.identifierString + "</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula' id='" + bSimuConstant.jebId
					+ "'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		html.append("</table>" + IBSimu.NEWLINE);

		htmlOut.println(html);

		// Axioms view
		html = new StringBuilder();
		html.append("<table width='100%' cellpadding='3' cellspacing='3'>"
				+ IBSimu.NEWLINE);

		html.append("<tr>" + IBSimu.NEWLINE);
		html.append("<td></td>" + IBSimu.NEWLINE);
		html.append("<td><input class='middle' type='button' id='jeb.ui.axiomCheckbox' value='Check Axiom' onclick='jeb.scheduler.checkAxioms()'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td><input class='middle' type='button' value='Clear Result' onclick='jeb.scheduler.clearAxiomsEvaluation()'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		html.append("<tr style='background-color:#f0f0f0'>" + IBSimu.NEWLINE);
		html.append("<td name='axiomCheckbox' width='1%'><input type='checkbox' id='jeb.ui.AXIOMS_CHECKED' onclick='jeb.ui.update(this.id)'></td>"
				+ IBSimu.NEWLINE);
		html.append("<td width='10%' align='center'><b>Axiom</b></td>"
				+ IBSimu.NEWLINE);
		html.append("<td align='center'><b>Predicate</b></td>" + IBSimu.NEWLINE);
		html.append("<td align='center'><b>Value</b></td>" + IBSimu.NEWLINE);
		html.append("</tr>" + IBSimu.NEWLINE);

		for (BSimuAxiom bSimuAxiom : bSimuAxioms) {
			html.append("<tr>" + IBSimu.NEWLINE);
			html.append("<td name='axiomCheckbox'><input type='checkbox'></td>"
					+ IBSimu.NEWLINE);
			html.append("<td class='labelDotted'>" + bSimuAxiom.getLabel()
					+ "</td>" + IBSimu.NEWLINE);
			html.append("<td class='formula' onmouseover='jeb.ui.showTip(event, "
					+ bSimuAxiom.getJebId()
					+ ")' onmouseout='jeb.ui.hideTip(event)'>"
					+ bSimuAxiom.getPredicateString()
					+ "</td>"
					+ IBSimu.NEWLINE);
			html.append("<td class='formula' id='" + bSimuAxiom.getJebId()
					+ "' align='center'></td>" + IBSimu.NEWLINE);
			html.append("</tr>" + IBSimu.NEWLINE);
		}

		html.append("</table>" + IBSimu.NEWLINE);

		htmlOut.println(html);

		// tip div
		htmlOut.println("<div id='jeb.ui.tip' style='background-color:#ffffcc;position:absolute;visibility:hidden;padding:5px'></div>");

		// include *.js
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='fabric.min.js'></script>");
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='biginteger.js'></script>");
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='jeb.js'></script>");
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='set.js'></script>");

		// include all extended contexts
		for (ISCInternalContext context : contexts) {
			htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='"
					+ context.getElementName() + ".js'></script>");
		}

		// include context.js
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='"
				+ contextName + ".js'></script>");

		// include *.user.js
		htmlOut.println("<script type='text/javascript'"/* defer='defer' */+ " src='jeb_user.js'></script>");

		// end body
		htmlOut.println("</body>");
		htmlOut.println("</html>");
	}

	public void translate() throws RodinDBException {

		// Open output file
		try {
			jsOut = new PrintWriter(jsFileName, "UTF-8");
			htmlOut = new PrintWriter(htmlFileName, "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open output file.");
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		// output context html
		outputJebHtml();

		try {

			jsOut.println("/******************************************************************************");
			jsOut.println(" * CONTEXT [" + contextName + "]");
			jsOut.println(" * Generated at " + Util.now());
			jsOut.println(" * " + IBSimu.VERSION);
			jsOut.println(" ******************************************************************************/");
			jsOut.println("");

			// translation of sets
			jsOut.println("/* Sets */");
			for (BSimuSet bSimuSet : bSimuSets) {
				jsOut.println(bSimuSet.translate());
			}
			jsOut.println("");

			// translation of constants
			jsOut.println("/* Constants */");
			for (BSimuConstant bSimuConstant : bSimuConstants) {
				bSimuConstant.translate();
				jsOut.println(bSimuConstant.getResult());
			}
			jsOut.println("");

			// translation of axioms
			jsOut.println("/* Axioms */");
			for (BSimuAxiom bSimuAxiom : bSimuAxioms) {
				bSimuAxiom.translate();
				jsOut.println(bSimuAxiom.getResult());
				// if (!bSimuAxiom.isSuccess()) {
				// userOut.println(bSimuAxiom.getUserTodo());
				// }
			}
			jsOut.println("");

			// cache constants name
			if (bSimuSets.size() > 0 || bSimuConstants.size() > 0) {
				jsOut.println("/* Cache constants */");
			}
			for (BSimuSet bSimuSet : bSimuSets) {
				jsOut.println("jeb.__constants.push( '" + bSimuSet.jebId
						+ "' );");
			}
			for (BSimuConstant bSimuConstant : bSimuConstants) {
				jsOut.println("jeb.__constants.push( '" + bSimuConstant.jebId
						+ "' );");
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		// Close the output file
		jsOut.close();
		htmlOut.close();

	}

}