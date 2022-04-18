package services

import storage.Event

interface EventHandler {
    fun handle(event: Event)
}