package com.ritik.eventbackend.services.impl;

import com.ritik.eventbackend.entities.Event;
import com.ritik.eventbackend.entities.TicketType;
import com.ritik.eventbackend.entities.User;
import com.ritik.eventbackend.entities.enums.EventStatusEnum;
import com.ritik.eventbackend.exceptions.EventNotFoundException;
import com.ritik.eventbackend.exceptions.EventUpdateException;
import com.ritik.eventbackend.exceptions.TicketTypeNotFoundException;
import com.ritik.eventbackend.exceptions.UserNotFoundException;
import com.ritik.eventbackend.payload.CreateEventRequest;
import com.ritik.eventbackend.payload.UpdateEventRequest;
import com.ritik.eventbackend.payload.UpdateTicketTypeRequest;
import com.ritik.eventbackend.repositories.EventRepository;
import com.ritik.eventbackend.repositories.UserRepository;
import com.ritik.eventbackend.services.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new UserNotFoundException("User with id: %s not found".formatted(organizerId)));

        Event newEvent = new Event();
        List<TicketType> newTicketTypes = event.getTicketTypes().stream().map(
                ticketType -> {
                    TicketType newTicketType = new TicketType();
                    newTicketType.setName(ticketType.getName());
                    newTicketType.setPrice(ticketType.getPrice());
                    newTicketType.setDescription(ticketType.getDescription());
                    newTicketType.setTotalAvailable(ticketType.getTotalAvailable());
                    newTicketType.setEvent(newEvent);
                    return newTicketType;
                }).toList();

        newEvent.setName(event.getName());
        newEvent.setStart(event.getStart());
        newEvent.setEnd(event.getEnd());
        newEvent.setVenue(event.getVenue());
        newEvent.setSalesStart(event.getSalesStart());
        newEvent.setSalesEnd(event.getSalesEnd());
        newEvent.setStatus(event.getStatus());
        newEvent.setOrganizer(organizer);
        newEvent.setTicketTypes(newTicketTypes);

        return eventRepository.save(newEvent);
    }

    @Override
    public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable);
    }

    @Override
    public Optional<Event> getEventForOrganizer(UUID organizerId, UUID id) {
        return eventRepository.findByIdAndOrganizerId(id, organizerId);
    }

    @Override
    @Transactional
    public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {
        if (event.getId() == null) {
            throw new EventUpdateException("Event ID cannot be null");
        }

        if (!id.equals(event.getId())) {
            throw new EventUpdateException("Cannot update the ID of an event");
        }

        Event currentEvent = eventRepository.findByIdAndOrganizerId(id, organizerId).orElseThrow(() -> new EventNotFoundException("Event with id: %s not found".formatted(id)));

        currentEvent.setName(event.getName());
        currentEvent.setVenue(event.getVenue());
        currentEvent.setStatus(event.getStatus());
        currentEvent.setStart(event.getStart());
        currentEvent.setEnd(event.getEnd());
        currentEvent.setSalesStart(event.getSalesStart());
        currentEvent.setSalesEnd(event.getSalesEnd());

        Set<UUID> requestTicketTypeIds = event.getTicketTypes()
                .stream()
                .map(UpdateTicketTypeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        currentEvent.getTicketTypes().removeIf(
                currentTicketType -> !requestTicketTypeIds.contains(currentTicketType.getId())
        );

        Map<UUID, TicketType> existingTicketTypeIndex = currentEvent.getTicketTypes().stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (UpdateTicketTypeRequest ticketType : event.getTicketTypes()) {
            if (ticketType.getId() == null) {
                // Create
                TicketType ticketTypeToCreate = new TicketType();
                ticketTypeToCreate.setName(ticketType.getName());
                ticketTypeToCreate.setPrice(ticketType.getPrice());
                ticketTypeToCreate.setDescription(ticketType.getDescription());
                ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                ticketTypeToCreate.setEvent(currentEvent);
                currentEvent.getTicketTypes().add(ticketTypeToCreate);

            } else if (existingTicketTypeIndex.containsKey(ticketType.getId())) {
                // Update
                TicketType existingTicketType = existingTicketTypeIndex.get(ticketType.getId());
                existingTicketType.setName(ticketType.getName());
                existingTicketType.setPrice(ticketType.getPrice());
                existingTicketType.setDescription(ticketType.getDescription());
                existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
            } else {
                throw new TicketTypeNotFoundException("Ticket type with ID %s not found".formatted(ticketType.getId()));
            }
        }

        return eventRepository.save(currentEvent);
    }

    @Override
    @Transactional
    public void deleteEventForOrganizer(UUID organizerId, UUID id) {
        getEventForOrganizer(organizerId, id).ifPresent(eventRepository::delete);
    }

    @Override
    public Page<Event> listPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatusEnum.PUBLISHED, pageable);
    }

    @Override
    public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
        return eventRepository.searchEvents(query, pageable);
    }

    @Override
    public Optional<Event> getPublishedEvent(UUID id) {
        return eventRepository.findByIdAndStatus(id, EventStatusEnum.PUBLISHED);
    }
}
