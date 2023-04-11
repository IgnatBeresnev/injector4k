package me.beresnev.injector

import me.beresnev.injector.logger.InjectorLogger
import me.beresnev.injector.winapi.*

// TODO refactor when context receivers are stable
internal lateinit var logger: InjectorLogger

class Injector(injectorLogger: InjectorLogger) {
    init {
        logger = injectorLogger
    }

    /**
     * @param executablePath absolute path to the executable that needs to be injected into
     * @param dllPath absolute path to the dll that needs to be injected
     * @param mainFunctionName name of the top-level function exported as `@CName("..")`
     * @throws IllegalStateException if any of the vital steps have failed and the injector cannot proceed
     */
    fun inject(executablePath: String, dllPath: String, mainFunctionName: String) {
        require(executablePath.endsWith(".exe", ignoreCase = true)) { "Expected the executable to be an .exe file, got $executablePath" }
        require(dllPath.endsWith(".dll", ignoreCase = true)) { "Expected the dll path to be a .dll file, got $dllPath" }

        logger.info { "Starting DLL injection" }
        logger.debug { "------------------" }
        logger.debug { "Executable path: $executablePath" }
        logger.debug { "DLL path: $dllPath" }
        logger.debug { "Main function name: $mainFunctionName" }
        logger.debug { "------------------" }

        val process = createProcess(executablePath)?.also { it.open() } ?: throw IllegalStateException("Unable to create a process for $executablePath")
        sleep(2000)

        process.injectDll(dllPath) ?: throw IllegalStateException("Unable to inject the DLL $dllPath")
        sleep(5000)

        process.callExportedDllFunction(dllPath, mainFunctionName) ?: throw IllegalStateException("Unable to call exported DLL function $mainFunctionName")
        sleep(1000)

        logger.info { "Successfully injected the DLL" }
    }
}




