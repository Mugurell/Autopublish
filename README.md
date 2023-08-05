# Local projects autopublish plugin
| An anti-bikeshedding (ktlint ™) solution for using local projects in your own!
---
###### (STAR - Simple To Autopublish Repositories)


&nbsp;
#### Situation
- In a distributed way of working being able to easily test changes in a hosting application and ensuring a smooth integration of feature projects is essential for a smooth and speedy development process.
- Currently available solutions like [Artifactory](https://jfrog.com/blog/what-is-artifactory-jfrog/) are great and immensely configurable allowing support for a multitude of scenarios. But they need a big effort to setup, adopt, maintain and use.
- Simpler approaches relying on a manual process by developers like using a [local Maven repository](https://maven.apache.org/guides/introduction/introduction-to-repositories.html) still need [a complex setup and teardown processes](https://www.jetbrains.com/help/idea/add-a-gradle-library-to-the-maven-repository.html).


#### Task
- Need a simpler way to manage the integration of external projects in your own.
- Should support automatic detection of project changes and only build new artifacts when the need arise.
- Should support an automatic setup with minimal steps needed to configure what local projects are to be consumed from local artifacts.
- Should support an automatic teardown  process with minimal steps needed to come back to using release artifacts from official repositories.
- Should be fast, should be simple, should be reliable.


#### Action
- Leverage Gradle's support for publishing build artifacts. Which is probably already set up in most projects.
- Choose to use a Gradle plugin to support all the needed functionality.
    - It is simple to adopt
    - It can run every time the project is evaluated (when dependencies are synchronized / when a new build is made)
    - It can easily interact with the project it is applied to
    - It can run other needed commands
    - It can interact with the local filesystem for it's own configuration / host project configuration / dependent modules configurations.
- Strive to need the least amount of configuration.
- Ensure the functionality is dependable .
- Open source code for the entire functionality, easy to validate. Feel free to build this project yourself!


#### Result
- **Minimal setup to adopt**:

```
gradle / kts code to adopt
```


- **Minimal setup to use:**

       ```
  autoPublish {
  modulesConfigurationInput.set(file("..")) <optional>.
  modulesChangesStatusOutput.set(file("")) <optional>.
  generateInputTemplate = true <optional>
  }

       ```

    - `modulesConfigurationInput` allows specifying the path of a properties file (key/value) controlling which and where from external dependencies will be autopublished. Defaults to `../autopublish/modules.properties`.
    - `modulesChangesStatusOutput` allows specifying where to persist the current state of each external dependency set for autopublish. This persisted state will be used to build and link new versions only when the code of external libraries was changed. Defaults to `build/reports/autopublish`.
    - `generateInputTemplate` allows to control whether to generate an example of the expected configuration used for controlling which and where from external dependencies will be autopublished. Defaults to `true`.

  &nbsp;
  The configuration file should contain clear indication which and where from should external dependencies be built and linked in the current host application:

       ```
      Declare the list of projects available locally to automatically build and integrate.
      Declaration should follow the <dependency>=<local path> structure, eg:
      com.sample\:example=../anotherProject
      The path to the other project can be relative to this project or an absolute path.

       ```

 ---

> [Artifacts validation data (checksums)](/verification).
> 
> Public GPG signature used: FF02C66BA552BAFD256831B0067CE7FBD32634D9.
> 
> See [Gradle - Verifying dependencies](https://docs.gradle.org/current/userguide/dependency_verification.htm) for why and how to use this.
 ---
  


- **Minimap teardown process:**
    - Just delete / comment the configuration line(s) for the local projects which should not be integrated in the hosting project anymore.

- **Simple, straigh-forward functionality:**
    - If there are _any_ local projects set for autopublishing:
        - Calculate a hash of their files every time the host project executes a task that need the latest available version of the dependent projects' code.
        - Only publish new artifacts of the dependent projects if they have new changes.
        - Automatically setup the host project to use local build artifacts from the default local Maven repository.
    - If there are _no_ local projects set for autopublishing:
        - The host project will function as if the autopublish functionality was never there.

&nbsp;
&nbsp;

---
---

&nbsp;
&nbsp;


### Limitations:
- General words of warning for using mavenLocal() as a source repository in your projects - [Gradle - The case for mavenLocal()](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:case-for-maven-local).

### Future improvements:
- Check the currently open tickets.


&nbsp;
&nbsp;

---
---

&nbsp;
&nbsp;


### License

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/