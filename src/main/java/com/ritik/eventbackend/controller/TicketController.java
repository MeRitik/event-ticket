package com.ritik.eventbackend.controller;

import java.time.Duration;
import java.util.UUID;

import com.ritik.eventbackend.payload.dtos.GetTicketResponseDto;
import com.ritik.eventbackend.payload.dtos.ListTicketResponseDto;
import com.ritik.eventbackend.services.QrCodeService;
import com.ritik.eventbackend.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ritik.eventbackend.util.JwtUtil.parseUserId;

@RestController
@RequestMapping(path = "/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final QrCodeService qrCodeService;
    private final ModelMapper modelMapper;

    @GetMapping
    public Page<ListTicketResponseDto> listTickets(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        return ticketService.listTicketsForUser(parseUserId(jwt), pageable)
                .map(ticket -> modelMapper.map(ticket, ListTicketResponseDto.class));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<GetTicketResponseDto> getTicket(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable("ticketId") UUID ticketId) {
        return ticketService
                .getTicketForUser(parseUserId(jwt), ticketId)
                .map(ticket -> modelMapper.map(ticket, GetTicketResponseDto.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{ticketId}/qr-codes")
    public ResponseEntity<byte[]> getTicketQrCode(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID ticketId) {
        byte[] qrCodeImage = qrCodeService.getQrCodeImageForUserAndTicket(parseUserId(jwt), ticketId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);

        return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
    }
}