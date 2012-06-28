package com.github.marschall;

import java.io.IOException
import java.util.Locale
import java.util.Set
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.servlets.EventSource
import org.eclipse.jetty.servlets.EventSource.Emitter
import org.eclipse.jetty.servlets.EventSourceServlet

class ServerTimeServlet: EventSourceServlet() {

    private val emitters: Set<Emitter> = CopyOnWriteArraySet()

    private volatile var executor: ScheduledExecutorService? = null


    override fun init() {
        super.init()
        this.executor = Executors.newSingleThreadScheduledExecutor()
        this.executor!!.scheduleAtFixedRate(UpdateSender(), 0, 5, TimeUnit.SECONDS)
    }


    override fun destroy() {
        this.executor!!.shutdown()
        this.emitters.clear()
        super.destroy()
    }

    override fun newEventSource(request: HttpServletRequest?): EventSource? {
        return TimeEventSource()
    }

    class UpdateSender: Runnable {
        public override fun run() {
            val serverTime = formattedTime()
            for (emitter in emitters) {
                try {
                    LOG.info(serverTime)
                    emitter.data(serverTime)
                } catch (e: IOException) {
                    LOG.log(Level.SEVERE, "could not send update to client", e)
                }
            }

        }
    }


    class TimeEventSource: EventSource {

        private volatile var emitter: Emitter? = null

        override fun onOpen(emitter: Emitter?) {
            this.emitter = emitter
            emitters.add(emitter!!)
        }

        override fun onClose() {
            emitters.remove(this.emitter)
        }

    }

}

fun formattedTime(): String {
    return String.format(Locale.US, "%tT", System.currentTimeMillis())!!
}

val LOG: Logger = Logger.getLogger("event-source-sample")!!