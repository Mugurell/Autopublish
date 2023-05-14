/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package com.autopublish.model

/**
 * Local module identification data.
 *
 * @property module Informations about a particular external project.
 * @property state The current indexed status of [module]. `Null` if unknown.
 */
internal data class ModuleState(
    val module: Module,
    val state: String? = null,
)
