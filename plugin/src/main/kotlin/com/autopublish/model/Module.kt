package com.autopublish.model

/**
 * Local module identification data.
 *
 * @property name Full name of the dependency expected to be produced by the module.
 * Contains both the group and the artifact id but no version.
 * @property localPath Local path of where the module can be found.
 * Can be relative to the current project or an absolute one.
 */
internal data class Module(
    val name: String,
    val localPath: String,
)
