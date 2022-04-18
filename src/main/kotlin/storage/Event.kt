package storage

import java.time.Duration
import java.time.LocalDateTime

sealed class Event(val date: LocalDateTime, val login: String) {
    class CreateAccount(date: LocalDateTime, login: String, val duration: Duration) : Event(date, login)
    class ExtendAccount(date: LocalDateTime, login: String, val duration: Duration) : Event(date, login)
    class Entering(date: LocalDateTime, login: String) : Event(date, login)
    class Exiting(date: LocalDateTime, login: String) : Event(date, login)
}