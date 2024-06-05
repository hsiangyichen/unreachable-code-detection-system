package server.backend;

import java.util.*;
import java.util.stream.Collectors;

import ProgramAnalysis.FlagTuple;
import ProgramAnalysis.VariableRange;
import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;

import org.springframework.web.bind.annotation.*;
import spoon.support.reflect.code.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class BackendServer {

	public Map<String, List<String>> globalUnreachableMap;

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello, World!";
	}

	public String TESTSTRING = "class A { public String m(String bob, int joe) { if (joe >2) { return bob; \n return bob;} else\n { return bob; \n return \" stuff\"} \n return \"am here\"; \n int i = 2; } public String b(String ann, int stan) { System.out.println(\"yeah\");\n return ann;} }";

	@GetMapping("/analyze")
	public Map<String, List<String>> analyze(@RequestParam String file) {
		Map<String, Map<Integer, String>> unreachableMap;
		try {
			unreachableMap = analyzeFile(file);
		} catch (Exception e) {
			List<String> errorMessage = new ArrayList<>();
			errorMessage.add("file is not a valid java file");
			Map<String, List<String>> errorMap = new HashMap<>();
			errorMap.put("error", errorMessage);
			return errorMap;
		}
		Map<String, List<String>> finalMap = new HashMap<>();
		for (String funcKey :unreachableMap.keySet()) {
			List<String> unreachableStatements = new ArrayList<>();
			for (Integer key : unreachableMap.get(funcKey).keySet()) {
				String formattedResult = String.format("line number %d: %s", key, unreachableMap.get(funcKey).get(key));
				unreachableStatements.add(formattedResult);
			}
			finalMap.put( funcKey, unreachableStatements);
		}
		this.globalUnreachableMap = finalMap;
		for (String key: this.globalUnreachableMap.keySet()) {
			System.out.println(key);
			for(String lines : this.globalUnreachableMap.get(key)) {
				System.out.println("		" + lines);
			}
			System.out.println();
		}
		return finalMap;
	}
	public Map<String, Map<Integer, String>> analyzeFile(String file) {
		CtClass classAST = Launcher.parseClass(file);
		Set<CtMethod> methods = classAST.getMethods();
		HashMap<String, Map<Integer, String>> map = new HashMap<>();
		for (CtMethod method : methods) {
			// formatting the function signature start
			List<CtStatement> statements = method.getBody().getStatements();
			String parameters = method.getParameters().toString();
			parameters = parameters.substring(1, parameters.length() - 1);
			String visibility = method.isPrivate() ? "private" : "public";
			String returnType = method.getType().toString();
			String functionName = method.getSimpleName();
			String functionSignature = String.format("%s %s %s(%s):", visibility, returnType, functionName, parameters);
			// formatting function signature end

			Map<Integer, String> unreachableMap = new HashMap<>();
			Map<String, VariableRange> varMapLocal = new HashMap<>();
			FlagTuple flag = new FlagTuple(false, false);

			//putting all parameters symbols into map start
			for (int i = 0; i < method.getParameters().toArray().length; i++) {
				String varSymbol = method.getParameters().get(i).toString().split(" ")[1];
				VariableRange newRange = new VariableRange();
				newRange.infUpperBound = true;
				newRange.infLowerBound = true;
				varMapLocal.put(varSymbol, newRange);
			}
			//putting all parameters symbols into map end



			processStatements(statements, unreachableMap, varMapLocal, flag );

			map.put(functionSignature, unreachableMap);
		}

		return map;
	}

	public FlagTuple ifStatementHelper(CtIf ifStatement, Map<Integer, String> unreachableMap, Map<String, VariableRange> varMapLocal ) {
		CtExpression<?> conditional = ifStatement.getCondition();
		Boolean doubleLiteralCheck = doubleLiteralCheck((CtBinaryOperator<?>) conditional);

		if (doubleLiteralCheck != null) {
			// specific case if condition is between two fixed values start

			if (doubleLiteralCheck.booleanValue()) {
				unreachableMap.put(ifStatement.getElseStatement().getPosition().getLine(), ifStatement.getElseStatement().toString());
				return processStatementNew(ifStatement.getThenStatement(), unreachableMap, varMapLocal);
			} else {
				unreachableMap.put(ifStatement.getPosition().getLine(), ifStatement.getThenStatement().toString());
				return processStatementNew(ifStatement.getElseStatement(), unreachableMap, varMapLocal);			}

			// specific case if condition is between two fixed values end
		}
		else {
			List<Map<String, VariableRange>> varList = variableConditionCheck((CtBinaryOperator<?>) conditional, varMapLocal);
			if (varList.getFirst() == null) {
				unreachableMap.put(ifStatement.getPosition().getLine(), ifStatement.getThenStatement().toString());
				return processStatementNew(ifStatement.getElseStatement(), unreachableMap, varMapLocal);
			}
			else if (varList.getLast() == null) {
				if( ifStatement.getElseStatement() != null )
					unreachableMap.put(ifStatement.getElseStatement().getPosition().getLine(), ifStatement.getElseStatement().toString());
				return processStatementNew(ifStatement.getThenStatement(), unreachableMap, varMapLocal);

			} else {
				Map<String, VariableRange> mThen = varList.getFirst();
				Map<String, VariableRange> mElse = varList.getLast();
				FlagTuple f1= processStatementNew(ifStatement.getThenStatement(), unreachableMap, mThen);
				FlagTuple f2= processStatementNew(ifStatement.getElseStatement(), unreachableMap, mElse);
				// changed from mElse to varMapLocal
				for (String key : varMapLocal.keySet()) {
					if (!mElse.containsKey(key)) {
						mElse.put(key, mThen.get(key));
					} else {
						VariableRange v1 = mElse.get(key);
						VariableRange v2 = mThen.get(key);
						v1.unionRange( v2 );
					}
				}

				for (String key: mElse.keySet()) {
					varMapLocal.put(key, mElse.get(key));
				}
				return new FlagTuple(f1.returnFlag && f2.returnFlag, f1.breakFlag && f2.returnFlag);

			}

		}
	}

	public boolean conditionHelper( CtExpression condition, Map<String, VariableRange> varMapLocal ) {
		if( condition instanceof  CtBinaryOperator )
			return this.binaryConditionHelper( (CtBinaryOperator) condition, varMapLocal );
		return false;
	}

	/**require: condition passed in to parameter must be a CtBinaryOperator**/
	/**function: checks if a boolean condition has no variables on either side, return true, if always true, false if always false, null if there is a variable**/
	/**modification: no modification to any data in this function**/
	public Boolean doubleLiteralCheck(CtBinaryOperator condition) {
		String operator = condition.getKind().name();
		CtExpression<?> a = condition.getLeftHandOperand();
		CtExpression<?> b = condition.getRightHandOperand();
		if( a instanceof CtLiteral<?> && b instanceof CtLiteral<?> ) {
			Integer aVal =((CtLiteral<Integer>) a).getValue();
			Integer bVal =((CtLiteral<Integer>) b).getValue();
			Boolean degenerateCase;
			switch (operator) {
				case "GT" -> degenerateCase = aVal > bVal;
				case "LT" -> degenerateCase = aVal < bVal;
				case "GE"-> degenerateCase = aVal >= bVal;
				case "LE" -> degenerateCase = aVal <= bVal;
				case "EQ" -> degenerateCase = aVal.equals(bVal);
				default -> degenerateCase = false;
			};
			return degenerateCase;
		}
		return null;

	}

	public List<VariableRange> gtCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val + 1;
		v1.infLowerBound = false;
		v1.infUpperBound = variableMaps.get(var).infUpperBound;
		v1.upperBound = variableMaps.get(var).upperBound;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = variableMaps.get(var).infLowerBound;
		v2.infUpperBound = false;
		v2.upperBound = val;
		newRange.add(v1);
		newRange.add(v2);
		return newRange;
	}

	public List<VariableRange> leCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val + 1;
		v1.infLowerBound = false;
		v1.infUpperBound = variableMaps.get(var).infUpperBound;
		v1.upperBound = variableMaps.get(var).upperBound;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = variableMaps.get(var).infLowerBound;
		v2.infUpperBound = false;
		v2.upperBound = val;
		newRange.add(v2);
		newRange.add(v1);
		return newRange;
	}

	public List<VariableRange> geCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val;
		v1.infLowerBound = false;
		v1.infUpperBound = variableMaps.get(var).infUpperBound;
		v1.upperBound = variableMaps.get(var).upperBound;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = variableMaps.get(var).infLowerBound;
		v2.infUpperBound = false;
		v2.upperBound = val - 1;
		newRange.add(v1);
		newRange.add(v2);
		return newRange;
	}

	public List<VariableRange> ltCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val;
		v1.infLowerBound = false;
		v1.infUpperBound = variableMaps.get(var).infUpperBound;
		v1.upperBound = variableMaps.get(var).upperBound;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = variableMaps.get(var).infLowerBound;
		v2.infUpperBound = false;
		v2.upperBound = val - 1;
		newRange.add(v2);
		newRange.add(v1);
		return newRange;
	}

	public List<VariableRange> eqCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val;
		v1.infLowerBound = false;
		v1.infUpperBound = false;
		v1.upperBound = val;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = true;
		v2.infUpperBound = true;
		v2.upperBound = variableMaps.get(var).upperBound;
		newRange.add(v1);
		newRange.add(v2);
		return newRange;
	}

	public List<VariableRange> neqCreator(String var, Integer val, Map<String, VariableRange> variableMaps) {
		List<VariableRange> newRange = new ArrayList<>();
		VariableRange v1 = new VariableRange();
		v1.lowerBound = val;
		v1.infLowerBound = false;
		v1.infUpperBound = false;
		v1.upperBound = val;
		VariableRange v2 = new VariableRange();
		v2.lowerBound = variableMaps.get(var).lowerBound;
		v2.infLowerBound = variableMaps.get(var).infLowerBound;
		v2.infUpperBound = variableMaps.get(var).infUpperBound;
		v2.upperBound = variableMaps.get(var).upperBound;
		newRange.add(v2);
		newRange.add(v1);
		return newRange;
	}

	public List<VariableRange> boundChecking(String var, Integer val, String op, Map<String, VariableRange> variableMaps) {
		boolean infubound = variableMaps.get(var).infUpperBound;
		boolean inflbound = variableMaps.get(var).infLowerBound;
		Integer ubound = variableMaps.get(var).upperBound;
		Integer lbound = variableMaps.get(var).lowerBound;

		//case where upper bound locked but lower bound infinite
		if (!infubound) {
			switch (op) {
				case "GT" -> {
					if (ubound<= val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}

				}
				case "LE" -> {
					if (ubound<= val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}
				}
				case "GE" -> {
					if (ubound< val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}
				}
				case "LT" -> {
					if (ubound< val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}
				}
				case "EQ" -> {
					if (ubound< val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}
				}
				default -> {
					if (ubound < val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}
				}
			}
		}
		//case where upper bound infinite but lower bound not
		if (!inflbound) {
			switch (op) {
				case "GT" -> {
					if (lbound> val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}

				}
				case "LE" -> {
					if (lbound> val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}
				}
				case "GE" -> {
					if (lbound>= val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}
				}
				case "LT" -> {
					if (lbound>= val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}
				}
				case "EQ" -> {
					if (lbound > val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(null);
						newRange.add(variableMaps.get(var));
						return newRange;
					}
				}
				default -> {
					if (lbound > val) {
						List<VariableRange> newRange = new ArrayList<>();
						newRange.add(variableMaps.get(var));
						newRange.add(null);
						return newRange;
					}
				}
			}
		}

		switch (op) {
			case "GT" -> {
				return gtCreator(var, val, variableMaps);
			}
			case "LE" -> {
				return leCreator(var, val, variableMaps);
			}
			case "GE" -> {
				return geCreator(var, val, variableMaps);
			}
			case "LT" -> {
				return ltCreator(var, val, variableMaps);
			}
			case "EQ" -> {
				return eqCreator(var, val, variableMaps);
			}
			default -> {
				return neqCreator(var, val, variableMaps);
			}
		}
	}

	public Map<String, VariableRange> varMapClone(Map<String, VariableRange> varMapLocal) {
		Map<String, VariableRange> b1 = new HashMap<>();
		for (String key: varMapLocal.keySet()) {
			b1.put(key, (VariableRange) varMapLocal.get(key).clone());
		}
		return b1;
	}

	public List<Map<String, VariableRange>> varRangeToNewMap(List<VariableRange> vRange, Map<String, VariableRange> varMapLocal, String var) {
		List<Map<String, VariableRange>> newList = new ArrayList<>();
		for (VariableRange vr: vRange) {
			if (vr !=null) {
				Map<String, VariableRange> b1 = varMapClone(varMapLocal);
				b1.put(var, vr);
				newList.add(b1);
			} else {
				newList.add(null);
			}
		}
		return newList;
	}

	public List<Map<String, VariableRange>> variableConditionCheck(CtBinaryOperator condition, Map<String, VariableRange> varMapLocal) {
		String operator = condition.getKind().name();
		CtExpression<?> a = condition.getLeftHandOperand();
		CtExpression<?> b = condition.getRightHandOperand();
		List<VariableRange> newRange;
		if (a instanceof CtVariableRead<?> && b instanceof CtVariableRead<?>) {
			VariableRange bVal = varMapLocal.get(((CtVariableRead<?>) b).toString());
			String aVal =((CtVariableRead<?>) a).toString();
			if (bVal.infLowerBound || bVal.infUpperBound || (bVal.lowerBound != bVal.upperBound)) {
				VariableRange c1 = (VariableRange) varMapLocal.get(aVal).clone();
				VariableRange c2 = (VariableRange) varMapLocal.get(aVal).clone();
				newRange = new ArrayList<>();
				newRange.add(c1);
				newRange.add(c2);
				return varRangeToNewMap(newRange, varMapLocal, aVal);

			} else {
				Integer bActualVal = bVal.lowerBound;
				switch (operator) {
					case "GT" -> {
						newRange = boundChecking(aVal, bActualVal, "GT" ,varMapLocal);
					}
					case "LT" -> {
						newRange = boundChecking(aVal, bActualVal, "LT", varMapLocal);

					}
					case "GE"-> {
						newRange = boundChecking(aVal, bActualVal, "GE",varMapLocal);

					}
					case "LE" -> {
						newRange = boundChecking(aVal, bActualVal, "LE",varMapLocal);
					}
					case "EQ" -> {
						newRange = boundChecking(aVal, bActualVal, "EQ", varMapLocal);


					}
					default -> {
						newRange = boundChecking(aVal, bActualVal, "NEQ",varMapLocal);
					}
				}
				return varRangeToNewMap(newRange, varMapLocal, aVal);
			}

		}
		else if( a instanceof CtVariableRead<?> && b instanceof CtLiteral<?>) {
			String aVal =((CtVariableRead<?>) a).toString();
			Integer bVal = ((CtLiteral<Integer>) b).getValue();
			switch (operator) {
				case "GT" -> {
					newRange = boundChecking(aVal, bVal, "GT" ,varMapLocal);
				}
				case "LT" -> {
					newRange = boundChecking(aVal, bVal, "LT", varMapLocal);

				}
				case "GE"-> {
					newRange = boundChecking(aVal, bVal, "GE",varMapLocal);

				}
				case "LE" -> {
					newRange = boundChecking(aVal, bVal, "LE",varMapLocal);
				}
				case "EQ" -> {
					newRange = boundChecking(aVal, bVal, "EQ",varMapLocal);

				}
				default -> {
					newRange = boundChecking(aVal, bVal, "NEQ",varMapLocal);
				}
			}
			return varRangeToNewMap(newRange, varMapLocal, aVal);

		} else if ( b instanceof CtVariableRead<?> && a instanceof CtLiteral<?>) {
			String aVal =((CtVariableRead<?>) b).toString();
			Integer bVal =((CtLiteral<Integer>) a).getValue();
			switch (operator) {
				case "GT" -> {
					newRange = boundChecking(aVal, bVal, "GT",varMapLocal);
				}
				case "LT" -> {
					newRange = boundChecking(aVal, bVal, "LT",varMapLocal);

				}
				case "GE"-> {
					newRange = boundChecking(aVal, bVal, "GE",varMapLocal);

				}
				case "LE" -> {
					newRange = boundChecking(aVal, bVal, "LE",varMapLocal);
				}
				case "EQ" -> {
					newRange = boundChecking(aVal, bVal, "EQ",varMapLocal);

				}
				default -> {
					newRange = boundChecking(aVal, bVal, "NEQ",varMapLocal);
				}
			}
			return varRangeToNewMap(newRange, varMapLocal, aVal);

		} else {
			List<Map<String, VariableRange>> defaultCase = new ArrayList<>();
			Map<String, VariableRange> c1 = varMapClone(varMapLocal);
			Map<String, VariableRange> c2 = varMapClone(varMapLocal);

			defaultCase.add(c1);
			defaultCase.add(c2);
			return defaultCase;
		}
	}

	public boolean binaryConditionHelper( CtBinaryOperator condition, Map<String, VariableRange> varMapLocal ) {
		String operator = condition.getKind().name();
		if( operator.equals( "AND" ) || operator.equals( "OR" ) ) {
			boolean a = this.conditionHelper( condition.getLeftHandOperand(), varMapLocal );
			boolean b = this.conditionHelper( condition.getRightHandOperand() , varMapLocal);
			return operator.equals( "AND" )? a && b : a || b;
		}
		VariableRange a = this.intExpressionHelper( condition.getLeftHandOperand(), varMapLocal );
		VariableRange b = this.intExpressionHelper( condition.getRightHandOperand(), varMapLocal );
		return switch (operator) {
			case "GT" -> a.gt(b);
			case "LT" -> a.lt(b);
			case "GE" -> a.ge(b);
			case "LE" -> a.le(b);
			case "EQ" -> a.equals(b);
			default -> false;
		};
	}

	public void variableHelper( CtLocalVariable<?> variableStatement, Map<String, VariableRange> varMapLocal ) {
		String varName = variableStatement.getSimpleName();
		if( variableStatement.getDefaultExpression() == null ) {
			varMapLocal.put(varName, new VariableRange());
			return;
		}
		varMapLocal.put( varName, this.intExpressionHelper( variableStatement.getDefaultExpression(), varMapLocal ) );
		return;
	}

	public VariableRange intExpressionHelper(CtExpression expression, Map<String, VariableRange> varMapLocal ) {
		VariableRange varRange = new VariableRange();
		if( expression instanceof CtLiteral<?> ) {
			varRange.lowerBound = ((CtLiteral<Integer>) expression).getValue();
			varRange.upperBound = ((CtLiteral<Integer>) expression).getValue();
			return varRange;
		}
		if( expression instanceof CtBinaryOperator<?> ) {
			return this.binaryOperatorHelper( (CtBinaryOperator) expression, varMapLocal );
		}
		if( expression instanceof CtVariableRead<?> ) {
			return (VariableRange) varMapLocal.get( ((CtVariableRead<?>) expression).toString() ).clone();
		}
		return new VariableRange();
	}

	public VariableRange binaryOperatorHelper( CtBinaryOperator binaryOperator, Map<String, VariableRange> varMapLocal ) {
		VariableRange varRange = new VariableRange();
		VariableRange a = this.intExpressionHelper( binaryOperator.getLeftHandOperand(), varMapLocal );
		VariableRange b = this.intExpressionHelper( binaryOperator.getRightHandOperand(), varMapLocal );
		if( binaryOperator.getKind().name().equals( "PLUS" ) )
			varRange.plus( a, b );
		else if( binaryOperator.getKind().name().equals( "MINUS" ) )
			varRange.minus( a, b );
		else if( binaryOperator.getKind().name().equals( "MUL" ) )
			varRange.mult( a, b );
		else if( binaryOperator.getKind().name().equals( "DIV" ) )
			varRange.div( a, b );
		return varRange;
	}



	public FlagTuple whileLoopHelper(CtWhile statement, Map<Integer, String> unreachableMap, Map<String, VariableRange> varMapLocal) {
		CtExpression<?> conditional = statement.getLoopingExpression();

		/**
		 * If the conditional is not valid, then everything inside it shouldn't run
		 * **/
		List<Map<String, VariableRange>> checkConditionalValid = variableConditionCheck((CtBinaryOperator) conditional, varMapLocal);
		List<CtStatement> functionBody = ((CtBlockImpl) ((CtWhileImpl) statement).getBody()).getStatements();
		if (checkConditionalValid.get(0) == null) {
			// put all the while loop's body into the unreachable map
			for (CtStatement currStatement : functionBody) {
				int lineNum = currStatement.getPosition().getLine();
				unreachableMap.put(lineNum, currStatement.toString());
			}
			// make the statement for while
			int lineNumWhileStart = statement.getPosition().getLine();
			String whileLoopStatement = conditional.toString() + "{";
			unreachableMap.put( lineNumWhileStart, whileLoopStatement);
			// make statement for closing of for loop
			int lineNumClosingBracket = lineNumWhileStart + functionBody.size() + 1;
			String whileLoopClosing = "}";
			unreachableMap.put(lineNumClosingBracket, whileLoopClosing);
			return new FlagTuple( false, false );
		}

		boolean infiniteLoopFlag = false;

		// specific case if condition is between two fixed values start
		Boolean doubleLiteralCheck = doubleLiteralCheck((CtBinaryOperator<?>) conditional);
		if (doubleLiteralCheck != null) {
			if (!doubleLiteralCheck.booleanValue()) {
				unreachableMap.put(statement.getPosition().getLine(), statement.toString());
				return new FlagTuple(false, false);
			} else
				infiniteLoopFlag = true;
		}

		// TODO: figure out the variable in our condition
		// TODO: if the case is simple, set a flag to try to calculate maxNumberOfIterations
		// TODO: Iterate through the loop to find delta
		// TODO: If flag for calculating maxNumberOfIterations, calculate it
		// 		TODO: If it is not clear how many loops, set it to be unknown
		// TODO: Repeat until both unreachableList and delta stop changing
		// TODO: Union the resulting VML of the loop with the initial VML
		// TODO: If infiniteLoopFlag, then set FlagTuple( true, false )

		boolean simpleCondition = checkForEasyCondition( conditional );
		boolean unknownNumberOfLoops = !simpleCondition;
		infiniteLoopFlag = infiniteLoopFlag || ( simpleCondition && checkForInfiniteLoopCondition( (CtBinaryOperator<?>) conditional, varMapLocal ) );

		Map<String, VariableRange> varMapLocal_zero = deepCopyVarMap( varMapLocal );
		Map<String, VariableRange> varMapLocal_curr_ite;
		Map<String, VariableRange> delta = new HashMap<>();
		Map<String, VariableRange> delta_last_ite;
		Map<Integer, String> unreachable_old;
		Map<Integer, String> unreachable_curr = new HashMap<>();
		FlagTuple flag = new FlagTuple( false, false );
		int numberLoops = 1;

		do {
			unreachable_old = unreachable_curr;
			unreachable_curr = new HashMap<>();
			if( unknownNumberOfLoops )
				varMapLocal_curr_ite = addDeltaUnknownIterations( varMapLocal_zero, delta );
			else
				varMapLocal_curr_ite = addDelta( varMapLocal_zero, delta, numberLoops );
			delta_last_ite = deepCopyVarMap( delta );
			flag = processStatements( functionBody, unreachable_curr, varMapLocal_curr_ite, flag );
			if( flag.returnFlag || flag.breakFlag )
				break;
			if( unknownNumberOfLoops )
				delta = getDeltaUnknownIterations( varMapLocal_curr_ite, varMapLocal_zero );
			else
				delta = getDelta( varMapLocal_curr_ite,varMapLocal_zero, delta, numberLoops );
			if( simpleCondition ) {
				unknownNumberOfLoops = checkForUnknownNumberOfIterations((CtBinaryOperator) conditional, varMapLocal, delta);
				if( !unknownNumberOfLoops ) {
					numberLoops = michelleCalculateWhileNumLoops((CtBinaryOperator) conditional, varMapLocal_zero, delta);
					unknownNumberOfLoops = numberLoops == -1;
				}

			}
		} while ( differentUnreachableMaps(unreachable_curr, unreachable_old) || changeInDelta( delta, delta_last_ite ) );

		// Update varMapLocal
		if( unknownNumberOfLoops )
			varMapLocal_curr_ite = addDeltaUnknownIterations( varMapLocal_zero, delta );
		else
			varMapLocal_curr_ite = addDelta( varMapLocal_zero, delta, numberLoops );
		for( String key: varMapLocal.keySet() ) {
			varMapLocal.get( key ).unionRange(varMapLocal_curr_ite.get(key));
		}
		unreachableMap.putAll(unreachable_curr);
		flag.breakFlag = false;
		if( infiniteLoopFlag )
			flag.returnFlag = true;
		return flag;
	}

	private boolean checkForUnknownNumberOfIterations(CtBinaryOperator conditional, Map<String, VariableRange> varMapLocal, Map<String, VariableRange> delta) {
		String lhsName = ((CtVariableReadImpl)((CtBinaryOperatorImpl)conditional).getLeftHandOperand()).getVariable().getSimpleName();
		if( varMapLocal.get( lhsName ).infLowerBound || varMapLocal.get( lhsName ).infUpperBound )
			return true;
		if( ( delta.get( lhsName ).upperBound > 0 || delta.get( lhsName ).infUpperBound ) && ( delta.get( lhsName ).lowerBound < 0 || delta.get( lhsName ).infLowerBound ) )
			return true;
		if( ((CtBinaryOperatorImpl)conditional).getRightHandOperand() instanceof CtLiteral<?> )
			return false;
		String rhsName = ((CtVariableReadImpl)((CtBinaryOperatorImpl)conditional).getRightHandOperand()).getVariable().getSimpleName();
		if( varMapLocal.get( rhsName ).infLowerBound || varMapLocal.get( rhsName ).infUpperBound )
			return true;
		if( ( !delta.get( rhsName ).equals( 0 ) ) )
			return true;
		return false;
	}

	private boolean checkForInfiniteLoopCondition(CtBinaryOperator<?> expression, Map<String, VariableRange> varMapLocal) {
		String operator = expression.getKind().name();
		VariableRange lhs = this.intExpressionHelper( expression.getLeftHandOperand(), varMapLocal );
		VariableRange rhs = this.intExpressionHelper( expression.getRightHandOperand(), varMapLocal );
		if( !operator.equals( "EQ" ) && !operator.equals( "NEQ" ) )
			return false;
		if( operator.equals( "EQ" ) )
			return lhs.equals( rhs ) && !lhs.infUpperBound && !lhs.infLowerBound;
		if( operator.equals( "NEQ" ) )
			return !lhs.equals( rhs ) || lhs.infLowerBound || lhs.infUpperBound;
		return false;
	}

	private Map<String, VariableRange> getDeltaUnknownIterations(Map<String, VariableRange> varMapLocal_i, Map<String, VariableRange> varMapLocal_zero ) {
		Map<String, VariableRange> delta_new = new HashMap<>();
		for( String key: varMapLocal_zero.keySet() ) {
			VariableRange range = new VariableRange();
			VariableRange currRange = varMapLocal_i.get( key );
			VariableRange oldRange = varMapLocal_zero.get( key );
			if( currRange.infUpperBound || currRange.upperBound - oldRange.upperBound > 0 )
				range.upperBound = 1;
			if( currRange.infLowerBound || currRange.lowerBound - oldRange.lowerBound < 0 )
				range.lowerBound = 1;
			delta_new.put( key, range );
		}
		return delta_new;
	}

	private Map<String, VariableRange> addDeltaUnknownIterations(Map<String, VariableRange> varMapLocal_zero, Map<String, VariableRange> delta) {
		Map<String, VariableRange> varMapLocal_i = new HashMap<>();
		VariableRange infRange = new VariableRange();
		infRange.infUpperBound = true;
		for( String key: varMapLocal_zero.keySet() ) {
			VariableRange range = (VariableRange) varMapLocal_zero.get( key ).clone();
			if( delta.containsKey( key ) ) {
				VariableRange rangeDelta = (VariableRange) delta.get( key ).clone();
				rangeDelta.mult( rangeDelta, infRange );
				rangeDelta.plus( rangeDelta, range );
				range.unionRange( rangeDelta );
			}
			varMapLocal_i.put( key, range );
		}
		return varMapLocal_i;
	}

	private boolean checkForEasyCondition(CtExpression<?> conditional) {
		if( !(conditional instanceof CtBinaryOperator<?> cond) )
			return false;
		if( cond.getKind().toString().equals( "AND" ) || cond.getKind().toString().equals( "OR" ))
			return false;
		if( !( cond.getLeftHandOperand() instanceof CtVariableRead<?>) )
			return false;
		if( !( cond.getRightHandOperand() instanceof CtLiteral<?> ) && !( cond.getRightHandOperand() instanceof CtVariableRead<?>) )
			return false;
		return true;
	}

	/**
	 * Returns k, which is the smallest value of delta that is not <= 0
	 * **/
	private int michelleHelpDeltaLT(String leftVarName, Map<String, VariableRange> delta ) {
		// check if the range starts with a value greater than 0
		int delta_lower = delta.get(leftVarName).lowerBound;
		int delta_upper = delta.get(leftVarName).upperBound;
		if (delta_lower > 0) {
			return delta_lower;
		} else if (delta_upper <= 0) {
			// the entire range is less than or equal to 0, return 0 --> it's going to be infinite
			return 0;
		}
		else {
			// if the range includes 0, the smallest positive number is 1
			return 1;
		}
	}

	/**
	 * Returns k,  which is the greatest value in the range which is NOT >= 0
	 * k is -1 or something smaller
	 * **/
	private int michelleFindGreatestNonPositive(String leftVarName, Map<String, VariableRange> delta ) {
		// check if the entire range is less than 0
		int delta_lower = delta.get(leftVarName).lowerBound;
		int delta_upper = delta.get(leftVarName).upperBound;
		if (delta_upper < 0) {
			return delta_upper;
		} else if (delta_lower >= 0) {
			// If the start of the range is >= 0, there's no number in the range that is < 0
			return 0;
		} else {
			// If the range includes 0, the largest number less than 0 is -1
			return -1;
		}
	}

	/**
	 * Either both are literals, or a variable read
	 * The operator is going to be >, >=, <, <=
	 * Takes in delta --> which is how much it can change in one iteration
	 * Assume: at least 1 variable on the left side
	 * Right side can be a variable or a constant
	 * Restriction: loop can terminate
	 **/
	private int michelleCalculateWhileNumLoops(CtBinaryOperator<?> expression, Map<String, VariableRange> varMapLocal_zero, Map<String, VariableRange> delta ) {
		VariableRange left = this.intExpressionHelper(expression.getLeftHandOperand(), varMapLocal_zero);
		String leftVarName = ((CtVariableReadImpl)((CtBinaryOperatorImpl)expression).getLeftHandOperand()).getVariable().toString();
		VariableRange right = this.intExpressionHelper(expression.getRightHandOperand(), varMapLocal_zero);
		String operator = ((CtBinaryOperatorImpl)expression).getKind().name();


		/**
		 * left = [1,2]
		 * a+=1
		 * a-=2
		 *
		 if the delta.left side is negative, then it has a minus equal somewhere
		 right side is positive, plus equal
		 **/
		int count = 0;
		switch (operator) {
			case "LT" -> {
				float k = (float) this.michelleHelpDeltaLT(leftVarName, delta);
				float i = (float) left.lowerBound;
				float n = (float) right.upperBound;
				if (k == 0) {
					count = -1;
				} else {
					count = (int) Math.ceil((n - i) / k);
				}
			}
			case "LE" -> {
				float k = (float) this.michelleHelpDeltaLT(leftVarName, delta);
				float i = (float) left.lowerBound;
				float n = (float) right.upperBound;
				if (k == 0) {
					count = -1;
				} else {
					count = (int) Math.ceil((n - i+1) / k);
				}
			}
			case "GT" -> {
				float k = (float) this.michelleFindGreatestNonPositive(leftVarName, delta);
				float i = (float) left.upperBound;
				float n = (float) right.lowerBound;
				if (k == 0) {
					count = -1;
				} else {
					count = (int) Math.ceil((n - i) / k);
				}
			}
			case "GE"-> {
				float k = (float) this.michelleFindGreatestNonPositive(leftVarName, delta);
				float i = (float) left.upperBound;
				float n = (float) right.lowerBound;
				if (k == 0) {
					count = -1;
				} else {
					count = (int) Math.ceil((n - i+1) / k);
				}
			}
			default -> {
				// NEQ, EQ
				return -1;
			}
		}
		return count;
	}

	public FlagTuple processStatementNew(CtStatement statement, Map<Integer, String> unreachableMap, Map<String, VariableRange> varMapLocal) {
		if( statement == null ) {
			return new FlagTuple( false, false );
		}
		if (statement instanceof CtIf) {
			return this.ifStatementHelper( (CtIf) statement, unreachableMap, varMapLocal );
		}

		if (statement instanceof CtBlock<?>) {
			return this.processStatements( ((CtBlock<?>) statement).getStatements() , unreachableMap, varMapLocal, new FlagTuple( false, false ) );
		}

		if (statement instanceof CtReturn<?>) {
			return new FlagTuple(true, false);
		}

		if ( statement instanceof CtLocalVariable<?>) {
			this.variableHelper( (CtLocalVariable<?>) statement, varMapLocal );
			return new FlagTuple(false, false);
		}

		if ( statement instanceof CtAssignment<?,?>) {
			return this.assignmentHelper( (CtAssignment<?,?>) statement, varMapLocal );
		}

		if (statement instanceof CtWhile) {
			return this.whileLoopHelper( (CtWhile) statement, unreachableMap, varMapLocal );
		}

		if ( statement instanceof CtFor) {
			ArrayList listOfVariablesChanged = new ArrayList();
			return forLoopHelper((CtFor) statement, unreachableMap, listOfVariablesChanged, varMapLocal);
		}
		return new FlagTuple(false, false);
	}

	private FlagTuple assignmentHelper(CtAssignment statement, Map<String, VariableRange> varMapLocal) {
		String varName = ((CtVariableAccessImpl)statement.getAssigned()).getVariable().getSimpleName();
		VariableRange toAssign = this.intExpressionHelper( statement.getAssignment(), varMapLocal );

		if( statement instanceof CtOperatorAssignmentImpl ) {
			String operator = ((CtOperatorAssignmentImpl) statement).getKind().name();
			if( operator.equals( "PLUS" ) ) {
				varMapLocal.get( varName ).plus( varMapLocal.get( varName ), toAssign );
			} else if( operator.equals( "MINUS" ) ) {
				varMapLocal.get( varName ).minus( varMapLocal.get( varName ), toAssign );
			} else if( operator.equals( "MULT" ) ) {
				varMapLocal.get( varName ).mult( varMapLocal.get( varName ), toAssign );
			}
			return new FlagTuple( false, false );
		}
		varMapLocal.put( varName, toAssign );
		return new FlagTuple( false, false );
	}


	private int forLoopCounter(String initalizedStartingVarName, Map<String, VariableRange> varMapLocal, CtExpression<Boolean> conditionalFunction, CtStatement updatingFunction) {
		String operator = ((CtBinaryOperatorImpl)conditionalFunction).getKind().name();
		// get i
		VariableRange startingVar = varMapLocal.get(initalizedStartingVarName);

		// get n
		VariableRange loopBound = this.intExpressionHelper( ((CtBinaryOperatorImpl<Boolean>) conditionalFunction).getRightHandOperand(), varMapLocal );

		// get k
		String variableK = ((CtOperatorAssignmentImpl)updatingFunction).getAssignment().toString();
		int k = Integer.parseInt(variableK);

		int count = 0;
		float n = (float) loopBound.lowerBound;
		float i = (float) startingVar.lowerBound;

		switch (operator) {
			case "LT" -> {
				if( ((CtOperatorAssignmentImpl) updatingFunction).getKind().name().equals( "PLUS" ) )
					count = (int) Math.ceil((n - i) / k);
				else
					count = -1;
			}
			case "LE" -> {
				if( ((CtOperatorAssignmentImpl) updatingFunction).getKind().name().equals( "PLUS" ) )
					count = (int) Math.ceil((n - i+1) / k);
				else
					count = -1;
			}
			case "GT" -> {
				if( ((CtOperatorAssignmentImpl) updatingFunction).getKind().name().equals( "MINUS" ) )
					count = (int) Math.ceil((n - i) / k);
				else
					count = -1;
			}
			case "GE"-> {
				if( ((CtOperatorAssignmentImpl) updatingFunction).getKind().name().equals( "MINUS" ) )
					count = (int) Math.ceil((n - i+1) / k);
				else
					count = -1;
			}
			case "EQ" -> {
				if (i == n) {
					count = 1;
				} else
					count = 0;
			}
			default -> {
				// "NEQ"
				count = -1;
			}
		}
		return count;
	}

	public FlagTuple forLoopHelper(CtFor statement, Map<Integer, String> unreachableMap, ArrayList listOfVariablesChanged, Map<String, VariableRange> varMapLocal) {

		// deal with initializing function
		CtStatement initializedStartingVar = statement.getForInit().get(0);
		String initalizedStartingVarName = ((CtLocalVariableImpl)initializedStartingVar).getSimpleName();
		VariableRange initializedStartingVarValue = this.intExpressionHelper(((CtLocalVariableImpl<?>) initializedStartingVar).getDefaultExpression(), varMapLocal);
		// add it to the local varMap
		varMapLocal.put(initalizedStartingVarName, initializedStartingVarValue);

		// extract other functions
		CtExpression<Boolean> conditionalExpression = statement.getExpression();
		CtStatement updatingFunction = statement.getForUpdate().get(0);

		// get body
		List<CtStatement> functionBody = ((CtBlock) statement.getBody()).getStatements();

		FlagTuple x = checkIfForLoopInitialized(statement, unreachableMap, varMapLocal, initializedStartingVar, conditionalExpression, updatingFunction, functionBody);
		if (x != null) return x;

		// count how many times loop happened
		int numberLoops = forLoopCounter(initalizedStartingVarName, varMapLocal, conditionalExpression, updatingFunction);
		boolean infiniteLoopFlag = false;

		if (numberLoops < 0) {
			infiniteLoopFlag = true; // only matters when exploding ranges
		}

		// lines exclusive to for
		int k = Integer.parseInt(((CtOperatorAssignmentImpl)updatingFunction).getAssignment().toString());
		if( ((CtOperatorAssignmentImpl) updatingFunction).getKind().name().equals( "PLUS" ) )
			varMapLocal.get( initalizedStartingVarName ).upperBound += k * numberLoops;
		else
			varMapLocal.get( initalizedStartingVarName ).lowerBound += k * numberLoops;

		// Pass through the body
		Map<String, VariableRange> varMapLocal_zero = deepCopyVarMap( varMapLocal );
		Map<String, VariableRange> varMapLocal_curr_ite;
		Map<String, VariableRange> delta = new HashMap<>();
		Map<String, VariableRange> delta_last_ite;
		Map<Integer, String> unreachable_old;
		Map<Integer, String> unreachable_curr = new HashMap<>();
		FlagTuple flag = new FlagTuple( false, false );

		do {
			unreachable_old = unreachable_curr;
			unreachable_curr = new HashMap<>();
			varMapLocal_curr_ite = addDelta( varMapLocal_zero, delta, numberLoops );
			delta_last_ite = deepCopyVarMap( delta );
			flag = processStatements( functionBody, unreachable_curr, varMapLocal_curr_ite, flag );
			if( flag.returnFlag || flag.breakFlag )
				break;
			delta = getDelta( varMapLocal_curr_ite,varMapLocal_zero, delta, numberLoops );
		} while ( differentUnreachableMaps(unreachable_curr, unreachable_old) || changeInDelta( delta, delta_last_ite ) );

		// Update varMapLocal
		varMapLocal_curr_ite = addDelta( varMapLocal_zero, delta, numberLoops );
		for( String key: varMapLocal.keySet() ) {
			varMapLocal.get( key ).unionRange(varMapLocal_curr_ite.get(key));
		}
		unreachableMap.putAll(unreachable_curr);
		flag.breakFlag = false;
		if( infiniteLoopFlag )
			flag.returnFlag = true;
		return flag;
	}

	private FlagTuple checkIfForLoopInitialized(CtFor statement, Map<Integer, String> unreachableMap, Map<String, VariableRange> varMapLocal, CtStatement initializedStartingVar, CtExpression<Boolean> conditionalExpression, CtStatement updatingFunction, List<CtStatement> functionBody) {
		/**
		 * Check if the initialized function and conditional runs
		 * If it doesn't run, everything in the for loop body is dead code
		 * Add entire `for loop` to unreachable map
		 **/
		List<Map<String, VariableRange>> checkFirstRunResult = variableConditionCheck((CtBinaryOperator) conditionalExpression, varMapLocal);
		//  null state means that the then case is never true
		if (checkFirstRunResult.get(0) == null) {
			// put all the for loop's body into the unreachable map
			for ( CtStatement currStatement: functionBody) {
				int lineNum = currStatement.getPosition().getLine();
				unreachableMap.put(lineNum, currStatement.toString());
			}
			int lineNumForStart = statement.getPosition().getLine();

			// make the statement for for-loop
			String forLoopStartingStatement = "for (" + initializedStartingVar.toString() + ";" + conditionalExpression.toString() + ";" + updatingFunction.toString() + ") {";
			unreachableMap.put(lineNumForStart, forLoopStartingStatement);

			// make statement for closing of for loop
			int lineNumClosingBracket = lineNumForStart + functionBody.size() + 1;
			String forLoopClosing = "}";
			unreachableMap.put(lineNumClosingBracket, forLoopClosing);

			// return so that the function which called the for loop can still run
			return new FlagTuple(false, false);
		}
		return null;
	}

	public FlagTuple processStatements(List<CtStatement> statements, Map<Integer, String> unreachableMap, Map<String, VariableRange> varMapLocal, FlagTuple flag ) {
		for (CtStatement statement : statements) {
			if (!flag.returnFlag) {
				flag = processStatementNew(statement, unreachableMap, varMapLocal );
			} else {
				int lineNum = statement.getPosition().getLine();
				unreachableMap.put(lineNum, statement.toString());
			}
		}
		return flag;
	}

	public Map<String, VariableRange> deepCopyVarMap( Map<String, VariableRange> varMap ) {
		return varMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (VariableRange) e.getValue().clone()));
	}

	// returns true if the two maps have the same keys;
	public boolean differentUnreachableMaps(Map<Integer, String> a, Map<Integer, String> b ) {
		return !a.keySet().equals(b.keySet());
	}

	public boolean changeInDelta( Map<String, VariableRange> delta, Map<String, VariableRange> delta_old ) {
		for( String var: delta.keySet() ) {
			if( !delta_old.containsKey( var ) )
				return true;
			if( !delta.get( var ).equals( delta_old.get( var ) ) )
				return true;
		}
		return false;
	}

	public Map<String, VariableRange> getDelta( Map<String, VariableRange> varMapLocal_i, Map<String, VariableRange> varMapLocal_zero, Map<String, VariableRange> delta_old, int n ) {
		Map<String, VariableRange> delta_new = new HashMap<>();
		for( String key: varMapLocal_zero.keySet() ) {
			VariableRange range = varMapLocal_i.get( key );
			if( delta_old.containsKey( key ) ) {
				VariableRange range_old = (VariableRange) varMapLocal_zero.get( key ).clone();
				VariableRange rangeDelta = new VariableRange().fromInt( n );
				rangeDelta.mult( rangeDelta, delta_old.get( key ) );
				rangeDelta.plus( rangeDelta, range_old );
				range_old.unionRange( rangeDelta );
				range.lowerBound -= range_old.lowerBound;
				range.upperBound -= range_old.upperBound;
				range.infLowerBound = range.infLowerBound || range_old.infLowerBound;
				range.infUpperBound = range.infUpperBound || range_old.infUpperBound;
				range.unionRange( delta_old.get( key ) );
			} else {
				range.lowerBound -= varMapLocal_zero.get( key ).lowerBound;
				range.upperBound -= varMapLocal_zero.get( key ).upperBound;
			}
			delta_new.put( key, range );
		}
		return delta_new;
	}

	public Map<String, VariableRange> addDelta( Map<String, VariableRange> varMapLocal_zero, Map<String, VariableRange> delta, int n ) {
		Map<String, VariableRange> varMapLocal_i = new HashMap<>();
		for( String key: varMapLocal_zero.keySet() ) {
			VariableRange range = (VariableRange) varMapLocal_zero.get( key ).clone();
			if( delta.containsKey( key ) ) {
				VariableRange rangeDelta = new VariableRange().fromInt( n );
				rangeDelta.mult( rangeDelta, delta.get( key ) );
				rangeDelta.plus( rangeDelta, range );
				range.unionRange( rangeDelta );
			}
			varMapLocal_i.put( key, range );
		}
		return varMapLocal_i;
	}

}