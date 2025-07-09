package com.ritik.eventbackend.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ritik.eventbackend.entities.QrCode;
import com.ritik.eventbackend.entities.Ticket;
import com.ritik.eventbackend.entities.enums.QrCodeStatusEnum;
import com.ritik.eventbackend.exceptions.QrCodeGenerationException;
import com.ritik.eventbackend.exceptions.QrCodeNotFoundException;
import com.ritik.eventbackend.repositories.QrCodeRepository;
import com.ritik.eventbackend.services.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    private static final int QR_HEIGHT = 300;
    private static final int QR_WIDTH = 300;

    private final QrCodeRepository qrCodeRepository;
    private final QRCodeWriter qrCodeWriter;

    @Override
    public QrCode generateQrCode(Ticket ticket) {
        try {
            UUID uuid = UUID.randomUUID();
            String qrCodeData = generateQrCodeImage(uuid);

            QrCode qrCode = new QrCode();
            qrCode.setId(uuid);
            qrCode.setStatus(QrCodeStatusEnum.ACTIVE);
            qrCode.setValue(qrCodeData);
            qrCode.setTicket(ticket);

            return qrCodeRepository.saveAndFlush(qrCode);
        } catch(IndexOutOfBoundsException | WriterException | IOException ex) {
            throw new QrCodeGenerationException("Failed to generate QR Code", ex);
        }
    }

    private String generateQrCodeImage(UUID uuid) throws WriterException, IOException {
        BitMatrix bitMatrix = qrCodeWriter.encode(
                uuid.toString(),
                BarcodeFormat.QR_CODE,
                QR_WIDTH,
                QR_HEIGHT
        );

        BufferedImage qrCodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(qrCodeImage, "png", os);
            byte[] imageBytes = os.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }

    }

    @Override
    public byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId) {
        QrCode qrCode = qrCodeRepository.findByTicketIdAndTicketPurchaserId(ticketId, userId).orElseThrow(QrCodeNotFoundException::new);

        try {
            return Base64.getDecoder().decode(qrCode.getValue());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid base64 QR Code for ticket ID: {}", ticketId, ex);
            throw new QrCodeGenerationException();
        }
    }
}