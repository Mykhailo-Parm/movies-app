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
        System.out.println("[IPC] Validating session " + request.getSessionId() + " with Movie Service...");

        MovieSessionDTO session = movieServiceClient.getSession(request.getSessionId());

        if (session == null) {
            // Check if Movie Service is available
            if (!movieServiceClient.isServiceHealthy()) {
                throw new IllegalArgumentException(
                        "Cannot validate session: Movie Service is not available at " +
                                movieServiceClient.getServiceUrl() + ". Please ensure the service is running."
                );
            }

            // Service is available but session not found
            throw new IllegalArgumentException(
                    "Session " + request.getSessionId() + " not found in Movie Service. " +
                            "Please verify the session ID is correct."
            );
        }

        // Validate session status
        if (!"Scheduled".equals(session.getStatus())) {
            throw new IllegalArgumentException(
                    "Session " + request.getSessionId() + " is not available for booking. " +
                            "Current status: " + session.getStatus() + ". Only 'Scheduled' sessions can be booked."
            );
        }

        // Validate available seats
        if (session.getAvailableSeats() < request.getSeats().size()) {
            throw new IllegalArgumentException(
                    "Not enough available seats in session " + request.getSessionId() + ". " +
                            "Requested: " + request.getSeats().size() + ", Available: " + session.getAvailableSeats()
            );
        }

        // Check if requested seats are already booked
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

        System.out.println("[IPC SUCCESS] Booking " + newId + " created for session " +
                request.getSessionId() + " (validated via Movie Service)");

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
                System.out.println("[STATUS CHANGE] Booking " + id + " confirmed");
            } else if ("CANCELLED".equals(request.getStatus())) {
                System.out.println("[STATUS CHANGE] Booking " + id + " cancelled");
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
            throw new IllegalArgumentException("Booking " + id + " is already cancelled");
        }

        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot cancel confirmed booking " + id + ". Please contact support for assistance."
            );
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        System.out.println("[CANCELLATION] Booking " + id + " cancelled successfully");
    }

    public void deleteBooking(String id) {
        if (bookingRepository.findById(id).isEmpty()) {
            throw new BookingNotFoundException("Booking with ID " + id + " not found");
        }
        bookingRepository.deleteById(id);
        System.out.println("[DELETION] Booking " + id + " deleted from system");
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
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID is required and cannot be empty");
        }
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required and cannot be empty");
        }
        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected for booking");
        }
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required and cannot be empty");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required and cannot be empty");
        }

        // Validate email format
        if (!request.getCustomerEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Customer email is not in valid format");
        }

        // Validate seat data
        for (CreateBookingRequest.SeatRequest seat : request.getSeats()) {
            if (seat.getSeatId() == null || seat.getSeatId().trim().isEmpty()) {
                throw new IllegalArgumentException("Seat ID is required for all seats");
            }
            if (seat.getRow() <= 0 || seat.getNumber() <= 0) {
                throw new IllegalArgumentException("Seat row and number must be positive");
            }
        }
    }

    private void checkSeatsAvailability(String sessionId, List<CreateBookingRequest.SeatRequest> requestedSeats) {
        for (CreateBookingRequest.SeatRequest seat : requestedSeats) {
            if (bookingRepository.isSeatBooked(sessionId, seat.getSeatId())) {
                throw new SeatAlreadyBookedException(
                        "Seat " + seat.getSeatId() + " (Row " + seat.getRow() + ", Number " +
                                seat.getNumber() + ") is already booked for session " + sessionId +
                                ". Please select a different seat."
                );
            }
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("CANCELLED".equals(currentStatus)) {
            throw new IllegalArgumentException(
                    "Cannot change status of cancelled booking. Current status: CANCELLED"
            );
        }
        if ("CONFIRMED".equals(currentStatus) && "PENDING".equals(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot change confirmed booking back to pending. " +
                            "Invalid transition: CONFIRMED -> PENDING"
            );
        }

        // Validate status value
        if (!List.of("PENDING", "CONFIRMED", "CANCELLED").contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status: " + newStatus + ". " +
                            "Allowed values: PENDING, CONFIRMED, CANCELLED"
            );
        }
    }
}