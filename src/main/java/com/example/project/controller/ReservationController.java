package com.example.project.controller;

import com.example.project.common.ApiResponse;
import com.example.project.dto.reservation.ReservationDTO;
import com.example.project.dto.reservation.UpdateReservationDto;
import com.example.project.model.Reservation;
import com.example.project.service.IReservationService;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    IReservationService reservationService;

    private static final Logger LOG = LogManager.getLogger(ReservationController.class);

    @Async
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @GetMapping("/user")
    public CompletableFuture<ResponseEntity<List<ReservationDTO>>> getUserReservation(@RequestParam("userEmail") String userEmail) throws NotFoundException {
        List<Reservation> reservations = reservationService.getUserReservations(userEmail);
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservationDTOS.add(new ReservationDTO(reservation));
        }
        return CompletableFuture.completedFuture(ResponseEntity.ok(reservationDTOS));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createReservation(@RequestBody ReservationDTO reservationDTO) throws NotFoundException {
        reservationService.createReservation(reservationDTO);
        return new ResponseEntity<>(new ApiResponse(true,
                String.format("Reservation has been created for user %s!", reservationDTO.getUserEmail())), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateReservation(@RequestBody UpdateReservationDto updateReservationDto) throws NotFoundException {
        reservationService.updateReservation(updateReservationDto);
        return new ResponseEntity<>(new ApiResponse(true,
                String.format("Reservation has been updated for user %s!", updateReservationDto.getUserEmail())), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CASHIER')")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelReservation(@RequestParam("id") String id) throws NotFoundException {
        reservationService.cancelReservation(id);
        return new ResponseEntity<>(new ApiResponse(true, "Reservation has been canceled!"), HttpStatus.OK);
    }

}
