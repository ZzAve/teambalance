package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.InvalidEventException
import nl.jvandis.teambalance.api.event.EventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class EventGuestControllerTest {
    private val eventGuestRepository: EventGuestRepository = mock()
    private val eventRepository: EventRepository = mock()
    private val controller = EventGuestController(eventGuestRepository, eventRepository)

    private val eventId = "event-abc-123"
    private val eventTeamBalanceId = TeamBalanceId(eventId)

    @Test
    fun `GET guests returns list of guests for event`() {
        val guest =
            EventGuest(
                id = 1L,
                teamBalanceId = TeamBalanceId("guest-uuid-1"),
                eventId = eventTeamBalanceId,
                name = "Jan Guest",
                phone = "+31612345678",
                note = "libero sub",
            )
        `when`(eventRepository.exists(eventTeamBalanceId)).thenReturn(true)
        `when`(eventGuestRepository.findAllByEventId(eventTeamBalanceId)).thenReturn(listOf(guest))

        val result = controller.getGuests(eventId)

        assertEquals(1, result.guests.size)
        assertEquals("Jan Guest", result.guests[0].name)
        assertEquals("guest-uuid-1", result.guests[0].id)
        assertEquals(eventId, result.guests[0].eventId)
        assertEquals("+31612345678", result.guests[0].phone)
        assertEquals("libero sub", result.guests[0].note)
    }

    @Test
    fun `GET guests returns empty list when no guests`() {
        `when`(eventRepository.exists(eventTeamBalanceId)).thenReturn(true)
        `when`(eventGuestRepository.findAllByEventId(eventTeamBalanceId)).thenReturn(emptyList())

        val result = controller.getGuests(eventId)

        assertEquals(0, result.guests.size)
    }

    @Test
    fun `GET guests throws InvalidEventException when event does not exist`() {
        `when`(eventRepository.exists(eventTeamBalanceId)).thenReturn(false)

        assertThrows(InvalidEventException::class.java) {
            controller.getGuests(eventId)
        }
    }

    @Test
    fun `POST guest adds guest to event and returns created guest`() {
        val internalEventId = 42L
        val guestTeamBalanceId = TeamBalanceId("guest-uuid-new")
        val request = AddEventGuestRequest(name = "Maria Sub")
        val createdGuest =
            EventGuest(
                id = 10L,
                teamBalanceId = guestTeamBalanceId,
                eventId = eventTeamBalanceId,
                name = "Maria Sub",
                phone = null,
                note = null,
            )
        `when`(eventRepository.findInternalId(eventTeamBalanceId)).thenReturn(internalEventId)
        `when`(
            eventGuestRepository.insert(
                eventInternalId = internalEventId,
                eventTeamBalanceId = eventTeamBalanceId,
                name = "Maria Sub",
                phone = null,
                note = null,
            ),
        ).thenReturn(createdGuest)

        val result = controller.addGuest(eventId, request)

        assertEquals("Maria Sub", result.name)
        assertEquals("guest-uuid-new", result.id)
        assertEquals(eventId, result.eventId)
    }

    @Test
    fun `POST guest with phone and note stores all fields`() {
        val internalEventId = 42L
        val request = AddEventGuestRequest(name = "Pieter Sub", phone = "+31699887766", note = "weekend only")
        val createdGuest =
            EventGuest(
                id = 11L,
                teamBalanceId = TeamBalanceId("guest-uuid-2"),
                eventId = eventTeamBalanceId,
                name = "Pieter Sub",
                phone = "+31699887766",
                note = "weekend only",
            )
        `when`(eventRepository.findInternalId(eventTeamBalanceId)).thenReturn(internalEventId)
        `when`(
            eventGuestRepository.insert(
                eventInternalId = internalEventId,
                eventTeamBalanceId = eventTeamBalanceId,
                name = "Pieter Sub",
                phone = "+31699887766",
                note = "weekend only",
            ),
        ).thenReturn(createdGuest)

        val result = controller.addGuest(eventId, request)

        assertEquals("Pieter Sub", result.name)
        assertEquals("+31699887766", result.phone)
        assertEquals("weekend only", result.note)
    }

    @Test
    fun `POST guest throws InvalidEventException when event does not exist`() {
        `when`(eventRepository.findInternalId(eventTeamBalanceId)).thenReturn(null)

        assertThrows(InvalidEventException::class.java) {
            controller.addGuest(eventId, AddEventGuestRequest(name = "Someone"))
        }
    }

    @Test
    fun `DELETE guest removes guest by id`() {
        val guestId = "guest-uuid-to-delete"
        val guestTeamBalanceId = TeamBalanceId(guestId)

        controller.deleteGuest(eventId, guestId)

        verify(eventGuestRepository).deleteById(guestTeamBalanceId)
    }
}
