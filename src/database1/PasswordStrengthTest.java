// public class PasswordStrengthTest {

//     public static String getPasswordStrength(String password) {
//         int score = 0;

//         // Length check
//         if (password.length() >= 8) score++;
//         if (password.length() >= 12) score++;

//         // Character variety checks
//         if (password.matches(".*[a-z].*")) score++; // lowercase
//         if (password.matches(".*[A-Z].*")) score++; // uppercase
//         if (password.matches(".*[0-9].*")) score++; // numbers
//         if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // special characters

//         // Return strength with emoji
//         if (score <= 2) {
//             return "🟡 Weak";
//         } else if (score <= 4) {
//             return "🟠 Medium";
//         } else {
//             return "🟢 Strong";
//         }
//     }

//     public static void main(String[] args) {
//         // Test cases
//         String[] testPasswords = {
//             "",           // Empty
//             "123",        // Weak
//             "password",   // Weak (only lowercase, < 8 chars)
//             "Password",   // Medium (upper + lower, < 8 chars)
//             "Password1",  // Medium (upper + lower + number, < 8 chars)
//             "Password1!", // Strong (upper + lower + number + special, < 8 chars)
//             "MyPassword123", // Strong (all types, >= 8 chars)
//             "VeryStrongPassword123!", // Strong (all types, longer)
//         };

//         System.out.println("Testing Password Strength:");
//         for (String password : testPasswords) {
//             String strength = getPasswordStrength(password);
//             System.out.println("'" + password + "' -> " + strength);
//         }

//         System.out.println("\nPassword strength test completed!");
//     }
// }