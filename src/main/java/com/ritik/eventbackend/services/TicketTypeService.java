package com.ritik.eventbackend.services;

import com.ritik.eventbackend.entities.Ticket;

import java.util.UUID;

public interface TicketTypeService {
  Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}
