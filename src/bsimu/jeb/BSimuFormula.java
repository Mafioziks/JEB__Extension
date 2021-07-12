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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.OutputStreamWriter;
//import java.util.regex.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.AtomicExpression;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.BoolExpression;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.MultiplePredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.PredicateVariable;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.QuantifiedExpression.Form;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.UnaryPredicate;

import bsimu.IBSimu;
import bsimu.Util;
import bsimu.jeb.Graph;

public class BSimuFormula {

	private ArrayList<BoundIdentDecl> boundIdentDeclList;
	private boolean success;
	private int env;
	File myObj = new File("C:/Users/Yeab/desktop/eclipsefile/a.txt");
	File myObj2 = new File("C:/Users/Yeab/desktop/eclipsefile/b.txt");
	File myObj3 = new File("C:/Users/Yeab/desktop/eclipsefile/dependecyGraph.html");
	public void fileWr(String toWrite, String fileName)
	{
		String toW = toWrite;
		String filename = fileName;
			try {
				//myObj.createNewFile();
				if(!myObj.exists() && !myObj2.exists())
				{
					myObj.createNewFile();
					myObj2.createNewFile();
				}
				
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			try {
				FileWriter fw = new FileWriter(myObj, true);
				FileWriter fw2 = new FileWriter(myObj2, true);
				BufferedWriter bw = new BufferedWriter(fw);
				BufferedWriter bw2 = new BufferedWriter(fw2);
			    if(filename == " ") {
			    	 bw2.write(toW);
			    	 bw2.newLine();
			    	 bw2.close();
			    }
			    else
			    {
			    	bw.write(toW);
			        bw.newLine();
					bw.close();
			    }
			   
			  //  bw.write(toW);
			  //  bw.newLine();
			    //bw.close();
			} catch(IOException e)
			{
				e.printStackTrace();
			}
			
		
	}
	

	public BSimuFormula(int env) {
		boundIdentDeclList = new ArrayList<BoundIdentDecl>();
		this.env = env;
		this.success = true;
	}

	public boolean isSuccess() {
		return success;
	}

	public String parsePredicate(Predicate predicate) {
		
		String result = "";

		if (predicate instanceof RelationalPredicate) {
			result = parseRelationalPredicate((RelationalPredicate) predicate);
		} else if (predicate instanceof BinaryPredicate) {
			result = parseBinaryPredicate((BinaryPredicate) predicate);
		} else if (predicate instanceof AssociativePredicate) {
			result = parseAssociativePredicate((AssociativePredicate) predicate);
		} else if (predicate instanceof LiteralPredicate) {
			result = parseLiteralPredicate((LiteralPredicate) predicate);
		} else if (predicate instanceof SimplePredicate) {
			result = parseSimplePredicate((SimplePredicate) predicate);
		} else if (predicate instanceof UnaryPredicate) {
			result = parseUnaryPredicate((UnaryPredicate) predicate);
		} else if (predicate instanceof QuantifiedPredicate) {
			result = parseQuantifiedPredicate((QuantifiedPredicate) predicate);
		} else if (predicate instanceof MultiplePredicate) {
			result = parseMultiplePredicate((MultiplePredicate) predicate);
		} else if (predicate instanceof PredicateVariable) {
			result = parsePredicateVariable((PredicateVariable) predicate);
		}

		// Unsupported
		if ("".equals(result)) {
			success = false;
		}

		return result;
	}

	private String parseBinaryPredicate(BinaryPredicate binaryPredicate) {

		Predicate left = binaryPredicate.getLeft();
		Predicate right = binaryPredicate.getRight();
		int tag = binaryPredicate.getTag();
		StringBuilder result = new StringBuilder();

		switch (tag) {
		case Formula.LIMP:
			result.append("$B.implication(" + parsePredicate(left) + ", "
					+ parsePredicate(right) + ")");
			break;
		case Formula.LEQV:
			result.append("$B.equivalence(" + parsePredicate(left) + ", "
					+ parsePredicate(right) + ")");
			break;
		}

		return result.toString();
	}

	private String parseRelationalPredicate(RelationalPredicate predicate) {

		StringBuilder result = new StringBuilder();
		Expression left = predicate.getLeft();
		Expression right = predicate.getRight();
		int tag = predicate.getTag();

		switch (tag) {
		case Formula.EQUAL:
			result.append("$B.equal(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.NOTEQUAL:
			result.append("$B.notEqual(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.LT:
			result.append("$B.lessThan(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.LE:
			result.append("$B.lessEqual(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.GT:
			result.append("$B.greaterThan(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.GE:
			result.append("$B.greaterEqual(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.IN:
			result.append("$B.belong(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.NOTIN:
			result.append("$B.notBelong(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.SUBSET:
			result.append("$B.properSubset(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.NOTSUBSET:
			result.append("$B.notProperSubset(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.SUBSETEQ:
			result.append("$B.subset(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		case Formula.NOTSUBSETEQ:
			result.append("$B.notSubset(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")");
			break;
		}

		return result.toString();
	}

	private String parseAssociativePredicate(
			AssociativePredicate associativePredicate) {

		int tag = associativePredicate.getTag();
		Predicate[] children = associativePredicate.getChildren();
		StringBuilder result = new StringBuilder();

		switch (tag) {
		case Formula.LAND:
			result.append("$B.and(");
			break;
		case Formula.LOR:
			result.append("$B.or(");
			break;
		}

		for (int i = 0; i < children.length; i++) {
			result.append(parsePredicate(children[i]));
			if (i < children.length - 1) {
				result.append(", ");
			}
		}

		result.append(")");
		return result.toString();
	}

	private String parseLiteralPredicate(LiteralPredicate literalPredicate) {

		int tag = literalPredicate.getTag();
		StringBuilder result = new StringBuilder();

		switch (tag) {
		case Formula.BTRUE:
			result.append("$B.bTrue()");
			break;
		case Formula.BFALSE:
			result.append("$B.bFalse()");
			break;
		}

		return result.toString();
	}

	private String parseSimplePredicate(SimplePredicate simplePredicate) {

		int tag = simplePredicate.getTag();
		Expression expression = simplePredicate.getExpression();
		StringBuilder result = new StringBuilder();

		switch (tag) {
		case Formula.KFINITE:
			result.append("$B.finite(" + parseExpression(expression) + ")");
			break;
		}

		return result.toString();
	}

	private String parseUnaryPredicate(UnaryPredicate unaryPredicate) {

		int tag = unaryPredicate.getTag();
		Predicate predicate = unaryPredicate.getChild();
		StringBuilder result = new StringBuilder();

		switch (tag) {
		case Formula.NOT:
			result.append("$B.not(" + parsePredicate(predicate) + ")");
			// int childTag = predicate.getTag();
			// if (childTag == Formula.LAND || childTag == Formula.LOR
			// || childTag == Formula.LIMP) {
			// result.append("!(" + parsePredicate(predicate) + ")");
			// } else {
			// result.append("!" + parsePredicate(predicate));
			// }
			break;
		}

		return result.toString();
	}

	private String parseQuantifiedPredicate(
			QuantifiedPredicate quantifiedPredicate) {

		int originalSize = boundIdentDeclList.size();
		BoundIdentDecl[] boundIdentDecls = quantifiedPredicate
				.getBoundIdentDecls();
		for (BoundIdentDecl boundIdentDecl : boundIdentDecls) {
			boundIdentDeclList.add(boundIdentDecl);
		}
		String transformedPredicate = "";

		int tag = quantifiedPredicate.getTag();
		StringBuilder result = new StringBuilder();
		Predicate predicate = quantifiedPredicate.getPredicate();
		BoundIdentDecl[] quantifiers = quantifiedPredicate.getBoundIdentDecls();
		String r = "r1";
		String quantifedTestResult = testPairs(quantifiedPredicate);
		if(quantifedTestResult!="")
		{
			while (boundIdentDeclList.size() > originalSize) {
				boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
			}
			return quantifedTestResult;
		}
		else
		{
				StringBuilder quantifiersString = new StringBuilder();
				StringBuilder domainArray = new StringBuilder();
				List<String> domainArray2 = new ArrayList<String>();
				int domSize;
				String depend = "";
				String depend2 = "";
				List<String> dependentQuantifiers = new ArrayList<String>();
					List<String> independentQuantifiers = new ArrayList<String>();
			    List<String> dependencyOrder = new ArrayList<String>();
				domainArray.append("[");
		
				for (int i = 0; i < quantifiers.length; i++)
				{
					String name = quantifiers[i].getName();
					quantifiersString.append(name);
					domainArray.append(parseDomain(name, predicate));
					domainArray2.add((parseDomain(name, predicate)).toString());
					if (i < quantifiers.length - 1) {
							quantifiersString.append(", ");
							domainArray.append(", ");
						}
					if(tag == 851)
					{
						domSize = domainArray2.size() - 1;
						for(int k = 0; k < quantifiers.length; k++)					
							if(quantifiers[k].getName() != name)
							{
								if(domainArray2.get(domSize).contains(quantifiers[k].getName() + ")")||domainArray2.get(domSize).contains("("+ quantifiers[k].getName() + ", "))
								{
									depend = name + " -> " + quantifiers[k].getName() ;
									depend2 = domainArray2.get(domSize);
									dependentQuantifiers.add(name);
									independentQuantifiers.add(quantifiers[k].getName());
								}
							}
					}
				}
				domainArray.append("]");
				if(dependentQuantifiers.size()>0)
				{
					
						transformedPredicate = transformQuantifiedPredicate(quantifiedPredicate,dependentQuantifiers, independentQuantifiers);
					//	fileWr(transformedPredicate, "");
						while (boundIdentDeclList.size() > originalSize) {
							boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
						}
						return transformedPredicate;
				}
				
				else
				{
					
		
					String predicateFunction = "function(" + quantifiersString
							+ "){return " + parsePredicate(predicate) + ";}";
			
					switch (tag) {
					case Formula.FORALL:
						result.append("$B.forAll(" + predicateFunction + ", " + domainArray
								+ ")");
						break;
					case Formula.EXISTS:
						result.append("$B.exists(" + predicateFunction + ", " + domainArray
								+ ")");
						break;
					}
			
					while (boundIdentDeclList.size() > originalSize) {
						boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
					}
			
					return result.toString();
				  }
		}
	}

	private String parseMultiplePredicate(MultiplePredicate multiplePredicate) {

		StringBuilder result = new StringBuilder();
		Expression[] expressions = multiplePredicate.getChildren();

		result.append("$B.partition(");

		for (int i = 0; i < expressions.length; i++) {
			result.append(parseExpression(expressions[i]));
			if (i < expressions.length - 1) {
				result.append(", ");
			}
		}

		result.append(")");
		return result.toString();
	}

	private String parsePredicateVariable(PredicateVariable predicateVariable) {

		// Unsupported
		success = false;
		return "";
	}

	public String parseExpression(Expression expression) {

		int tag = expression.getTag();

		if (expression instanceof FreeIdentifier) {
			String identifier = expression.toString();
			if (env == IBSimu.CONTEXT) {
				return IBSimu.CONSTANT_PREFIX + identifier;
			} else {
				int bSimuTag = BSimuGlobal.getTag(identifier);
				if (bSimuTag == IBSimu.CONSTANT) {
					return IBSimu.CONSTANT_PREFIX + identifier;
				} else if (bSimuTag == IBSimu.GLOBAL_VARIABLE) {
					return IBSimu.VARIABLE_PREFIX + identifier + ".value";
				} else if (bSimuTag == IBSimu.UNDEFINE) {
					return IBSimu.ARGUMENT_PREFIX + identifier + ".value";
				}
			}

		} else if (expression instanceof BoundIdentifier) {
			BoundIdentifier boundIdentifier = (BoundIdentifier) expression;
			BoundIdentDecl[] boundIdentDecls = new BoundIdentDecl[boundIdentDeclList
					.size()];
			boundIdentDeclList.toArray(boundIdentDecls);
			String identifier = boundIdentifier.getDeclaration(boundIdentDecls)
					.getName();
		
			return Util.escapePrimedVariable(identifier);

		} else if (expression instanceof IntegerLiteral) {
			// replace UNICODE minus
			String literal = expression.toString().replace('\u2212', '-');
			if (BSimuGlobal.INTEGER_VERSION == 0) {
				return literal;
			} else if (BSimuGlobal.INTEGER_VERSION == 1) {
				return "'" + literal + "'";
			} else {
				return "$B('" + literal + "')";
			}

		} else if (expression instanceof SetExtension) {
			return parseSetExtension((SetExtension) expression);

		} else if (expression instanceof BinaryExpression) {
			return parseBinaryExpression((BinaryExpression) expression);

		} else if (expression instanceof AssociativeExpression) {
			return parseAssociativeExpression((AssociativeExpression) expression);

		} else if (expression instanceof AtomicExpression) {
			switch (tag) {
			case Formula.INTEGER:
				return "$B.INTEGER";
			case Formula.NATURAL:
				return "$B.NATURAL";
			case Formula.NATURAL1:
				return "$B.NATURAL1";
			case Formula.BOOL:
				return "$B.BOOL";
			case Formula.TRUE:
				return "$B.TRUE";
			case Formula.FALSE:
				return "$B.FALSE";
			case Formula.EMPTYSET:
				return "$B.EmptySet";
			case Formula.KPRED:
				return "$B.pred";
			case Formula.KSUCC:
				return "$B.succ";
			case Formula.KPRJ1_GEN:
				return "$B.prj1";
			case Formula.KPRJ2_GEN:
				return "$B.prj2";
			case Formula.KID_GEN:
				return "$B.id";
			}

		} else if (expression instanceof BoolExpression) {
			Predicate predicate = ((BoolExpression) expression).getPredicate();
			return "$B.bool(" + parsePredicate(predicate) + ")";

		} else if (expression instanceof UnaryExpression) {
			Expression child = ((UnaryExpression) expression).getChild();
			switch (tag) {
			case Formula.KCARD:
				return "$B.card(" + parseExpression(child) + ")";
			case Formula.POW:
				return "$B.PowerSet(" + parseExpression(child) + ")";
			case Formula.POW1:
				return "$B.PowerSet1(" + parseExpression(child) + ")";
			case Formula.KUNION:
				return "$B.union(" + parseExpression(child) + ")";
			case Formula.KINTER:
				return "$B.inter(" + parseExpression(child) + ")";
			case Formula.KDOM:
				return "$B.dom(" + parseExpression(child) + ")";
			case Formula.KRAN:
				return "$B.ran(" + parseExpression(child) + ")";
			case Formula.KMIN:
				return "$B.min(" + parseExpression(child) + ")";
			case Formula.KMAX:
				return "$B.max(" + parseExpression(child) + ")";
			case Formula.CONVERSE:
				return "$B.converse(" + parseExpression(child) + ")";
			case Formula.UNMINUS:
				return "$B.unminus(" + parseExpression(child) + ")";
			}

		} else if (expression instanceof QuantifiedExpression) {
			return parseQuantifiedExpression((QuantifiedExpression) expression);
		}

		// Unsupported
		success = false;
		return "";
	}

	private String parseSetExtension(SetExtension setExtension) {

		StringBuilder result = new StringBuilder();
		Expression[] expressions = setExtension.getMembers();

		result.append("$B.SetExtension(");

		for (int i = 0; i < expressions.length; i++) {
			result.append(parseExpression(expressions[i]));
			if (i < expressions.length - 1) {
				result.append(", ");
			}
		}
		result.append(")");
		return result.toString();
	}

	private String parseBinaryExpression(BinaryExpression binaryExpression) {

		int tag = binaryExpression.getTag();
		Expression left = binaryExpression.getLeft();
		Expression right = binaryExpression.getRight();

		switch (tag) {
		case Formula.MAPSTO:
			return "$B.Pair(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.REL:
			return "$B.Relations(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.TREL:
			return "$B.TotalRelations(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.SREL:
			return "$B.SurjectiveRelations(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.STREL:
			return "$B.TotalSurjectiveRelations(" + parseExpression(left)
					+ ", " + parseExpression(right) + ")";
		case Formula.PFUN:
			return "$B.PartialFunctions(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.TFUN:
			return "$B.TotalFunctions(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.PINJ:
			return "$B.PartialInjections(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.TINJ:
			return "$B.TotalInjections(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.PSUR:
			return "$B.PartialSurjections(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.TSUR:
			return "$B.TotalSurjections(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.TBIJ:
			return "$B.TotalBijections(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.SETMINUS:
			return "$B.setMinus(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.CPROD:
			return "$B.CartesianProduct(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.DPROD:
			return "$B.directProduct(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.PPROD:
			return "$B.parallelProduct(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.DOMRES:
			return "$B.domainRestriction(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.DOMSUB:
			return "$B.domainSubtraction(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.RANRES:
			return "$B.rangeRestriction(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.RANSUB:
			return "$B.rangeSubtraction(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.UPTO:
			return "$B.UpTo(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.MINUS:
			return "$B.minus(" + parseExpression(left) + " ,"
					+ parseExpression(right) + ")";

		case Formula.DIV:
			return "$B.divide(" + parseExpression(left) + " ,"
					+ parseExpression(right) + ")";

		case Formula.MOD:
			return "$B.mod(" + parseExpression(left) + " ,"
					+ parseExpression(right) + ")";
		case Formula.EXPN:
			return "$B.pow(" + parseExpression(left) + " ,"
					+ parseExpression(right) + ")";
		case Formula.FUNIMAGE:
			return "$B.functionImage(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		case Formula.RELIMAGE:
			return "$B.relationImage(" + parseExpression(left) + ", "
					+ parseExpression(right) + ")";
		}

		// Unsupported
		success = false;
		return "";
	}

	private String parseAssociativeExpression(
			AssociativeExpression associativeExpression) {

		StringBuilder result = new StringBuilder();
		Expression[] expressions = associativeExpression.getChildren();

		int tag = associativeExpression.getTag();

		switch (tag) {
		case Formula.BUNION:
			result.append("$B.setUnion(");
			break;
		case Formula.BINTER:
			result.append("$B.setInter(");
			break;
		case Formula.BCOMP:
			result.append("$B.backwardComposition(");
			break;
		case Formula.FCOMP:
			result.append("$B.forwardComposition(");
			break;
		case Formula.OVR:
			result.append("$B.override(");
			break;
		case Formula.PLUS:
			result.append("$B.plus(");
			break;
		case Formula.MUL:
			result.append("$B.multiply(");
			break;
		}

		for (int i = 0; i < expressions.length; i++) {
			result.append(parseExpression(expressions[i]));
			if (i < expressions.length - 1) {
				result.append(", ");
			}
		}

		result.append(")");
		return result.toString();
	}

	private String parseQuantifiedExpression(
			QuantifiedExpression quantifiedExpression) {

		int originalSize = boundIdentDeclList.size();
		BoundIdentDecl[] boundIdentDecls = quantifiedExpression
				.getBoundIdentDecls();
		for (BoundIdentDecl boundIdentDecl : boundIdentDecls) {
			boundIdentDeclList.add(boundIdentDecl);
		}

		int tag = quantifiedExpression.getTag();
		StringBuilder result = new StringBuilder();
		Form form = quantifiedExpression.getForm();
		Predicate predicate = quantifiedExpression.getPredicate();
		Expression expression = quantifiedExpression.getExpression();
		BoundIdentDecl[] quantifiers = quantifiedExpression
				.getBoundIdentDecls();
		

		StringBuilder quantifiersString = new StringBuilder();
		StringBuilder domainArray = new StringBuilder();
		List<String> domainArray2 = new ArrayList<String>();
	    String depend="";
	    String depend2="";
	    int domSize = 0;
	    String fina="";
	    List<String>  dependentQuantifiers = new ArrayList<String>();
	    List<String> independentQuantifiers = new ArrayList<String>();
		domainArray.append("[");
		for (int i = 0; i < quantifiers.length; i++) {
			String name = quantifiers[i].getName();
			quantifiersString.append(name);
			domainArray.append(parseDomain(name, predicate));
			domainArray2.add((parseDomain(name, predicate)).toString());
			if (i < quantifiers.length - 1) {
				quantifiersString.append(", ");
				domainArray.append(", ");	
			}
			domSize = domainArray2.size() - 1;
				for(int k = 0; k < quantifiers.length; k++)					
					if(quantifiers[k].getName() != name)
					{
						if(domainArray2.get(domSize).contains(quantifiers[k].getName() + ")")||domainArray2.get(domSize).contains("("+ quantifiers[k].getName() + ",")||domainArray2.get(domSize).contains("("+ quantifiers[k].getName() + " ,"))
						{
							depend = name + " -> " + quantifiers[k].getName() ;
							depend2 = domainArray2.get(domSize);
							dependentQuantifiers.add(name);
							independentQuantifiers.add(quantifiers[k].getName());
						}
					}
				
		}
		domainArray.append("]");	
		if(dependentQuantifiers.size()>0)
		{
				fina = transformQuantifiedExpression(quantifiedExpression, dependentQuantifiers,independentQuantifiers);
				//fileWr(fina, "");
				while (boundIdentDeclList.size() > originalSize) {
					boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
				}
				return fina;
		}
		else
		{
			String predicateFunction = "function(" + quantifiersString
					+ "){return " + parsePredicate(predicate) + ";}";

			String expressionFunction = "function(" + quantifiersString
					+ "){return " + parseExpression(expression) + ";}";

			switch (tag) {
			case Formula.QUNION:
				result.append("$B.quantifiedUnion(" + predicateFunction + ", "
						+ expressionFunction + ", " + domainArray + ")");
				break;
			case Formula.QINTER:
				result.append("$B.quantifiedInter(" + predicateFunction + ", "
						+ expressionFunction + ", " + domainArray + ")");
				break;
			case Formula.CSET:
				if (form == Form.Explicit) {
					result.append("$B.SetComprehension(" + predicateFunction + ", "
							+ expressionFunction + ", " + domainArray + ")");
				} else if (form == Form.Implicit) {
					result.append("$B.SetComprehension(" + predicateFunction + ", "
							+ expressionFunction + ", " + domainArray + ")");
				} else if (form == Form.Lambda) {
					result.append("$B.Lambda(" + predicateFunction + ", "
							+ expressionFunction + ", " + domainArray + ")");
				}
				break;
			}

			while (boundIdentDeclList.size() > originalSize) {
				boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
			}

			return result.toString();
			
		}
		
	}
	
	//Function to transform quantified Expression to the equivalent implementation
	//accepts quantified expression, list of dependent quantifiers, list of independent quantifiers
	public String transformQuantifiedExpression(QuantifiedExpression quantifiedExpression, List<String> dependentQuantifiers, List<String> independentQuantifiers)
	{
		
		Predicate predicate = quantifiedExpression.getPredicate();
		Expression expression = quantifiedExpression.getExpression();
		BoundIdentDecl[] quantifiers = quantifiedExpression
				.getBoundIdentDecls();
		HashMap<String, Integer> exchangeTable = new HashMap<String, Integer>();
		int tag = predicate.getTag();
		String result;
		ArrayList<Integer> orderedList = new ArrayList<Integer>();
		ArrayList<ArrayList<BoundIdentDecl>> Identifiers = new ArrayList<ArrayList<BoundIdentDecl>>();
		ArrayList<ArrayList<Predicate>> Predicates = new ArrayList<ArrayList<Predicate>>();
		ArrayList<Predicate> associativePredicates = new ArrayList<Predicate>();
		ArrayList<QuantifiedExpression> quantifiedExpressions = new ArrayList<QuantifiedExpression>();
		ArrayList<RelationalPredicate> chi = new ArrayList<>();
		FormulaFactory ff = predicate.getFactory();
		Form iForm = quantifiedExpression.getForm();
		int IdentifierCurrentIndex;
		int identifierNewIndex;
		
		Graph g =  new Graph(quantifiers.length);
		for(int z = 0; z < quantifiers.length; z++)
		{
			exchangeTable.put(quantifiers[z].toString(), z);
		}
	
		for(int it = 0; it < independentQuantifiers.size(); it++)
		{
					g.addEdge(exchangeTable.get(independentQuantifiers.get(it)),exchangeTable.get(dependentQuantifiers.get(it)));
		
		}
		
		
		orderedList = g.top_sort();
		
		HashSet groups = new HashSet();
		groups.add(orderedList);
		int groupSize = groups.size();
		long n = orderedList.stream().distinct().count();
		
		for (int i = 0; i < n; i++)
		{
            Identifiers.add(new ArrayList<BoundIdentDecl>());
		}
        for(int i = 0; i < orderedList.size(); i++)
		{
			Identifiers.get(orderedList.get(i)).add(quantifiers[i]);
		}
        
	
		
		if (predicate instanceof AssociativePredicate) {
			Predicate[] children = ((AssociativePredicate) predicate)
					.getChildren();
			
			for(int m = 0; m < children.length; m++ )
			{
				chi.add((RelationalPredicate) children[m]);
			}
			for (int i = 0; i < Identifiers.size(); i++)
			{
	            Predicates.add(new ArrayList<Predicate>());
			}
			for(int i = 0; i < chi.size(); i++ )
			{
				boolean stop = true;
				if(chi.get(i).getTag() == Formula.IN)
				{
				
					RelationalPredicate relationalPredicate = (RelationalPredicate) chi.get(i);
					Expression left = relationalPredicate.getLeft();
					Expression right = relationalPredicate.getRight();
					result = parseExpression(left);
				
					
				for(int j = 0; j < Identifiers.size() && stop; j++)
					{
						for(int k = 0; k < Identifiers.get(j).size(); k++)
						{
							if(Identifiers.get(j).get(k).toString().equals(result) && j < Identifiers.size())
							{
						
										BoundIdentifier temp[] = chi.get(i).getLeft().getBoundIdentifiers();
										IdentifierCurrentIndex = temp[0].getBoundIndex();
										identifierNewIndex = ((Identifiers.get(j).size() -1) - k);
										if(j == 0)
										{
											if(identifierNewIndex == IdentifierCurrentIndex)
												{
													Predicates.get(j).add(chi.get(i));
												}
											else 
												{
													if(identifierNewIndex - IdentifierCurrentIndex < 0)
													{
													
														Predicates.get(j).add(chi.get(i).shiftBoundIdentifiers(identifierNewIndex - IdentifierCurrentIndex));
													}
												}
											chi.remove(i);
											i--;
											stop = false;
											break;
										}
										else
										{
											Predicates.get(j).add(chi.get(i));
											chi.remove(i);
											i--;
											stop = false;
											break;
										}	
							}
						}
						
					}
				}			
			}
		}
		for(int v = 0; v < chi.size();v++ )
		{
			Predicates.get(Predicates.size()-1).add(chi.get(v));
		}
		
		for(int i = 0; i<  Predicates.size(); i++)
		{
			if(Predicates.get(i).size() > 1)
			{
				associativePredicates.add(ff.makeAssociativePredicate(tag, Predicates.get(i), null));
			}
			else if(Predicates.get(i).size() == 1)
			{
				associativePredicates.add(Predicates.get(i).get(0));
			}	
					
		}
	
		
		int counter = 0;
		int m = Identifiers.size() - 1;
		for(int i = associativePredicates.size()-1; i >= 0; i--)
		{
			
			
			if(i == associativePredicates.size()-1)
				
			{
				quantifiedExpressions.add(ff.makeQuantifiedExpression(Formula.CSET, Identifiers.get(m), associativePredicates.get(i), expression, null, iForm));
			}
			else
			{
				quantifiedExpressions.add(ff.makeQuantifiedExpression(
						Formula.CSET, 
						Identifiers.get(m),
						associativePredicates.get(i), 
						quantifiedExpressions.get(counter), null, iForm));
				counter++;
			}
			m--;
		
			
		}
		Expression finalUnaryFormula = ff.makeUnaryExpression(Formula.KUNION,quantifiedExpressions.get(quantifiedExpressions.size()-1) , null);
		int originalSize = boundIdentDeclList.size();
		while (boundIdentDeclList.size() > originalSize) 
		{
			boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
		}

		String returnedFinalFormula = parseExpression(finalUnaryFormula);
		return returnedFinalFormula;
}
	
private String transformQuantifiedPredicate(QuantifiedPredicate quantifiedPredicate, List<String> dependentQuantifiers, List<String> independentQuantifiers) {

			Predicate predicate = quantifiedPredicate.getPredicate();
			//Expression expression = quantifiedExpression.getExpression();
			BoundIdentDecl[] quantifiers = quantifiedPredicate
					.getBoundIdentDecls();
			HashMap<String, Integer> exchangeTable = new HashMap<String, Integer>();
			int tag = predicate.getTag();
			String result;
			ArrayList<Integer> orderedList = new ArrayList<Integer>();
			ArrayList<ArrayList<BoundIdentDecl>> Identifiers = new ArrayList<ArrayList<BoundIdentDecl>>();
			ArrayList<ArrayList<Predicate>> Predicates = new ArrayList<ArrayList<Predicate>>();
			ArrayList<Predicate> associativePredicates = new ArrayList<Predicate>();
			ArrayList<BinaryPredicate> BinaryPredicates = new ArrayList<BinaryPredicate>();
			ArrayList<RelationalPredicate> chi = new ArrayList<>();
			ArrayList<QuantifiedPredicate> QuantifiedPredicatesInner = new ArrayList<QuantifiedPredicate>(); 
			ArrayList<BinaryPredicate> BinaryPredicateInner = new ArrayList<BinaryPredicate>();
			FormulaFactory ff = predicate.getFactory();
			ArrayList<Predicate> rightPredicates = new ArrayList<Predicate>();
			int IdentifierCurrentIndex;
			int identifierNewIndex;
			
			Graph g =  new Graph(quantifiers.length);
			for(int z = 0; z < quantifiers.length; z++)
			{
				exchangeTable.put(quantifiers[z].toString(), z);
			}
		
			for(int it = 0; it < independentQuantifiers.size(); it++)
			{
						g.addEdge(exchangeTable.get(independentQuantifiers.get(it)),exchangeTable.get(dependentQuantifiers.get(it)));
			
			}
			
			
			orderedList = g.top_sort();
			HashSet groups = new HashSet();
			groups.add(orderedList);
			int groupSize = groups.size();
			long n = orderedList.stream().distinct().count();
			
			for (int i = 0; i < n; i++)
			{
		        Identifiers.add(new ArrayList<BoundIdentDecl>());
			}
		    for(int i = 0; i < orderedList.size(); i++)
			{
				Identifiers.get(orderedList.get(i)).add(quantifiers[i]);
			}
		    
		    if (predicate instanceof BinaryPredicate) { // forAll implication
				
		    	Predicate leftPredicate = ((BinaryPredicate) predicate).getLeft();
		    	Predicate rightPredicate = ((BinaryPredicate) predicate).getRight();
			   
				if (leftPredicate instanceof AssociativePredicate) {
					Predicate[] children = ((AssociativePredicate) leftPredicate)
							.getChildren();
					for(int m = 0; m < children.length; m++ )
					{
						chi.add((RelationalPredicate) children[m]);
					}
					for (int i = 0; i < Identifiers.size(); i++)
					{
			            Predicates.add(new ArrayList<Predicate>());
					}
					
					for(int i =0; i < chi.size(); i++)
					{
						boolean stop = true;
						if(chi.get(i).getTag() == Formula.IN)
						{
							RelationalPredicate relationalPredicate = (RelationalPredicate) chi.get(i);
							Expression left = relationalPredicate.getLeft();
							Expression right = relationalPredicate.getRight();
							result = parseExpression(left);
							
							for(int j = 0; j < Identifiers.size() && stop; j++)
							{
								for(int k = 0; k < Identifiers.get(j).size(); k++)
								{
									if(Identifiers.get(j).get(k).toString().equals(result) && j < Identifiers.size())
									{
										BoundIdentifier temp[] = chi.get(i).getLeft().getBoundIdentifiers();
										IdentifierCurrentIndex = temp[0].getBoundIndex();
										identifierNewIndex = ((Identifiers.get(j).size() -1) - k);
										if(j == 0)
										{
											if(identifierNewIndex == IdentifierCurrentIndex)
												{
													Predicates.get(j).add(chi.get(i));
												}
											else 
												{
													if(identifierNewIndex - IdentifierCurrentIndex < 0)
													{
														
														Predicates.get(j).add(chi.get(i).shiftBoundIdentifiers(identifierNewIndex - IdentifierCurrentIndex));
													}
													
													
												}
											chi.remove(i);
											i--;
											stop = false;
											break;
											
										}
										else
										{
											Predicates.get(j).add(chi.get(i));
											chi.remove(i);
											i--;
											stop = false;
											break;
										}
										
									}
								}
								
							}
							
						}
					}
				  if(chi.size() > 0)
				    {
				    	for(int v = 0; v < chi.size();v++ )
						{
					    	
							Predicates.get(Predicates.size()-1).add(chi.get(v));
						}
				    }
				  rightPredicates.add(rightPredicate);
				  
					
				}
		
			} 
			 
		    
			
			for(int i = 0; i <  Predicates.size(); i++)
			{
				if(Predicates.get(i).size() > 1)
				{
					associativePredicates.add(ff.makeAssociativePredicate(Formula.LAND, Predicates.get(i), null));
				}
				else if(Predicates.get(i).size() == 1)
				{
					associativePredicates.add(Predicates.get(i).get(0));
				}	
						
			}
			int associativePredicatesSize = associativePredicates.size() - 1;
			int counter = 0;
			for(int c = associativePredicatesSize; c >= 0; c--)
			{
				if( c == associativePredicates.size()-1)
				{
					BinaryPredicateInner.add(ff.makeBinaryPredicate(Formula.LIMP, associativePredicates.get(c), rightPredicates.get(0), null));
					QuantifiedPredicatesInner.add(ff.makeQuantifiedPredicate(Formula.FORALL, Identifiers.get(c), BinaryPredicateInner.get(0), null));
				}
				else
				{
					BinaryPredicateInner.add(ff.makeBinaryPredicate(Formula.LIMP, associativePredicates.get(c), QuantifiedPredicatesInner.get(counter), null));
					QuantifiedPredicatesInner.add(ff.makeQuantifiedPredicate(Formula.FORALL, Identifiers.get(c), BinaryPredicateInner.get(counter + 1), null));
					counter++;
				}
			}
			int originalSize = boundIdentDeclList.size();
			while (boundIdentDeclList.size() > originalSize) 
			{
				boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
			}
			
			String returnedFinalBinaryFormula = parsePredicate(QuantifiedPredicatesInner.get(QuantifiedPredicatesInner.size()-1));
			String a = "a";
			return returnedFinalBinaryFormula;

}

public String testPairs(QuantifiedPredicate quantifiedPredicate)
{
			Predicate predicate = quantifiedPredicate.getPredicate();
			String finalResult="";
			ArrayList<RelationalPredicate> chi = new ArrayList<>();
			ArrayList<Expression> expressions = new ArrayList<Expression>();
			boolean control = false;
			 
			
			if (predicate instanceof BinaryPredicate) { // forAll implication
				Predicate leftPredicate = ((BinaryPredicate) predicate).getLeft();
				Predicate rightPredicate = ((BinaryPredicate) predicate).getRight();
				
				
				if (leftPredicate.getTag() == Formula.IN)
				{
					RelationalPredicate relationalPredicate = (RelationalPredicate) leftPredicate;
					Expression BiLeft = relationalPredicate.getLeft();
					Expression BiRight = relationalPredicate.getRight();
					
					if(BiLeft.getTag() == Formula.MAPSTO)
					{
						BinaryExpression domain = (BinaryExpression) BiLeft;
						Expression left = domain.getLeft();
						Expression right = domain.getRight();
						
						expressions.add(left);
						expressions.add(right);
						expressions.add(BiRight);
						
						finalResult = transformPairs(quantifiedPredicate, expressions, chi, rightPredicate);
					}
				}
				
				else if(leftPredicate instanceof AssociativePredicate)
				{
					Predicate[] children = ((AssociativePredicate) leftPredicate).getChildren();
					
					for(int m = 0; m < children.length; m++ )
					{
						chi.add((RelationalPredicate) children[m]);
					}
					for(int i = 0; i < children.length; i++)
					{
						if(chi.get(i).getTag() == Formula.IN)
						{
							RelationalPredicate domain = (RelationalPredicate) children[i];
							Expression BiLeft = domain.getLeft();
							Expression BiRight = domain.getRight();
							
							if(BiLeft.getTag()== Formula.MAPSTO)
							{
								BinaryExpression binaryPredicate = (BinaryExpression) BiLeft;
								Expression right = binaryPredicate.getRight();
								Expression left = binaryPredicate.getLeft();
							
								chi.remove(i);
								expressions.add(left);
								expressions.add(right);
								expressions.add(BiRight);
								control = true;
								i--;
							}
						}
					}
					if(control)
					{
					finalResult = transformPairs(quantifiedPredicate, expressions, chi, rightPredicate);
					}
				}
				
			}
		return finalResult;
}

/*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
 		The transformPairs function is used to fix the iteration over pairs problem 																					   
		This function takes the quantifiedPredicate which the iteration over pairs issue occurs,
		The expressions in the quantified Predicate, for example if the formula is ∀x, y · x ↦y ∈ A ⇒ (……)  x and y in x ↦y ∈ A are passed as an expression in an array
		The Right predicate is the right side of the belongs statement, e.g., x ↦y ∈ A in this case A is the rightPredicate
		The chi array includes everything that's not part of the domain array definition and before the implication
		This function takes the above arguments and produces a new specification where the iteration over pairs problem is fixed.
		e.g.,  input ∀x, y · x ↦y ∈ A ⇒ (……) 
			   return ∀x, y, z · z ∈ A ∧ x = prj1(z) ∧ y = prj2(z) ⇒ (……)
		
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
public String transformPairs(QuantifiedPredicate quantifiedPredicate, ArrayList<Expression> expressions, ArrayList<RelationalPredicate> chi, Predicate RightPredicate )
{
			//an array of associtive predicate list created to hold associatve predicates aka anything that's joined using the and operator
	 		ArrayList<Predicate> associativePredicateList = new ArrayList<>();
			ArrayList<BoundIdentDecl> listBoundIdentDecl = new ArrayList<>();
			//Adding all quantifiers in the quantified predicate into an array
			BoundIdentDecl[] quantifiers = quantifiedPredicate.getBoundIdentDecls();
			//Adding all the free identifiers into an array
			FreeIdentifier freeId[] = quantifiedPredicate.getFreeIdentifiers();
			int tag = quantifiedPredicate.getTag();
			String newIdentifier = "";
			String finalResult = "";
			
			//sending arguments to the new identifier builder function to create new identifier based on the quantifiers and the free identifiers
					
			newIdentifier = newIdentifierBuilder(quantifiers, freeId);
			
			//coping an instance of the FromulaFactory onto a formula factory variable
			FormulaFactory ff = quantifiedPredicate.getFactory();
			//making a bound identifier which can letter be used with the new identifier
			BoundIdentifier newBoundIdentifier = ff.makeBoundIdentifier(quantifiers.length, null);
			//assigning the new identifier a name, 
			BoundIdentDecl newBoundIdentDecl = ff.makeBoundIdentDecl(newIdentifier, newBoundIdentifier.getSourceLocation());
			Expression newIdentifierExpression = (Expression) newBoundIdentifier;
			
			//creating the belongs to statement using the new identifier, i.e. z ∈ A
			RelationalPredicate relationalPredicateIn = ff.makeRelationalPredicate(Formula.IN, newIdentifierExpression, expressions.get(2), null);
			
			//creating and assigning the projection function prj1 and prj2 then assigning them as a function => prj1(z) and prj2(z)
			AtomicExpression atomicExpressionPrjOne = ff.makeAtomicExpression(Formula.KPRJ1_GEN, null);
			AtomicExpression atomicExpressionPrjTwo = ff.makeAtomicExpression(Formula.KPRJ2_GEN, null);
			BinaryExpression binaryExpressionPrjOneFun = ff.makeBinaryExpression(Formula.FUNIMAGE, atomicExpressionPrjOne, newIdentifierExpression, null);
			BinaryExpression binaryExpressionPrjTwoFun = ff.makeBinaryExpression(Formula.FUNIMAGE, atomicExpressionPrjTwo, newIdentifierExpression, null);
			
			//assigning the projection function to the quantified variables, i.e. a = prj1(z) and b = prj2(z)
			RelationalPredicate relationalPredicateEqualOne = ff.makeRelationalPredicate(Formula.EQUAL, expressions.get(0), binaryExpressionPrjOneFun, null);
		    RelationalPredicate relationalPredicateEqualTwo = ff.makeRelationalPredicate(Formula.EQUAL, expressions.get(1), binaryExpressionPrjTwoFun, null);
		    
			
		   //adding all the newly creating expressions to the associtive predicate list
			associativePredicateList.add(relationalPredicateIn);
			associativePredicateList.add(relationalPredicateEqualOne);
			associativePredicateList.add(relationalPredicateEqualTwo);
			//adding the remaining items to from chi to associative predicate list
			if(chi.size() > 0)
			{
				for(int i = 0; i < chi.size(); i++)
				{
					associativePredicateList.add(chi.get(i));
				}
			}
			//adding the newly created identifier to the list of BoundIdentDeclaration list
			listBoundIdentDecl.add(newBoundIdentDecl);
			
			for(int i =0 ; i< quantifiers.length; i++)
			{
				listBoundIdentDecl.add(quantifiers[i]);
			}
			
			//creating the associative predicate from the expressions using the logical and connector, the result is z ∈ A ∧ x = prj1(z) ∧ y = prj2(z)
			AssociativePredicate finalAssociativeFormula = ff.makeAssociativePredicate(Formula.LAND, associativePredicateList, null);
			//creating the implication using Logical implication to join the right and left side of the formula, the result is z ∈ A ∧ x = prj1(z) ∧ y = prj2(z) ⇒ (……)
			BinaryPredicate transformedPredicate = ff.makeBinaryPredicate(Formula.LIMP, finalAssociativeFormula, RightPredicate, null);
			//finally adding the quantified variables to create the quantified predicate, the final result is ∀x, y, z · z ∈ A ∧ x = prj1(z) ∧ y = prj2(z) ⇒ (……)
			QuantifiedPredicate transformedQunatifiedPredicate = ff.makeQuantifiedPredicate(tag, listBoundIdentDecl, transformedPredicate, null);
					
			finalResult = parsePredicate(transformedQunatifiedPredicate);
		
	return finalResult;
}


//This function is used in the process of transforming in case when a pair is used as a domain
//In this process we need to generate new quantified variable, this where it happens.
//takes an array of quantifiers and free identifiers in the specification and returns a new quantified variable
//the new identifier needs to something unique, i.e. it should not occur in the specification a new identifier needs to be added on
//so we take a letter from a - z and compare it with quantifiers and free identifiers and if the letter is not present
//in the specification it is used as a new identifier or quantified varibale

public String newIdentifierBuilder(BoundIdentDecl[] quantifiers, FreeIdentifier[] freeIds)
{
	//empty string for the new identifier or quantified variable
	String newIdentifier ="";	
	List<String> listChar = new ArrayList<>();
	
	// free id is the free identifiers in the specification
	FreeIdentifier[] freeId = freeIds;
	
	//listChar is the alphabets from a - z 
	for (int i = 0; i <= 26; i++) {
        char c = (char) (i + 'a');
        listChar.add(String.valueOf(c));
    }
	
	//identifiers pool is a collection of free identifiers and quantifiers in the specification in short freeIds  + quantifiers
	
	ArrayList<String> identifiersPool = new ArrayList<>();
    
	//adding quantifier to the identifiers pool
	
    for(int i = 0; i < quantifiers.length; i++)
	{
	    identifiersPool.add(quantifiers[i].toString());
	}
    
    //adding freed identifiers to the identifiers pool
    
	for(int j = 0; j < freeId.length; j++)
	{
	    identifiersPool.add(freeId[j].toString());
	}
	
	//removing all the instances that are found in identifiers pool from listChar or a - z, the remaining values are unique letter which
	//doesn't exist in the specification
	
	listChar.removeAll(identifiersPool);
	//finally assign the first value from the remaining list of characters and return it as a new identifier
	
	newIdentifier = listChar.get(0);
	return newIdentifier;
}
public void parseRightPair(Predicate rightPredicate, ArrayList result, ArrayList projection)
{
	Predicate predicate = rightPredicate;
	FormulaFactory rp = rightPredicate.getFactory();
	//String formula = predicate.toString();
	ArrayList<Expression> rs = result;
	ArrayList<String> pr = projection;
	FormulaFactory ff = predicate.getFactory();
	//String projection = prjName;
	if(predicate instanceof RelationalPredicate)	
	{
		int tag = predicate.getTag();
		//predicate.
		Expression left = ((RelationalPredicate) predicate).getLeft();
		Expression right = ((RelationalPredicate) predicate).getRight();
		
		for(int i = 0; i < result.size(); i++)
		{
			String abc = parseExpression(rs.get(i));
			String bcd = parseExpression(rs.get(i+1));
			String parse1 = parseExpression(left);
			String parse2 = parseExpression(right);
			if(abc.equals(parseExpression(left)) && bcd.equals(parseExpression(right)))
			{
				
				
				IParseResult formula1 = ff.parseExpression(abc, null);
				IParseResult formula2 = ff.parseExpression(bcd, null);
				Predicate finalf = ff.makeRelationalPredicate(tag, formula1.getParsedExpression(), formula2.getParsedExpression(), null);
				fileWr(finalf.toString(), " ");
				break;
			}
		}
	}
	
}

public void dependecyGraph(String predicate, List<String> independetQ, List<String> dependentQ) throws UnsupportedEncodingException
	{
		
		try {
			
			if(!myObj3.exists())
			{
				myObj3.createNewFile();
			}
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try {
		
			
			String Header = "<!DOCTYPE html>\r\n"
					+ "<html lang=\"en\">\r\n"
					+ "  <head>\r\n"
					+ "    <meta charset=\"utf-8\" />\r\n"
					+ "    <meta name=\"viewport\"\r\n"
					+ "      content=\"width=device-width, initial-scale=1\" />\r\n"
					+ "    <title>React App</title>\r\n"
					+ "    <style>\r\n"
					+ "      #container {\r\n"
					+ "        align-self: center;\r\n"
					+ "        border: 1oem;\r\n"
					+ "      }\r\n"
					+ "    </style>\r\n"
					+ "  </head>\r\n"
					+ "  <body>\r\n"
					+ "\r\n"
					+ "    <div id=\"container\"><h3>Original formula</h3>" 
					+"<code>"
					+ predicate
					+"</code>\r\n"
					+ "</div>\r\n"
					+ "    <div id=\"sandy\">"
					+"<div style="
					+ "\"width:50px;height:50px;border:1px solid #000;\""
					+">"
					+ dependentQ.get(0)
					+ "</div>"
					+"<br>"
					+"<div style="
					+ "\"width:50px;height:50px;border:1px solid #000;\""
					+">"
					+ "<h3>"
					+ independetQ.get(0)
					+ "</h3"
					+ "</div>"
					+ "</div>\r\n"
					+ "\r\n"
					+ "  </body>\r\n"
					+ "</html>";
			
				
	            PrintWriter writer = new PrintWriter(myObj3);
	            
	            writer.write(Header);
	            writer.close();
				
			
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
			
		} 
	}
	public List<String> trimArray(List<String> abc)
	{
		List<String> trimmed = new ArrayList<String>(); 
		trimmed = abc;
		
		for(int i = 0; i < trimmed.size(); i++)
		{
			if(trimmed.get(i)=="," || trimmed.get(i)=="[")
			{
				trimmed.remove(i);
			}
		}
		
		
		
		return trimmed;
	}

	public String parseAssignment(Assignment assignment) {

		StringBuilder result = new StringBuilder();
		int tag = assignment.getTag();
		StringBuilder identifiersArray = new StringBuilder();
		FreeIdentifier[] identifiers;

		switch (tag) {
		case Formula.BECOMES_EQUAL_TO:

			BecomesEqualTo becomesEqualTo = (BecomesEqualTo) assignment;
			identifiers = becomesEqualTo.getAssignedIdentifiers();
			Expression[] rights = becomesEqualTo.getExpressions();
			StringBuilder expressionsArray = new StringBuilder();
			identifiersArray.append("[");
			expressionsArray.append("[");

			for (int i = 0; i < identifiers.length; i++) {
				String identifier = IBSimu.VARIABLE_PREFIX
						+ identifiers[i].getName();
				identifiersArray.append(identifier);
				expressionsArray.append(parseExpression(rights[i]));
			}
			identifiersArray.append("]");
			expressionsArray.append("]");

			result.append(IBSimu.TAB + "$B.becomesEqualTo(" + identifiersArray
					+ ", " + expressionsArray + ");");
			break;

		case Formula.BECOMES_MEMBER_OF:

			BecomesMemberOf becomesMemberOf = (BecomesMemberOf) assignment;
			identifiers = becomesMemberOf.getAssignedIdentifiers();
			Expression right = becomesMemberOf.getSet();
			String rightExpression = parseExpression(right);
			String identifier = IBSimu.VARIABLE_PREFIX
					+ identifiers[0].getName();
			result.append(IBSimu.TAB + "$B.becomesMemberOf(" + identifier
					+ ", " + rightExpression + ");");
			break;

		case Formula.BECOMES_SUCH_THAT:

			BecomesSuchThat becomesSuchThat = (BecomesSuchThat) assignment;
			identifiers = becomesSuchThat.getAssignedIdentifiers();
			Predicate predicate = becomesSuchThat.getCondition();

			int originalSize = boundIdentDeclList.size();
			BoundIdentDecl[] boundIdentDecls = becomesSuchThat
					.getPrimedIdents();
			for (BoundIdentDecl boundIdentDecl : boundIdentDecls) {
				boundIdentDeclList.add(boundIdentDecl);
			}

			StringBuilder domainArray = new StringBuilder();
			identifiersArray.append("[");
			domainArray.append("[");
			for (int i = 0; i < identifiers.length; i++) {
				String name = identifiers[i].getName();
				String primedName = IBSimu.VARIABLE_PREFIX + name + "._value";
				identifiersArray.append(IBSimu.VARIABLE_PREFIX + name);
				domainArray.append(parseDomain(primedName, predicate));
				if (i < identifiers.length - 1) {
					identifiersArray.append(", ");
					domainArray.append(", ");
				}
			}
			identifiersArray.append("]");
			domainArray.append("]");

			String predicateFunction = "function(){return "
					+ parsePredicate(predicate) + ";}";

			result.append(IBSimu.TAB + "$B.becomesSuchThat(" + identifiersArray
					+ ", " + predicateFunction + ", " + domainArray + ")");

			while (boundIdentDeclList.size() > originalSize) {
				boundIdentDeclList.remove(boundIdentDeclList.size() - 1);
			}
			break;
		}

		return result.toString();
	}

	private Object parseDomain(String name, Predicate predicate) {

		StringBuilder result = new StringBuilder();
		boolean patternMatched = false;
		
		if (predicate instanceof BinaryPredicate) { // forAll implication
			Predicate leftPredicate = ((BinaryPredicate) predicate).getLeft();
			if (leftPredicate.getTag() == Formula.IN) {
				RelationalPredicate relationalPredicate = (RelationalPredicate) leftPredicate;
				Expression left = relationalPredicate.getLeft();
				Expression right = relationalPredicate.getRight();
				if (name.equals(parseExpression(left))) {
				
					result.append(parseExpression(right));
					patternMatched = true;
				}
				else if(left.getTag() == Formula.MAPSTO)
				{
					result.append(parseExpression(right));
					patternMatched = true;
				}
			} else if (leftPredicate instanceof AssociativePredicate) {
				Predicate[] children = ((AssociativePredicate) leftPredicate)
						.getChildren();

				for (int i = 0; i < children.length; i++) {
					if (children[i].getTag() == Formula.IN) {
						RelationalPredicate relationalPredicate = (RelationalPredicate) children[i];
						Expression left = relationalPredicate.getLeft();
						Expression right = relationalPredicate.getRight();
						//int left_tag = left.getTag();
						if (name.equals(parseExpression(left))) {
							result.append(parseExpression(right));							
							patternMatched = true;
							break;
						}
				
					}  
				}	

			}

		} else if (predicate instanceof AssociativePredicate) {
			Predicate[] children = ((AssociativePredicate) predicate)
					.getChildren();
			
			//Predicate abc = ((AssociativePredicate)predicate);
			//abc.su
			String results="";
			String thiss = "";
			for (int i = 0; i < children.length; i++) {
				if (children[i].getTag() == Formula.IN) {
					RelationalPredicate relationalPredicate = (RelationalPredicate) children[i];
					Expression left = relationalPredicate.getLeft();
					Expression right = relationalPredicate.getRight();
					thiss = left.getFactory().toString();
					String a ="";
				
					results = parseExpression(left);
					if (name.equals(parseExpression(left))) {
						result.append(parseExpression(right));
						patternMatched = true;
						break;
					}
				}
			}	

		} else if (predicate instanceof RelationalPredicate) {
			RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;
			if (relationalPredicate.getTag() == Formula.IN) {
				Expression left = relationalPredicate.getLeft();
				Expression right = relationalPredicate.getRight();
				if (name.equals(parseExpression(left))) {
					result.append(parseExpression(right));
					//domainArrayS.add(result.toString());
					//if(domainArrayS.contains(names +",") || domainArrayS.contains("(" + names))
					//{
						//fileWr(names.toString());
					//	fileWr(domainArrayS.toString() + "relational");
					//}
					//fileWr(result.toString());
				
					patternMatched = true;
				}
			}
		}

		if (!patternMatched) { // unsupported pattern
			result.append("$B.EmptySet");
		}

		return result.toString();
	}



}