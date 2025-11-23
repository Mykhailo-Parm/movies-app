package com.nure.PZ2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class CinemaConsoleClient {

    private static final String MOVIE_SERVICE_URL = "http://localhost:8081/api";
    private static final String BOOKING_SERVICE_URL = "http://localhost:8082/api";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8083/api";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        printWelcome();

        while (running) {
            printMenu();
            System.out.print("Choose option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> testMovieService(scanner);
                    case 2 -> testBookingService(scanner);
                    case 3 -> testPaymentService(scanner);
                    case 4 -> testCompleteWorkflow(scanner);
                    case 0 -> {
                        running = false;
                        System.out.println("\nGoodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void printWelcome() {
        System.out.println("\n============================================================");
        System.out.println("          CINEMA MANAGEMENT SYSTEM - REST CLIENT            ");
        System.out.println("============================================================");
    }

    private static void printMenu() {
        System.out.println("\n------------------------------------------------------------");
        System.out.println("MAIN MENU:");
        System.out.println("------------------------------------------------------------");
        System.out.println("1. Test Movie Service");
        System.out.println("2. Test Booking Service");
        System.out.println("3. Test Payment Service");
        System.out.println("4. Complete Workflow (search -> book -> pay)");
        System.out.println("0. Exit");
        System.out.println("------------------------------------------------------------");
    }

    // ============ MOVIE SERVICE TESTS ============

    private static void testMovieService(Scanner scanner) {
        System.out.println("\nMOVIE SERVICE - Testing");
        System.out.println("1. Get all movies");
        System.out.println("2. Search movies by title");
        System.out.println("3. Get movie by ID");
        System.out.println("4. Get all sessions");
        System.out.println("5. Get sessions by movie ID");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> getAllMovies();
                case 2 -> searchMovies(scanner);
                case 3 -> getMovieById(scanner);
                case 4 -> getAllSessions();
                case 5 -> getSessionsByMovieId(scanner);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getAllMovies() throws Exception {
        System.out.println("\nRequest: GET /movies");
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void searchMovies(Scanner scanner) throws Exception {
        System.out.print("Enter search text: ");
        String searchText = scanner.nextLine();

        System.out.println("\nRequest: GET /movies?search=" + searchText);
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies?search=" + searchText);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getMovieById(Scanner scanner) throws Exception {
        System.out.print("Enter movie ID (e.g., mov-001): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /movies/" + id);
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + id);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getAllSessions() throws Exception {
        System.out.println("\nRequest: GET /movies/sessions");
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/sessions");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getSessionsByMovieId(Scanner scanner) throws Exception {
        System.out.print("Enter movie ID (e.g., mov-002): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /movies/" + id + "/sessions");
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + id + "/sessions");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    // ============ BOOKING SERVICE TESTS ============

    private static void testBookingService(Scanner scanner) {
        System.out.println("\nBOOKING SERVICE - Testing");
        System.out.println("1. Get all bookings");
        System.out.println("2. Get booking by ID");
        System.out.println("3. Create new booking");
        System.out.println("4. Cancel booking");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> getAllBookings();
                case 2 -> getBookingById(scanner);
                case 3 -> createBooking(scanner);
                case 4 -> cancelBooking(scanner);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getAllBookings() throws Exception {
        System.out.println("\nRequest: GET /bookings");
        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getBookingById(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID (e.g., bk-1001): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /bookings/" + id);
        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings/" + id);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void createBooking(Scanner scanner) throws Exception {
        System.out.print("Enter session ID (e.g., sess-1001): ");
        String sessionId = scanner.nextLine();

        System.out.print("Enter user ID (e.g., user-9001): ");
        String userId = scanner.nextLine();

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        String json = String.format("""
            {
              "sessionId": "%s",
              "userId": "%s",
              "customerName": "%s",
              "customerEmail": "%s",
              "seats": [
                {"row": 5, "number": 10, "seatId": "R5N10"}
              ],
              "notes": "Booking via console client"
            }
            """, sessionId, userId, name, email);

        System.out.println("\nRequest: POST /bookings");
        System.out.println("Body:");
        System.out.println(json);

        String response = sendPostRequest(BOOKING_SERVICE_URL + "/bookings", json);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void cancelBooking(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID to cancel: ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: DELETE /bookings/" + id);
        String response = sendDeleteRequest(BOOKING_SERVICE_URL + "/bookings/" + id);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    // ============ PAYMENT SERVICE TESTS ============

    private static void testPaymentService(Scanner scanner) {
        System.out.println("\nPAYMENT SERVICE - Testing");
        System.out.println("1. Get all payments");
        System.out.println("2. Find payment by booking ID");
        System.out.println("3. Create new payment");
        System.out.println("4. Refund payment");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> getAllPayments();
                case 2 -> getPaymentByBookingId(scanner);
                case 3 -> createPayment(scanner);
                case 4 -> refundPayment(scanner);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getAllPayments() throws Exception {
        System.out.println("\nRequest: GET /payments");
        String response = sendGetRequest(PAYMENT_SERVICE_URL + "/payments");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getPaymentByBookingId(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID (e.g., bk-1001): ");
        String bookingId = scanner.nextLine();

        System.out.println("\nRequest: GET /payments?bookingId=" + bookingId);
        String response = sendGetRequest(PAYMENT_SERVICE_URL + "/payments?bookingId=" + bookingId);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void createPayment(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID: ");
        String bookingId = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Select method (CARD/PAYPAL/CASH): ");
        String method = scanner.nextLine().toUpperCase();

        String json = String.format("""
            {
              "bookingId": "%s",
              "amount": {"value": %.2f, "currency": "EUR"},
              "method": "%s"
            }
            """, bookingId, amount, method);

        System.out.println("\nRequest: POST /payments");
        System.out.println("Body:");
        System.out.println(json);

        String response = sendPostRequest(PAYMENT_SERVICE_URL + "/payments", json);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void refundPayment(Scanner scanner) throws Exception {
        System.out.print("Enter payment ID for refund: ");
        String id = scanner.nextLine();

        String json = "{\"reason\": \"Booking cancellation\"}";

        System.out.println("\nRequest: POST /payments/" + id + "/refund");
        String response = sendPostRequest(PAYMENT_SERVICE_URL + "/payments/" + id + "/refund", json);
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    // ============ COMPLETE WORKFLOW TEST ============

    private static void testCompleteWorkflow(Scanner scanner) throws Exception {
        System.out.println("\nCOMPLETE WORKFLOW: Search -> Book -> Pay");
        System.out.println("============================================================");

        // Step 1: Search movies
        System.out.println("\nStep 1: Get available movies");
        String movies = sendGetRequest(MOVIE_SERVICE_URL + "/movies");
        System.out.println(movies);

        System.out.print("\nEnter movie ID to view sessions: ");
        String movieId = scanner.nextLine();

        // Step 2: View sessions
        System.out.println("\nStep 2: Get movie sessions");
        String sessions = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + movieId + "/sessions");
        System.out.println(sessions);

        System.out.print("\nEnter session ID for booking: ");
        String sessionId = scanner.nextLine();

        // Step 3: Create booking
        System.out.println("\nStep 3: Create booking");
        String bookingJson = String.format("""
            {
              "sessionId": "%s",
              "userId": "user-9999",
              "customerName": "Test User",
              "customerEmail": "test@example.com",
              "seats": [
                {"row": 8, "number": 15, "seatId": "R8N15"}
              ],
              "notes": "Complete workflow test"
            }
            """, sessionId);

        String bookingResponse = sendPostRequest(BOOKING_SERVICE_URL + "/bookings", bookingJson);
        System.out.println(bookingResponse);

        System.out.print("\nEnter created booking ID: ");
        String bookingId = scanner.nextLine();

        // Step 4: Payment
        System.out.println("\nStep 4: Process payment");
        String paymentJson = String.format("""
            {
              "bookingId": "%s",
              "amount": {"value": 10.5, "currency": "EUR"},
              "method": "CARD"
            }
            """, bookingId);

        String paymentResponse = sendPostRequest(PAYMENT_SERVICE_URL + "/payments", paymentJson);
        System.out.println(paymentResponse);

        System.out.println("\nComplete workflow finished successfully!");
    }

    // ============ HTTP METHODS ============

    private static String sendGetRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        return formatJson(response.body());
    }

    private static String sendPostRequest(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        return formatJson(response.body());
    }

    private static String sendDeleteRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private static String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }
}