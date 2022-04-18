package services

import exceptions.AccountAlreadyExistsException
import exceptions.AccountDoesNotExistException
import storage.Event.*
import storage.EventStorage
import utils.AccountInfo
import java.time.Duration
import java.time.LocalDateTime.now

class ManagerService(private val storage: EventStorage) {
    fun getAccountInfo(login: String): AccountInfo {
        val expirationDate = storage.getAccountExpirationDate(login)
        return AccountInfo(login, expirationDate)
    }

    fun createAccount(login: String, duration: Duration) {
        if (checkAccountExists(login)) {
            throw AccountAlreadyExistsException()
        }
        storage.addEvent(
            CreateAccount(now(), login, duration)
        )
    }

    fun extendAccount(login: String, duration: Duration) {
        if (!checkAccountExists(login)) {
            throw AccountDoesNotExistException()
        }
        storage.addEvent(
            ExtendAccount(now(), login, duration)
        )
    }


    private fun checkAccountExists(login: String) : Boolean {
        return storage.getEventsByLogin(login).any { it is CreateAccount }
    }
}