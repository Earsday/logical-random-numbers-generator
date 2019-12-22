import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class Version1 {

  private final static String[] REQUIREMENT_SCORES = {//
      "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", //
      "0~4", "0~4", "0-0", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", //
      "0~4", "0~4", "0~4", "0~4", "0~4", "0~4", "0~4"};

  private final static int[] REQUIREMENT_SUM = {//
      21, 20, 21, 20, 20, 20, 20, 20, 21, 21, //
      19, 22, 21, 24, 19, 23, 19, 21, 20, 20, //
      19, 23, 19, 21, 20, 19, 20, 22, 21, 20, //
      19, 22, 22, 19, 20, 20};

  private static int[][] results = new int[REQUIREMENT_SUM.length][REQUIREMENT_SCORES.length];

  private static int randomDigit(int min, int max) {
    return new Random().nextInt((max - min) + 1) + min;
  }

  private static int add(int resultArrayIndex, int baseItemIndex, int delta) {
    return results[resultArrayIndex][baseItemIndex] + delta;
  }

  private static int minus(int resultArrayIndex, int baseItemIndex, int delta) {
    return results[resultArrayIndex][baseItemIndex] - delta;
  }

  private static int dispatchRequirement(String requirement, int resultArrayIndex) {
    if (requirement.indexOf("~") > 0) {
      final String[] range = requirement.split("~");
      if (range.length >= 2) {
        final int rangeLimitA = Integer.parseInt(range[0]);
        final int rangeLimitB = Integer.parseInt(range[1]);
        return randomDigit(Integer.min(rangeLimitA, rangeLimitB), Integer.max(rangeLimitA, rangeLimitB));
      } else {
        throw new RuntimeException("number range expression is not completed");
      }
    } else if (requirement.indexOf("+") > 0) {
      final String[] logics = requirement.split("\\+");
      if (logics.length >= 2) {
        final int baseItemIndex = Integer.parseInt(logics[0]);
        final int delta = Integer.parseInt(logics[1]);
        return add(resultArrayIndex, baseItemIndex, delta);
      } else {
        throw new RuntimeException("minus expression is not completed");
      }
    } else if (requirement.indexOf("-") > 0) {
      final String[] logics = requirement.split("-");
      if (logics.length >= 2) {
        final int baseItemIndex = Integer.parseInt(logics[0]);
        final int delta = Integer.parseInt(logics[1]);
        return minus(resultArrayIndex, baseItemIndex, delta);
      } else {
        throw new RuntimeException("minus expression is not completed");
      }
    } else {
      throw new RuntimeException("Unknown format");
    }
  }

  public static void main(String[] args) {
    System.out.println("Starting to generate " + REQUIREMENT_SUM.length + " groups of scores:");
    long totalMilliSeconds = 0;
    long startTime = new Date().getTime();
    for (int i = 0; i < REQUIREMENT_SUM.length;) {
      int total = 0;
      for (int j = 0; j < REQUIREMENT_SCORES.length; j++) {
        final int result = dispatchRequirement(REQUIREMENT_SCORES[j], i);
        results[i][j] = result;
        total += result;
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
