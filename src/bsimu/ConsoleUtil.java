/*******************************************************************************
 * Copyright (c) 2016
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT Licence
 * which accompanies this distribution, and is available with this package
 *
 * Contributors:
 *    Arnaud Dieumegard - initial implementation
 *    
 * Content inspired from:
 * 	- http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
 *  - http://wiki.eclipse.org/FAQ_How_do_I_find_the_active_workbench_page%3F
 *******************************************************************************/

package bsimu;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * This class provides utility functions retrieving the JeB console
 * and displaying a message on the console.
 */
public class ConsoleUtil {
	
	// Static name of the JeB console
	private static String consoleName = "JeB console";

	/**
	 * Use this method to retrieve the JeB console
	 * @return The instance of MessageConsole for the JeB output. Creates it if it does not exists
	 */
	private static MessageConsole getConsole() {
		// Get the system objects
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		// Check if console already exists
		for (int i = 0; i < existing.length; i++) {
			if (consoleName.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		// No console found, so create a new one
		MessageConsole myConsole = new MessageConsole(consoleName, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
	
	/**
	 * Retrieve the console message stream and displays the JeB console
	 * @return A console message stream to display messages in the JeB console
	 */
	public static MessageConsoleStream getConsoleMessageStreamAndDisplayConsole(){
		// Get console
		MessageConsole console = getConsole();

		// Get WorkBench active page
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		
		// Display console
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(id);
			view.display(console);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		// Return console message stream
		return console.newMessageStream();
	}
}
