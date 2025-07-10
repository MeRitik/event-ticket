package com.ritik.eventbackend.controller;

import static com.ritik.eventbackend.util.JwtUtil.parseUserId;

import com.ritik.eventbackend.entities.Event;
import com.ritik.eventbackend.payload.CreateEventRequest;
import com.ritik.eventbackend.payload.UpdateEventRequest;
import com.ritik.eventbackend.payload.dtos.*;
import com.ritik.eventbackend.services.EventService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final ModelMapper modelMapper;
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponseDto> createEvent(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestBody CreateEventRequestDto createEventRequestDto) {
        CreateEventRequest createEventRequest = modelMapper.map(createEventRequestDto, CreateEventRequest.class);
        UUID userId = parseUserId(jwt);

        Event createdEvent = eventService.createEvent(userId, createEventRequest);
        CreateEventResponseDto responseDto = modelMapper.map(createdEvent, CreateEventResponseDto.class);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{eventId}")
    public ResponseEntity<UpdateEventResponseDto> updateEvent(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable UUID eventId,
                                                              @RequestBody UpdateEventRequestDto updateEventRequestDto) {
        UpdateEventRequest updateEventRequest = modelMapper.map(updateEventRequestDto, UpdateEventRequest.class);
        UUID userId = parseUserId(jwt);

        Event updatedEvent = eventService.updateEventForOrganizer(userId, eventId, updateEventRequest);
        UpdateEventResponseDto responseDto = modelMapper.map(updatedEvent, UpdateEventResponseDto.class);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDto>> listEvents(@AuthenticationPrincipal Jwt jwt,
                                                                 Pageable pageable) {
        UUID userId = parseUserId(jwt);
        Page<Event> events = eventService.listEventsForOrganizer(userId, pageable);
        Page<ListEventResponseDto> dtoPage = events.map(e -> modelMapper.map(e, ListEventResponseDto.class));
        return new ResponseEntity<>(dtoPage, HttpStatus.OK);
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetEventDetailsResponseDto> getEvent(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID eventId) {
        UUID userId = parseUserId(jwt);
        return eventService.getEventForOrganizer(userId, eventId)
                .map(e -> modelMapper.map(e, GetEventDetailsResponseDto.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{eventId}")
    public ResponseEntity<Void> deleteEvent(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID eventId) {
        UUID userId = parseUserId(jwt);
        eventService.deleteEventForOrganizer(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}