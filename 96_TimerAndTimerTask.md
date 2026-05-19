# Lesson 96: Timer and TimerTask

## Key Concepts
- `java.util.Timer` schedules tasks for future execution in a background thread
- `TimerTask` is an abstract class with a `run()` method — override it to define the task
- `scheduleAtFixedRate(TimerTask, delay, period)` runs the task repeatedly at fixed intervals
- `timer.cancel()` stops the timer
- `javax.swing.Timer` is a Swing-friendly alternative that fires events on the EDT (Event Dispatch Thread)

## Code Example

```java
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Timer Demo ===");

        Timer timer = new Timer();

        TimerTask countdownTask = new TimerTask() {
            int count = 10;

            @Override
            public void run() {
                System.out.println("Countdown: " + count);
                count--;
                if (count < 0) {
                    System.out.println("Time's up!");
                    timer.cancel();
                }
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 1000);

        javax.swing.Timer swingTimer = new javax.swing.Timer(2000, e -> {
            System.out.println("Swing timer tick (every 2 seconds)");
        });

        new Timer("delay-start").schedule(new TimerTask() {
            @Override
            public void run() {
                // Prevent main from exiting immediately
            }
        }, 12000);
    }
}
```

## Explanation
1. `Timer` creates a background thread that executes scheduled tasks.
2. `TimerTask` is an abstract class — here we create an anonymous subclass and override `run()`.
3. `scheduleAtFixedRate(task, delay, period)` runs the task after a 0ms initial delay and then every 1000ms.
4. The `countdownTask` decrements a counter each second. When it reaches -1, it prints "Time's up!" and cancels the timer.
5. `javax.swing.Timer` (Swing Timer) accepts a lambda for its `ActionListener`. It runs on the EDT, making it safe for updating Swing components.
6. A third timer keeps the program alive for about 12 seconds so the countdown can complete.

## Expected Output

```
=== Timer Demo ===
Countdown: 10
Countdown: 9
Countdown: 8
Countdown: 7
Countdown: 6
Countdown: 5
Countdown: 4
Countdown: 3
Countdown: 2
Countdown: 1
Countdown: 0
Time's up!
```
