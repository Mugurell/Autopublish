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
