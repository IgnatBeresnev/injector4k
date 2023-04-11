package me.beresnev.injector.ui

import libui.ktx.TextArea
import me.beresnev.injector.logger.InjectorLogger
import me.beresnev.injector.logger.LogMsgComposer
import me.beresnev.injector.winapi.getSystemTime

class TextAreaLogger(
    private val loggerLevel: LoggingLevel,
    private val textArea: TextArea
) : InjectorLogger {

    override fun trace(msgComposer: LogMsgComposer) = log(LoggingLevel.Trace, msgComposer)
    override fun debug(msgComposer: LogMsgComposer) = log(LoggingLevel.Debug, msgComposer)
    override fun info(msgComposer: LogMsgComposer) = log(LoggingLevel.Info, msgComposer)
    override fun warn(msgComposer: LogMsgComposer) = log(LoggingLevel.Warn, msgComposer)
    override fun error(msgComposer: LogMsgComposer) = log(LoggingLevel.Error, msgComposer)

    private fun log(msgLevel: LoggingLevel, msgComposer: LogMsgComposer) {
        if (loggerLevel > msgLevel) return

        val logMsg = msgComposer.compose()?.takeIf { it.isNotBlank() }?.let {
            "[${getSystemTime()}][Injector][${msgLevel.toPrintable()}]: $it"
        } ?: return

        log(logMsg)
    }

    private fun LoggingLevel.toPrintable(): String =
        when (this) {
            LoggingLevel.Error -> "ERROR"
            LoggingLevel.Warn -> "WARN"
            LoggingLevel.Info -> "INFO"
            LoggingLevel.Debug -> "DEBUG"
            LoggingLevel.Trace -> "TRACE"
        }

    private fun log(msg: String) {
        textArea.append("$msg\r\n")
    }
}

sealed class LoggingLevel(private val level: Int) : Comparable<LoggingLevel> {
    object Error : LoggingLevel(5)
    object Warn : LoggingLevel(4)
    object Info : LoggingLevel(3)
    object Debug : LoggingLevel(2)
    object Trace : LoggingLevel(1)

    override fun compareTo(other: LoggingLevel): Int = level.compareTo(other.level)
}
