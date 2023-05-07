package com.autopublish.storage

import com.autopublish.model.Module
import com.autopublish.model.ModuleState
import org.gradle.api.Project
import java.io.File
import java.util.Properties

private val modulesConfigurationPath = "autopublish${File.separatorChar}modules.properties"
private val modulesStatusesPath = "autopublish${File.separatorChar}modules-statuses.properties"


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
    internal val autopublishingIsEnabled = if (!localConfigurationFile.exists()) {
        println("Autopublish disabled. Missing modules.properties.")
        false
    } else {
        Properties().apply { load(localConfigurationFile.reader()) }.isNotEmpty()
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
        println("Updated the current changes status of the project from ${module.localPath}.")
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
