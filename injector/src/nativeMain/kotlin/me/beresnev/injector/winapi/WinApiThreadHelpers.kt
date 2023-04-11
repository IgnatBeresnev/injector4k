package me.beresnev.injector.winapi

import kotlinx.cinterop.*
import me.beresnev.injector.logger
import platform.windows.*

data class ThreadInfo(
    val id: Int,
    val handle: HANDLE
)

/**
 * If the function succeeds, the return value is a handle to the new thread.
 * If the function fails, the return value is NULL.
 *
 * @param startFunctionAddress A pointer to the application-defined function of type [LPTHREAD_START_ROUTINE] to be
 *                             executed by the thread and represents the starting address of the thread in the remote
 *                             process. The function must exist in the remote process.
 * @param paramPointer         A pointer to a variable to be passed to the thread function.
 *
 * @return threadId if thread has been created
 */
fun ProcessInfo.createRemoteThread(
    startFunctionAddress: FARPROC,
    paramPointer: LPVOID? = null
): ThreadInfo? {
    logger.trace {
        "Creating remote thread with startAddressPointer=\"${startFunctionAddress.rawValue}\" " +
                "and paramPointer=${paramPointer?.rawValue}"
    }
    val threadInfo = memScoped {
        val threadIdVar = alloc<DWORDVar>()

        CreateRemoteThread(
            hProcess = this@createRemoteThread.processHandle,
            lpThreadAttributes = null,
            dwStackSize = 0.toUInt(),
            lpStartAddress = startFunctionAddress.reinterpret(),
            lpParameter = paramPointer,
            dwCreationFlags = 0.convert(),
            lpThreadId = threadIdVar.ptr
        )?.let { handle ->
            ThreadInfo(
                id = threadIdVar.value.toInt(),
                handle = handle
            )
        }
    }

    return if (threadInfo == null) {
        logger.error {
            "Failed to create a thread with startAddress=\"${startFunctionAddress.rawValue}\" and " +
                    "parameter=\"${paramPointer.rawValue}\""
        }
        null
    } else {
        logger.debug { "Successfully created thread, id: \"${threadInfo.id}\"" }
        threadInfo
    }
}

/**
 * Awaits for the thread defined by [ThreadInfo]
 */
fun ThreadInfo.await() {
    logger.trace { "Waiting for thread with id=\"${this.id}\"" }

    // https://learn.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-waitforsingleobject#return-value
    val code = when (WaitForSingleObject(this.handle, INFINITE)) {
        WAIT_ABANDONED -> "WAIT_ABANDONED"
        WAIT_OBJECT_0 -> "WAIT_OBJECT_0"
        WAIT_TIMEOUT.toUInt() -> "WAIT_TIMEOUT"
        WAIT_FAILED -> "WAIT_FAILED; ${getWinApiLastErrorMessage()}"
        else -> "Unknown"
    }
    logger.debug { "Finished waiting on thread with id=\"${this.id}\"; Return code: $code" }
}
