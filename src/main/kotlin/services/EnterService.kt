package services

import exceptions.AccountAlreadyEnteredException
import exceptions.AccountExpiredException
import exceptions.AccountNotEnteredException
import storage.Event.*
import storage.EventStorage
import java.time.LocalDateTime.now

class EnterService(private val storage: EventStorage) {

    fun enter(login: String) {
        assertCanEnter(login)

        storage.addEvent(
            Entering(now(), login)
        )
    }

    fun exit(login: String) {
        assertCanExit(login)

        storage.addEvent(
            Exiting(now(), login)
        )
    }

    private fun assertCanEnter(login: String) {
        val expirationDate = storage.getAccountExpirationDate(login)

        if (expirationDate < now()) {
            throw AccountExpiredException()
        }

        if (storage.getEventsByLogin(login).last() is Entering) {
            throw AccountAlreadyEnteredException()
        }
    }

    private fun assertCanExit(login: String) {
        if (storage.getEventsByLogin(login).lastOrNull() !is Entering) {
            throw AccountNotEnteredException()
        }
    }
}