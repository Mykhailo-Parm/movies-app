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
                    case 5 -> testErrorHandling(scanner);
                    case 6 -> testContentNegotiation(scanner);
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
        System.out.println("5. Test Error Handling (404, 400, 409)");
        System.out.println("6. Test Content Negotiation (JSON/XML)");
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
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies", "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void searchMovies(Scanner scanner) throws Exception {
        System.out.print("Enter search text: ");
        String searchText = scanner.nextLine();

        System.out.println("\nRequest: GET /movies?search=" + searchText);
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies?search=" + searchText, "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getMovieById(Scanner scanner) throws Exception {
        System.out.print("Enter movie ID (e.g., mov-001): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /movies/" + id);
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + id, "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getAllSessions() throws Exception {
        System.out.println("\nRequest: GET /movies/sessions");
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/sessions", "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getSessionsByMovieId(Scanner scanner) throws Exception {
        System.out.print("Enter movie ID (e.g., mov-002): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /movies/" + id + "/sessions");
        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + id + "/sessions", "application/json");
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
        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings", "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getBookingById(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID (e.g., bk-1001): ");
        String id = scanner.nextLine();

        System.out.println("\nRequest: GET /bookings/" + id);
        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings/" + id, "application/json");
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
        String response = sendGetRequest(PAYMENT_SERVICE_URL + "/payments", "application/json");
        System.out.println("\nResponse:");
        System.out.println(response);
    }

    private static void getPaymentByBookingId(Scanner scanner) throws Exception {
        System.out.print("Enter booking ID (e.g., bk-1001): ");
        String bookingId = scanner.nextLine();

        System.out.println("\nRequest: GET /payments?bookingId=" + bookingId);
        String response = sendGetRequest(PAYMENT_SERVICE_URL + "/payments?bookingId=" + bookingId, "application/json");
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
        String movies = sendGetRequest(MOVIE_SERVICE_URL + "/movies", "application/json");
        System.out.println(movies);

        System.out.print("\nEnter movie ID to view sessions: ");
        String movieId = scanner.nextLine();

        // Step 2: View sessions
        System.out.println("\nStep 2: Get movie sessions");
        String sessions = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + movieId + "/sessions", "application/json");
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

    // ============ ERROR HANDLING TESTS ============

    private static void testErrorHandling(Scanner scanner) {
        System.out.println("\n=== ERROR HANDLING DEMONSTRATION ===");
        System.out.println("This section demonstrates proper HTTP error codes:");
        System.out.println("1. Test 404 Not Found");
        System.out.println("2. Test 400 Bad Request");
        System.out.println("3. Test 409 Conflict");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> test404NotFound();
                case 2 -> test400BadRequest();
                case 3 -> test409Conflict();
            }
        } catch (Exception e) {
            System.out.println("Error demonstration completed: " + e.getMessage());
        }
    }

    private static void test404NotFound() {
        System.out.println("\n--- Testing 404 Not Found ---");

        // Test 1: Non-existent movie
        System.out.println("\n1. Getting non-existent movie (mov-999):");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MOVIE_SERVICE_URL + "/movies/mov-999"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // Test 2: Non-existent booking
        System.out.println("\n2. Getting non-existent booking (bk-9999):");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKING_SERVICE_URL + "/bookings/bk-9999"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // Test 3: Non-existent payment
        System.out.println("\n3. Getting non-existent payment (pay-9999):");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_SERVICE_URL + "/payments/pay-9999"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static void test400BadRequest() {
        System.out.println("\n--- Testing 400 Bad Request ---");

        // Test 1: Invalid booking request (missing required fields)
        System.out.println("\n1. Creating booking with missing required fields:");
        try {
            String invalidJson = """
                {
                  "sessionId": "",
                  "userId": "",
                  "customerName": "",
                  "seats": []
                }
                """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKING_SERVICE_URL + "/bookings"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // Test 2: Invalid payment method
        System.out.println("\n2. Creating payment with invalid method:");
        try {
            String invalidJson = """
                {
                  "bookingId": "bk-1001",
                  "amount": {"value": 10.0, "currency": "EUR"},
                  "method": "INVALID_METHOD"
                }
                """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_SERVICE_URL + "/payments"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static void test409Conflict() {
        System.out.println("\n--- Testing 409 Conflict ---");

        // Test 1: Duplicate payment for same booking
        System.out.println("\n1. Creating duplicate payment for existing booking:");
        try {
            String json = """
                {
                  "bookingId": "bk-1001",
                  "amount": {"value": 21.0, "currency": "EUR"},
                  "method": "CARD"
                }
                """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYMENT_SERVICE_URL + "/payments"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // Test 2: Booking already booked seat
        System.out.println("\n2. Booking already reserved seat:");
        try {
            String json = """
                {
                  "sessionId": "sess-1002",
                  "userId": "user-9002",
                  "customerName": "Another User",
                  "customerEmail": "another@example.com",
                  "seats": [
                    {"row": 7, "number": 12, "seatId": "R7N12"}
                  ]
                }
                """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BOOKING_SERVICE_URL + "/bookings"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    // ============ CONTENT NEGOTIATION TEST ============

    private static void testContentNegotiation(Scanner scanner) {
        System.out.println("\n=== CONTENT NEGOTIATION DEMONSTRATION ===");
        System.out.println("Testing JSON and XML response formats");
        System.out.println("1. Get movies in JSON format");
        System.out.println("2. Get movies in XML format");
        System.out.println("3. Get bookings in JSON format");
        System.out.println("4. Get bookings in XML format");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> testJsonResponse();
                case 2 -> testXmlResponse();
                case 3 -> testBookingsJson();
                case 4 -> testBookingsXml();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testJsonResponse() throws Exception {
        System.out.println("\n--- JSON Response Format ---");
        System.out.println("Request: GET /movies");
        System.out.println("Accept: application/json\n");

        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies", "application/json");
        System.out.println("Response:");
        System.out.println(response);
    }

    private static void testXmlResponse() throws Exception {
        System.out.println("\n--- XML Response Format ---");
        System.out.println("Request: GET /movies");
        System.out.println("Accept: application/xml\n");

        String response = sendGetRequest(MOVIE_SERVICE_URL + "/movies", "application/xml");
        System.out.println("Response:");
        System.out.println(response);
    }

    private static void testBookingsJson() throws Exception {
        System.out.println("\n--- JSON Response Format ---");
        System.out.println("Request: GET /bookings");
        System.out.println("Accept: application/json\n");

        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings", "application/json");
        System.out.println("Response:");
        System.out.println(response);
    }

    private static void testBookingsXml() throws Exception {
        System.out.println("\n--- XML Response Format ---");
        System.out.println("Request: GET /bookings");
        System.out.println("Accept: application/xml\n");

        String response = sendGetRequest(BOOKING_SERVICE_URL + "/bookings", "application/xml");
        System.out.println("Response:");
        System.out.println(response);
    }

    // ============ HTTP METHODS ============

    private static String sendGetRequest(String url, String acceptHeader) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", acceptHeader)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (acceptHeader.equals("application/json")) {
            return formatJson(response.body());
        }
        return response.body();
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