package io.github.floverfelt.find.and.replace.maven.plugin;

import io.github.floverfelt.find.and.replace.maven.plugin.tasks.ProcessFilesTask;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "find-and-replace", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class FindAndReplaceMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  /**
   * The base directory from which to perform the find and replace.
   * This is relative to the location of the pom.
   *
   */
  @Parameter(property = "baseDir", defaultValue = "${basedir}")
  private String baseDir;

  /**
   * Whether the find and replace is recursive from the baseDir.
   *
   */
  @Parameter(property = "recursive", defaultValue = "false")
  private boolean recursive;

  /**
   * A CSV of what type of replacement(s) being done. Valid values are:
   *  file-contents
   *  filenames
   *  directory-names
   *
   *  file-contents will replace the find regex within a file.
   *  filenames will replace the find regex within a file's name.
   *  directory-names will replace the find regex within a directory's name
   *
   * To run the find and replace for multiple types, pass them as a CSV:
   *    file-contents,filenames,directory-names
   */
  @Parameter(property = "replacementType", required = true)
  private String replacementType;

  /**
   * The regex string to find.
   *
   */
  @Parameter(property = "findRegex", required = true)
  private String findRegex;

  /**
   * The value to replace the matching findRegex with.
   *
   */
  @Parameter(property = "replaceValue", required = true)
  private String replaceValue;

  /**
   * A CSV of the file types to search in.
   * For example for the value: .xml
   * Only files ending with .xml will be renamed.
   *
   * For the value: .xml,.properties
   * Only files ending with .xml,.properties will be renamed.
   *
   * Ignored for directories.
   *
   */
  @Parameter(property = "fileMask")
  private String fileMask;

  /**
   * Regex filenames/directory-names to exclude.
   *
   */
  @Parameter(property = "exclusions")
  private String exclusions;

  /**
   * Skip execution of the plugin.
   *
   */
  @Parameter(property = "skip", defaultValue = "false")
  private boolean skip;

  private static final String FILE_CONTENTS = "file-contents";
  private static final String FILENAMES = "filenames";
  private static final String DIRECTORY_NAMES = "directory-names";

  private Path baseDirPath;

  private List<String> validReplacementTypes = Arrays.asList(FILE_CONTENTS, FILENAMES, DIRECTORY_NAMES);
  private boolean processFileContents = false;
  private boolean processFilenames = false;
  private boolean processDirectoryNames = false;

  private List<String> fileMaskList = new ArrayList<>();
  private List<Pattern> exclusionsList = new ArrayList<>();

  public void execute() throws MojoExecutionException, MojoFailureException {

    if (skip) {
      getLog().warn("Skipping execution of find-and-replace-maven-plugin.");
      return;
    }

    setup();

    getLog().info("Executing find-and-replace maven plugin with options: " + this.toString());

    try {
      ProcessFilesTask.process(getLog(), baseDirPath, recursive, Pattern.compile(findRegex), replaceValue, fileMaskList,
          exclusionsList, processFileContents, processFilenames, processDirectoryNames);
    } catch (Exception e) {
      throw new MojoFailureException("Unable to process files.", e);
    }

  }

  private void setup() throws MojoExecutionException {

    setupReplacementTypes();

    setupFileMasks();

    setupExclusions();

    setupBaseDir();

  }

  private void setupReplacementTypes() throws MojoExecutionException {

    String[] replacementTypeList = StringUtils.split(replacementType, ",");

    String logMessage = "Mode set to ";

    for (String replacementTypeVal : replacementTypeList) {

      if (!validReplacementTypes.contains(replacementTypeVal)) {
        throw new MojoExecutionException("Invalid replacementType specified: " + replacementTypeVal);
      }

      if (FILE_CONTENTS.equals(replacementTypeVal)) {
        getLog().info(logMessage + FILE_CONTENTS);
        processFileContents = true;
      }

      if (FILENAMES.equals(replacementTypeVal)) {
        getLog().info(logMessage + FILENAMES);
        processFilenames = true;
      }

      if (DIRECTORY_NAMES.equals(replacementTypeVal)) {
        getLog().info(logMessage + DIRECTORY_NAMES);
        processDirectoryNames = true;
      }

    }

  }

  private void setupFileMasks() {

    if (StringUtils.isNotEmpty(fileMask)) {
      fileMaskList = Arrays.asList(StringUtils.split(fileMask, ","));
      getLog().info("fileMasks set to: " + fileMaskList);
    }

  }

  private void setupExclusions() {

    if (StringUtils.isNotEmpty(exclusions)) {
      getLog().info("Compiling regex for exclusions: " + exclusions);
      exclusionsList.add(Pattern.compile(exclusions));
    }

  }

  private void setupBaseDir() {

    if (project == null) {
      baseDirPath = Paths.get(baseDir);
    } else if (baseDir.equals(project.getBasedir().getAbsolutePath())) {
      baseDirPath = Paths.get(baseDir);
    } else {
      baseDirPath = Paths.get(project.getBasedir().getAbsolutePath(), baseDir);
    }

    getLog().info("baseDir set to: " + baseDirPath.toString());

  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FindAndReplaceMojo{");
    sb.append("baseDir='").append(baseDir).append('\'');
    sb.append(", recursive=").append(recursive);
    sb.append(", replacementType='").append(replacementType).append('\'');
    sb.append(", findRegex='").append(findRegex).append('\'');
    sb.append(", replaceValue='").append(replaceValue).append('\'');
    sb.append(", fileMask='").append(fileMask).append('\'');
    sb.append(", exclusions='").append(exclusions).append('\'');
    sb.append(", skip=").append(skip);
    sb.append(", baseDirPath=").append(baseDirPath);
    sb.append(", processFileContents=").append(processFileContents);
    sb.append(", processFilenames=").append(processFilenames);
    sb.append(", processDirectoryNames=").append(processDirectoryNames);
    sb.append(", fileMaskList=").append(fileMaskList);
    sb.append(", exclusionsList=").append(exclusionsList);
    sb.append('}');
    return sb.toString();
  }
}
