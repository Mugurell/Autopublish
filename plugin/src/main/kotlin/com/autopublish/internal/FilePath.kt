/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

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
