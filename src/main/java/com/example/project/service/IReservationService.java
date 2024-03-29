package com.example.project.service;

import com.example.project.dto.reservation.ReservationDTO;
import com.example.project.dto.reservation.UpdateReservationDto;
import com.example.project.model.Reservation;
import javassist.NotFoundException;

import java.util.List;

public interface IReservationService {

    List<Reservation> getUserReservations(String userEmail) throws NotFoundException;

    void createReservation(ReservationDTO reservationDTO) throws NotFoundException;

    void updateReservation(UpdateReservationDto updateReservationDto) throws NotFoundException;

    void cancelReservation(String id) throws NotFoundException;
}
