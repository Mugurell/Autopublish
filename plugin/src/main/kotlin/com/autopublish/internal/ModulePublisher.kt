/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package com.autopublish.internal

import com.autopublish.model.Module
import org.gradle.api.Project

/**
 * Publish this module to the default local Apache Maven repository.
 *
 * This will also set as a project property for this module
 *
 *     localVersion=<milliseconds since epoch>
 *
 * which is useful in ensuring the module is always built with a higher version.
 *
 * As a current limitation the module has to be setup beforehand to consume this property in it's
 * `publishing` configuration.
 */
internal suspend fun Module.publish() {
    execute(
        command = arrayOf("./gradlew", "publishToMavenLocal", "-PlocalVersion=${System.currentTimeMillis()}"),
        location = localPath
    )
}

/**
 * Configure the project to first search Maven Local for the highest version of the
 * specific [modules] as local artifacts.
 *
 * @param modules List of dependencies containing the group and dependency name but without the version.
 */
internal fun Project.useLatestLocalArtifacts(modules: List<String>) {
    // Have Maven Local as the first repository in which artifacts are searched for.
    if (!repositories.contains(repositories.mavenLocal())) {
        // This could throw if Maven Local is already configured in the project.
        repositories.addFirst(repositories.mavenLocal())
    }

    configurations.all {
        it.resolutionStrategy.dependencySubstitution { depSubstitution ->
            modules.forEach { autopublishedModule ->
                depSubstitution.substitute(depSubstitution.module(autopublishedModule))
                    .using(depSubstitution.module("$autopublishedModule:+"))
                    .because("Use the latest available local version.")
            }
        }
    }
}
