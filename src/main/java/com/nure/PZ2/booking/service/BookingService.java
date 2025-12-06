package com.nure.PZ2.booking.service;

import com.nure.PZ2.booking.client.MovieServiceClient;
import com.nure.PZ2.booking.dto.*;
import com.nure.PZ2.booking.exception.BookingNotFoundException;
import com.nure.PZ2.booking.exception.SeatAlreadyBookedException;
import com.nure.PZ2.booking.model.Booking;
import com.nure.PZ2.booking.model.Booking.Seat;
import com.nure.PZ2.booking.model.Booking.Price;
import com.nure.PZ2.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MovieServiceClient movieServiceClient;
    private final AtomicInteger idCounter = new AtomicInteger(1003);

    public BookingService(BookingRepository bookingRepository,
                          MovieServiceClient movieServiceClient) {
        this.bookingRepository = bookingRepository;
        this.movieServiceClient = movieServiceClient;
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookingDTO getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found"));
        return convertToDTO(booking);
    }

    public List<BookingDTO> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookingDTO createBooking(CreateBookingRequest request) {
        validateBookingRequest(request);

        // INTER-SERVICE CALL: Validate session exists in Movie Service
        MovieSessionDTO session = movieServiceClient.getSession(request.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException(
                    "Session " + request.getSessionId() + " not found in Movie Service"
            );
        }

        if (!"Scheduled".equals(session.getStatus())) {
            throw new IllegalArgumentException(
                    "Session " + request.getSessionId() + " is not available for booking"
            );
        }

        if (session.getAvailableSeats() < request.getSeats().size()) {
            throw new IllegalArgumentException(
                    "Not enough available seats in session " + request.getSessionId()
            );
        }

        checkSeatsAvailability(request.getSessionId(), request.getSeats());

        String newId = "bk-" + String.format("%04d", idCounter.getAndIncrement());
        LocalDateTime now = LocalDateTime.now();

        List<Seat> seats = request.getSeats().stream()
                .map(s -> new Seat(s.getRow(), s.getNumber(), s.getSeatId()))
                .collect(Collectors.toList());

        // Use price from Movie Service session
        Price totalPrice = new Price(
                session.getPrice().getValue() * seats.size(),
                session.getPrice().getCurrency()
        );

        Booking booking = new Booking(
                newId,
                request.getSessionId(),
                request.getUserId(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                seats,
                totalPrice,
                "PENDING",
                now,
                now.plusMinutes(15),
                null,
                request.getNotes()
        );

        Booking savedBooking = bookingRepository.save(booking);

        System.out.println("[IPC] Booking " + newId + " created for session " +
                request.getSessionId() + " from Movie Service");

        return convertToDTO(savedBooking);
    }

    public BookingDTO updateBooking(String id, UpdateBookingRequest request) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found"));

        if (request.getStatus() != null) {
            validateStatusTransition(existingBooking.getStatus(), request.getStatus());
            existingBooking.setStatus(request.getStatus());

            if ("CONFIRMED".equals(request.getStatus())) {
                existingBooking.setConfirmedAt(LocalDateTime.now());
                existingBooking.setExpiresAt(null);
            }
        }

        if (request.getNotes() != null) {
            existingBooking.setNotes(request.getNotes());
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return convertToDTO(updatedBooking);
    }

    public void cancelBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + id + " not found"));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public void deleteBooking(String id) {
        if (!bookingRepository.findById(id).isPresent()) {
            throw new BookingNotFoundException("Booking with ID " + id + " not found");
        }
        bookingRepository.deleteById(id);
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setSessionId(booking.getSessionId());
        dto.setUserId(booking.getUserId());
        dto.setCustomerName(booking.getCustomerName());
        dto.setCustomerEmail(booking.getCustomerEmail());

        List<BookingDTO.SeatDTO> seatDTOs = booking.getSeats().stream()
                .map(seat -> {
                    BookingDTO.SeatDTO seatDTO = new BookingDTO.SeatDTO();
                    seatDTO.setRow(seat.getRow());
                    seatDTO.setNumber(seat.getNumber());
                    seatDTO.setSeatId(seat.getSeatId());
                    return seatDTO;
                })
                .collect(Collectors.toList());
        dto.setSeats(seatDTOs);

        BookingDTO.PriceDTO priceDTO = new BookingDTO.PriceDTO();
        priceDTO.setValue(booking.getTotalPrice().getValue());
        priceDTO.setCurrency(booking.getTotalPrice().getCurrency());
        dto.setTotalPrice(priceDTO);

        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setExpiresAt(booking.getExpiresAt());
        dto.setConfirmedAt(booking.getConfirmedAt());
        dto.setNotes(booking.getNotes());

        return dto;
    }

    private void validateBookingRequest(CreateBookingRequest request) {
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new IllegalArgumentException("Session ID is required");
        }
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
    }

    private void checkSeatsAvailability(String sessionId, List<CreateBookingRequest.SeatRequest> requestedSeats) {
        for (CreateBookingRequest.SeatRequest seat : requestedSeats) {
            if (bookingRepository.isSeatBooked(sessionId, seat.getSeatId())) {
                throw new SeatAlreadyBookedException(
                        "Seat " + seat.getSeatId() + " is already booked for this session"
                );
            }
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("CANCELLED".equals(currentStatus)) {
            throw new IllegalArgumentException("Cannot change status of cancelled booking");
        }
        if ("CONFIRMED".equals(currentStatus) && "PENDING".equals(newStatus)) {
            throw new IllegalArgumentException("Cannot change confirmed booking back to pending");
        }
    }
}