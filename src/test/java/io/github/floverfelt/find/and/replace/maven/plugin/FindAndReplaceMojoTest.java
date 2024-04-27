package io.github.floverfelt.find.and.replace.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * These behave more like integration tests than unit tests.
 * They dynamically generate the folders/files, and then check that the plugin is working as expected.
 */
@RunWith(BlockJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FindAndReplaceMojoTest {

  private final FindAndReplaceMojo findAndReplaceMojo = new FindAndReplaceMojo();

  private Path runningTestsPath;

  private Path textTestFile;

  private Path xmlTestFile;

  private Path ymlTestFile;

  private Path nonUtfTestFile;

  @Before
  public void setUpAll() throws NoSuchFieldException, IllegalAccessException, IOException, URISyntaxException {
    final URL integration = ClassLoader.getSystemResource("integration");
    Path integrationFolder = Paths.get(Objects.requireNonNull(
            integration.toURI()));
    runningTestsPath = Paths.get(integrationFolder.toString(), "runner");

    Files.createDirectory(runningTestsPath);

    textTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.txt"),
            Paths.get(runningTestsPath.toString(), "test-file.txt"));
    xmlTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.xml"),
            Paths.get(runningTestsPath.toString(), "test-file.xml"));
    ymlTestFile = Files.copy(Paths.get(integrationFolder.toString(), "test-file.yml"),
            Paths.get(runningTestsPath.toString(), "test-file.yml"));
    nonUtfTestFile = Files.copy(Paths.get(integrationFolder.toString(), "non-utf"),
            Paths.get(runningTestsPath.toString(), "non-utf"));

    setFieldValue(findAndReplaceMojo, "baseDir", runningTestsPath.toString());

  }

  @After
  public void cleanup() throws IOException {
    recursiveDelete(runningTestsPath);
  }

  @Test
  public void testDirectoryNames() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_top_directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), secondDirName);

    assertTrue(Files.exists(expectedFirstDirPath));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_top-directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), secondDirName);

    assertTrue(Files.exists(expectedFirstDirPath));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_top_directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), "test_sub_directory");

    assertTrue(Files.exists(expectedFirstDirPath));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesRecursiveReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    Path expectedFirstDirPath = Paths.get(runningTestsPath.toString(), "test_top-directory");
    Path expectedSecondDirPath = Paths.get(expectedFirstDirPath.toString(), "test_sub-directory");

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
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "exclusions", "-top-");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    Path expectedSecondDirPath = Paths.get(firstDir.toString(), "test_sub_directory");

    assertTrue(Files.exists(firstDir));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testDirectoryNamesRecursiveExclusionsReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-top-directory"));
    String secondDirName = "test-sub-directory";
    Files.createDirectories(Paths.get(firstDir.toString(), secondDirName));

    setFieldValue(findAndReplaceMojo, "findRegex", "-");
    setFieldValue(findAndReplaceMojo, "replaceValue", "_");
    setFieldValue(findAndReplaceMojo, "processDirectoryNames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "directory-names");
    setFieldValue(findAndReplaceMojo, "exclusions", "-top-");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    Path expectedSecondDirPath = Paths.get(firstDir.toString(), "test_sub-directory");

    assertTrue(Files.exists(firstDir));

    assertTrue(Files.exists(expectedSecondDirPath));

  }

  @Test
  public void testFilenames() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path nonRecurseFile = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file-name")));
    assertTrue(Files.exists(nonRecurseFile));

  }

  @Test
  public void testFilenamesReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path nonRecurseFile = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file_name")));
    assertTrue(Files.exists(nonRecurseFile));

  }

  @Test
  public void testFilenamesRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file-name")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-file-name")));

  }

  @Test
  public void testFilenamesRecursiveReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file_name")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-file_name")));

  }

  @Test
  public void testFilenamesRecursiveFileMasks() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some_file_name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some_file_name.txt"));
    Files.createFile(Paths.get(secondDir.toString(), "some_file_name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file-name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-file-name.txt")));
    assertTrue(Files.exists(Paths.get(secondDir.toString(), "some-file-name.yml")));

  }

  @Test
  public void testFilenamesRecursiveFileMasksReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some_file_name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some_file_name.txt"));
    Files.createFile(Paths.get(secondDir.toString(), "some_file_name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-file_name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-file_name.txt")));
    assertTrue(Files.exists(Paths.get(secondDir.toString(), "some-file_name.yml")));

  }

  @Test
  public void testFilenamesRecursiveFileMasksWithExclusions() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some_file_name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_xml_file_name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some_txt_file_name.txt"));
    Path unchangedExclusion = Files.createFile(Paths.get(secondDir.toString(), "some_yml_file_name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-xml-file-name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-txt-file-name.txt")));
    assertTrue(Files.exists(unchangedExclusion));

  }

  @Test
  public void testFilenamesRecursiveFileMasksWithExclusionsReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path secondDir = Files.createDirectories(Paths.get(firstDir.toString(), "test-sub-dir"));
    Path unchangedFile1 = Files.createFile(Paths.get(runningTestsPath.toString(), "some_file_name"));
    Path unchangedFile2 = Files.createFile(Paths.get(firstDir.toString(), "some_file_name"));
    Path unchangedFile3 = Files.createFile(Paths.get(secondDir.toString(), "some_file_name"));
    Files.createFile(Paths.get(runningTestsPath.toString(), "some_xml_file_name.xml"));
    Files.createFile(Paths.get(firstDir.toString(), "some_txt_file_name.txt"));
    Path unchangedExclusion = Files.createFile(Paths.get(secondDir.toString(), "some_yml_file_name.yml"));

    setFieldValue(findAndReplaceMojo, "findRegex", "_");
    setFieldValue(findAndReplaceMojo, "replaceValue", "-");
    setFieldValue(findAndReplaceMojo, "processFilenames", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "filenames");
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.txt,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(Files.exists(unchangedFile1));
    assertTrue(Files.exists(unchangedFile2));
    assertTrue(Files.exists(unchangedFile3));

    assertTrue(Files.exists(Paths.get(runningTestsPath.toString(), "some-xml_file_name.xml")));
    assertTrue(Files.exists(Paths.get(firstDir.toString(), "some-txt_file_name.txt")));
    assertTrue(Files.exists(unchangedExclusion));

  }

  @Test
  public void testFileContents() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));

    assertFalse(fileContains(textTestFile.toFile(), "asdf"));
    assertFalse(fileContains(xmlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(ymlTestFile.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsMultiLineRegex() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf\r\n\r\n-test-");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertFalse(fileContains(xmlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(ymlTestFile.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));

  }
  @Test
  public void testFileContentsMultiLineRegex2() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf[\\w\\W]{4,37}-test-");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(ymlTestFile.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "-test-"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsNonStandardEncodingFindRegex() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace non-standard with standard
    setFieldValue(findAndReplaceMojo, "findRegex", "ìíîï");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertFalse(fileContains(nonUtfTestFile.toFile(), "àáâãäåæçèéêëìíîï", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsNonStandardEncodingFindRegexReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace non-standard with standard
    setFieldValue(findAndReplaceMojo, "findRegex", "ìíîï");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertTrue(fileContains(nonUtfTestFile.toFile(), "àáâãäåæçèéêëìíîï", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsNonStandardReplaceValue() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace standard with non-standard
    setFieldValue(findAndReplaceMojo, "findRegex", "test");
    String replaceValue = "çåæ";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertFalse(fileContains(nonUtfTestFile.toFile(), "test", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsNonStandardReplaceValueReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace standard with non-standard
    setFieldValue(findAndReplaceMojo, "findRegex", "test");
    String replaceValue = "çåæ";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertTrue(fileContains(nonUtfTestFile.toFile(), "test", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsNonStandardFindAndReplaceValue() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace non-standard with non-standard
    setFieldValue(findAndReplaceMojo, "findRegex", "àáâãäåæçèéêëìíîï");
    String replaceValue = "çåæìíîïàáâãäå";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertFalse(fileContains(nonUtfTestFile.toFile(), "àáâãäåæçèéêëìíîï", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsNonStandardFindAndReplaceValueReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    // Replace non-standard with non-standard
    setFieldValue(findAndReplaceMojo, "findRegex", "àáâãäåæçèéêëìíîï");
    String replaceValue = "çåæìíîïàáâãäå";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "encoding", "ISO-8859-1");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(nonUtfTestFile.toFile(), replaceValue, StandardCharsets.ISO_8859_1));
    assertTrue(fileContains(nonUtfTestFile.toFile(), "àáâãäåæçèéêëìíîï", StandardCharsets.ISO_8859_1));

  }

  @Test
  public void testFileContentsRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertFalse(fileContains(textTestFile.toFile(), "asdf"));
    assertFalse(fileContains(xmlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(ymlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertFalse(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertFalse(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsRecursiveReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsNotRecursive() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", false);
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertFalse(fileContains(textTestFile.toFile(), "asdf"));
    assertFalse(fileContains(xmlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(ymlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsNotRecursiveReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", false);
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertTrue(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsRecursiveFileMasks() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertFalse(fileContains(xmlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(ymlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertFalse(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsRecursiveFileMasksReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertTrue(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsRecursiveFileMasksExclusions() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertFalse(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));
    assertFalse(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

  }

  @Test
  public void testFileContentsRecursiveFileMasksExclusionsReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-directory"));
    Path testFileXmlMoved = Files.copy(xmlTestFile, Paths.get(firstDir.toString(), xmlTestFile.toFile().getName()));
    Path testFileYmlMoved = Files.copy(ymlTestFile, Paths.get(firstDir.toString(), ymlTestFile.toFile().getName()));
    Path testFileTxtMoved = Files.copy(textTestFile, Paths.get(firstDir.toString(), textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "asdf");
    String replaceValue = "value successfully replaced";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    assertFalse(fileContains(textTestFile.toFile(), replaceValue));
    assertTrue(fileContains(xmlTestFile.toFile(), replaceValue));
    assertFalse(fileContains(ymlTestFile.toFile(), replaceValue));
    assertTrue(fileContains(testFileXmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileYmlMoved.toFile(), replaceValue));
    assertFalse(fileContains(testFileTxtMoved.toFile(), replaceValue));

    assertTrue(fileContains(textTestFile.toFile(), "asdf"));
    assertTrue(fileContains(xmlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(ymlTestFile.toFile(), "asdf"));
    assertTrue(fileContains(testFileXmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileYmlMoved.toFile(), "asdf"));
    assertTrue(fileContains(testFileTxtMoved.toFile(), "asdf"));

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
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents,filenames,directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    // Only xml files should be processed
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "replaceAll", true);

    findAndReplaceMojo.execute();

    Path firstDirRenamed = Paths.get(runningTestsPath.toString(), "rep-directory");
    Path secondDirRenamed = Paths.get(firstDirRenamed.toString(), "rep-sub-directory");
    Path firstXmlFile = Paths.get(runningTestsPath.toString(), "rep-file.xml");
    Path firstTxtFile = Paths.get(runningTestsPath.toString(), "test-file.txt");
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
    assertFalse(fileContains(firstXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(firstTxtFile));
    assertFalse(fileContains(firstTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(firstTxtFile.toFile(), "test-"));
    assertTrue(Files.exists(firstYmlFile));
    assertFalse(fileContains(firstYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(firstYmlFile.toFile(), "test-"));

    // Assert first dir
    assertTrue(Files.exists(firstDirRenamed));
    assertTrue(Files.exists(secondXmlFile));
    assertTrue(fileContains(secondXmlFile.toFile(), replaceValue));
    assertFalse(fileContains(secondXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(secondTxtFile));
    assertFalse(fileContains(secondTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(secondTxtFile.toFile(), "test-"));
    assertTrue(Files.exists(secondYmlFile));
    assertFalse(fileContains(secondYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(secondYmlFile.toFile(), "test-"));

    // Assert second dir
    assertTrue(Files.exists(secondDirRenamed));
    assertTrue(Files.exists(thirdXmlFile));
    assertTrue(fileContains(thirdXmlFile.toFile(), replaceValue));
    assertFalse(fileContains(thirdXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(thirdTxtFile));
    assertFalse(fileContains(thirdTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(thirdTxtFile.toFile(), "test-"));
    assertTrue(Files.exists(thirdYmlFile));
    assertFalse(fileContains(thirdYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(thirdYmlFile.toFile(), "test-"));

  }

  @Test
  public void testEverythingReplaceFirst() throws IOException, NoSuchFieldException, IllegalAccessException,
          MojoExecutionException, MojoFailureException {

    Path firstDir = Files.createDirectory(Paths.get(runningTestsPath.toString(), "test-test-directory"));
    Path secondDir = Files.createDirectory(Paths.get(firstDir.toString(), "test-test-sub-directory"));
    Files.copy(xmlTestFile, Paths.get(firstDir.toString(), "test-" + xmlTestFile.toFile().getName()));
    Files.copy(ymlTestFile, Paths.get(firstDir.toString(), "test-" + ymlTestFile.toFile().getName()));
    Files.copy(textTestFile, Paths.get(firstDir.toString(), "test-" + textTestFile.toFile().getName()));
    Files.copy(xmlTestFile, Paths.get(secondDir.toString(), "test-" + xmlTestFile.toFile().getName()));
    Files.copy(ymlTestFile, Paths.get(secondDir.toString(), "test-" + ymlTestFile.toFile().getName()));
    Files.copy(textTestFile, Paths.get(secondDir.toString(), "test-" + textTestFile.toFile().getName()));

    setFieldValue(findAndReplaceMojo, "findRegex", "test-");
    String replaceValue = "rep-";
    setFieldValue(findAndReplaceMojo, "replaceValue", replaceValue);
    setFieldValue(findAndReplaceMojo, "processFileContents", true);
    setFieldValue(findAndReplaceMojo, "replacementType", "file-contents,filenames,directory-names");
    setFieldValue(findAndReplaceMojo, "recursive", true);
    // Only xml files should be processed
    setFieldValue(findAndReplaceMojo, "fileMask", ".xml,.yml");
    setFieldValue(findAndReplaceMojo, "exclusions", ".yml$");
    setFieldValue(findAndReplaceMojo, "replaceAll", false);

    findAndReplaceMojo.execute();

    Path firstDirRenamed = Paths.get(runningTestsPath.toString(), "rep-test-directory");
    Path secondDirRenamed = Paths.get(firstDirRenamed.toString(), "rep-test-sub-directory");
    Path firstXmlFile = Paths.get(runningTestsPath.toString(), "rep-file.xml");
    Path firstTxtFile = Paths.get(runningTestsPath.toString(), "test-file.txt");
    Path firstYmlFile = Paths.get(runningTestsPath.toString(), "test-file.yml");
    Path secondXmlFile = Paths.get(firstDirRenamed.toString(), "rep-test-file.xml");
    Path secondTxtFile = Paths.get(firstDirRenamed.toString(), "test-test-file.txt");
    Path secondYmlFile = Paths.get(firstDirRenamed.toString(), "test-test-file.yml");
    Path thirdXmlFile = Paths.get(secondDirRenamed.toString(), "rep-test-file.xml");
    Path thirdTxtFile = Paths.get(secondDirRenamed.toString(), "test-test-file.txt");
    Path thirdYmlFile = Paths.get(secondDirRenamed.toString(), "test-test-file.yml");

    // Assert root dir
    assertTrue(Files.exists(firstXmlFile));
    assertTrue(fileContains(firstXmlFile.toFile(), replaceValue));
    assertTrue(fileContains(firstXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(firstTxtFile));
    assertFalse(fileContains(firstTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(firstXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(firstYmlFile));
    assertFalse(fileContains(firstYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(firstXmlFile.toFile(), "test-"));

    // Assert first dir
    assertTrue(Files.exists(firstDirRenamed));
    assertTrue(Files.exists(secondXmlFile));
    assertTrue(fileContains(secondXmlFile.toFile(), replaceValue));
    assertTrue(fileContains(secondXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(secondTxtFile));
    assertFalse(fileContains(secondTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(secondTxtFile.toFile(), "test-"));
    assertTrue(Files.exists(secondYmlFile));
    assertFalse(fileContains(secondYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(secondYmlFile.toFile(), "test-"));

    // Assert second dir
    assertTrue(Files.exists(secondDirRenamed));
    assertTrue(Files.exists(thirdXmlFile));
    assertTrue(fileContains(thirdXmlFile.toFile(), replaceValue));
    assertTrue(fileContains(thirdXmlFile.toFile(), "test-"));
    assertTrue(Files.exists(thirdTxtFile));
    assertFalse(fileContains(thirdTxtFile.toFile(), replaceValue));
    assertTrue(fileContains(thirdTxtFile.toFile(), "test-"));
    assertTrue(Files.exists(thirdYmlFile));
    assertFalse(fileContains(thirdYmlFile.toFile(), replaceValue));
    assertTrue(fileContains(thirdYmlFile.toFile(), "test-"));

  }

  private void setFieldValue(Object obj, String fieldName, Object val)
          throws NoSuchFieldException, IllegalAccessException {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, val);
  }

  private void recursiveDelete(Path pathToBeDeleted) throws IOException {

    Files.walk(pathToBeDeleted)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
  }

  private boolean fileContains(File file, String findValue) throws IOException {
    return fileContains(file, findValue, Charset.defaultCharset());
  }

  private boolean fileContains(File file, String findValue, Charset charset) throws IOException {

    try (FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, charset);
         BufferedReader fileReader = new BufferedReader(isr)) {

      Stream<String> lines = fileReader.lines();

      return lines.anyMatch(line -> line.contains(findValue));
    }
  }

}
