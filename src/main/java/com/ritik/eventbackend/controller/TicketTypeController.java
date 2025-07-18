package com.ritik.eventbackend.controller;

import java.util.UUID;

import com.ritik.eventbackend.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ritik.eventbackend.util.JwtUtil.parseUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/events/{eventId}/ticket-types")
public class TicketTypeController {

  private final TicketTypeService ticketTypeService;

  @PostMapping(path = "/{ticketTypeId}/tickets")
  public ResponseEntity<Void> purchaseTicket(
          @AuthenticationPrincipal Jwt jwt,
          @PathVariable UUID ticketTypeId,
          @PathVariable String eventId) {
    ticketTypeService.purchaseTicket(parseUserId(jwt), ticketTypeId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}