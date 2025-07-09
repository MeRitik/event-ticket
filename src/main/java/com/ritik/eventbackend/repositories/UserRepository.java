package com.ritik.eventbackend.repositories;

import java.util.UUID;

import com.ritik.eventbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

}
