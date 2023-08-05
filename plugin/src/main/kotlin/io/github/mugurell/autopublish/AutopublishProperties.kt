package io.github.mugurell.autopublish

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

private val modulesConfigurationPath = "autopublish${File.separatorChar}modules.properties"

private fun defaultReportsDir(project: Project): Directory = project.layout.buildDirectory.get()
    .dir(ReportingExtension.DEFAULT_REPORTS_DIR_NAME)
    .dir("autopublish")

abstract class AutopublishProperties(
    private val project: Project,
) {
    @get:Internal
    internal val modulesConfigurationInputProp
        get() = modulesConfigurationInput.convention { File(modulesConfigurationPath) }

    @get:Internal
    internal val modulesChangesStatusOutputProp
        get() = modulesChangesStatusOutput.convention { defaultReportsDir(project).asFile }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val modulesConfigurationInput: RegularFileProperty

    @get:OutputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val modulesChangesStatusOutput: RegularFileProperty

    @get:Internal
    internal abstract val generateInputTemplateProp: Property<Boolean>
    var generateInputTemplate: Boolean
        @Optional
        @Input
        get() = generateInputTemplateProp.getOrElse(false)
        set(value) = generateInputTemplateProp.set(value)
}
