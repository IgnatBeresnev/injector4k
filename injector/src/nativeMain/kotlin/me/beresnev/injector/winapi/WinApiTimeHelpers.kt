package me.beresnev.injector.winapi

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.GetSystemTime
import platform.windows.GetTickCount64
import platform.windows.SYSTEMTIME

data class DateTime(
    val day: Int,
    val month: Int,
    val year: Int,

    val dayOfWeek: Int,

    val hour: Int,
    val minute: Int,
    val second: Int,
    val millis: Int,
) {
    override fun toString(): String = "$day/$month/$year, $hour:$minute:$second"
}

/**
 * @return millis since the system was started
 */
fun getCurrentTick(): ULong = GetTickCount64()

fun getSystemTime(): DateTime = memScoped {
    val out = alloc<SYSTEMTIME>()
    GetSystemTime(out.ptr)
    DateTime(
        day = out.wDay.toInt(),
        month = out.wMonth.toInt(),
        year = out.wYear.toInt(),
        dayOfWeek = out.wDayOfWeek.toInt(),
        hour = out.wHour.toInt(),
        minute = out.wMinute.toInt(),
        second = out.wSecond.toInt(),
        millis = out.wMilliseconds.toInt()
    )
}
