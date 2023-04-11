package me.beresnev.injector.ui

import libui.ktx.*
import me.beresnev.injector.Injector
import kotlin.system.exitProcess

fun launchGUI() {
    InjectorGUI(title = "Kotlin DLL injector", width = 600, height = 400).launch()
}

class InjectorGUI(
    private val title: String,
    private val width: Int,
    private val height: Int
) {
    private val availableLoggingLevels = listOf(
        FormLoggingLevel(LoggingLevel.Trace, "TRACE"),
        FormLoggingLevel(LoggingLevel.Debug, "DEBUG"),
        FormLoggingLevel(LoggingLevel.Info, "INFO"),
        FormLoggingLevel(LoggingLevel.Warn, "WARN"),
        FormLoggingLevel(LoggingLevel.Error, "ERROR"),
    )

    private lateinit var exePathField: TextField
    private lateinit var dllPathField: TextField
    private lateinit var mainFunctionNameField: TextField
    private lateinit var logLevelCombobox: Combobox
    private lateinit var closeAfterInjection: Checkbox

    private lateinit var logTextArea: TextArea

    fun launch() {
        appWindow(title = title, width = width, height = height) {
            vbox {
                form {
                    label = "Process exe"
                    processExeTextField()

                    label = "Injected DLL"
                    dllPathTextField()

                    label = "Main function name"
                    mainFunctionNameTextField()

                    hbox {
                        label = "Debug level"
                        debugLevelCombobox()
                        closeInjectorOnLaunchCheckbox()

                        stretchy = true
                    }
                }
                injectButton()
                logTextArea()
            }
        }
    }

    private fun Container.processExeTextField(): HBox {
        return hbox {
            exePathField = textfield(readonly = true) {
                stretchy = true
            }
            chooseFileButton(exePathField)
        }
    }

    private fun Container.dllPathTextField(): HBox {
        return hbox {
            dllPathField = textfield(readonly = true) {
                stretchy = true
            }
            chooseFileButton(dllPathField)
        }
    }

    private fun Container.chooseFileButton(textField: TextField, label: String = "Browse..."): Button {
        return button(label) {
            action {
                val chosenFilePath = OpenFileDialog()
                if (chosenFilePath != null) {
                    textField.value = chosenFilePath
                }
            }
        }
    }

    private fun Container.mainFunctionNameTextField() {
        mainFunctionNameField = textfield()
    }

    private fun Container.debugLevelCombobox(): Combobox {
        logLevelCombobox = combobox {
            availableLoggingLevels.forEach {
                item(it.displayName)
            }
            value = availableLoggingLevels.indexOfFirst { it.level == LoggingLevel.Info }
        }
        return logLevelCombobox
    }

    private fun Container.closeInjectorOnLaunchCheckbox() {
        closeAfterInjection = checkbox("Close after successful injection") {
            value = true
        }
    }

    private fun Container.injectButton(): Button {
        return button("Inject") {
            action {
                val loggingLevel = availableLoggingLevels[logLevelCombobox.value].level
                val logger = TextAreaLogger(loggingLevel, logTextArea)
                try {
                    Injector(logger).inject(
                        executablePath = exePathField.value,
                        dllPath = dllPathField.value,
                        mainFunctionName = mainFunctionNameField.value,
                    )
                    if (closeAfterInjection.value) {
                        exitProcess(0)
                    }
                } catch (e: Exception) {
                    logger.error { "Unable to perform the injection: ${e.message}" }
                }
            }
        }
    }

    private fun VBox.logTextArea() {
        logTextArea = textarea {
            readonly = true
            stretchy = true
        }
    }
}

private data class FormLoggingLevel(val level: LoggingLevel, val displayName: String)
