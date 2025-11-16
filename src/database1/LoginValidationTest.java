// import java.util.regex.Pattern;

// public class LoginValidationTest {

//     // Mock database check - in real implementation this would query the database
//     private static boolean mockEmailExists(String email) {
//         // Simulate some existing emails for testing
//         return email.equals("test@example.com") || email.equals("user@domain.com");
//     }

//     // Mock authentication - in real implementation this would check password hash
//     private static boolean mockAuthenticate(String email, String passwordHash) {
//         // Simulate correct password for test emails
//         return (email.equals("test@example.com") && passwordHash.equals("mock_hash_for_test123")) ||
//                (email.equals("user@domain.com") && passwordHash.equals("mock_hash_for_userpass"));
//     }

//     public static boolean isValidEmail(String email) {
//         String regex = "^[\\w+.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
//         return Pattern.compile(regex).matcher(email).matches();
//     }

//     public static String mockHashPassword(String password) {
//         // Simple mock hash for testing
//         return "mock_hash_for_" + password;
//     }

//     public static void main(String[] args) {
//         System.out.println("Testing Login Validation Logic:");
//         System.out.println("=====================================");

//         // Test cases
//         String[] testEmails = {
//             "test@example.com",      // Valid existing email
//             "user@domain.com",      // Valid existing email
//             "newuser@test.com",     // Valid but non-existing email
//             "invalid-email",        // Invalid format, 2-3 chars
//             "ab",                   // Invalid format, 2 chars
//             "invalid@",             // Invalid format
//             ""                      // Empty
//         };

//         String[] testPasswords = {
//             "test123",     // Correct password for test@example.com
//             "userpass",    // Correct password for user@domain.com
//             "wrongpass",   // Wrong password
//             "short",       // Wrong password
//             ""             // Empty password
//         };

//         System.out.println("Email Validation Tests:");
//         for (String email : testEmails) {
//             if (email.isEmpty()) {
//                 System.out.println("'' -> Empty email");
//                 continue;
//             }

//             if (isValidEmail(email)) {
//                 if (mockEmailExists(email)) {
//                     System.out.println("'" + email + "' -> Email is registered ✓");
//                 } else {
//                     System.out.println("'" + email + "' -> Email is not registered, please do the sign-up process ✗");
//                 }
//             } else {
//                 if (email.length() >= 2 && email.length() <= 3) {
//                     System.out.println("'" + email + "' -> Invalid email ✗");
//                 } else {
//                     System.out.println("'" + email + "' -> Invalid email format ✗");
//                 }
//             }
//         }

//         System.out.println("\nPassword Validation Tests:");
//         for (String email : new String[]{"test@example.com", "user@domain.com"}) {
//             for (String password : testPasswords) {
//                 if (password.isEmpty()) {
//                     System.out.println("Email: '" + email + "', Password: '' -> Empty password");
//                     continue;
//                 }

//                 String passwordHash = mockHashPassword(password);
//                 if (mockAuthenticate(email, passwordHash)) {
//                     System.out.println("Email: '" + email + "', Password: '" + password + "' -> Password is valid ✓");
//                 } else {
//                     System.out.println("Email: '" + email + "', Password: '" + password + "' -> Password is invalid ✗");
//                 }
//             }
//         }

//         System.out.println("\nLogin validation test completed!");
//     }
// }