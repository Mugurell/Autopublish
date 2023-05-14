/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package com.autopublish.storage

import com.autopublish.internal.getPathInProject
import com.autopublish.internal.log
import com.autopublish.model.Module
import com.autopublish.model.ModuleState
import org.gradle.api.Project
import java.io.File
import java.util.Properties

private val modulesConfigurationPath = "autopublish${File.separatorChar}modules.properties"
private val modulesStatusesPath = "autopublish${File.separatorChar}modules-statuses.properties"

private val createDefaultModulesConfigurationMessage =
    """Configuration for what modules should be autopublished
      >The expected structure is <dependency>=<local path>, eg:
      >com.sample:example=../anotherProject
      >The path can be relative to this project or an absolute path.
      >""".trimMargin(">")


/**
 * Handles all operations on the local storage for this plugin and the project it is applied on.
 *
 * @param project The [Project] this plugin is applied to. Used to resolve storage paths.
 */
internal class LocalDataRepository(private val project: Project) {
    private val localConfigurationFile = project.rootProject.file(modulesConfigurationPath)
    private val modulesStatusesFile = project.rootProject.file(modulesStatusesPath)

    /**
     * Check if there is any module set to be autopublished.
     *
     * @param project Host project interested in consuming external modules and configured as such.
     * @param logCurrentStatus Whether to automatically log the current status: if modules are to be autopublished or not.
     *
     * @return `true` if external modules are declared for autopublication.
     */
    internal fun checkIfAutopublishingIsEnabled(): Boolean {
        ensurePluginConfigurationIsNotTrackedByGit()

        return when {
            !localConfigurationFile.exists() -> {
                log("Autopublish disabled. Missing modules.properties file.")
                createStarterModuleConfigurationFile()
                false
            }
            localConfigurationFile.asProperties.isEmpty -> {
                log("Autopublish disabled. No modules set for autopublishing.")
                false
            }
            else -> {
                log("Auto publication of local modules is enabled.")
                true
            }
        }
    }

    /**
     * Get a list of all the modules configured to be autopublished together with their known status.
     */
    internal fun getLocalModulesProperties(): List<ModuleState> {
        // Support the scenario in which none of the needed modules have been indexed yet.
        val statuses = if (modulesStatusesFile.exists()) {
            modulesStatusesFile.asProperties.entries
        } else {
            emptyList<Map.Entry<Any, Any>>()
        }

        return localConfigurationFile.asProperties.entries
            .map { module ->
                ModuleState(
                    Module(module.key.toString(), module.value.toString()),
                    statuses.firstOrNull { status -> status.key == module.value }?.value.toString()
                )
            }
    }

    /**
     * Persist locally the provided [status] for [module].
     *
     * @param module An external module which has been indexed and needs to have it's status persisted on disk.
     * @param status The current status of [module].
     */
    internal fun updateModule(module: Module, status: String) {
        with (modulesStatusesFile.ensureExists().asProperties) {
            setProperty(module.localPath, status)
            store(modulesStatusesFile.writer(), "Current repository state of autopublishing modules")
        }
        log("Persisted the current state of the project from: ${getPathInProject(project, module.localPath)}")
    }

    private fun createStarterModuleConfigurationFile() {
        with (localConfigurationFile.ensureExists().asProperties) {
            store(localConfigurationFile.writer(), createDefaultModulesConfigurationMessage)
        }
        log("Created ${getPathInProject(project, localConfigurationFile.toString())} as a started file.")
    }

    private fun ensurePluginConfigurationIsNotTrackedByGit() {
        with(File(project.rootProject.projectDir.absolutePath, ".gitignore").ensureExists()) {
            if (readLines().none { it == "/autopublish" || it == "autopublish" }) {
                appendText("/autopublish")
            }
        }
    }
}

/**
 * Load this [File] as a new [Properties] object.
 */
private val File.asProperties
    get() = Properties().apply { load(reader()) }

/**
 * Create this [File] if it doesn't already exist.
 */
private fun File.ensureExists(): File {
    if (!exists()) {
        createNewFile()
    }
    return this
}
