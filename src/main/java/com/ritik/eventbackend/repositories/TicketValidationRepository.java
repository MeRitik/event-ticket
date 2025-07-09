package com.ritik.eventbackend.repositories;

import java.util.UUID;

import com.ritik.eventbackend.entities.TicketValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketValidationRepository extends JpaRepository<TicketValidation, UUID> {
}
