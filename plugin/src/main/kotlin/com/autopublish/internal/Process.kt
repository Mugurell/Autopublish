package com.autopublish.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStream

/**
 * Run [command] in the specified [location].
 * The operation is blocking and will execute in a separate new process.
 *
 * @param command Exact command to execute. Can contain multiple arguments.
 * @param location Exact location where this command should be executed.
 * The indicated path can be relative or absolute.
 * @param redirectOutputStream Optional listener for all output of running the indicated command.
 * Data will be sent as a [Flow] of output lines.
 * @param redirectOutputStream Optional listener for all errors of running the indicated command.
 * Data will be sent as a [Flow] of output lines.
 */
suspend fun execute(
    vararg command: String,
    location: String,
    redirectOutputStream: (suspend (Flow<String>) -> Unit)? = null,
    redirectErrorStream: (suspend (Flow<String>) -> Unit)? = null,
): Int {
    return runBlocking {
        ProcessBuilder(*command).apply {
            directory(File(location))
        }
            .start().also { process ->
                redirectOutputStream?.let { process.inputStream.publishStream(it) }
                redirectErrorStream?.let { process.errorStream.publishStream(it) }
            }
            .waitFor()
    }
}

private suspend fun InputStream.publishStream(consumer: suspend (Flow<String>) -> Unit) =
    bufferedReader().use {
        it.lineSequence().asFlow().let { f -> consumer(f) }
    }
