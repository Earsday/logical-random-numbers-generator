import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ini4j.Ini;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * The algorithm of generating numbers has been improved, which speeds up the whole process.</br>
 * A configuration file 'settings.ini' is introduced to maintain the following settings:
 * <ul>
 * <li>name of input csv file
 * <li>name of output csv file
 * <li>start number of the header of output csv file
 * </ul>
 * Therefore, 'ini4j' has to be introduced to read settings. Additionally, code in this class have
 * been refactored. The operations of csv file have been moved to another individual class.</br>
 * </br>
 * 
 * @author Earsday
 */
public class Version4 {

  private final static List<String> SCORE_EXPRESSION_LIST = new ArrayList<>();
  private final static List<Integer> SCORE_SUM_LIST = new ArrayList<>();
  private final static Map<Integer, List<Integer>> SCORE_SUM_INDEX_MAP = new HashMap<>();

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

  private static <T> T readSetting(Ini ini, Object sectionName, Object optionName, Class<T> clz) {
    final T setting = ini.get(sectionName, optionName, clz);
    if (setting.getClass().isAssignableFrom(String.class)) {
      if (setting == null || ((String) setting).isEmpty()) {
        final String errMsg = String.format("'%s' of '%s' is not configured in settings.ini", optionName, sectionName);
        throw new RuntimeException(errMsg);
      }
    }
    final String log = String.format("> '%s' of '%s': %s;", optionName, sectionName, setting);
    System.out.println(log);
    return setting;
  }

  private static <T> Consumer<String> predicateConsumer(Function<String, T> f, Consumer<T> c) {
    return s -> {
      if (s != null && !s.isEmpty()) {
        c.accept(f.apply(s));
      }
    };
  }

  private static void parseInputFile(String inputFilename) throws FileNotFoundException, IOException {
    System.out.println("\nStarting to analyze file '" + inputFilename + "':");
    // prepare processors for fields in input file
    final Map<String, Consumer<String>> dataConsumers = new HashMap<>();
    dataConsumers.put("score", predicateConsumer(Function.identity(), SCORE_EXPRESSION_LIST::add));
    dataConsumers.put("sum", predicateConsumer(s -> {
      final int sum = Integer.parseInt(s);
      if (SCORE_SUM_INDEX_MAP.containsKey(sum)) {
        SCORE_SUM_INDEX_MAP.get(sum).add(SCORE_SUM_LIST.size());
      } else {
        final List<Integer> indexList = new LinkedList<>();
        indexList.add(SCORE_SUM_LIST.size());
        SCORE_SUM_INDEX_MAP.put(sum, indexList);
      }
      return sum;
    }, SCORE_SUM_LIST::add));
    // read csv file and process data
    CsvHelper.readCsv(inputFilename, dataConsumers);
    // print process results
    System.out.println("> Score expressions: " + SCORE_EXPRESSION_LIST.stream().collect(Collectors.joining(", ")) + ";");
    System.out.println("> Score sums: " + SCORE_SUM_LIST.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ";");
    System.out.println("> Score sums with index: " + SCORE_SUM_INDEX_MAP.entrySet().stream().collect(StringBuilder::new,
        (result, entry) -> result.append(entry.getKey()).append(":[")
            .append(entry.getValue().stream().map(index -> String.valueOf(index + 1)).collect(Collectors.joining(", "))).append("]; "),
        StringBuilder::append));
  }

  private static void generateNumbers(int headerStartNumber) {
    results = new int[SCORE_SUM_LIST.size()][SCORE_EXPRESSION_LIST.size()];
    boolean[] resultsLineFilled = new boolean[SCORE_SUM_LIST.size()];
    System.out.println("\nStarting to generate " + SCORE_SUM_LIST.size() + " groups of scores:");
    System.out.println(">  index  :" + IntStream.range(headerStartNumber, headerStartNumber + SCORE_EXPRESSION_LIST.size())
        .collect(StringBuilder::new, (r, a) -> r.append(String.format("%3d|", a)), StringBuilder::append));
    long totalMilliSeconds = 0;
    long startTime = new Date().getTime();
    for (int i = 0, k = 1; i < SCORE_SUM_LIST.size();) {
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
      if (SCORE_SUM_INDEX_MAP.containsKey(sum)) {
        final List<Integer> indexList = SCORE_SUM_INDEX_MAP.get(sum);
        final int index = indexList.remove(0);
        if (indexList.isEmpty()) {
          SCORE_SUM_INDEX_MAP.remove(sum);
        }
        for (int j = 0; j < results[i].length; j++) {
          results[index][j] = results[i][j];
        }
        resultsLineFilled[index] = true;
        final long milliSeconds = new Date().getTime() - startTime;
        final String scoreAdditionExpression = Arrays.stream(results[i]).mapToObj(String::valueOf).collect(Collectors.joining(" + "));
        final String log =
            String.format("> %3d(%3d): %s = %d, took %d milliSeconds;", k++, index + 1, scoreAdditionExpression, sum, milliSeconds);
        System.out.println(log);
        totalMilliSeconds += milliSeconds;
        while (i < SCORE_SUM_LIST.size() && resultsLineFilled[i]) {
          i++;
        }
        startTime = new Date().getTime();
      }
    }
    System.out.println("Score generation finished within " + totalMilliSeconds / 1000 + " seconds\n");
  }

  public static void main(String[] args) {
    try {
      // read settings
      System.out.println("Starting to load settings from file 'settings.ini':");
      final Ini ini = new Ini(new File("settings.ini"));
      final String inputFilename = readSetting(ini, "input", "filename", String.class);
      final String outputFilename = readSetting(ini, "output", "filename", String.class);
      final int headerStartNumber = readSetting(ini, "output", "header-start-number", Integer.class);

      parseInputFile(inputFilename);

      generateNumbers(headerStartNumber);

      // write results to output file
      CsvHelper.writeCsv(outputFilename, headerStartNumber, results);
      System.out.println(outputFilename + " generated");
    } catch (IOException | RuntimeException ex) {
      // show error
      System.out.println("ERROR: " + ex.getMessage());
      return;
    }
  }

}
