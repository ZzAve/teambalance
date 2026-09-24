package nl.jvandis.teambalance.api.attendees

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.InvalidEventException
import nl.jvandis.teambalance.api.event.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "event-guests")
@RequestMapping(path = ["/api/events/{eventId}/guests"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EventGuestController(
    private val eventGuestRepository: EventGuestRepository,
    private val eventRepository: EventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getGuests(
        @PathVariable eventId: String,
    ): EventGuestsResponse {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.debug("Getting guests for event {}", eventTeamBalanceId)

        if (!eventRepository.exists(eventTeamBalanceId)) {
            throw InvalidEventException(eventTeamBalanceId)
        }

        val guests = eventGuestRepository.findAllByEventId(eventTeamBalanceId)
        return EventGuestsResponse(guests = guests.map { it.expose() })
    }

    @ResponseStatus(CREATED)
    @PostMapping
    fun addGuest(
        @PathVariable eventId: String,
        @RequestBody request: AddEventGuestRequest,
    ): EventGuestResponse {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.debug("Adding guest '{}' to event {}", request.name, eventTeamBalanceId)

        val eventInternalId =
            eventRepository.findInternalId(eventTeamBalanceId)
                ?: throw InvalidEventException(eventTeamBalanceId)

        val guest =
            eventGuestRepository.insert(
                eventInternalId = eventInternalId,
                eventTeamBalanceId = eventTeamBalanceId,
                name = request.name,
                phone = request.phone,
                note = request.note,
            )
        return guest.expose()
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{guestId}")
    fun deleteGuest(
        @PathVariable eventId: String,
        @PathVariable guestId: String,
    ) {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        val guestTeamBalanceId = TeamBalanceId(guestId)
        log.debug("Deleting guest {} from event {}", guestTeamBalanceId, eventTeamBalanceId)

        eventGuestRepository.deleteById(guestTeamBalanceId)
    }

    @ExceptionHandler(InvalidEventException::class)
    fun handleInvalidEvent(e: InvalidEventException): ResponseEntity<Error> =
        ResponseEntity
            .status(NOT_FOUND)
            .body(
                Error(
                    status = NOT_FOUND,
                    reason = "Could not find event with id ${e.teamBalanceId}",
                ),
            )

    @ExceptionHandler(GuestNotFoundException::class)
    fun handleGuestNotFound(e: GuestNotFoundException): ResponseEntity<Error> =
        ResponseEntity
            .status(NOT_FOUND)
            .body(
                Error(
                    status = NOT_FOUND,
                    reason = "Could not find guest with id ${e.guestId}",
                ),
            )
}

data class AddEventGuestRequest(
    val name: String,
    val phone: String? = null,
    val note: String? = null,
)

data class EventGuestsResponse(
    val guests: List<EventGuestResponse>,
)

data class EventGuestResponse(
    val id: String,
    val eventId: String,
    val name: String,
    val phone: String?,
    val note: String?,
)

fun EventGuest.expose() =
    EventGuestResponse(
        id = teamBalanceId.value,
        eventId = eventId.value,
        name = name,
        phone = phone,
        note = note,
    )

data class GuestNotFoundException(
    val guestId: TeamBalanceId,
) : RuntimeException()
