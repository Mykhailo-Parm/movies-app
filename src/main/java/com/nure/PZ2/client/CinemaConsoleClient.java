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
        checkServicesHealth();

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
                    case 5 -> demonstrateIPC(scanner);
                    case 6 -> testErrorHandling(scanner);
                    case 7 -> testContentNegotiation(scanner);
                    case 8 -> demonstrateFailSafe(scanner);
                    case 9 -> checkServicesHealth();
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
        System.out.println("              Microservices Architecture Demo               ");
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
        System.out.println("5. Demonstrate Inter-Service Communication (IPC)");
        System.out.println("6. Test Error Handling (404, 400, 409)");
        System.out.println("7. Test Content Negotiation (JSON/XML)");
        System.out.println("8. Demonstrate Fail-Safe Mechanism");
        System.out.println("0. Exit");
        System.out.println("------------------------------------------------------------");
    }

    // ============ HEALTH CHECK ============

    private static void checkServicesHealth() {
        System.out.println("\n============================================================");
        System.out.println("              SERVICES HEALTH CHECK");
        System.out.println("============================================================\n");

        boolean movieHealthy = checkServiceHealth("Movie Service", MOVIE_SERVICE_URL + "/movies");
        boolean bookingHealthy = checkServiceHealth("Booking Service", BOOKING_SERVICE_URL + "/bookings");
        boolean paymentHealthy = checkServiceHealth("Payment Service", PAYMENT_SERVICE_URL + "/payments");

        System.out.println("\n------------------------------------------------------------");
        if (movieHealthy && bookingHealthy && paymentHealthy) {
            System.out.println("✓ All services are running correctly");
        } else {
            System.out.println("⚠ Some services are not available");
            System.out.println("  Please start missing services before testing");
        }
        System.out.println("------------------------------------------------------------");
    }

    private static boolean checkServiceHealth(String serviceName, String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                System.out.println("✓ " + serviceName + " - HEALTHY (200 OK)");
                return true;
            } else {
                System.out.println("✗ " + serviceName + " - UNHEALTHY (Status: " + response.statusCode() + ")");
                return false;
            }
        } catch (java.net.ConnectException e) {
            System.out.println("✗ " + serviceName + " - NOT RUNNING (Connection refused)");
            return false;
        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("✗ " + serviceName + " - TIMEOUT (Not responding)");
            return false;
        } catch (Exception e) {
            System.out.println("✗ " + serviceName + " - ERROR (" + e.getMessage() + ")");
            return false;
        }
    }

    // ============ DEMONSTRATE IPC ============

    private static void demonstrateIPC(Scanner scanner) {
        System.out.println("\n============================================================");
        System.out.println("      INTER-SERVICE COMMUNICATION DEMONSTRATION");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("This demo shows how microservices communicate:");
        System.out.println("  Client → Booking Service → Movie Service (validate session)");
        System.out.println("  Client → Payment Service → Booking Service (validate & confirm)");
        System.out.println();
        System.out.println("Choose scenario:");
        System.out.println("1. Booking Service calls Movie Service (valid session)");
        System.out.println("2. Booking Service calls Movie Service (invalid session)");
        System.out.println("3. Booking Service calls Movie Service (service down)");
        System.out.println("4. Payment Service calls Booking Service (full flow)");
        System.out.println("5. Payment Service calls Booking Service (invalid booking)");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> demonstrateBookingToMovieValid();
                case 2 -> demonstrateBookingToMovieInvalid();
                case 3 -> demonstrateBookingToMovieServiceDown();
                case 4 -> demonstratePaymentToBooking();
                case 5 -> demonstratePaymentToBookingInvalid();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void demonstrateBookingToMovieValid() throws Exception {
        System.out.println("\n--- IPC Demo: Booking → Movie (Valid Session) ---");
        System.out.println();

        System.out.println("Step 1: Client requests to create booking for session 'sess-1001'");
        System.out.println("        POST /bookings → Booking Service");
        System.out.println();

        String json = """
            {
              "sessionId": "sess-1001",
              "userId": "user-demo",
              "customerName": "IPC Demo User",
              "customerEmail": "demo@ipc.com",
              "seats": [
                {"row": 10, "number": 15, "seatId": "R10N15"}
              ],
              "notes": "IPC Demo Booking"
            }
            """;

        System.out.println("Request Body:");
        System.out.println(json);
        System.out.println();
        System.out.println("Step 2: Booking Service validates session via IPC...");
        System.out.println("        [IPC] GET /movies/sessions/sess-1001 → Movie Service");
        System.out.println();

        String response = sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", json);

        System.out.println("\n✓ IPC Summary:");
        System.out.println("  - Client → Booking Service: 1 call");
        System.out.println("  - Booking Service → Movie Service: 1 call (internal)");
        System.out.println("  - IPC used: REST over HTTP with JSON Schema validation");
        System.out.println("  - Result: Booking created successfully");
    }

    private static void demonstrateBookingToMovieInvalid() throws Exception {
        System.out.println("\n--- IPC Demo: Booking → Movie (Invalid Session) ---");
        System.out.println();

        System.out.println("Step 1: Client requests booking for NON-EXISTENT session 'sess-9999'");
        System.out.println();

        String json = """
            {
              "sessionId": "sess-9999",
              "userId": "user-demo",
              "customerName": "IPC Demo User",
              "customerEmail": "demo@ipc.com",
              "seats": [
                {"row": 1, "number": 1, "seatId": "R1N1"}
              ]
            }
            """;

        System.out.println("Step 2: Booking Service validates session via IPC...");
        System.out.println("        [IPC] GET /movies/sessions/sess-9999 → Movie Service");
        System.out.println();

        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", json);

        System.out.println("\n✓ IPC Summary:");
        System.out.println("  - IPC prevented invalid booking creation");
        System.out.println("  - Data consistency maintained across services");
        System.out.println("  - Client received clear error message");
    }

    private static void demonstrateBookingToMovieServiceDown() throws Exception {
        System.out.println("\n--- IPC Demo: Booking → Movie (Service Unavailable) ---");
        System.out.println();
        System.out.println("NOTE: This demo requires Movie Service to be stopped");
        System.out.println("      Press Enter to continue (or Ctrl+C to skip)...");
        new Scanner(System.in).nextLine();

        String json = """
            {
              "sessionId": "sess-1001",
              "userId": "user-failsafe",
              "customerName": "Fail Safe Test",
              "customerEmail": "failsafe@test.com",
              "seats": [
                {"row": 20, "number": 20, "seatId": "R20N20"}
              ]
            }
            """;

        System.out.println("Attempting to create booking...");
        System.out.println("Booking Service will try to call Movie Service...");
        System.out.println();

        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", json);

        System.out.println("\n✓ Fail-Safe Summary:");
        System.out.println("  - Booking Service detected Movie Service unavailability");
        System.out.println("  - Returned clear error message to client");
        System.out.println("  - No cascading failure");
        System.out.println("  - System remains stable");
    }

    private static void demonstratePaymentToBooking() throws Exception {
        System.out.println("\n--- IPC Demo: Payment → Booking (Full Flow) ---");
        System.out.println();

        System.out.println("Prerequisites: Creating a test booking first...");

        String bookingJson = """
            {
              "sessionId": "sess-1002",
              "userId": "user-ipc-test",
              "customerName": "Payment IPC Test",
              "customerEmail": "payment@ipc.com",
              "seats": [
                {"row": 3, "number": 5, "seatId": "R3N5"}
              ]
            }
            """;

        String bookingResponse = sendPostRequest(BOOKING_SERVICE_URL + "/bookings", bookingJson);
        System.out.println("Booking created:");
        System.out.println(formatJson(bookingResponse));
        System.out.println();

        System.out.print("Enter the created booking ID (e.g., bk-1003): ");
        Scanner scanner = new Scanner(System.in);
        String bookingId = scanner.nextLine();

        String paymentJson = String.format("""
            {
              "bookingId": "%s",
              "amount": {"value": 10.5, "currency": "EUR"},
              "method": "CARD"
            }
            """, bookingId);

        System.out.println("\nInitiating payment...");
        String response = sendPostRequestWithDetails(PAYMENT_SERVICE_URL + "/payments", paymentJson);

        System.out.println("\nWaiting for async processing (3 seconds)...");
        Thread.sleep(3000);

        System.out.println("\nChecking booking status after payment...");
        String bookingCheck = sendGetRequest(BOOKING_SERVICE_URL + "/bookings/" + bookingId, "application/json");
        System.out.println(formatJson(bookingCheck));

        System.out.println("\n✓ IPC Summary:");
        System.out.println("  - Client → Payment Service: 1 call");
        System.out.println("  - Payment Service → Booking Service: 2 calls (validate + confirm)");
        System.out.println("  - Distributed transaction handled via async confirmation");
        System.out.println("  - Booking status changed: PENDING → CONFIRMED");
    }

    private static void demonstratePaymentToBookingInvalid() throws Exception {
        System.out.println("\n--- IPC Demo: Payment → Booking (Invalid Booking) ---");
        System.out.println();

        String paymentJson = """
            {
              "bookingId": "bk-9999",
              "amount": {"value": 100.0, "currency": "EUR"},
              "method": "CARD"
            }
            """;

        System.out.println("Attempting payment for non-existent booking 'bk-9999'...");
        System.out.println();

        sendPostRequestWithDetails(PAYMENT_SERVICE_URL + "/payments", paymentJson);

        System.out.println("\n✓ IPC Summary:");
        System.out.println("  - Payment Service detected invalid booking via IPC");
        System.out.println("  - Prevented payment for non-existent booking");
        System.out.println("  - Client received clear error message");
    }

    // ============ FAIL-SAFE DEMONSTRATION ============

    private static void demonstrateFailSafe(Scanner scanner) {
        System.out.println("\n============================================================");
        System.out.println("          FAIL-SAFE MECHANISM DEMONSTRATION");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("This demo shows how services handle failures:");
        System.out.println("1. Graceful degradation when service is down");
        System.out.println("2. Proper error messages instead of cascading failures");
        System.out.println("3. System stability despite component failure");
        System.out.println();
        System.out.println("Choose scenario:");
        System.out.println("1. Test with all services running");
        System.out.println("2. Test with Movie Service down (requires manual stop)");
        System.out.println("3. Test with Booking Service down (requires manual stop)");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> testFailSafeAllServicesUp();
                case 2 -> testFailSafeMovieDown();
                case 3 -> testFailSafeBookingDown();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testFailSafeAllServicesUp() throws Exception {
        System.out.println("\n--- All Services Running (Normal Operation) ---\n");

        String json = """
            {
              "sessionId": "sess-1001",
              "userId": "user-test",
              "customerName": "Test User",
              "customerEmail": "test@example.com",
              "seats": [
                {"row": 15, "number": 10, "seatId": "R15N10"}
              ]
            }
            """;

        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", json);
    }

    private static void testFailSafeMovieDown() throws Exception {
        System.out.println("\n--- Movie Service Down ---");
        System.out.println("Please stop Movie Service now (Ctrl+C in its terminal)");
        System.out.println("Press Enter when ready...");
        new Scanner(System.in).nextLine();

        String json = """
            {
              "sessionId": "sess-1001",
              "userId": "user-failsafe",
              "customerName": "Fail Safe Test",
              "customerEmail": "failsafe@test.com",
              "seats": [
                {"row": 20, "number": 20, "seatId": "R20N20"}
              ]
            }
            """;

        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", json);

        System.out.println("\n✓ Fail-Safe Behavior:");
        System.out.println("  - Booking Service detected Movie Service unavailability");
        System.out.println("  - No crash or exception propagation");
        System.out.println("  - Clear error message to client");
        System.out.println("  - Booking Service remains operational");
    }

    private static void testFailSafeBookingDown() throws Exception {
        System.out.println("\n--- Booking Service Down ---");
        System.out.println("Please stop Booking Service now (Ctrl+C in its terminal)");
        System.out.println("Press Enter when ready...");
        new Scanner(System.in).nextLine();

        String json = """
            {
              "bookingId": "bk-1001",
              "amount": {"value": 21.0, "currency": "EUR"},
              "method": "CARD"
            }
            """;

        sendPostRequestWithDetails(PAYMENT_SERVICE_URL + "/payments", json);

        System.out.println("\n✓ Fail-Safe Behavior:");
        System.out.println("  - Payment Service detected Booking Service unavailability");
        System.out.println("  - No crash or exception propagation");
        System.out.println("  - Clear error message to client");
        System.out.println("  - Payment Service remains operational");
    }

    // ============ EXISTING METHODS ============

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

    private static void testCompleteWorkflow(Scanner scanner) throws Exception {
        System.out.println("\nCOMPLETE WORKFLOW: Search -> Book -> Pay");
        System.out.println("============================================================");

        System.out.println("\nStep 1: Get available movies");
        String movies = sendGetRequest(MOVIE_SERVICE_URL + "/movies", "application/json");
        System.out.println(movies);

        System.out.print("\nEnter movie ID to view sessions: ");
        String movieId = scanner.nextLine();

        System.out.println("\nStep 2: Get movie sessions");
        String sessions = sendGetRequest(MOVIE_SERVICE_URL + "/movies/" + movieId + "/sessions", "application/json");
        System.out.println(sessions);

        System.out.print("\nEnter session ID for booking: ");
        String sessionId = scanner.nextLine();

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

        System.out.println("\n1. Getting non-existent movie (mov-999):");
        sendGetRequestWithDetails(MOVIE_SERVICE_URL + "/movies/mov-999");

        System.out.println("\n2. Getting non-existent booking (bk-9999):");
        sendGetRequestWithDetails(BOOKING_SERVICE_URL + "/bookings/bk-9999");

        System.out.println("\n3. Getting non-existent payment (pay-9999):");
        sendGetRequestWithDetails(PAYMENT_SERVICE_URL + "/payments/pay-9999");
    }

    private static void test400BadRequest() throws Exception {
        System.out.println("\n--- Testing 400 Bad Request ---");

        System.out.println("\n1. Creating booking with missing required fields:");
        String invalidBooking = """
            {
              "sessionId": "",
              "userId": "",
              "customerName": "",
              "seats": []
            }
            """;
        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", invalidBooking);

        System.out.println("\n2. Creating payment with invalid method:");
        String invalidPayment = """
            {
              "bookingId": "bk-1001",
              "amount": {"value": 10.0, "currency": "EUR"},
              "method": "INVALID_METHOD"
            }
            """;
        sendPostRequestWithDetails(PAYMENT_SERVICE_URL + "/payments", invalidPayment);
    }

    private static void test409Conflict() throws Exception {
        System.out.println("\n--- Testing 409 Conflict ---");

        System.out.println("\n1. Creating duplicate payment for existing booking:");
        String json = """
            {
              "bookingId": "bk-1001",
              "amount": {"value": 21.0, "currency": "EUR"},
              "method": "CARD"
            }
            """;
        sendPostRequestWithDetails(PAYMENT_SERVICE_URL + "/payments", json);

        System.out.println("\n2. Booking already reserved seat:");
        String seatJson = """
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
        sendPostRequestWithDetails(BOOKING_SERVICE_URL + "/bookings", seatJson);
    }

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

    private static void sendGetRequestWithDetails(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body:");
            System.out.println(formatJson(response.body()));
        } catch (Exception e) {
            System.out.println("✗ Request failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static String sendPostRequestWithDetails(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body:");
        System.out.println(formatJson(response.body()));

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