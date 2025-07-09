package com.ritik.eventbackend.services;

import com.ritik.eventbackend.entities.QrCode;
import com.ritik.eventbackend.entities.Ticket;

import java.util.UUID;

public interface QrCodeService {
  QrCode generateQrCode(Ticket ticket);
  byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
