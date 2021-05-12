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

import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IEventBProject;
import org.eventb.core.IInvariant;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.Predicate;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import bsimu.ExceptionHandler;
import bsimu.IBSimu;
import bsimu.Util;

public class BSimuInvariant {

	private IInvariant iInvariant;
	private String predicateString;
	private Predicate predicate;
	private String machineName;
	private String label;
	private String jebId;
	private boolean success = true;
	private StringBuilder result = new StringBuilder();
	private StringBuilder userTodo = new StringBuilder();
	private boolean kept;

	public BSimuInvariant(BSimuMachine bSimuMachine, ISCInvariant iSCInvariant)
			throws RodinDBException {
		String machineName = iSCInvariant.getSource().getParent()
				.getElementName();
		IRodinProject iRodinProject = iSCInvariant.getRodinProject();
		IEventBProject iEventBProject = (IEventBProject) iRodinProject
				.getAdapter(IEventBProject.class);
		IMachineRoot iMachineRoot = iEventBProject.getMachineRoot(machineName);
		this.iInvariant = iMachineRoot.getInvariant(iSCInvariant.getSource()
				.getElementName());
		try {
			this.predicate = iSCInvariant.getPredicate(bSimuMachine.getTypeEnvironment());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		this.predicateString = Util.escapeHtml(predicate.toString());
		this.machineName = iInvariant.getRoot().getElementName();
		this.label = iInvariant.getLabel();
		this.jebId = IBSimu.INVARIANT_PREFIX + "i" + BSimuGlobal.getAutoId();
		this.kept = true;

		FreeIdentifier[] identifiers = predicate.getFreeIdentifiers();
		for (FreeIdentifier freeIdentifier : identifiers) {
			String identifier = freeIdentifier.toString();
			if (BSimuGlobal.getTag(identifier) == IBSimu.UNDEFINE) {
				this.kept = false;
				break;
			}
		}
	}

	public boolean isKept() {
		return this.kept;
	}

	public String getPredicateString() {
		return predicateString;
	}

	public String getMachine() {
		return machineName;
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
		Util.debugPredicate(predicate, IBSimu.MACHINE);

		userTodo.append("// TODO Auto-generated function stub: invariant ["
				+ machineName + "/" + label + "]" + IBSimu.NEWLINE);
		userTodo.append(jebId + ".predicate = function() {" + IBSimu.NEWLINE);
		userTodo.append(IBSimu.TAB + "return true;" + IBSimu.NEWLINE);
		userTodo.append("};" + IBSimu.NEWLINE);

		result.append(jebId + " = new jeb.lang.Invariant( '" + jebId + "', '"
				+ label + "' );" + IBSimu.NEWLINE);
		result.append(jebId + ".predicate = function() {" + IBSimu.NEWLINE);

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
			System.out.println("\nError in invariant " + machineName + "/"
					+ this.label);
			System.out.println(predicate);
			System.out.println(predicate.getSyntaxTree());
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

		result.append("};" + IBSimu.NEWLINE);

	}

}
