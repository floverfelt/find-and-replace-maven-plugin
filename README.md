# Find and Replace Maven Plugin

There's a lack of an open source find and replace plugin in the Maven repository. 

The most widely recommended one is the [maven-replacer-plugin](https://mvnrepository.com/artifact/com.google.code.maven-replacer-plugin/maven-replacer-plugin) which 
is very old and just a wrapper around the [ant task](https://ant.apache.org/manual/Tasks/replace.html) which lacks support for file and directory names.

The find-and-replace maven plugin is an attempt to remedy these issues. It's open source, uses vanilla Java 8, and allows for replacement within files, for file names, and for directory names.

It's intended to be simple to read and contribute to.

## Documentation

Documentation can be found [here](https://floverfelt.org/find-and-replace-maven-plugin).

## Usage

The find-and-replace-maven-plugin is lifecycle phase agnostic. Simply add it to your pom wherever you'd like it to execute.

```
<plugin>
   <groupId>io.github.floverfelt</groupId>
   <artifactId>find-and-replace-maven-plugin</artifactId>
   <executions>
      <execution>
         <id>exec</id>
         <phase>package</phase>
         <goals>
            <goal>find-and-replace</goal>
         </goals>
         <configuration>
            <!-- Possible replacementType values: file-contents, filenames, directory-names. To run for multiple types, pass the values as a CSV list. -->
            <replacementType>directory-names</replacementType>
            <baseDir>testing/</baseDir>
            <findRegex>_</findRegex>
            <replaceValue>-</replaceValue>
            <recursive>true</recursive>
         </configuration>
      </execution>
   </executions>
</plugin>
```

## Contribution

If you'd like to contribute, feel free to raise a pull request or issue. You can support the plugin monetarily [here](https://www.buymeacoffee.com/floverfelt).
