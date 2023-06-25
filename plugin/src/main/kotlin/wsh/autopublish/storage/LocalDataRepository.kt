/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package wsh.autopublish.storage

import wsh.autopublish.internal.getPathInProject
import wsh.autopublish.internal.log
import wsh.autopublish.model.Module
import wsh.autopublish.model.ModuleState
import org.gradle.api.Project
import wsh.autopublish.AutopublishProperties
import java.io.File
import java.util.Properties

private val createDefaultModulesConfigurationMessage =
    """Configuration for what modules should be autopublished
      >The expected structure is <dependency>=<local path>, eg:
      >com.sample\:example=../anotherProject
      >The path can be relative to this project or an absolute path.
      >""".trimMargin(">")

private val moduleNotConfiguredMessage =
    """Autopublish disabled. Missing modules.properties file.
       >Use:
       >autoPublish {
       >    modulesConfigurationInput = file(modules configuration properties file)
       >}
       >to provide the properties file containing the modules to be autopublished.
      >""".trimMargin(">")


/**
 * Handles all operations on the local storage for this plugin and the project it is applied on.
 *
 * @param project The [Project] this plugin is applied to. Used to resolve storage paths.
 */
internal class LocalDataRepository(
    private val project: Project,
    private val config: AutopublishProperties,
) {
    private val configurationFile = config.modulesConfigurationInputProp.asFile.get()
    private val modulesStatusesFile = config.modulesChangesStatusOutputProp.asFile.get()

    /**
     * Check if there is any module set to be autopublished.
     *
     * @return `true` if external modules are declared for autopublication.
     */
    internal fun checkIfAutopublishingIsEnabled() = when {
        !configurationFile.exists() -> {
            log(moduleNotConfiguredMessage)
            if (config.generateInputTemplate) {
                createStarterModuleConfigurationFile()
            }
            false
        }

        configurationFile.asProperties.isEmpty -> {
            log("Autopublish disabled. No modules set for autopublishing.")
            false
        }

        else -> {
            log("Auto publication of local modules is enabled.")
            true
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

        return configurationFile.asProperties.entries
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
        with (configurationFile.ensureExists().asProperties) {
            store(configurationFile.writer(), createDefaultModulesConfigurationMessage)
        }
        log("Created ${getPathInProject(project, configurationFile.toString())} as a started file.")
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
        parentFile.mkdirs()
        createNewFile()
    }
    return this
}
