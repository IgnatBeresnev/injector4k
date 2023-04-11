package me.beresnev.injector.logger

interface InjectorLogger {
    fun trace(msgComposer: LogMsgComposer)
    fun debug(msgComposer: LogMsgComposer)
    fun info(msgComposer: LogMsgComposer)
    fun warn(msgComposer: LogMsgComposer)
    fun error(msgComposer: LogMsgComposer)
}

fun interface LogMsgComposer {
    fun compose(): String?
}
