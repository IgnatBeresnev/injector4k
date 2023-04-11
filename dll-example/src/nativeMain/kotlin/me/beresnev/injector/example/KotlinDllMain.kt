package me.beresnev.injector.example

import kotlinx.cinterop.*
import platform.windows.*

@CName("kotlinDllMain")
fun kotlinDllMain() {
   Sleep(10000)
   MessageBoxA(null, "Hello, World!", "Hi!", MB_OK.toUInt())
}
