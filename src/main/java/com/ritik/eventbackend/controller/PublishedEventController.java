package com.ritik.eventbackend.controller;

import com.ritik.eventbackend.entities.Event;
import com.ritik.eventbackend.payload.dtos.GetPublishedEventDetailsResponseDto;
import com.ritik.eventbackend.payload.dtos.ListPublishedEventResponseDto;
import com.ritik.eventbackend.services.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {
    private final EventService eventService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<ListPublishedEventResponseDto>> listPublishedEvents(@RequestParam(required = false) String q,
                                                                                   Pageable pageable) {
        Page<Event> events;
        if (q != null && !q.trim().isEmpty()) {
            events = eventService.searchPublishedEvents(q, pageable);
        } else {
            events = eventService.listPublishedEvents(pageable);
        }

        return ResponseEntity.ok(events.map(event -> modelMapper.map(event, ListPublishedEventResponseDto.class)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<GetPublishedEventDetailsResponseDto> getPublishedEventDetails(@PathVariable UUID eventId) {
        return eventService.getPublishedEvent(eventId)
                .map(e -> modelMapper.map(e, GetPublishedEventDetailsResponseDto.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}