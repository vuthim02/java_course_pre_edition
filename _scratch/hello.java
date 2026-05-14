// public class hello {
//     public static void main(String[] args) {
//         // overflow -The silent killer
//         int a = 2147483647; // maximum value for int
//         int b = 1; // Adding 1 to the maximum value will cause an overflow
//         // int c = a + b; // This will cause an overflow
//         System.out.println("Result of overflow: " + (a + b)); // This will print a negative number due to overflow   ,why negative? Because in Java, when an integer overflows, it wraps around to the minimum value. In this case, adding 1 to the maximum value of 2147483647 causes it to wrap around to -2147483648, which is the minimum value for an int. This is why the result of the overflow is a negative number.
     
//     }
// }

// public class hello {
//     public static void main(String[] args) {
//         // underflow - The silent killer
//         int a = -2147483648; // minimum value for int
//         int b = -1; // Subtracting 1 from the minimum value will cause an underflow
//         // int c = a + b; // This will cause an underflow
//         System.out.println("Result of underflow: " + (a + b)); // This will print a positive number due to underflow   ,why positive? Because in Java, when an integer underflows, it wraps around to the maximum value. In this case, subtracting 1 from the minimum value of -2147483648 causes it to wrap around to 2147483647, which is the maximum value for an int. This is why the result of the underflow is a positive number.
     
//     }
// }

// public class hello {
//     public static void main(String[] args) {
//         // overflow and underflow in floating-point numbers
//         double a = 1.7E+308; // maximum value for double
//         double b = 1.0; // Adding 1.0 to the maximum value will cause an overflow
//         System.out.println("Result of overflow in double: " + (a + b)); // This will print "Infinity" due to overflow

//         double c = -1.7E+308; // minimum value for double
//         double d = -1.0; // Subtracting 1.0 from the minimum value will cause an underflow
//         System.out.println("Result of underflow in double: " + (c + d)); // This will print "-Infinity" due to underflow
//     }
// }

// public class hello {
//     public static void main(String[] args) {
//         int max = Integer.MAX_VALUE;
//         int overflow = max + 1;
        
//         try{
//             int safe = Math.addExact(max, 1);
//         }catch(ArithmeticException e){
//             System.out.println("Overflow!");
//         }
//     }
// }

// public class hello {

//     public class Person{
//         int age;
//         String name;
//         boolean active;
//         double salary;
//     }
//     public static void main(String[] aStrings){
//         String name;
//         name = "Vuthim";
//         System.out.println(name);
//         // name = "Vuthim" wiill cause an error because it is not a valid statement. In Java, you cannot assign a value to a variable without declaring it first. The correct way to assign a value to the variable "name" is to declare it first and then assign the value, like this:
//         // String name; // Declare the variable
//     }
// }

// import java.util.ArrayList;
// import java.util.List;

// public class hello {
//     public static void main(String[] args) {
//        Integer boxed = 54;
//        int unboxed = boxed;

//        List<Integer> numbers = new ArrayList<>();
//        numbers.add(55);
//        int value = numbers.get(0);

//        System.out.println("Boxed value: " + boxed);
//        System.out.println("Unboxed value: " + unboxed);
//        System.out.println("Value from list: " + value);
//     }
// }

// public class hello {
//     public static void main(String[] args) {
//         // Short-Circuit Evaluation
//         String name = null;
//         boolean valid = name != null && name.length() >0;
//         System.out.println("Is the name valid? " + valid);
//         boolean quick =true || false;
//         System.out.println("Result of quick evaluation: " + quick);
//     }
// }

// Write a program that takes two integers and prints: sum, difference, product, quotient, remainder.
// Demonstrate integer division truncation vs floating-point division.
// Show the difference between x++ and ++x in a program.
// Use short-circuit evaluation to safely check if a String is null AND has length > 0.
// Write a temperature converter: Fahrenheit → Celsius. Use the formula: C = (F - 32) * 5/9. Why does 5/9 give 0? Fix it!

// public class hello {
//     public static void main(String[] args) {
//         int num1 = 10;
//         int num2 = 3;

//         // Sum
//         int sum = num1 + num2;
//         System.out.println("Sum: " + sum);

//         // Difference
//         int difference = num1 - num2;
//         System.out.println("Difference: " + difference);

//         // Product
//         int product = num1 * num2;
//         System.out.println("Product: " + product);

//         // Quotient (integer division)
//         int quotientInt = num1 / num2;
//         System.out.println("Quotient (integer division): " + quotientInt);

//         // Quotient (floating-point division)
//         double quotientFloat = (double) num1 / num2;
//         System.out.println("Quotient (floating-point division): " + quotientFloat);

//         // Remainder
//         int remainder = num1 % num2;
//         System.out.println("Remainder: " + remainder);

//         // Demonstrating x++ vs ++x
//         int x = 5;
//         System.out.println("x++: " + x++); // prints 5, then x becomes 6
//         System.out.println("After x++: " + x); // prints 6

//         x = 5; // reset x
//         System.out.println("++x: " + ++x); // x becomes 6, then prints 6
//     }
// }
// 
// public class hello {

//     public static void main(String[] args) {
//         for (int i= 0; i <= 10; i++){
//             for(int j = 0; j <= 10;j++){
//                 System.out.printf("%4d", i*j); // why printf 
//             }
//             System.out.println();
//         }
//     }
// }

// public class hello{
//     public static void main(String[] args) {
//         outer:
//         for (int i = 0; i < 3; i++){
//             for (int j = 0; j <3 ;j++){
//                 if(  i == 1 && j == 1){
//                     break outer;
//                 }
//                 System.out.println(i +" , " + j );
//             }
//         }
//     }
// }

// public class hello{
//     public static void main(String [] args){
//         int i = 3;
//         while(i <= 10){
//             System.out.println(i);
//         }
//     }
// }

public class hello{
    public static void main(String[] args){
        int[] numbers = {12,34,45,76,67};
        String str = "Hello Java!";
        System.out.println(str.length());
        System.out.println(numbers.length);
    }
}