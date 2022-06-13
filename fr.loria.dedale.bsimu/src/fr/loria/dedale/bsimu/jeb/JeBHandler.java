/*******************************************************************************
 * Copyright (c) 2016
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT Licence
 * which accompanies this distribution, and is available with this package
 *
 * Contributors:
 *    Arnaud Dieumegard - initial implementation
 * 
 * Content is inspired from the original JeBAction class available in the 
 * previous version of the plugin.
 *******************************************************************************/

package fr.loria.dedale.bsimu.jeb;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.console.MessageConsoleStream;
import org.rodinp.core.IRodinDB;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinCore;

import fr.loria.dedale.bsimu.ConsoleUtil;
import fr.loria.dedale.bsimu.ExceptionHandler;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class JeBHandler extends AbstractHandler {

	private IRodinProject[] iRodinProjects;
	private String relativePath = "jeb";

	/**
	 * The constructor.
	 */
	public JeBHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		MessageConsoleStream out = ConsoleUtil.getConsoleMessageStreamAndDisplayConsole();
		out.println("// Running Jeb...");

		IWorkspaceRoot iWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IRodinDB iRrodinDB = RodinCore.valueOf(iWorkspaceRoot);

		try {
			iRodinProjects = iRrodinDB
					.getChildrenOfType(IRodinProject.ELEMENT_TYPE);

			for (IRodinProject iRodinProject : iRodinProjects) {
				BSimuGlobal.initProject();
				BSimuProject bSimuProject = new BSimuProject(iRodinProject,
						relativePath, out);
				bSimuProject.translate();
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		
		return null;
	}
}
