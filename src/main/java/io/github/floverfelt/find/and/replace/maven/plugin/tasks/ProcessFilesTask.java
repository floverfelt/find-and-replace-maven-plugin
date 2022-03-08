package io.github.floverfelt.find.and.replace.maven.plugin.tasks;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.maven.plugin.logging.Log;

public class ProcessFilesTask {

  private ProcessFilesTask() {
    // do not instantiate
  }

  /**
   * Stupid simple implementation of file walking, renaming, etc.
   *
   * @param log                   the maven-plugin log
   * @param baseDir               the directory to start in
   * @param isRecursive           whether to recurse further
   * @param findRegex             the regex to find
   * @param replaceValue          the value to replace the found regex
   * @param fileMasks             the file masks to use
   * @param exclusions            the filename regex to exclude
   * @param processFileContents   whether to process file contents
   * @param processFilenames      whether to process file names
   * @param processDirectoryNames whether to process directory names
   * @param charset               encoding to be used for file handles
   */
  public static void process(Log log, Path baseDir, boolean isRecursive, Pattern findRegex, String replaceValue,
                             List<String> fileMasks, List<Pattern> exclusions, boolean processFileContents,
                             boolean processFilenames, boolean processDirectoryNames, Charset charset) throws IOException {

    // Load in the files in the base dir
    List<File> filesToProcess = new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File(baseDir.toUri()).listFiles())));

    ListIterator<File> iterator = filesToProcess.listIterator();

    while (iterator.hasNext()) {

      File file = iterator.next();

      // Remove the file from processing
      iterator.remove();

      // Perform dir checks
      if (file.isDirectory()) {
        iterator = processDirectory(iterator, isRecursive, file, processDirectoryNames, exclusions, findRegex, replaceValue, log);
      }

      if (file.isFile()) {
        processFile(log, exclusions, file, fileMasks, processFileContents, findRegex, replaceValue, processFilenames, charset);
      }

    }

  }


  private static ListIterator<File> processDirectory(ListIterator<File> iterator, boolean isRecursive, File file,
                                                     boolean processDirectoryNames, List<Pattern> exclusions, Pattern findRegex,
                                                     String replaceValue, Log log) throws IOException {

    // Rename the directory
    if (processDirectoryNames && !shouldExcludeFile(exclusions, file)) {
      file = renameFile(log, file, findRegex, replaceValue);
    }

    // If recursive, add child files to iterator
    if (isRecursive) {
      File[] filesToAdd = file.listFiles();
      assert filesToAdd != null;
      for (File f : filesToAdd) {
        iterator.add(f);
        iterator.previous();
      }
    }

    return iterator;

  }


  private static boolean shouldExcludeFile(List<Pattern> exclusions, File file) {

    for (Pattern p : exclusions) {
      if (p.matcher(file.getName()).find()) {
        return true;
      }
    }

    return false;

  }

  private static boolean shouldProcessFile(List<String> fileMasks, File file) {

    if (fileMasks.isEmpty()) {
      return true;
    }

    for (String fileMask : fileMasks) {
      if (file.getName().endsWith(fileMask)) {
        return true;
      }
    }

    return false;

  }

  private static File renameFile(Log log, File file, Pattern findRegex, String replaceValue) throws IOException {

    Path filePath = file.toPath();
    Path parentDir = filePath.getParent();
    String oldName = file.getName();
    Matcher matcher = findRegex.matcher(oldName);
    String newName = matcher.replaceAll(replaceValue);

    if (!newName.equals(oldName)) {
      Path targetPath = Paths.get(parentDir.toString(), newName);

      log.info(String.format("Renaming %s to %s", oldName, newName));

      return new File(Files.move(filePath, targetPath).toUri());

    }

    return file;

  }

  private static void processFileContents(File file, Pattern findRegex, String replaceValue, Charset charset) throws IOException {

    File tempFile = File.createTempFile("tmp", "tmp", file.getParentFile());

    try (FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, charset);
         BufferedReader fileReader = new BufferedReader(isr)) {
      try (FileOutputStream fos = new FileOutputStream(tempFile);
           OutputStreamWriter osr = new OutputStreamWriter(fos, charset);
           BufferedWriter fileWriter = new BufferedWriter(osr)) {

        Stream<String> lines = fileReader.lines();

        lines.forEach(line -> {
          Matcher matcher = findRegex.matcher(line);
          line = matcher.replaceAll(replaceValue);
          try {
            fileWriter.write(line + "\n");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }

    if (!file.delete()) {
      throw new IOException("Failed to delete file at: " + file.getPath());
    }

    if (!tempFile.renameTo(file)) {
      throw new IOException("Failed to rename temp file at: " + tempFile.getPath() + " to " + file.getPath());
    }

  }

  private static void processFile(Log log, List<Pattern> exclusions, File file, List<String> fileMasks,
                                  boolean processFileContents, Pattern findRegex, String replaceValue,
                                  boolean processFilenames, Charset charset) throws IOException {

    if (shouldExcludeFile(exclusions, file) || !shouldProcessFile(fileMasks, file)) {
      return;
    }

    if (processFileContents) {
      processFileContents(file, findRegex, replaceValue, charset);
    }

    if (processFilenames) {
      renameFile(log, file, findRegex, replaceValue);
    }

  }

}
