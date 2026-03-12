package edu.uob;

import java.util.*;

public class EvaluateCondition {

    public static boolean evaluateCondition(String condition, List<String> columns, String[] rowValues) {
        condition = condition.trim();
        if (condition.isEmpty()) {
            return false;
        }

        // Convert infix condition to postfix (RPN)
        List<String> postfixTokens = infixToPostfix(condition);

        // Evaluate the postfix expression
        return evaluatePostfix(postfixTokens, columns, rowValues);
    }

    public static List<String> infixToPostfix(String condition) {
        Stack<String> operators = new Stack<>();
        List<String> output = new ArrayList<>();

        // Tokenize the condition correctly
        List<String> tokens = tokenizeCondition(condition);

        for (String token : tokens) {
            if (isOperand(token)) {
                output.add(token); // Operand (attribute, number, or value)
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                operators.pop(); // Remove '(' from stack
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    private static List<String> tokenizeCondition(String condition) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < condition.length(); i++) {
            char c = condition.charAt(i);

            // Handle parentheses separately
            if (c == '(' || c == ')') {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(Character.toString(c));
            }
            // Handle operators (>, <, >=, <=, ==, !=)
            else if (isOperatorChar(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString()); // Store previous token (column name or value)
                    currentToken.setLength(0);
                }

                currentToken.append(c);

                // Check for two-character operators (>=, <=, ==, !=)
                if (i + 1 < condition.length() && isOperatorChar(condition.charAt(i + 1))) {
                    currentToken.append(condition.charAt(i + 1));
                    i++; // Skip next character
                }

                tokens.add(currentToken.toString()); // Store operator
                currentToken.setLength(0);
            }
            // Handle spaces (store previous token if needed)
            else if (c == ' ') {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            }
            // Handle column names, values, and literals
            else {
                currentToken.append(c);
            }
        }

        // Store last token if exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private static boolean isOperatorChar(char c) {
        return c == '=' || c == '!' || c == '>' || c == '<';
    }

    private static boolean isOperand(String token) {
        return !isOperator(token) && !token.equals("(") && !token.equals(")");
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") ||
                token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR");
    }

    private static int precedence(String operator) {
        switch (operator) {
            case "*": case "/": return 3;  // Highest precedence (multiplication and division)
            case "+": case "-": return 2;  // Lower precedence (addition and subtraction)
            case "AND": return 1; // Boolean AND
            case "OR": return 0; // Boolean OR (lowest)
            default: return -1;
        }
    }

    private static boolean evaluatePostfix(List<String> tokens, List<String> columns, String[] rowValues) {
        Stack<Boolean> stack = new Stack<>();
        String singleCondition = new String();
        int counter = 0;

        for (String token : tokens) {
            if (token.equalsIgnoreCase("AND")) {
                boolean b = stack.pop();
                boolean a = stack.pop();
                stack.push(a && b);
            } else if (token.equalsIgnoreCase("OR")) {
                boolean b = stack.pop();
                boolean a = stack.pop();
                stack.push(a || b);
            } else {
                if (counter < 3) {
                    singleCondition = singleCondition + token + " ";
                    counter++;
                }
                if (counter == 3) {
                    stack.push(evaluateSingleCondition(singleCondition, columns, rowValues));
                    singleCondition = "";
                    counter = 0;
                }
            }
        }

        return stack.isEmpty() ? false : stack.pop();
    }

    private static boolean evaluateSingleCondition(String condition, List<String> columns, String[] rowValues) {
        String[] parts = condition.split("\\s+");//condition.split("(?<=[><=!])\\s*|\\s*(?=[><=!])");
        if (parts.length < 3) {
            return false;
        }

        String columnName = parts[0].trim();
        String operator = parts[1].trim();
        String value = parts[2].trim();

        int columnIndex = columns.indexOf(columnName);
        if (columnIndex == -1) {
            return false;
        }

        String actualValue = rowValues[columnIndex];

        try {
            switch (operator) {
                case "==":
                    return actualValue.equals(value);
                case "!=":
                    return !actualValue.equals(value);
                case ">":
                    return Double.parseDouble(actualValue) > Double.parseDouble(value);
                case "<":
                    return Double.parseDouble(actualValue) < Double.parseDouble(value);
                case ">=":
                    return Double.parseDouble(actualValue) >= Double.parseDouble(value);
                case "<=":
                    return Double.parseDouble(actualValue) <= Double.parseDouble(value);
                case "LIKE":
                    return actualValue.contains(value.replaceAll("'", ""));
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
