package com.ritik.eventbackend.services;

import java.util.Optional;
import java.util.UUID;

import com.ritik.eventbackend.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
  Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);
  Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId);
}
