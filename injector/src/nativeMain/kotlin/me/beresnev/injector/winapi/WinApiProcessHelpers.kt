@file:Suppress("PrivatePropertyName")

package me.beresnev.injector.winapi

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import me.beresnev.injector.logger
import platform.windows.*

private val DEFAULT_PROCESS_PERMISSIONS = listOf(
    PROCESS_CREATE_THREAD,
    PROCESS_QUERY_INFORMATION,
    PROCESS_VM_READ,
    PROCESS_VM_WRITE,
    PROCESS_VM_OPERATION
)

data class ProcessInfo(
    val processId: DWORD,
    val threadId: DWORD? = null,
    val processHandle: HANDLE?,
    val threadHandle: HANDLE? = null
)

fun createProcess(exePath: String): ProcessInfo? {
    logger.trace {
        "Creating process for \"$exePath\""
    }
    val processInfo = memScoped {
        val processInfo = alloc<PROCESS_INFORMATION>()
        val startup = alloc<STARTUPINFO>()

        CreateProcessW(
            lpApplicationName = exePath,
            lpCommandLine = null,
            lpProcessAttributes = null,
            lpThreadAttributes = null,
            bInheritHandles = 0,
            dwCreationFlags = CREATE_DEFAULT_ERROR_MODE,
            lpEnvironment = null, // use parent's environment, otherwise things like DirectX might not be loaded
            lpCurrentDirectory = exePath.substring(0, exePath.lastIndexOf("\\")), // important for some programs
            lpStartupInfo = startup.ptr,
            lpProcessInformation = processInfo.ptr
        ).takeIf { it != 0 }?.let {
            ProcessInfo(
                processId = processInfo.dwProcessId,
                threadId = processInfo.dwThreadId,
                processHandle = processInfo.hProcess,
                threadHandle = processInfo.hThread
            )
        }
    }

    if (processInfo == null) {
        logger.error { "Failed to create a process for \"$exePath\"" }
    } else {
        logger.debug { "Process created with ID: \"${processInfo.processId}\" (${processInfo.processId.toString(16)})" }
    }
    return processInfo
}

/**
 * @return true if the process was opened
 */
fun ProcessInfo.open(permissions: List<Int> = DEFAULT_PROCESS_PERMISSIONS): Boolean =
    open(this.processId, permissions) != null

/**
 * @return process information if the process was opened, null otherwise
 */
fun open(
    processId: DWORD,
    permissions: List<Int> = DEFAULT_PROCESS_PERMISSIONS
): ProcessInfo? {
    logger.trace {
        "Opening process with id=\"${processId}\""
    }
    val permissionsFlag = permissions.reduce { acc, value -> acc or value }

    val processHandle = OpenProcess(permissionsFlag.toUInt(), 0, processId)
    return if (processHandle == null) {
        logger.error { "Unable to open the process with id=\"${processId}\"" }
        null
    } else {
        logger.debug { "Opened process with id=\"${processId}\"" }
        ProcessInfo(
            processId = processId,
            processHandle = processHandle
        )
    }
}
