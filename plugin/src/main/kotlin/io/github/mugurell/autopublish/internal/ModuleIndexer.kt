/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package io.github.mugurell.autopublish.internal

import io.github.mugurell.autopublish.model.ModuleState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest

/**
 * Get whether this module has any changes from the last time it was indexed.
 *
 * @return new module changes identifier if it was modified recently or
 * `null` if it doesn't have recent modifications.
 */
internal suspend fun ModuleState.getUpdatedStatus(): String? {
    val newState = hashRepo(module.localPath)
    val hasChanges = state != newState

    return if (hasChanges) newState else null
}

/**
 * Calculate a hash of all the changes of the repository at [location].
 * This will check all tracked and untracked project files that could affect it's functionality.
 */
private suspend fun hashRepo(location: String): String {
    val hasher = MessageDigest.getInstance("SHA-256")

    // Get the current git revision
    execute(
        command = arrayOf("git", "rev-parse", "HEAD"),
        location = location,
        redirectOutputStream = { hasher.update(it.first().toByteArray()) },
    )

    // Get a list of all modified files tracked and untracked without the ones part of .gitignore
    // -all would add all such files
    // -n ensures this is a dry run
    execute(
        command = arrayOf("git", "add", "--all", "-n"),
        location = location,
        redirectOutputStream = {
            // Delegate to the git hashing mechanism to infer if files had recently been changed.
            it.collect { change ->
                val modifiedFile = change.substringAfter("add ").removeSurrounding("'")
                execute(
                    command = arrayOf("git", "hash-object", modifiedFile),
                    location = location,
                    redirectOutputStream = { fileHash ->
                        fileHash.firstOrNull()?.let {
                            hasher.update(it.toByteArray())
                        }
                    }
                )
            }
        },
    )

    return String(hasher.digest(), Charsets.ISO_8859_1)
}