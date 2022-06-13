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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.ui.console.MessageConsoleStream;
import org.eventb.core.IContextRoot;
import org.eventb.core.IExtendsContext;
import org.eventb.core.IMachineRoot;
import org.eventb.core.IRefinesMachine;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import fr.loria.dedale.bsimu.ExceptionHandler;
import fr.loria.dedale.bsimu.IBSimu;
import fr.loria.dedale.bsimu.Util;

public class BSimuProject {
	
	private IRodinProject bSimuProject;
	private MessageConsoleStream out;
	private String projectName;
	private String projectPath;
	private PrintWriter htmlOut;
	private String htmlFileName;
	private IMachineRoot[] iMachineRoots;
	private IContextRoot[] iContextRoots;
	private ArrayList<IMachineRoot> abstractMachines;
	private ArrayList<IContextRoot> abstractContexts;
	private ArrayList<IContextRoot> displayedContexts;

	public BSimuProject(IRodinProject iRodinProject, String relativePath, MessageConsoleStream out)
			throws RodinDBException {
		this.bSimuProject = iRodinProject;
		this.out = out; 
		this.projectName = bSimuProject.getProject().getName();
		this.projectPath = iRodinProject.getProject().getLocation().toString()
				+ File.separator + relativePath;
		this.iMachineRoots = bSimuProject
				.getRootElementsOfType(IMachineRoot.ELEMENT_TYPE);
		this.iContextRoots = bSimuProject
				.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
		this.abstractMachines = new ArrayList<IMachineRoot>();
		this.abstractContexts = new ArrayList<IContextRoot>();
		this.htmlFileName = projectPath + File.separator + "index.html";
	}

	public String getProjectPath() {
		return projectPath;
	}

	private void outputLibFile(String fileName) {
		URL url;
		try {
			url = new URL("platform:/plugin/fr.loria.dedale.bsimu.jeb/lib/" + fileName);
			InputStream inputStream = url.openConnection().getInputStream();
			BufferedReader inFile = new BufferedReader(new InputStreamReader(
					inputStream));

			String outputFileName = projectPath + File.separator + fileName;
			PrintWriter outFile = new PrintWriter(outputFileName, "UTF-8");

			String inputLine;
			while ((inputLine = inFile.readLine()) != null) {
				outFile.println(inputLine);
			}

			inFile.close();
			outFile.close();
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}

	}

	private ArrayList<IMachineRoot> getRefinementList(
			ArrayList<IMachineRoot> sourcelist) {
		ArrayList<IMachineRoot> resultList = new ArrayList<IMachineRoot>();
		try {
			for (IMachineRoot source : sourcelist) {
				String sourceName = source.getRodinFile().getBareName();
				for (IMachineRoot machine : iMachineRoots) {
					IRefinesMachine[] refinesMachines = machine
							.getRefinesClauses();
					for (int i = 0; i < refinesMachines.length; i++) {
						if (sourceName.equals(refinesMachines[i]
								.getAbstractMachineName())) {
							if (!resultList.contains(machine)) {
								resultList.add(machine);
							}
						}
					}
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		return resultList;
	}

	private ArrayList<IContextRoot> getExtensionList(
			ArrayList<IContextRoot> sourcelist) {
		ArrayList<IContextRoot> resultList = new ArrayList<IContextRoot>();
		try {
			for (IContextRoot source : sourcelist) {
				String sourceName = source.getRodinFile().getBareName();
				for (IContextRoot context : iContextRoots) {
					IExtendsContext[] extendsContexts = context
							.getExtendsClauses();
					for (int i = 0; i < extendsContexts.length; i++) {
						if (sourceName.equals(extendsContexts[i]
								.getAbstractContextName())) {
							if (!displayedContexts.contains(context)
									&& !resultList.contains(context)) {
								displayedContexts.add(context);
								resultList.add(context);
							}
						}
					}
				}
			}
		} catch (RodinDBException e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
		return resultList;
	}

	public void translate() {

		try {			
			out.println("// BEGIN Translation of project ["
					+ projectName + "]");

			// create jeb output directory
			File newFolder = new File(projectPath);
			if (!newFolder.exists()) {
				newFolder.mkdir();
			}

			// create empty jeb.user.js
			File newFile = new File(projectPath + File.separator
					+ "jeb_user.js");
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			// output lib files
			outputLibFile("biginteger.js");
			outputLibFile("jeb.js");
			outputLibFile("set.js");
			outputLibFile("jeb.css");
			outputLibFile("MIT-LICENSE.txt");

			long milliseondes = 0;
			long beginTime = Util.milliseconds();

			// translate contexts
			BSimuGlobal.setAutoAxiomId(1);
			IContextRoot[] iContextRoots = bSimuProject
					.getRootElementsOfType(IContextRoot.ELEMENT_TYPE);
			for (IContextRoot iContextRoot : iContextRoots) {
				milliseondes = Util.milliseconds();
				BSimuContext bSimuConext = new BSimuContext(projectPath,
						iContextRoot);
				bSimuConext.translate();
				if (bSimuConext.isAbstractContext()) {
					abstractContexts.add(iContextRoot);
				}
				milliseondes = Util.milliseconds() - milliseondes;
				out.println("// Context:\t" + milliseondes + "ms\t"
						+ bSimuConext.getContextName());
			}

			// translate machines
			for (IMachineRoot iMachineRoot : iMachineRoots) {
				milliseondes = Util.milliseconds();
				BSimuGlobal.initMachine();
				BSimuMachine bSimuMachine = new BSimuMachine(this, iMachineRoot);
				bSimuMachine.translate();
				if (bSimuMachine.isAbstractMachine()) {
					abstractMachines.add(iMachineRoot);
				}
				milliseondes = Util.milliseconds() - milliseondes;
				out.println("// Machine:\t" + milliseondes + "ms\t"
						+ bSimuMachine.getMachineName());
			}

			// construct extended context list
			int maxColumnOfContext = 0;
			StringBuilder contextList = new StringBuilder();
			for (IContextRoot iContextRoot : abstractContexts) {
				String contextName = iContextRoot.getRodinFile().getBareName();
				contextList.append("<tr>" + IBSimu.NEWLINE);
				contextList.append("<td><a href='" + contextName + "."
						+ IBSimu.PAGE_EXT + "'>" + contextName + "</a></td>"
						+ IBSimu.NEWLINE);
				displayedContexts = new ArrayList<IContextRoot>();
				ArrayList<IContextRoot> sourceList = new ArrayList<IContextRoot>();
				displayedContexts.add(iContextRoot);
				sourceList.add(iContextRoot);
				ArrayList<IContextRoot> extensions = getExtensionList(sourceList);
				int count = 0;
				while (extensions.size() > 0) {
					sourceList = new ArrayList<IContextRoot>();
					contextList.append("<td>");
					for (IContextRoot extension : extensions) {
						String extensionName = extension.getRodinFile()
								.getBareName();
						contextList.append("<a href='" + extensionName + "."
								+ IBSimu.PAGE_EXT + "'>" + extensionName
								+ "</a><br>");
						sourceList.add(extension);
					}
					contextList.append("</td>" + IBSimu.NEWLINE);
					count++;
					if (count > maxColumnOfContext) {
						maxColumnOfContext = count;
					}
					extensions = getExtensionList(sourceList);
				}
				contextList.append("</tr>" + IBSimu.NEWLINE);
			}

			// construct contexts table header
			StringBuilder conextsHeader = new StringBuilder();
			conextsHeader.append("<tr style='background-color:#f0f0f0'>"
					+ IBSimu.NEWLINE);
			conextsHeader.append("<td><b>Abstract context</b></td>"
					+ IBSimu.NEWLINE);
			for (int i = 1; i <= maxColumnOfContext; i++) {
				conextsHeader.append("<td><b>" + Util.getOrdinalNumber(i)
						+ " extension</b></td>" + IBSimu.NEWLINE);
			}
			conextsHeader.append("</tr>");

			// construct refinement list
			int maxColumnOfMachine = 0;
			StringBuilder refinementList = new StringBuilder();
			for (IMachineRoot iMachineRoot : abstractMachines) {
				String machineName = iMachineRoot.getRodinFile().getBareName();
				refinementList.append("<tr>" + IBSimu.NEWLINE);
				refinementList.append("<td><a href='" + machineName + "."
						+ IBSimu.PAGE_EXT + "'>" + machineName + "</a></td>"
						+ IBSimu.NEWLINE);

				ArrayList<IMachineRoot> sourceList = new ArrayList<IMachineRoot>();
				sourceList.add(iMachineRoot);
				ArrayList<IMachineRoot> refinements = getRefinementList(sourceList);
				int count = 0;
				while (refinements.size() > 0) {
					sourceList = new ArrayList<IMachineRoot>();
					refinementList.append("<td>");
					for (IMachineRoot refinement : refinements) {
						String refinementName = refinement.getRodinFile()
								.getBareName();
						refinementList.append("<a href='" + refinementName
								+ "." + IBSimu.PAGE_EXT + "'>" + refinementName
								+ "</a><br>");
						sourceList.add(refinement);
					}
					refinementList.append("</td>" + IBSimu.NEWLINE);
					count++;
					if (count > maxColumnOfMachine) {
						maxColumnOfMachine = count;
					}
					refinements = getRefinementList(sourceList);
				}
				refinementList.append("</tr>" + IBSimu.NEWLINE);
			}
			// construct machines table header
			StringBuilder machinesHeader = new StringBuilder();
			machinesHeader.append("<tr style='background-color:#f0f0f0'>"
					+ IBSimu.NEWLINE);
			machinesHeader.append("<td><b>Abstract machine</b></td>"
					+ IBSimu.NEWLINE);
			for (int i = 1; i <= maxColumnOfMachine; i++) {
				machinesHeader.append("<td><b>" + Util.getOrdinalNumber(i)
						+ " refinement</b></td>" + IBSimu.NEWLINE);
			}
			machinesHeader.append("</tr>");

			// output index.html
			htmlOut = new PrintWriter(htmlFileName, "UTF-8");
			htmlOut.println("<html>");
			htmlOut.println("<head>");
			htmlOut.println("<meta charset='utf-8'>");
			htmlOut.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
			htmlOut.println("<title>" + projectName + "</title>");
			htmlOut.println("</head>");
			htmlOut.println("<body>");
			htmlOut.println("<center>");
			htmlOut.println("<font size='5'>Event-B project: <i>" + projectName
					+ "</i></font><br><br>");
			htmlOut.println("<table>");
			htmlOut.println(machinesHeader);
			htmlOut.println(refinementList);
			htmlOut.println("</table>");
			htmlOut.println("<br>");
			htmlOut.println("<table>");
			htmlOut.println(conextsHeader);
			htmlOut.println(contextList);
			htmlOut.println("</table>");
			htmlOut.println("<br><br><br>");
			htmlOut.println("--------------------------------------------------------------<br>");
			htmlOut.println("<font size='3'>Generated by " + IBSimu.VERSION
					+ "<br></font>");
			htmlOut.println("<font size='3'><a href='http://dedale.loria.fr/?q=en/JeB'>JavaScript simulation framework for Event-B<br></font>");
			htmlOut.println("<font size='3'><a href='http://dedale.loria.fr/'>http://dedale.loria.fr/</a><br></font>");
			htmlOut.println("</center>");
			htmlOut.println("</body>");
			htmlOut.println("</html>");

			// close output files
			htmlOut.close();

			out.println("// Sets:\t\t" + BSimuGlobal.sets);
			out.println("// Constants:\t" + BSimuGlobal.constants);
			out.println("// Parameters:\t" + BSimuGlobal.parameters);
			out.println("// Total times:\t"
					+ (Util.milliseconds() - beginTime) + "ms");
			out.println("// END Translation of project [" + projectName
					+ "]\n");
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
	}

}
