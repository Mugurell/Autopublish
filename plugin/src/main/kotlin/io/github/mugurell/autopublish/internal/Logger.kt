/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package io.github.mugurell.autopublish.internal

/**
 * Simple logging utility which prepends a stable prefix to allow for easy attribution of console messages.
 *
 * @param message Text that will be sent to the standard output stream.
 */
internal fun log(message: String) {
    println("Autopublish: $message")
}
