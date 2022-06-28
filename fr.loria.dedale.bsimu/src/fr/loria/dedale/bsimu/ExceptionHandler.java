/*******************************************************************************
 * Copyright (c) 2016
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT Licence
 * which accompanies this distribution, and is available with this package
 *
 * Contributors:
 *    Arnaud Dieumegard - initial implementation
 *******************************************************************************/

package fr.loria.dedale.bsimu;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * This class implements a Singleton providing services for handling exceptions
 * and dispatching them to the ErrorLog view of Eclipse.
 */
public class ExceptionHandler {

	private static ExceptionHandler instance;
	
	private static MessageConsoleStream out;
	
	private ExceptionHandler() {
		out = ConsoleUtil.getConsoleMessageStreamAndDisplayConsole();
	}
	
	public static ExceptionHandler getInstance() {
		if (instance == null) {
			instance = new ExceptionHandler();
		}
		return instance;
	}
	
	public void handleException(Exception e){

		// Create a new status
		Status status = new Status(IStatus.ERROR, "fr.loria.dedale.bsimu", e.getMessage());
		// Display error
		StatusManager.getManager().handle(status);
		
		// Notify user in JeB console
		out.println("//");
		out.println("// An error occured during the translation process. See error log for details.");
	}
}
