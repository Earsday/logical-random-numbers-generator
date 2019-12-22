import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Version2 {

  private final static String[] REQUIREMENT_SCORES = {//
      "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", //
      "0~4", "0~4", "0:x", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", //
      "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4"};

  private final static int[] REQUIREMENT_SUM = {//
      21, 20, 21, 20, 20, 20, 20, 20, 21, 21, //
      19, 22, 21, 24, 19, 23, 19, 21, 20, 20, //
      19, 23, 19, 21, 20, 19, 20, 22, 21, 20, //
      19, 22, 22, 19, 20, 20};

  private static int[][] results = new int[REQUIREMENT_SUM.length][REQUIREMENT_SCORES.length];

  private static int generateRandomInteger(int min, int max) {
    final Random random = new Random();
    return random.nextInt((max - min) + 1) + min;
  }

  private static int evaluateExpression(String expression, int variable) {
    final Expression exp = new ExpressionBuilder(expression).variable("x").build().setVariable("x", variable);
    return (int) exp.evaluate();
  }

  private static int dispatchRequirement(int rowIndex, int columnIndex) {
    final String requirement = REQUIREMENT_SCORES[columnIndex];
    if (requirement.indexOf("~") > 0) {
      final String[] range = requirement.split("~");
      if (range.length >= 2) {
        final int rangeLimitA = Integer.parseInt(range[0]);
        final int rangeLimitB = Integer.parseInt(range[1]);
        return generateRandomInteger(Integer.min(rangeLimitA, rangeLimitB), Integer.max(rangeLimitA, rangeLimitB));
      } else {
        throw new RuntimeException("number range expression '" + requirement + "' is not completed");
      }
    } else if (requirement.indexOf(":") > 0) {
      final String[] logics = requirement.split(":");
      if (logics.length >= 2) {
        final int baseItemIndex = Integer.parseInt(logics[0]);
        if (baseItemIndex >= 0 && baseItemIndex < columnIndex) {
          final int baseItemValue = results[rowIndex][baseItemIndex];
          final String expression = logics[1];
          return evaluateExpression(expression, baseItemValue);
        } else {
          final String errMsg = String.format("the index '%d' before ':' is lower than 0 or greater than or equals to current index '%d'",
              baseItemIndex, columnIndex);
          throw new RuntimeException(errMsg);
        }
      } else {
        throw new RuntimeException("logical expression '" + requirement + "' is not completed");
      }
    } else {
      throw new RuntimeException("'" + requirement + "' is Unknown format");
    }
  }

  public static void main(String[] args) {
    System.out.println("Starting to generate " + REQUIREMENT_SUM.length + " groups of scores:");
    long totalMilliSeconds = 0;
    long startTime = new Date().getTime();
    for (int i = 0; i < REQUIREMENT_SUM.length;) {
      int total = 0;
      for (int j = 0; j < REQUIREMENT_SCORES.length; j++) {
        try {
          int result = dispatchRequirement(i, j);
          results[i][j] = result;
          total += result;
        } catch (RuntimeException e) {
          final String errMsg = String.format("\n[%d,%d] %s", i, j, e.getMessage());
          System.out.println(errMsg);
          return;
        }
      }
      if (REQUIREMENT_SUM[i] == total) {
        final long milliSeconds = new Date().getTime() - startTime;
        System.out.println(i + ": " + Arrays.toString(results[i]) + " = " + total + ", took " + milliSeconds + " milliSeconds;");
        totalMilliSeconds += milliSeconds;
        i++;
        startTime = new Date().getTime();
      }
    }
    System.out.println("Score generation finished within " + totalMilliSeconds / 1000 + " seconds");
  }

}
