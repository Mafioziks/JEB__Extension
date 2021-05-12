/******************************************************************************
 * @author  : Faqing Yang
 * @date    : 2013/11/29
 * @version : 0.6.5
 *
 * Copyright (c) 2013 Faqing Yang
 * Licensed under the MIT license.
 * 
 ******************************************************************************/

package bsimu;

import org.eventb.core.ast.FormulaFactory;

public interface IBSimu {

	public final static String NEWLINE = System.getProperty("line.separator");

	public final static String TAB = "    ";

	public final static String TAB2 = TAB + TAB;

	public final static int CONTEXT = 0;

	public final static int MACHINE = 1;

	public final static int CONSTANT = 2;

	public final static int GLOBAL_VARIABLE = 3;

	public final static int PARAMETER = 4;

	public final static int LOCAL_VARIABLE = 5;

	public static final int UNDEFINE = 99;

	public final static String CONSTANT_PREFIX = "$cst.";

	public final static String AXIOM_PREFIX = "$axm.";

	public final static String VARIABLE_PREFIX = "$var.";

	public final static String INVARIANT_PREFIX = "$inv.";

	public final static String EVENT_PREFIX = "$evt.";

	public final static String ARGUMENT_PREFIX = "$arg.";

	public static final int NORMAL_GUARD = 0;

	public static final int PARAMETERIZED_GUARD = 1;

	public static final int LOCAL_VARIABLE_GUARD = 2;

	public FormulaFactory formulaFactory = FormulaFactory.getDefault();

	public final String VERSION = "JeB translator version 0.6.5";
	
	public final String PAGE_EXT = "html";

	public final boolean DEBUG = false;

}
