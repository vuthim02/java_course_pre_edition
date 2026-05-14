# File Manager

A console-based file manager using Java NIO. Demonstrates file system operations, recursion, and streams.

## Features

- **List files** — shows name, size, permissions, and last-modified date for each entry
- **Navigate directories** — change the current working directory with `cd`
- **Copy / Move / Delete** — supports both files and directories (recursive delete)
- **Search** — by name (case-insensitive substring) or glob pattern (`*.txt`, `*.java`)
- **Preview** — shows the first 10 lines of text files with a line-count summary
- **Recursive size** — calculates total size of a directory (sum of all regular files)
- **Print working directory** — shows the current absolute path

## How to run

```bash
javac FileManager.java
java FileManager
```

## Commands

| Command           | Description                               |
|-------------------|-------------------------------------------|
| `ls [path]`       | List directory contents with details      |
| `cd <path>`       | Change current directory                  |
| `pwd`             | Print working directory                   |
| `cp <src> <dst>`  | Copy a file or directory                  |
| `mv <src> <dst>`  | Move a file or directory                  |
| `rm <path>`       | Delete a file or directory (recursive)    |
| `search <query>`  | Search files by name or glob              |
| `preview <file>`  | Show first 10 lines of a text file        |
| `size [path]`     | Calculate total size of a file or subtree |
| `exit`            | Quit                                      |
