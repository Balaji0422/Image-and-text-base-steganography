// import java.util.regex.Pattern;

// public class NameValidationTest {

//     public static boolean isValidName(String name) {
//         // Check if name contains only alphabetic characters and spaces
//         // Allow names with spaces (like "Mary Jane") but no numbers or special characters
//         String regex = "^[a-zA-Z]+(\\s[a-zA-Z]+)*$";
//         return Pattern.compile(regex).matcher(name).matches() && name.length() >= 2;
//     }

//     public static void main(String[] args) {
//         // Test cases
//         String[] validNames = {"John", "Mary Jane", "Alice", "Bob Smith", "A", "AB"};
//         String[] invalidNames = {"John123", "Mary-Jane", "Alice@", "Bob1", "A1", "123", "", "John Doe 123", "Alice#Smith"};

//         System.out.println("Testing Valid Names:");
//         for (String name : validNames) {
//             boolean isValid = isValidName(name);
//             System.out.println("'" + name + "' -> " + (isValid ? "VALID" : "INVALID"));
//         }

//         System.out.println("\nTesting Invalid Names:");
//         for (String name : invalidNames) {
//             boolean isValid = isValidName(name);
//             System.out.println("'" + name + "' -> " + (isValid ? "VALID" : "INVALID"));
//         }

//         System.out.println("\nName validation test completed!");
//     }
// }