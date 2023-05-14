package com.autopublish.internal

/**
 * Simple utility for measuring the time needed for executing a particular method.
 * An advantage of this over the platform's offering is that this will return the original method's return value.
 *
 * @param timeCallback Consumer of the computed execution time of [block]
 * @param block Method which will have it's execution time measured.
 */
inline fun <T> measureTimeMillis(
    timeCallback: (Long) -> Unit,
    block: () -> T
): T {
    val startTime = System.currentTimeMillis()
    val result: T = block.invoke()
    timeCallback.invoke(System.currentTimeMillis() - startTime)

    return result
}
