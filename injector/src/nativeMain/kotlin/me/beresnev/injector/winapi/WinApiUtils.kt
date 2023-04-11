package me.beresnev.injector.winapi

import me.beresnev.injector.logger
import platform.windows.GetLastError
import platform.windows.Sleep

fun getWinApiLastErrorMessage(): String? {
    // TODO format with FormatMessageW() for a human-readable error
    return getWinApiLastError()?.let { "Win API LastError: $it" }
}

fun getWinApiLastError(): Int? = GetLastError().takeIf { it != 0u }?.toInt()

fun sleep(millis: Int, quiet: Boolean = false) {
    if (!quiet) {
        logger.debug { "Sleeping for $millis millis" }
    }
    Sleep(millis.toUInt())
}
