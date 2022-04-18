package storage

import exceptions.AccountDoesNotExistException
import services.EventHandler
import java.time.LocalDateTime

class EventStorage {
    private val listeners = mutableListOf<EventHandler>()
    private val events = ArrayList<Event>()

    fun addEvent(event: Event) {
        events.add(event)

        listeners.forEach { it.handle(event) }
    }

    fun subscribe(listener: EventHandler) {
        listeners.add(listener)
    }

    fun getEventsByLogin(login: String): List<Event> {
        return events.filter { it.login == login }
    }

    fun getAccountExpirationDate(login: String): LocalDateTime {
        val history = getEventsByLogin(login)

        if (history.isEmpty()) {
            throw AccountDoesNotExistException()
        }

        return history.fold(LocalDateTime.MIN) { acc, event ->
            when (event) {
                is Event.CreateAccount -> event.date + event.duration
                is Event.ExtendAccount -> acc + event.duration
                else -> acc
            }
        }
    }

    fun getAllEvents() = events
}