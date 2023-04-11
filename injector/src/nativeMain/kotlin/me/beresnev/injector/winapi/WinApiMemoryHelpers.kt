package me.beresnev.injector.winapi

import kotlinx.cinterop.*
import me.beresnev.injector.logger
import platform.windows.*

/**
 * If the function succeeds, the return value is the base address of the allocated region of pages.
 *
 * If the function fails, the return value is NULL.
 *
 * Allocated memory must be freed with [freeMemory] by passing the address returned by this function.
 */
fun ProcessInfo.allocateBytes(size: Int): LPVOID? {
    logger.trace {
        "VirtualAllocEx of $size bytes"
    }
    checkNotNull(this.processHandle) { "No process handle" }

    val allocMemoryAddress = memScoped {
        VirtualAllocEx(
            hProcess = this@allocateBytes.processHandle,
            lpAddress = null,
            dwSize = size.toUInt(),
            flAllocationType = (MEM_COMMIT or MEM_RESERVE).convert(),
            flProtect = PAGE_EXECUTE_READWRITE
        )
    }

    if (allocMemoryAddress == null) {
        logger.error { "Failed to allocate memory" }
    } else {
        logger.debug { "Allocated $size bytes at \"${allocMemoryAddress.rawValue}\"" }
    }
    return allocMemoryAddress
}

/**
 * Free the whole entire region that was previously reserved via [allocateBytes]
 */
fun ProcessInfo.freeMemory(address: LPVOID) {
    logger.trace {
        "VirtualFreeEx for process id=\"${this.processId}\" at address=\"${address.rawValue}\""
    }
    checkNotNull(this.processHandle) { "No process handle" }

    val isFree = VirtualFreeEx(this.processHandle, address, 0, MEM_RELEASE) != 0
    if (isFree) {
        logger.debug { "Freed memory at address=\"${address.rawValue}\"" }
    } else {
        logger.error { "Failed to free memory at address=\"${address.rawValue}\"" }
    }
}

fun writeProcessMemory(
    process: HANDLE,
    address: CPointer<CPointed>,
    value: String,
    size: UInt = value.length.toUInt()
): Long {
    return memScoped {
        val buff = alloc<SIZE_TVar>()
        val isSuccess = WriteProcessMemory(process, address, value.cstr.ptr, size, buff.ptr) == 1
        if (isSuccess) {
            logger.debug { "Successfully written $size bytes to ${address.rawValue}" }
            buff.value.toLong()
        } else {
            logger.error { "Was not able to write String data to \"${address.rawValue}\"; value=\"$value\"" }
            -1
        }
    }
}
