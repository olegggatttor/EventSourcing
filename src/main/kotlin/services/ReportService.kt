package services

import storage.Event
import storage.EventStorage
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class ReportService(eventStorage: EventStorage) : EventHandler {
    private val visits = mutableMapOf<LocalDate, MutableMap<String, Long>>()
    private val lastVisits = mutableMapOf<String, LocalDateTime>()
    private var totalDuration = Duration.ZERO

    init {
        eventStorage.getAllEvents().forEach { event ->
            handleEventInner(event)
        }

        eventStorage.subscribe(this)
    }


    override fun handle(event: Event) {
        handleEventInner(event)
    }

    fun getDailyVisitsStat() = visits.mapValues { it.value.values.sum() }

    fun getAverageFrequency(): Double {
        val dailyVisits = getDailyVisitsStat()

        return dailyVisits.values.sum().toDouble() / dailyVisits.size.toDouble()
    }

    fun getTotalDuration(): Duration = totalDuration

    private fun handleEventInner(event: Event) {
        when (event) {
            is Event.Entering -> {
                lastVisits[event.login] = event.date

                visits.computeIfAbsent(event.date.toLocalDate()) { mutableMapOf<String, Long>().withDefault { 0 } }
                visits[event.date.toLocalDate()]!![event.login] =
                    visits[event.date.toLocalDate()]!!.getValue(event.login) + 1
            }
            is Event.Exiting -> {
                val duration = Duration.between(event.date, lastVisits[event.login])
                totalDuration += duration
            }
        }
    }
}