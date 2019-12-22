import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class CsvHelper {

  public static <T> int indexOfArray(T[] objArr, T target) {
    if (objArr != null && objArr.length > 0 && target != null) {
      final String targetInLowerCase = target.toString().toLowerCase(Locale.ROOT);
      for (int i = 0; i < objArr.length; i++) {
        if (objArr[i] != null && targetInLowerCase.equals(objArr[i].toString().toLowerCase(Locale.ROOT))) {
          return i;
        }
      }
    }
    return -1;
  }

  public static void readCsv(String filename, Map<String, Consumer<String>> dataConsumers) throws FileNotFoundException, IOException {
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
              final Consumer<String> consumer = dataConsumerMap.get(j);
              if (consumer != null) {
                consumer.accept(data[j]);
              }
            }
          }
        }
      }
    }
  }

  public static void writeCsv(String filename, int headerStartIndex, int[][] content) throws IOException {
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
  }

}
