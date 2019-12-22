import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Internal variables for expressions and sum have been replaced by external csv file.</br>
 * Therefore, the following 2 methods have been introduced:
 * <ul>
 * <li>readCsv(String filename, Map<String, Consumer<String>> dataConsumers)
 * <li>writeCsv(String filename, int headerStartIndex, int[][] content)
 * </ul>
 * Additionally, more validation checks and error handling have been introduced.</br></br>
 * 
 * @author Earsday
 * @deprecated as newer version implemented, please refer to Version4
 */
public class Version3 {

  private final static List<String> SCORE_EXPRESSION_LIST = new ArrayList<>();
  private final static List<Integer> SCORE_SUM_LIST = new ArrayList<>();

  private static int[][] results;

  private static int generateRandomInteger(int min, int max) {
    final Random random = new Random();
    return random.nextInt((max - min) + 1) + min;
  }

  private static int evaluateExpression(String expression, int variable) {
    final Expression exp = new ExpressionBuilder(expression).variable("x").build().setVariable("x", variable);
    return (int) exp.evaluate();
  }

  private static int dispatchRequirement(int rowIndex, int columnIndex) {
    final String requirement = SCORE_EXPRESSION_LIST.get(columnIndex);
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
        if (baseItemIndex > 0 && baseItemIndex < columnIndex + 1) {
          final int baseItemValue = results[rowIndex][baseItemIndex - 1];
          final String expression = logics[1];
          return evaluateExpression(expression, baseItemValue);
        } else {
          final String errMsg = String.format("the index '%d' before ':' should be greater than 0 and smaller than current index '%d'",
              baseItemIndex, columnIndex + 1);
          throw new RuntimeException(errMsg);
        }
      } else {
        throw new RuntimeException("logical expression '" + requirement + "' is not completed");
      }
    } else {
      throw new RuntimeException("'" + requirement + "' is Unknown format");
    }
  }

  private static <T> int indexOfArray(T[] objArr, T target) {
    if (objArr != null && objArr.length > 0 && target != null) {
      final String targetInLowerCase = target.toString().toLowerCase(Locale.ROOT);
      for (int i = 0; i < objArr.length; i++) {
        if (targetInLowerCase.equals(objArr[i].toString().toLowerCase(Locale.ROOT))) {
          return i;
        }
      }
    }
    return -1;
  }

  private static void readCsv(String filename, Map<String, Consumer<String>> dataConsumers) {
    // validate input parameters
    if (filename == null) {
      throw new RuntimeException("filename should not be null");
    }
    final String trimmedFilename = filename.trim();
    if (trimmedFilename.isEmpty()) {
      throw new RuntimeException("filename should not be empty");
    }
    if (dataConsumers == null || dataConsumers.isEmpty()) {
      throw new RuntimeException("dataConsumers should not be null or empty");
    }

    // prepare
    final File csvFile = new File(filename);
    if (!csvFile.exists()) {
      throw new RuntimeException("'" + filename + "' does not exist!");
    }
    if (csvFile.isFile()) {
      try (final BufferedReader csvReader = new BufferedReader(new FileReader(filename))) {
        final Map<Integer, Consumer<String>> dataConsumerMap = new HashMap<>();
        String row = "";
        for (int i = 0; (row = csvReader.readLine()) != null; i++) {
          final String[] data = row.split(",");
          if (data.length == 0) {
            continue;
          }
          if (i == 0) { // header
            dataConsumers.entrySet().stream().forEach(dataConsumer -> {
              final String requiredColumnName = dataConsumer.getKey();
              final int index = indexOfArray(data, requiredColumnName);
              if (index < 0) {
                final String errMsg =
                    String.format("the required column '%s' not found in the header of the .csv file '%s'", requiredColumnName, filename);
                throw new RuntimeException(errMsg);
              } else {
                dataConsumerMap.put(index, dataConsumer.getValue());
              }
            });
          } else { // content
            for (int j = 0; j < data.length; j++) {
              dataConsumerMap.get(j).accept(data[j]);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  private static void writeCsv(String filename, int headerStartIndex, int[][] content) {
    try {
      // validate input parameters
      if (filename == null) {
        throw new RuntimeException("filename should not be null");
      }
      final String trimmedFilename = filename.trim();
      if (trimmedFilename.isEmpty()) {
        throw new RuntimeException("filename should not be empty");
      }
      if (content.length < 1) {
        throw new RuntimeException("content should not be empty");
      }

      // prepare
      final String finalFilename = trimmedFilename.toLowerCase(Locale.ROOT).endsWith(".csv") ? trimmedFilename : trimmedFilename + ".csv";
      final FileWriter csvWriter = new FileWriter(finalFilename);

      // write CSV header
      StringBuilder sb = new StringBuilder();
      for (int i = headerStartIndex; i < content[0].length + headerStartIndex;) {
        sb.append(i);
        if (i++ < content[0].length + headerStartIndex) {
          sb.append(", ");
        }
      }
      csvWriter.append(sb.toString());
      csvWriter.append("\n");

      // write CSV content
      for (int i = 0; i < content.length; i++) {
        sb = new StringBuilder();
        int j = 0;
        while (j < content[i].length) {
          sb.append(content[i][j]);
          if (++j < content[i].length) {
            sb.append(", ");
          }
        }
        csvWriter.append(sb.toString());
        csvWriter.append("\n");
      }

      // flush and close
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    String inputFilename = "rules.csv";
    String outputFilename = "results.csv";

    System.out.println("Starting to analyze file '" + inputFilename + "':");
    final Map<String, Consumer<String>> dataConsumers = new HashMap<>();
    dataConsumers.put("score", str -> {
      if (str != null && !str.isEmpty()) {
        SCORE_EXPRESSION_LIST.add(str);
      }
    });
    dataConsumers.put("sum", str -> {
      if (str != null && !str.isEmpty()) {
        SCORE_SUM_LIST.add(Integer.parseInt(str));
      }
    });
    try {
      readCsv(inputFilename, dataConsumers);
    } catch (RuntimeException ex) {
      System.out.println("ERROR: " + ex.getMessage());
      return;
    }
    System.out.println("Score expressions: " + SCORE_EXPRESSION_LIST.stream().collect(Collectors.joining(", ")) + ";");
    System.out.println("Score sums: " + SCORE_SUM_LIST.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ";");

    results = new int[SCORE_SUM_LIST.size()][SCORE_EXPRESSION_LIST.size()];
    System.out.println("\nStarting to generate " + SCORE_SUM_LIST.size() + " groups of scores:");
    long totalMilliSeconds = 0;
    long startTime = new Date().getTime();
    for (int i = 0; i < SCORE_SUM_LIST.size();) {
      int sum = 0;
      for (int j = 0; j < SCORE_EXPRESSION_LIST.size(); j++) {
        try {
          int result = dispatchRequirement(i, j);
          results[i][j] = result;
          sum += result;
        } catch (RuntimeException e) {
          final String errMsg = String.format("\nERROR: [%d,%d] %s", i + 1, j + 1, e.getMessage());
          System.out.println(errMsg);
          return;
        }
      }
      if (SCORE_SUM_LIST.get(i) == sum) {
        final long milliSeconds = new Date().getTime() - startTime;
        final String scoreAdditionExpression = Arrays.stream(results[i]).mapToObj(String::valueOf).collect(Collectors.joining(" + "));
        final String log = String.format("%3d: %s = %d, took %d milliSeconds;", ++i, scoreAdditionExpression, sum, milliSeconds);
        System.out.println(log);
        totalMilliSeconds += milliSeconds;
        startTime = new Date().getTime();
      }
    }
    System.out.println("Score generation finished within " + totalMilliSeconds / 1000 + " seconds\n");

    try {
      writeCsv(outputFilename, 5, results);
      System.out.println(outputFilename + " generated");
    } catch (RuntimeException e) {
      System.out.println("ERROR: " + e.getMessage());
      return;
    }
  }

}
