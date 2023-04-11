package me.beresnev.injector.logger

object NoopLogger : InjectorLogger {
    override fun trace(msgComposer: LogMsgComposer) {
        /* no-op */
    }

    override fun debug(msgComposer: LogMsgComposer) {
        /* no-op */
    }

    override fun info(msgComposer: LogMsgComposer) {
        /* no-op */
    }

    override fun warn(msgComposer: LogMsgComposer) {
        /* no-op */
    }

    override fun error(msgComposer: LogMsgComposer) {
        /* no-op */
    }
}

object PrintlnLogger : InjectorLogger {
    override fun trace(msgComposer: LogMsgComposer) {
        println(msgComposer.compose())
    }

    override fun debug(msgComposer: LogMsgComposer) {
        println(msgComposer.compose())
    }

    override fun info(msgComposer: LogMsgComposer) {
        println(msgComposer.compose())
    }

    override fun warn(msgComposer: LogMsgComposer) {
        println(msgComposer.compose())
    }

    override fun error(msgComposer: LogMsgComposer) {
        println(msgComposer.compose())
    }
}
