# Lesson 102: Date & Time API

## Key Concepts
- `java.time` package introduced in Java 8 replaces the old `Date`/`Calendar` classes
- `LocalDate` — date without time (e.g., 2026-05-19)
- `LocalTime` — time without date (e.g., 14:30:00)
- `LocalDateTime` — date and time without time zone
- `ZonedDateTime` — date and time with a time zone
- `DateTimeFormatter` — format and parse date/time objects
- `ChronoUnit` — measure time between dates (DAYS, MONTHS, YEARS, etc.)
- `plusDays()`, `minusMonths()`, etc. — immutable date manipulation

## Code Example

```java
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Date & Time API Demo ===\n");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime currentDateTime = LocalDateTime.now();

        System.out.println("Today: " + today);
        System.out.println("Current time: " + now);
        System.out.println("Current date-time: " + currentDateTime);

        LocalDate birthday = LocalDate.of(2000, 6, 15);
        System.out.println("\nBirthday: " + birthday);
        System.out.println("Day of week: " + birthday.getDayOfWeek());
        System.out.println("Day of year: " + birthday.getDayOfYear());
        System.out.println("Month: " + birthday.getMonth());

        LocalTime specificTime = LocalTime.of(14, 30, 0);
        System.out.println("\nSpecific time: " + specificTime);

        System.out.println("\n--- Date formatting ---");
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("hh:mm:ss a");

        System.out.println("Formatted: " + today.format(formatter1));
        System.out.println("Full date: " + today.format(formatter2));
        System.out.println("Time: " + currentDateTime.format(formatter3));

        System.out.println("\n--- Date manipulation ---");
        LocalDate futureDate = today.plusDays(10);
        System.out.println("10 days from now: " + futureDate);

        LocalDate pastDate = today.minusMonths(2);
        System.out.println("2 months ago: " + pastDate);

        LocalDate nextWeek = today.plusWeeks(1);
        System.out.println("Next week: " + nextWeek);

        System.out.println("\n--- Difference between dates ---");
        long daysBetween = ChronoUnit.DAYS.between(birthday, today);
        long monthsBetween = ChronoUnit.MONTHS.between(birthday, today);
        long yearsBetween = ChronoUnit.YEARS.between(birthday, today);

        System.out.println("Days since birthday: " + daysBetween);
        System.out.println("Months since birthday: " + monthsBetween);
        System.out.println("Years since birthday: " + yearsBetween);

        System.out.println("\n--- Zoned date/time ---");
        ZonedDateTime tokyoTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime londonTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));

        DateTimeFormatter formatter4 = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm");
        System.out.println("Tokyo: " + tokyoTime.format(formatter4));
        System.out.println("London: " + londonTime.format(formatter4));
        System.out.println("New York: " + nyTime.format(formatter4));
    }
}
```

## Explanation
1. `LocalDate.now()` gets the current date from the system clock. `LocalDate.of(year, month, day)` creates a specific date.
2. `LocalTime.now()` and `LocalTime.of(hour, minute, second)` work analogously for times.
3. `DateTimeFormatter.ofPattern("yyyy-MM-dd")` — uses pattern letters: `yyyy` (year), `MM` (month), `dd` (day), `EEEE` (day name), `MMMM` (month name), `hh` (hour 1-12), `mm` (minute), `ss` (second), `a` (AM/PM).
4. `today.plusDays(10)`, `today.minusMonths(2)` — these methods return new `LocalDate` instances (immutable).
5. `ChronoUnit.DAYS.between(start, end)` calculates the number of days between two dates.
6. `ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))` — gets the current date and time in a specific time zone.
7. All date-time classes in `java.time` are immutable and thread-safe.

## Expected Output

```
=== Date & Time API Demo ===

Today: 2026-05-19
Current time: (current local time)
Current date-time: (current date and time)

Birthday: 2000-06-15
Day of week: THURSDAY
Day of year: 167
Month: JUNE

Specific time: 14:30:00

--- Date formatting ---
Formatted: 2026-05-19
Full date: Tuesday, May 19, 2026
Time: (current time in hh:mm:ss AM/PM)

--- Date manipulation ---
10 days from now: 2026-05-29
2 months ago: 2026-03-19
Next week: 2026-05-26

--- Difference between dates ---
Days since birthday: (number of days)
Months since birthday: (number of months)
Years since birthday: (number of years)

--- Zoned date/time ---
Tokyo: (current Tokyo date and time)
London: (current London date and time)
New York: (current New York date and time)
```

*(Actual values depend on when the program runs.)*
