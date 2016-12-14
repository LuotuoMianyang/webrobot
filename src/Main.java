import java.math.BigInteger;
import java.util.Scanner;
import java.util.Stack;

/**
 * <b>简单计算</b>
 * 思路: 使用栈, 大数
 *
 * @author luguoqing
 * @date 2016-08-29
 */
public class Main {

    public static void main(String[] args) {
        Scanner cin = new Scanner(System.in);

        while (cin.hasNext()) {
            String expression = cin.nextLine();

            // 操作数
            Stack<BigInteger> operandStack = new Stack<BigInteger>();
            // 操作符
            Stack<Character> operatorStack = new Stack<Character>();

            for (int i = 0; i < expression.length();) {
                char ch = expression.charAt(i);

                switch (ch) {
                    case '+':
                    case '-':
                        while (!operatorStack.isEmpty() &&
                                (operatorStack.peek() == '+' ||
                                        operatorStack.peek() == '-' ||
                                        operatorStack.peek() == '*' ||
                                        operatorStack.peek() == '/' ||
                                        operatorStack.peek() == '^')) {
                            calculateOneOperator(operandStack, operatorStack);
                        }
                        operatorStack.push(ch);
                        break;
                    case '*':
                    case '/':
                        while (!operatorStack.isEmpty() &&
                                (operatorStack.peek() == '*' ||
                                        operatorStack.peek() == '/' ||
                                        operatorStack.peek() == '^')) {
                            calculateOneOperator(operandStack, operatorStack);
                        }
                        operatorStack.push(ch);
                        break;
                    // 注意1: 操作符优先级
                    case '^':
                        while (!operatorStack.isEmpty() &&
                                operatorStack.peek() == '^') {
                            calculateOneOperator(operandStack, operatorStack);
                        }
                        operatorStack.push(ch);
                        break;
                    case '(':
                        operatorStack.push(ch);
                        break;
                    case ')':
                        while (operatorStack.peek() != '(') {
                            calculateOneOperator(operandStack, operatorStack);
                        }
                        operatorStack.pop();
                        break;
                    // 注意2: 操作数的获取
                    default:
                        String sNumber = String.valueOf(ch);
                        while (i+1 < expression.length() && Character.isDigit(expression.charAt(i+1))) {
                            ++i;
                            sNumber += String.valueOf(expression.charAt(i));
                        }

                        BigInteger number = new BigInteger(sNumber);
                        operandStack.push(number);
                        break;
                }

                ++i;
            }

            while (!operatorStack.isEmpty()) {
                calculateOneOperator(operandStack, operatorStack);
            }

            System.out.println(operandStack.pop());
        }

        cin.close();
    }

    /**
     * 对操作符栈顶的一个操作符进行计算
     *
     * @param operandStack 操作数
     * @param operatorStack 操作符
     */
    private static void calculateOneOperator(Stack<BigInteger> operandStack, Stack<Character> operatorStack) {

        char operator = operatorStack.pop();
        BigInteger operand1 = operandStack.pop();
        BigInteger operand2 = operandStack.pop();

        // 注意3: 操作数的顺序
        switch (operator) {
            case '+':
                operandStack.push(operand2.add(operand1));
                break;
            case '-':
                operandStack.push(operand2.subtract(operand1));
                break;
            case '*':
                operandStack.push(operand2.multiply(operand1));
                break;
            case '/':
                operandStack.push(operand2.divide(operand1));
                break;
            case '^':
                int exponent = operand1.intValue();
                operandStack.push(operand2.pow(exponent));
                break;
        }
    }
}