/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package io.github.mugurell.autopublish

import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import io.github.mugurell.autopublish.internal.getPathInProject
import io.github.mugurell.autopublish.internal.getUpdatedStatus
import io.github.mugurell.autopublish.internal.log
import io.github.mugurell.autopublish.internal.measureTimeMillis
import io.github.mugurell.autopublish.internal.publish
import io.github.mugurell.autopublish.internal.useLatestLocalArtifacts
import io.github.mugurell.autopublish.model.Module

class AutopublishPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        val configuration = target.extensions.create("autoPublish", AutopublishProperties::class.java, this)

        project.afterEvaluate {
            measureTimeMillis(::logAutopublishingFinished) {
                val localData = LocalDataRepository(target, configuration)

                if (localData.checkIfAutopublishingIsEnabled()) {
                    autopublishModules(target, localData)
                }
            }
        }
    }

    private fun autopublishModules(
        target: Project,
        localData: LocalDataRepository
    ) {
        localData.getLocalModulesProperties().map {
            measureTimeMillis({ millis -> logModuleAutopublishingFinished(it.module.name, millis)}) {
                val updatedStatus = runBlocking { it.getUpdatedStatus() }
                val module = it.module
                val projectUri = getPathInProject(target, module.localPath)
                if (updatedStatus != null) {
                    log("Project at: $projectUri has changes. Publishing now.")

                    // First go through the publishing flow.
                    // Only if this succeeds we can persist that the new state is used.
                    runBlocking { module.publish() }
                    localData.updateModule(module, updatedStatus)

                    log("Current project is set ot use a new local build of [${module.name}]")
                } else {
                    log("Project at: $projectUri has no recent changes.")
                }
                module
            }
        }.toCollection(mutableListOf())
            .also { autopublishedModules ->
                target.useLatestLocalArtifacts(autopublishedModules.map { it.name })
                logModulesAppliedToProject(target, autopublishedModules)
            }
    }

    /**
     * Log a success message for the entire autopublish process.
     *
     * @param timeTook The time took by the entire autopublish process expressed in milliseconds.
     */
    private fun logAutopublishingFinished(timeTook: Long) = log(
        "Total execution time: ${timeTook / 1000} seconds."
    )

    /**
     * Log a success message for finishing the build process of a dependent module.
     *
     * @param timeTook The time took by the entire autopublish process expressed in milliseconds.
     */

    private fun logModuleAutopublishingFinished(moduleName: String, timeTook: Long) = log(
        "Project [$moduleName] took ${timeTook / 1000} seconds to be evaluated."
    )

    /**
     * Log a success message for the dependent modules the target project is set to use as local artifacts.
     *
     * @param target The project that will consume new local artifacts.
     * @param modules Names of the local artifacts as project dependencies.
     */
    private fun logModulesAppliedToProject(target: Project, modules: MutableList<Module>) = log(
        "Project [:${target.name}] will use the following dependencies from locally autopublished builds:\n\t" +
        modules
            .map { "[${it.name}] from ${getPathInProject(target, it.localPath)}"}
            .joinToString("\n\t")
    )
}
