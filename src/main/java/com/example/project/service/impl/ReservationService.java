package com.example.project.service.impl;

import com.example.project.dto.reservation.ReservationDTO;
import com.example.project.dto.reservation.UpdateReservationDto;
import com.example.project.model.Order;
import com.example.project.model.Reservation;
import com.example.project.model.Table;
import com.example.project.model.User;
import com.example.project.repository.IReservationRepository;
import com.example.project.service.IOrderService;
import com.example.project.service.IReservationService;
import com.example.project.service.ITableService;
import com.example.project.service.IUserService;
import com.example.project.util.TimeUtil;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService implements IReservationService {

    private static final Logger LOG = LogManager.getLogger(ReservationService.class);

    @Value("${day.start.time}")
    private String startWorkingTime;

    @Value("${day.end.time}")
    private String endWorkingTime;

    @Autowired
    IReservationRepository reservationRepository;
    @Autowired
    ITableService tableService;
    @Autowired
    IUserService userService;
    @Autowired
    IOrderService orderService;

    @Override
    public List<Reservation> getUserReservations(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        if (user == null) {
            LOG.error("User with email %s was not found");
            return null;
        }

        return reservationRepository.findByUserIdAndActive(user.getId());
    }

    @Override
    public void createReservation(ReservationDTO reservationDTO) throws NotFoundException {

        List<Table> availableTables = getAvailableTable(reservationDTO.getStartTime(), reservationDTO.getEndTime(), reservationDTO.getAmountOfPeople());

        if (availableTables.isEmpty()) {
            throw new IllegalArgumentException("Available tables for given amount of people was not found!");
        }

        User user = userService.getUserByEmail(reservationDTO.getUserEmail());
        if (user == null) {
            throw new NotFoundException(String.format("User with given email %s was not found!", reservationDTO.getUserEmail()));
        }

        if (getUserReservations(user.getEmail()).size() > 5) {
            throw new IllegalArgumentException(String.format("User %s has too much planned reservations!", user.getEmail()));
        }

        Reservation reservation = new Reservation();
        reservation.setActive(true);
        reservation.setDescription(reservationDTO.getDescription());
        reservation.setAmountOfPeople(reservationDTO.getAmountOfPeople());
        reservation.setStartTime(TimeUtil.formatLocalDateTime(reservationDTO.getStartTime()));
        reservation.setEndTime(TimeUtil.formatLocalDateTime(reservationDTO.getEndTime()));
        reservation.setUser(user);
        reservation.setCanceled(false);
        reservation.setTables(Collections.singletonList(availableTables.get(0)));

        reservationRepository.save(reservation);
    }

    private List<Table> getAvailableTable(LocalDateTime startTime, LocalDateTime endTime, Integer amountOfPeople) {
        List<Table> allTables = tableService.getAllTables();
        Set<Table> reservedTables = new HashSet<>();
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time of reservation can not be before start time!");
        }

        if (endTime.toLocalTime().isAfter(TimeUtil.formatStringToLocalTime(endWorkingTime)) ||
                startTime.toLocalTime().isBefore(TimeUtil.formatStringToLocalTime(startWorkingTime))) {
            throw new IllegalArgumentException(String.format("Invalid time selected! Start time: %s. End time: %s",
                    startWorkingTime.substring(0, 4), endWorkingTime.substring(0, 5)));
        }

        if (TimeUtil.formatLocalDateTimeFromLocalDate(startTime.toLocalDate()).plusDays(1)
                .compareTo(TimeUtil.formatLocalDateTimeFromLocalDate(endTime.toLocalDate())) <= 0) {
            throw new IllegalArgumentException("Too big time gap for reservation!");
        }

        List<Reservation> reservations = reservationRepository.getReservationByTime(startTime, endTime);
        reservations.forEach(reservation -> reservedTables.addAll(reservation.getTables()));
        allTables.removeAll(reservedTables);

        if (allTables.isEmpty()) {
            throw new IllegalArgumentException("Available tables for given time was not found!");
        }

        List<Table> availableTables = allTables.stream()
                .filter(table -> table.getMaxNumberOfSeats().equals(amountOfPeople))
                .collect(Collectors.toList());
        if (availableTables.isEmpty()) {
            availableTables = allTables.stream()
                    .filter(table -> table.getMinNumberOfSeats() <= amountOfPeople &&
                            table.getMaxNumberOfSeats() >= amountOfPeople)
                    .collect(Collectors.toList());
        }

        availableTables.sort(Comparator.comparing(Table::getMinNumberOfSeats));

        return availableTables;
    }

    @Override
    public void updateReservation(UpdateReservationDto updateReservationDto) throws NotFoundException {
        Reservation outdatedReservation = reservationRepository.getById(updateReservationDto.getId());
        if (outdatedReservation == null) {
            throw new NotFoundException(String.format("Reservation with id %s was not found!", updateReservationDto.getId()));
        }

        User user = userService.getUserByEmail(updateReservationDto.getUserEmail());
        if (user == null) {
            throw new NotFoundException(String.format("User with given email %s was not found!", updateReservationDto.getUserEmail()));
        }

        LocalDateTime start = updateReservationDto.getStartTime();
        LocalDateTime end = updateReservationDto.getEndTime();
        Integer amountOfPeople = updateReservationDto.getAmountOfPeople();

        List<Table> availableTable = new ArrayList<>();

        if (!outdatedReservation.getAmountOfPeople().equals(amountOfPeople) ||
                !outdatedReservation.getStartTime().equals(start) ||
                !outdatedReservation.getEndTime().equals(end)) {
            availableTable = getAvailableTable(start, end, amountOfPeople);
        }

        if (availableTable.isEmpty()) {
            throw new IllegalArgumentException("Available tables for given amount of people was not found!");
        }

        checkRating(user, outdatedReservation.getStartTime(), new LocalDateTime());

        outdatedReservation.setStartTime(updateReservationDto.getStartTime());
        outdatedReservation.setEndTime(updateReservationDto.getEndTime());
        outdatedReservation.setAmountOfPeople(updateReservationDto.getAmountOfPeople());
        outdatedReservation.setUser(user);
        outdatedReservation.setDescription(updateReservationDto.getDescription());
        outdatedReservation.setTables(Collections.singletonList(availableTable.get(0)));

        reservationRepository.save(outdatedReservation);
    }

    @Override
    public void cancelReservation(String id) throws NotFoundException {
        Reservation outdatedReservation = reservationRepository.getById(id);
        if (outdatedReservation == null) {
            throw new NotFoundException(String.format("Reservation with id %s was not found!", id));
        }

        checkRating(outdatedReservation.getUser(), outdatedReservation.getStartTime(), new LocalDateTime());

        outdatedReservation.setActive(false);
        outdatedReservation.setCanceled(true);
        reservationRepository.save(outdatedReservation);
    }

    private void checkRating(User user, LocalDateTime reservationStartTime, LocalDateTime currentTime) {
        if (reservationStartTime.minusHours(1).isBefore(currentTime) && user.getRating() > 0) {
            user.setRating(user.getRating() - 500);
            userService.update(user);
        }
    }

    //todo check schedulers if they work
    @Scheduled(cron = "${cron.check.table.reservation.time}")
    private void checkReservation() {
        List<Reservation> outdatedReservations = reservationRepository.findByStartTimeAndActive(TimeUtil.formatLocalDateTime(new LocalDateTime()));
        LocalDateTime currentTime = TimeUtil.formatLocalDateTime(new LocalDateTime()).minusMinutes(30);
        for (Reservation reservation : outdatedReservations) {
            LocalDateTime startTime = TimeUtil.formatLocalDateTime(reservation.getStartTime());
            if (currentTime.compareTo(startTime) >= 0) {
                User reservationUser = reservation.getUser();
                List<Order> orders = orderService.getUserOrdersByTime(reservationUser, startTime, currentTime);
                if (orders == null || orders.isEmpty()) {
                    reservationUser.setRating(reservationUser.getRating() - 500);
                    reservation.setActive(false);
                    reservation.setCanceled(true);
                }
            }
        }
        reservationRepository.saveAll(outdatedReservations);
    }

    @Scheduled(cron = "${cron.check.table.reservation.time}")
    private void checkReservationEndTime() {
        List<Reservation> outdatedReservations = reservationRepository.findByEndTimeIs(TimeUtil.formatLocalDateTime(new LocalDateTime()));
        for (Reservation reservation : outdatedReservations) {
            reservation.setActive(false);
        }
        reservationRepository.saveAll(outdatedReservations);
    }
}
