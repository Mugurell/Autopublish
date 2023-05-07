package com.autopublish

import com.autopublish.internal.getUpdatedStatus
import com.autopublish.internal.publish
import com.autopublish.internal.useLatestLocalArtifacts
import com.autopublish.storage.LocalDataRepository
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutopublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val localData = LocalDataRepository(target)

        if (localData.autopublishingIsEnabled) {
            println("Auto publication of local modules is enabled.")

            runBlocking {
                localData.getLocalModulesProperties().map {
                    val updatedStatus = it.getUpdatedStatus()
                    val module = it.module
                    if (updatedStatus != null) {
                        println("The ${module.name} repository has changes. Publishing now.")

                        // First go through the publishing flow.
                        // Only if this succeeds we can persist that the new state is used.
                        module.publish()
                        localData.updateModule(module, updatedStatus)

                        println("Autopublishing of ${module.name} is now complete.")
                    } else {
                        println("The ${module.name} repository has no recent changes.")
                    }
                    module.name
                }.toCollection(mutableListOf())
                    .also { autopublishedModules ->
                        target.useLatestLocalArtifacts(autopublishedModules)

                        println(when (autopublishedModules.size) {
                            0 -> "No new changes for modules set for autopublishing. Project $target will use existing artifacts."
                            1 -> "Project $target will now use a new version of [${autopublishedModules.joinToString()}]"
                            else -> "Project $target will now use new versions of [${autopublishedModules.joinToString()}]"
                        })
                    }
            }
        } else {
            println("Auto publication of local modules is disabled.")
        }
    }
}
