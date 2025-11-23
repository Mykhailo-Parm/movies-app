package com.nure.PZ2.booking.repository;

import com.nure.PZ2.booking.model.Booking;
import com.nure.PZ2.booking.model.Booking.Seat;
import com.nure.PZ2.booking.model.Booking.Price;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {

    private final List<Booking> bookings = new ArrayList<>();

    public BookingRepository() {
        initializeData();
    }

    private void initializeData() {
        bookings.add(new Booking(
                "bk-1001", "sess-1002", "user-9001",
                "Ivan Kovalchuk", "i.kovalchuk@gmail.com",
                Arrays.asList(
                        new Seat(7, 12, "R7N12"),
                        new Seat(7, 13, "R7N13")
                ),
                new Price(21.0, "EUR"), "CONFIRMED",
                LocalDateTime.of(2025, 10, 10, 14, 30),
                null,
                LocalDateTime.of(2025, 10, 10, 14, 31, 15),
                "Tickets with popcorn."
        ));

        bookings.add(new Booking(
                "bk-1002", "sess-1001", "user-9001",
                "Ivan Kovalchuk", "i.kovalchuk@gmail.com",
                Arrays.asList(new Seat(4, 5, "R4N5")),
                new Price(8.0, "EUR"), "PENDING",
                LocalDateTime.of(2025, 10, 12, 15, 0),
                LocalDateTime.of(2025, 10, 12, 15, 15),
                null, null
        ));
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings);
    }

    public Optional<Booking> findById(String id) {
        return bookings.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }

    public List<Booking> findBySessionId(String sessionId) {
        return bookings.stream()
                .filter(b -> b.getSessionId().equals(sessionId))
                .collect(Collectors.toList());
    }

    public List<Booking> findByUserId(String userId) {
        return bookings.stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Booking> findByStatus(String status) {
        return bookings.stream()
                .filter(b -> b.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public Booking save(Booking booking) {
        bookings.removeIf(b -> b.getId().equals(booking.getId()));
        bookings.add(booking);
        return booking;
    }

    public void deleteById(String id) {
        bookings.removeIf(b -> b.getId().equals(id));
    }

    public boolean isSessionBooked(String sessionId) {
        return bookings.stream()
                .anyMatch(b -> b.getSessionId().equals(sessionId) &&
                        !"CANCELLED".equals(b.getStatus()));
    }

    public boolean isSeatBooked(String sessionId, String seatId) {
        return bookings.stream()
                .filter(b -> b.getSessionId().equals(sessionId) &&
                        !"CANCELLED".equals(b.getStatus()))
                .flatMap(b -> b.getSeats().stream())
                .anyMatch(s -> s.getSeatId().equals(seatId));
    }
}