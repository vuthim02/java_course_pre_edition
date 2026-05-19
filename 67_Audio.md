# Lesson 67: Audio Playback

## Key Concepts
- `javax.sound.sampled` package provides classes for playing, stopping, and looping audio.
- `AudioSystem.getAudioInputStream(File)`: loads an audio file into an `AudioInputStream`.
- `AudioSystem.getClip()`: obtains a `Clip` for playback.
- `clip.open(audioStream)`: prepares the clip with audio data.
- `clip.start()`: begins or resumes playback.
- `clip.stop()`: pauses playback (position is retained).
- `clip.setMicrosecondPosition(0)`: resets playback to the beginning.
- `clip.loop(Clip.LOOP_CONTINUOUSLY)`: loops the audio indefinitely.
- `clip.close()`: releases audio resources.
- Supported format: WAV files.

## Code Example

```java
import java.io.File;
import java.util.Scanner;
import javax.sound.sampled.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        File file = new File("sound.wav");
        if (!file.exists()) {
            System.out.println("Audio file not found. Creating a note about playing audio.");
            System.out.println("To test audio playback, place a 'sound.wav' file in this directory.");
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            String response = "";

            while (!response.equals("Q")) {
                System.out.println("=== AUDIO PLAYER ===");
                System.out.println("P = Play");
                System.out.println("S = Stop");
                System.out.println("R = Reset");
                System.out.println("L = Loop");
                System.out.println("Q = Quit");
                System.out.print("Enter choice: ");
                response = scanner.nextLine().toUpperCase();

                switch (response) {
                    case "P" -> clip.start();
                    case "S" -> clip.stop();
                    case "R" -> clip.setMicrosecondPosition(0);
                    case "L" -> clip.loop(Clip.LOOP_CONTINUOUSLY);
                    case "Q" -> clip.close();
                    default -> System.out.println("Invalid choice.");
                }
            }

            System.out.println("Audio player closed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
    }
}
```

## Explanation
1. A `File` object checks if `"sound.wav"` exists. If not, the program prints a message and exits.
2. `AudioSystem.getAudioInputStream()` loads the WAV file into an `AudioInputStream`.
3. A `Clip` is obtained and opened with the audio stream.
4. A menu loop accepts commands:
   - **P** — starts playback from the current position.
   - **S** — stops playback (cursor stays at the current position).
   - **R** — resets the cursor to the beginning (0 microseconds).
   - **L** — loops continuously.
   - **Q** — closes the clip and exits.
5. The switch expression (Java 14+) handles each command cleanly.

## Expected Output

```
=== AUDIO PLAYER ===
P = Play
S = Stop
R = Reset
L = Loop
Q = Quit
Enter choice: p
=== AUDIO PLAYER ===
P = Play
S = Stop
R = Reset
L = Loop
Q = Quit
Enter choice: q
Audio player closed.
```
