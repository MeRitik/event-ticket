package com.ritik.eventbackend.services.impl;

import com.ritik.eventbackend.entities.Ticket;
import com.ritik.eventbackend.entities.TicketType;
import com.ritik.eventbackend.entities.User;
import com.ritik.eventbackend.entities.enums.TicketStatusEnum;
import com.ritik.eventbackend.exceptions.TicketTypeNotFoundException;
import com.ritik.eventbackend.exceptions.TicketsSoldOutException;
import com.ritik.eventbackend.exceptions.UserNotFoundException;
import com.ritik.eventbackend.repositories.TicketRepository;
import com.ritik.eventbackend.repositories.TicketTypeRepository;
import com.ritik.eventbackend.repositories.UserRepository;
import com.ritik.eventbackend.services.QrCodeService;
import com.ritik.eventbackend.services.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID %s was not found".formatted(userId)));

        TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId).orElseThrow(() -> new TicketTypeNotFoundException("Ticket type with ID %s was not found".formatted(ticketTypeId)));

        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException();
        }

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);
        return ticketRepository.save(ticket);

    }
}