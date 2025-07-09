package com.ritik.eventbackend.services.impl;

import com.ritik.eventbackend.entities.QrCode;
import com.ritik.eventbackend.entities.Ticket;
import com.ritik.eventbackend.entities.TicketValidation;
import com.ritik.eventbackend.entities.enums.QrCodeStatusEnum;
import com.ritik.eventbackend.entities.enums.TicketValidationMethod;
import com.ritik.eventbackend.entities.enums.TicketValidationStatusEnum;
import com.ritik.eventbackend.exceptions.QrCodeNotFoundException;
import com.ritik.eventbackend.exceptions.TicketTypeNotFoundException;
import com.ritik.eventbackend.repositories.QrCodeRepository;
import com.ritik.eventbackend.repositories.TicketRepository;
import com.ritik.eventbackend.repositories.TicketValidationRepository;
import com.ritik.eventbackend.services.TicketValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {

    private final QrCodeRepository qrCodeRepository;
    private final TicketValidationRepository ticketValidationRepository;
    private final TicketRepository ticketRepository;

    @Override
    public TicketValidation validateTicketByQrCode(UUID qrCodeId) {
        QrCode qrCode = qrCodeRepository.findByIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE).orElseThrow(() -> new QrCodeNotFoundException("QR Code with ID %s not found".formatted(qrCodeId)));

        Ticket ticket = qrCode.getTicket();
        return  validateTicket(ticket, TicketValidationMethod.QR_SCAN);
    }

    @Override
    public TicketValidation validateTicketManually(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketTypeNotFoundException::new);
        return validateTicket(ticket, TicketValidationMethod.MANUAL);
    }

    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethod ticketValidationMethod) {
        TicketValidation ticketValidation = new TicketValidation();
        ticketValidation.setTicket(ticket);
        ticketValidation.setValidationMethod(ticketValidationMethod);

        TicketValidationStatusEnum ticketValidationStatus = ticket.getValidations().stream()
                .filter(validity -> TicketValidationStatusEnum.VALID.equals(validity.getStatus()))
                .findFirst()
                .map(v -> TicketValidationStatusEnum.INVALID)
                .orElse(TicketValidationStatusEnum.VALID);

        ticketValidation.setStatus(ticketValidationStatus);
        return ticketValidationRepository.save(ticketValidation);
    }

}