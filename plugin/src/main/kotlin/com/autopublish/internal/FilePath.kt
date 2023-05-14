package com.autopublish.internal

import org.gradle.api.Project
import java.net.URI
import kotlin.io.path.Path

/**
 * Transform [relativePath] to an absolute one with the help of [project]s path.
 *
 * @param project The current project - it has a known absolute path.
 * @param relativePath Path relative to the current project.
 *
 * @return an [URI] to the file indicated by [relativePath].
 */
internal fun getPathInProject(
    project: Project,
    relativePath: String
) = URI(
    /*scheme*/  null,
    /*host*/    Path(project.rootProject.projectDir.absolutePath).resolve(relativePath).toString(),
    /*path*/    null,
    /*fragment*/null
).normalize()
