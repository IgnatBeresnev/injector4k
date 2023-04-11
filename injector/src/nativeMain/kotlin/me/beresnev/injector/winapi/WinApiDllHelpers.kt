package me.beresnev.injector.winapi

import kotlinx.cinterop.reinterpret
import me.beresnev.injector.logger
import platform.windows.*

fun ProcessInfo.injectDll(dllPath: String): ThreadInfo? {
    val dllPathAddress = allocateBytes(size = dllPath.length) ?: return null
    val processHandle = checkNotNull(this.processHandle)
    try {
        val isDllPathWritten =
            writeProcessMemory(
                process = processHandle,
                address = dllPathAddress.reinterpret(),
                value = dllPath
            ) > 0

        if (!isDllPathWritten) return null

        val loadLibraryFunctionAddress = findExportedDllFunction(
            dllName = "KERNEL32.DLL", // Case matters, sometimes it's just kernel32.dll
            functionName = "LoadLibraryA" // In some cases might need to call LoadLibraryW
        ) ?: return null

        sleep(2000)

        return createRemoteThread(loadLibraryFunctionAddress, dllPathAddress)?.also { it.await() }
    } finally {
        freeMemory(dllPathAddress)
    }
}

/**
 * The function must be accessible to the current process, won't find anything otherwise.
 * This means it can only be called if you spawned the child process via [createProcess].
 *
 * Starts a new thread in which it calls the specified function.
 */
fun ProcessInfo.callExportedDllFunction(dllName: String, functionName: String): ThreadInfo? {
    val botlinMainFunction = findExportedDllFunction(dllName, functionName) ?: return null
    return createRemoteThread(startFunctionAddress = botlinMainFunction)
}

/**
 * If the function succeeds, the return value is the address of the exported function or variable.
 */
private fun findExportedDllFunction(
    dllName: String,
    functionName: String
): FARPROC? {
    logger.trace {
        "Finding exported function \"$functionName\" for DLL \"$dllName\""
    }
    val dllHandle = findDll(dllName) ?: return null
    return dllHandle.findExportedFunction(functionName)
}

private fun findDll(dllName: String): HMODULE? {
    // GetModuleHandleA for internal DLLs (like kernel32.dll), LoadLibraryA for external (to get the injected dll)
    val dllHandle = GetModuleHandleA(dllName) ?: LoadLibraryA(dllName)
    if (dllHandle == null) {
        logger.error { "Failed to find DLL named \"$dllName\"" }
    } else {
        logger.debug { "Found DLL \"${dllName}\" under \"${dllHandle.rawValue}\"" }
    }
    return dllHandle
}

/**
 * @receiver DLL module returned by [findDll]
 */
private fun HMODULE.findExportedFunction(functionName: String): FARPROC? {
    val exportedFunctionAddress = GetProcAddress(this, functionName)
    if (exportedFunctionAddress == null) {
        logger.error { "Failed to find exported DLL function \"$functionName\"" }
    } else {
        logger.debug { "Found exported DLL function \"$functionName\" under \"${exportedFunctionAddress.rawValue}\"" }
    }
    return exportedFunctionAddress
}
