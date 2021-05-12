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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.Predicate;

import bsimu.jeb.BSimuFormula;

public class Util {

	private static final char[] hexChar = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static final String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";

	public static String escapeUnicode(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >> 7) > 0) {
				sb.append("\\u");
				sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character
														// for the left-most
														// 4-bits
				sb.append(hexChar[(c >> 8) & 0xF]); // hex for the second group
													// of 4-bits from the left
				sb.append(hexChar[(c >> 4) & 0xF]); // hex for the third group
				sb.append(hexChar[c & 0xF]); // hex for the last group, e.g.,
												// the right most 4-bits
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String escapeHtml(String s) {
		if (s == null) {
			return "";
		}

		String result = s;
		result = result.replaceAll("<", "&lt;");
		result = result.replaceAll(">", "&gt;");
		result = result.replaceAll("\u222a", " \u222a "); // setUnion
		result = result.replaceAll("\u2229", " \u2229 "); // setInter
		// result = result.replaceAll("\n", "<br>");
		return result;
	}

	public static String escapePrimedVariable(String s) {
		String result = s;
		if (s.endsWith("'")) {
			result = IBSimu.VARIABLE_PREFIX + s.substring(0, s.length() - 1)
					+ "._value";
		}
		return result;
	}

	public static String getOrdinalNumber(int number) {
		int hundredRemainder = number % 100;
		if (hundredRemainder >= 10 && hundredRemainder <= 20) {
			return number + "th";
		}
		int tenRemainder = number % 10;
		switch (tenRemainder) {
		case 1:
			return number + "st";
		case 2:
			return number + "nd";
		case 3:
			return number + "rd";
		default:
			return number + "th";
		}
	}

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	public static long milliseconds() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}

	public static void debug(Predicate formula, int env) {
		BSimuFormula bSimuFormula = new BSimuFormula(env);
		String debug = "";
		debug = bSimuFormula.parsePredicate(formula);
		System.out.println(formula.toString());
		System.out.println(debug);
		System.out.println(formula.getSyntaxTree());
	}

	public static void debugPredicate(Predicate formula, int env) {
		if (IBSimu.DEBUG) {
			BSimuFormula bSimuFormula = new BSimuFormula(env);
			String debug = "";
			debug = bSimuFormula.parsePredicate(formula);
			System.out.println(formula.toString());
			System.out.println(debug);
			System.out.println(formula.getSyntaxTree());
		}
	}

	public static void debugAssignment(Assignment formula) {
		if (IBSimu.DEBUG) {
			BSimuFormula bSimuFormula = new BSimuFormula(IBSimu.MACHINE);
			String debug = "";
			debug = bSimuFormula.parseAssignment(formula);
			System.out.println(formula.toString());
			System.out.println(debug);
			System.out.println(formula.getSyntaxTree());
		}
	}

	public static void charReplace(String filename) {
		try {
			StringBuffer sb = new StringBuffer();
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			String str = sb.toString();

			String path = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toString();
			String mappingFile = path + File.separator + "mapping.properties";
			InputStream in = new BufferedInputStream(new FileInputStream(
					mappingFile));
			Properties properties = new Properties();
			properties.load(in);

			Enumeration<?> en = properties.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String utfKey = new String(key.getBytes("iso-8859-1"), "UTF-8");
				str = str
						.replaceAll(utfKey, properties.getProperty(key).trim());
			}

			in.close();

			FileWriter fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(str);
			bw.flush();
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionHandler.getInstance().handleException(e);
		}
	}

}
