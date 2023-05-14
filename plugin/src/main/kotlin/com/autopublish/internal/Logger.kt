package com.autopublish.internal

/**
 * Simple logging utility which prepends a stable prefix to allow for easy attribution of console messages.
 *
 * @param message Text that will be sent to the standard output stream.
 */
internal fun log(message: String) {
    println("Autopublish: $message")
}
