package io.github.floverfelt.find.and.replace.maven.plugin;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * These behave more like integration tests than unit tests.
 * They dynamically generate the folders/files, and then check that the plugin is working as expected.
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class FindAndReplaceMojoTest {

  private FindAndReplaceMojo findAndReplaceMojo = new FindAndReplaceMojo();

  private Path runningTestsPath;

  private Path textTestFile;

  private Path xmlTestFile;

  private Path ymlTestFile;

  @Before
  public void setUpAll() throws NoSuchFieldException, IllegalAccessException, IOException {

    Path integrationFolder = Paths.get(Objects.requireNonNull(
        this.getClass().getClassLoader().getResource("integration")).getPath());
    runningTestsPath = Paths.get(integrationFolder.toString(), "runner");

    Files.createDirectory(runningTestsPath);

    textTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.txt"),
        Paths.get(runningTestsPath.toString(), "test-file.txt"));
    xmlTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.xml"),
        Paths.get(runningTestsPath.toString(), "test-file.xml"));
    ymlTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.yml"),
        Paths.get(runningTestsPath.toString(), "test-file.yml"));

    setFieldValue(findAndReplaceMojo, "baseDir", runningTestsPath.toString());

  }

  @After
  public void cleanup() throws IOException {
    recursiveDelete(runningTestsPath);
  }

  @Test
  public void testDirectoryNames() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","directory-names");

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), secondDirName);

    assertTrue(Files.exists(expectedFirstDirPath));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), "test_sub_directory");

    assertTrue(Files.exists(expectedFirstDirPath));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesRecursiveExclusions() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","directory-names");
    setFieldValue(findAndReplaceMojo, "exclusions", "-top-");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    Path expectedSecondDirPath = Paths.get(firstDir.toString(), "test_sub_directory");

    assertTrue(Files.exists(firstDir));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testFilenames() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "somefile")).toUri();
    Path nonRecurseFile = Files.createFile(Paths.get(firstDir.toString(), "somefile"));

    setFieldValue(findAndReplaceMojo, "findRegex", "file$");
    setFieldValue(findAndReplaceMojo, "replaceValue", "renamedfile");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","filenames");

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "somerenamedfile")));
    assertTrue(Files.exists(nonRecurseFile));

  }

  @Test
  public void testFilenamesRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "somefile")).toUri();
    Files.createFile(Paths.get(firstDir.toString(), "somefile"));

    setFieldValue(findAndReplaceMojo, "findRegex", "file$");
    setFieldValue(findAndReplaceMojo, "replaceValue", "renamedfile");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","filenames");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "somerenamedfile")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "somerenamedfile")));

  }

  @Test
  public void testFilenamesRecursiveFileMasks() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some-file-name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some-file-name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some-file-name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some-file-name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some-file-name.txt"));
    Files.createFile(Paths.get(secondDir.toString(), "some-file-name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "some-file-name");
    setFieldValue(findAndReplaceMojo, "replaceValue", "new-file-name");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","filenames");
    setFieldValue(findAndReplaceMojo, "fileMask",".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "new-file-name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "new-file-name.txt")));
    assertTrue(Files.exists(Paths.get(secondDir.toString(), "new-file-name.yml")));

  }

  @Test
  public void testFilenamesRecursiveFileMasksWithExclusions() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some-file-name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some-file-name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some-file-name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some-xml-file-name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some-txt-file-name.txt"));
    Path unchangedExclusion = Files.createFile(Paths.get(secondDir.toString(), "some-yml-file-name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "some");
    setFieldValue(findAndReplaceMojo, "replaceValue", "new");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType","filenames");
    setFieldValue(findAndReplaceMojo, "fileMask",".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions",".yml$");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "new-xml-file-name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "new-txt-file-name.txt")));
    assertTrue(Files.exists(unchangedExclusion));

  }

  @Test
  public void testFileContents() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents");

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));

}

  @Test
  public void testFileContentsRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    System.out.println(firstDir);
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileTxtMoved.toFile(), replaceValue));

  }

  @Test
  public void testFileContentsNotRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    System.out.println(firstDir);
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

  }

  @Test
  public void testFileContentsRecursiveFileMasks() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    System.out.println(firstDir);
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

  }

  @Test
  public void testFileContentsRecursiveFileMasksExclusions() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    System.out.println(firstDir);
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

  }

  @Test
  public void testEverything() throws IOException, NoSuchFieldException, IllegalAccessException,
      MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectory(Paths.get(firstDir.toString(), "test-sub-directory"));
    Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));
    Files.copy(xmlTestFile, Paths.get(secondDir.toString(), xmlTestFile.toFile().getName()));
    Files.copy(ymlTestFile, Paths.get(secondDir.toString(), ymlTestFile.toFile().getName()));
    Files.copy(textTestFile, Paths.get(secondDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "test-");
    String replaceValue = "rep-";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType","file-contents,filenames,directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    // Only xml files should be processed
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");

    findAndReplaceMojo.execute();

    Path firstDirRenamed = Paths.get(runningTestsPath.toString(), "rep-directory");
    Path secondDirRenamed = Paths.get(firstDirRenamed.toString(), "rep-sub-directory");
    Path firstXmlFile = Paths.get(runningTestsPath.toString(), "rep-file.xml");
    Path firstTxtfile = Paths.get(runningTestsPath.toString(), "test-file.txt");
    Path firstYmlFile = Paths.get(runningTestsPath.toString(), "test-file.yml");
    Path secondXmlFile = Paths.get(firstDirRenamed.toString(), "rep-file.xml");
    Path secondTxtFile = Paths.get(firstDirRenamed.toString(), "test-file.txt");
    Path secondYmlFile = Paths.get(firstDirRenamed.toString(), "test-file.yml");
    Path thirdXmlFile = Paths.get(secondDirRenamed.toString(), "rep-file.xml");
    Path thirdTxtFile = Paths.get(secondDirRenamed.toString(), "test-file.txt");
    Path thirdYmlFile = Paths.get(secondDirRenamed.toString(), "test-file.yml");

    // Assert root dir
    assertTrue(Files.exists(firstXmlFile));
    assertTrue(fileContains(firstXmlFile.toFile(), replaceValue));
    assertTrue(Files.exists(firstTxtfile));
    assertFalse(fileContains(firstTxtfile.toFile(), replaceValue));
    assertTrue(Files.exists(firstYmlFile));
    assertFalse(fileContains(firstYmlFile.toFile(), replaceValue));

    // Assert first dir
    assertTrue(Files.exists(firstDirRenamed));
    assertTrue(Files.exists(secondXmlFile));
    assertTrue(fileContains(secondXmlFile.toFile(), replaceValue));
    assertTrue(Files.exists(secondTxtFile));
    assertFalse(fileContains(secondTxtFile.toFile(), replaceValue));
    assertTrue(Files.exists(secondYmlFile));
    assertFalse(fileContains(secondYmlFile.toFile(), replaceValue));

    // Assert second dir
    assertTrue(Files.exists(secondDirRenamed));
    assertTrue(Files.exists(thirdXmlFile));
    assertTrue(fileContains(thirdXmlFile.toFile(), replaceValue));
    assertTrue(Files.exists(thirdTxtFile));
    assertFalse(fileContains(thirdTxtFile.toFile(), replaceValue));
    assertTrue(Files.exists(thirdYmlFile));
    assertFalse(fileContains(thirdYmlFile.toFile(), replaceValue));

  }

  private void setFieldValue(Object obj, String fieldName, Object val)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(obj, val);
  }

  private void recursiveDelete(Path pathToBeDeleted) throws IOException {

    Files.walk(pathToBeDeleted)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  private boolean fileContains(File file, String findValue) throws IOException {

    try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {

      Stream<String> lines = fileReader.lines();

      return lines.anyMatch(line -> line.contains(findValue));

    }

  }

}
