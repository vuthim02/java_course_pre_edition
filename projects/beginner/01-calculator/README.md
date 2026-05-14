# Calculator

A console-based calculator application demonstrating basic Java concepts: variables, methods, control flow, switch expressions, collections, and exception handling.

## Features

- **Basic operations**: addition (`+`), subtraction (`-`), multiplication (`*`), division (`/`), modulo (`%`)
- **Scientific functions**: `sin`, `cos`, `tan` (degrees), `log` (natural), `sqrt`, `pow`
- **Memory functions**: `M+` (add to memory), `M-` (subtract from memory), `MR` (recall), `MC` (clear)
- **Operation history**: all calculations are stored and can be viewed at any time
- **Input validation**: gracefully handles invalid numbers, division by zero, and unknown operators
- **Clean exit**: type `exit` to quit

## How to run

```bash
javac Calculator.java
java Calculator
```

## Commands

| Command      | Description                        |
|--------------|------------------------------------|
| `basic`      | Enter basic arithmetic mode        |
| `scientific` | Enter scientific function mode     |
| `memory`     | Enter memory mode (M+, M-, MR, MC) |
| `history`    | View all past calculations         |
| `clear`      | Clear operation history            |
| `exit`       | Exit the calculator                |
