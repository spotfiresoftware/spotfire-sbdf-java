# Java Library for Spotfire® Binary Data Format (SBDF)

This library provides a Java API to read and write files in the Spotfire
Binary Data Format (SBDF).

### Installation

If you must build this library (and cannot use the precompiled version from the GitHub Packages repository), 
execute the `package` goal using [Apache Maven](https://maven.apache.org):
```sh
$ mvn package
```

To use this library:

* If you are using [Apache Maven](https://maven.apache.org):
  * Configure authentication for GitHub Packages in your Maven `settings.xml` file as per [GitHub's documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages).
  * Add the GitHub Packages repository to your `pom.xml` file:
    ```xml
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/spotfiresoftware/spotfire-sbdf-java</url>
        </repository>
    </repositories>
    ```
  * Add a dependency to your `pom.xml` file:
    ```xml
    <dependency>
        <groupId>com.spotfire</groupId>
        <artifactId>sbdf</artifactId>
        <version>VERSION</version>
    </dependency>
    ```

* If you are not using Apache Maven, add the `sbdf-VERSION.jar` file to your project.

### License
BSD-type 3-Clause License.  See the file `LICENSE` included in the repository.
