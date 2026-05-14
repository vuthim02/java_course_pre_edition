# DevOps & Cloud — Lesson 1: Linux & Bash Mastery

## Essential Linux Commands

```bash
# File operations
ls -la                    # List all files with details
pwd                       # Print working directory
cd /path/to/dir           # Change directory
mkdir -p a/b/c            # Create directory (with parents)
cp -r src dest            # Copy recursively
mv src dest               # Move/rename
rm -rf dir                # Remove directory forcefully
touch file.txt            # Create empty file
cat file.txt              # Print file contents
less file.txt             # View file page by page
head -n 20 file.txt       # First 20 lines
tail -f file.txt          # Follow file (like logs)

# Permissions
chmod +x script.sh        # Make executable
chmod 755 file            # rwxr-xr-x
chown user:group file     # Change owner

# Process management
ps aux                    # All processes
top                       # Live process view
htop                      # Better top (install first)
kill -9 PID               # Force kill
kill -15 PID              # Graceful kill
pkill java                # Kill all java processes

# Network
curl -v http://localhost:8080   # HTTP request
wget https://example.com/file   # Download
ping google.com                 # Check connectivity
netstat -tulpn                 # Listening ports
ss -tulpn                      # Modern netstat
nc -vz localhost 8080          # Check port
telnet localhost 8080          # Raw TCP connection

# Disk
df -h                     # Disk usage
du -sh *                  # Directory sizes
du -sh .                  # Current dir total

# Archive
tar -czf archive.tar.gz dir/   # Compress
tar -xzf archive.tar.gz        # Extract
zip -r archive.zip dir/        # Zip
unzip archive.zip              # Unzip

# Search
grep -r "pattern" /path        # Search recursively
grep -rn "TODO" src/           # With line numbers
find /path -name "*.java"      # Find files
find . -type f -name "*.log" -delete  # Find and delete

# Text processing
sed 's/old/new/g' file.txt     # Replace text
awk '{print $1, $3}' file.txt  # Print columns
wc -l file.txt                 # Count lines
sort file.txt                  # Sort lines
uniq                          # Remove duplicates
```

## Essential Bash Scripting

```bash
#!/bin/bash
# deploy.sh — Deploy Java application

set -euo pipefail  # Fail on error, undefined vars, pipe failures

APP_NAME="myapp"
APP_VERSION="${1:-latest}"  # First argument or "latest"
JAR_FILE="target/$APP_NAME-$APP_VERSION.jar"
REMOTE_HOST="prod-server.com"
REMOTE_PATH="/opt/$APP_NAME"

echo "=== Deploying $APP_NAME v$APP_VERSION ==="

# Build
echo "Building..."
./mvnw clean package -DskipTests -q

# Check build
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: Build failed — $JAR_FILE not found"
    exit 1
fi

# Copy to server
echo "Copying to $REMOTE_HOST..."
scp "$JAR_FILE" "$REMOTE_HOST:$REMOTE_PATH/"

# Restart
echo "Restarting service..."
ssh "$REMOTE_HOST" "sudo systemctl restart $APP_NAME"

# Health check
echo "Checking health..."
sleep 5
if curl -sf http://$REMOTE_HOST:8080/actuator/health > /dev/null; then
    echo "=== Deploy successful! ==="
else
    echo "ERROR: Health check failed!"
    exit 1
fi
```

## Java-Specific Linux Tools

```bash
# Find Java processes
ps aux | grep java
jps -l                    # Java-specific process list

# Thread dump
jstack <pid>              # Thread dump
jstack <pid> > threaddump.txt

# Heap dump
jmap -dump:live,format=b,file=heap.hprof <pid>

# GC logs
jstat -gcutil <pid> 1s    # GC stats every second

# Kill gracefully
kill -3 <pid>             # SIGQUIT → thread dump
kill -15 <pid>            # SIGTERM → graceful shutdown
```

## File Permissions Deep Dive

```bash
# Standard Unix permissions: rwxrwxrwx (owner:group:other)
# Numeric: r=4, w=2, x=1
chmod 755 script.sh        # rwxr-xr-x — owner rwx, group+other rx
chmod 644 file.txt         # rw-r--r— — owner rw, group+other r
chmod 600 private.pem      # rw------- — only owner
chmod 700 ~/.ssh           # rwx------ — only owner can enter

# Symbolic mode
chmod u+x script.sh        # Add execute for user
chmod g-w file.txt         # Remove write for group
chmod o+r file.txt         # Add read for others
chmod a+x script.sh        # Add execute for all

# Ownership
chown admin:admin app.jar  # Set user and group
chown -R appuser:appgroup /opt/myapp  # Recursive
chgrp developers shared-file.txt      # Change group only

# Special permissions
chmod u+s binary           # setuid — runs as owner (e.g., /usr/bin/passwd)
chmod g+s directory/       # setgid — files inherit group
chmod +t /tmp              # sticky bit — only owner can delete files
# Examples:
# - setuid: 4755 (rwsr-xr-x)
# - setgid: 2755 (rwxr-sr-x)
# - sticky: 1755 (rwxr-xr-t)

# ACLs (Access Control Lists) — fine-grained permissions
setfacl -m u:john:rx file.txt       # Give john read+execute
setfacl -m g:developers:rwx /project # Give group full access
setfacl -x u:john file.txt           # Remove john's ACL
setfacl -b file.txt                  # Remove all ACLs
getfacl file.txt                     # View ACLs

# Default ACLs (inherit to new files)
setfacl -d -m g:developers:rwx /shared
```

## Process Management

```bash
# Process listing
ps aux                    # All processes (BSD syntax)
ps -ef                    # All processes (standard syntax)
ps -eo pid,ppid,cmd,%mem,%cpu --sort=-%mem  # Custom format, sorted

# Process tree
pstree -p                 # Tree with PIDs
pstree -p 1234            # Tree for specific PID

# Interactive monitoring
top                       # Default process view
top -o %MEM               # Sort by memory
top -u appuser            # Only one user's processes
htop                      # Better top (color, mouse, scroll)
htop -p 1234,5678         # Monitor specific PIDs

# Process signals
kill -l                   # List all signals
kill -15 1234             # SIGTERM — graceful shutdown
kill -9 1234              # SIGKILL — force kill (last resort)
kill -3 1234              # SIGQUIT — thread dump (Java)
kill -1 1234              # SIGHUP — reload config
kill -0 1234              # Check if process exists (no signal sent)
pkill -f "java.*myapp"    # Kill by pattern
pgrep -f "spring"         # Find PIDs by pattern

# Background and detached processes
nohup java -jar app.jar > app.log 2>&1 &  # Survive logout
disown %1                 # Remove job from shell's job table
bg %1                     # Resume job in background
fg %1                     # Bring job to foreground
jobs -l                   # List background jobs

# Nice/priority
nice -n 10 ./slow-task    # Run with low priority (higher nice = lower priority)
renice -n 5 -p 1234       # Change priority of existing process

# systemd service files
cat /etc/systemd/system/myapp.service
```

```ini
# /etc/systemd/system/myapp.service
[Unit]
Description=My Java Spring Boot Application
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=appuser
Group=appgroup
WorkingDirectory=/opt/myapp
Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk
Environment=SPRING_PROFILES_ACTIVE=prod
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/myapp/app.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
LimitNOFILE=65536
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
```

```bash
# systemd commands
sudo systemctl daemon-reload            # Reload after editing service file
sudo systemctl start myapp               # Start service
sudo systemctl stop myapp                # Stop service
sudo systemctl restart myapp             # Restart service
sudo systemctl enable myapp              # Start on boot
sudo systemctl disable myapp             # Disable auto-start
sudo systemctl status myapp              # Show status + recent logs
sudo systemctl is-active myapp           # Check if running
journalctl -u myapp -f                   # Follow service logs
journalctl -u myapp --since "1 hour ago" # Recent logs
journalctl -u myapp -p err              # Only error logs
```

## Shell Scripting

```bash
#!/bin/bash
# monitor.sh — Real service health monitor

set -euo pipefail

# Configuration
SERVICE_NAME="${1:-myapp}"
CHECK_INTERVAL=${2:-5}
ALERT_EMAIL="ops@company.com"
LOG_FILE="/var/log/monitor.log"

# Logging function
log() {
    local level="$1"
    local message="$2"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$level] $message" >> "$LOG_FILE"
}

# Check service health
check_health() {
    local url="http://localhost:8080/actuator/health"
    if curl -sf "$url" > /dev/null 2>&1; then
        log "INFO" "$SERVICE_NAME is healthy"
        return 0
    else
        log "ERROR" "$SERVICE_NAME is DOWN!"
        return 1
    fi
}

# Send alert
send_alert() {
    local subject="ALERT: $SERVICE_NAME is down!"
    local body="Service $SERVICE_NAME failed at $(date)"
    echo "$body" | mail -s "$subject" "$ALERT_EMAIL"
}

# Main loop
while true; do
    if ! check_health; then
        send_alert
        log "WARN" "Alert sent to $ALERT_EMAIL"
        # Try restart
        log "INFO" "Attempting restart..."
        if sudo systemctl restart "$SERVICE_NAME"; then
            log "INFO" "Restart command sent to $SERVICE_NAME"
        else
            log "ERROR" "Failed to send restart command!"
        fi
    fi
    sleep "$CHECK_INTERVAL"
done
```

### Variables and Parameter Expansion

```bash
# Variable basics
NAME="World"
echo "Hello, $NAME"
echo "Hello, ${NAME}"     # Same, but allows concatenation: ${NAME}_suffix

# Default values
USER="${USER:-default}"       # Use "default" if $USER is unset or empty
USER="${USER:=default}"       # Same, but also assigns default to variable
USER="${USER:-$OTHER}"        # Use $OTHER as fallback
USER="${USER:?ERROR: unset}"  # Error with message if unset
USER="${USER:+substituted}"   # Use "substituted" if $USER is set

# String manipulation
path="/home/user/docs/file.txt"
echo "${path#*/}"             # Remove shortest prefix: home/user/docs/file.txt
echo "${path##*/}"            # Remove longest prefix: file.txt
echo "${path%.txt}"           # Remove shortest suffix: /home/user/docs/file
echo "${path%/*}"             # Directory: /home/user/docs
echo "${path/file/document}"  # Replace first match: /home/user/docs/document.txt
echo "${path//file/doc}"      # Replace all: /home/user/docs/doc.txt
```

### Loops and Conditionals

```bash
# For loop
for file in *.java; do
    echo "Compiling $file..."
    javac "$file"
done

# C-style for loop
for ((i=1; i<=10; i++)); do
    echo "Iteration $i"
done

# While loop — read file line by line
while IFS= read -r line; do
    echo "Line: $line"
done < "input.txt"

# If/elif/else
if [ -f "$JAR_FILE" ]; then
    echo "JAR exists"
elif [ -d "$TARGET_DIR" ]; then
    echo "Target directory exists"
else
    echo "Neither exists"
fi

# Case statement
case "$ENV" in
    dev|staging)
        echo "Non-production environment"
        ;;
    prod)
        echo "Production — careful!"
        ;;
    *)
        echo "Unknown environment: $ENV"
        exit 1
        ;;
esac

# Boolean conditions
[ -f "$file" ]        # File exists and is regular
[ -d "$dir" ]         # Directory exists
[ -x "$binary" ]      # File is executable
[ -z "$string" ]      # String is empty
[ -n "$string" ]      # String is not empty
[ "$a" = "$b" ]       # String equals (POSIX)
[[ "$a" == "$b" ]]    # String equals (bash, pattern matching)
[[ "$a" =~ ^foo.* ]]  # Regex match
[[ "$a" -gt "$b" ]]   # Numeric comparison
```

### Functions and Error Handling

```bash
# Function definition
deploy() {
    local app_name="$1"      # Local scope
    local version="${2:-latest}"
    echo "Deploying $app_name v$version"
}

deploy "myapp" "1.2.0"       # Call function

# Error handling patterns
set -e                        # Exit on any error
set -u                        # Error on undefined variables
set -o pipefail               # Pipeline fails if any command fails
set -x                        # Print commands before executing (debug)

# Trap errors
cleanup() {
    echo "Cleaning up..."
    rm -f /tmp/tempfile
    exit 1
}
trap cleanup ERR               # Run cleanup on error
trap 'echo "Interrupted!"; exit' SIGINT SIGTERM

# Try/catch pattern
{
    echo "Starting risky operation..."
    mkdir /protected/dir 2>&1
} || {
    echo "Failed but continuing..."
}
```

## Network Tools

```bash
# curl — full HTTP debugging
curl -v http://localhost:8080/api/users          # Verbose (request + response headers)
curl -s http://localhost:8080/api/users          # Silent (no progress output)
curl -i http://localhost:8080/api/users          # Include response headers in output
curl -o output.json http://example.com/file.json # Save to file
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@example.com"}' # POST with JSON
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN"              # PUT with auth header
curl -k https://self-signed.local                # Skip SSL verification
curl -w "Status: %{http_code}, Time: %{time_total}s\n" -o /dev/null -s \
  http://localhost:8080/health                    # Extract status + timing
curl --connect-timeout 5 --max-time 30 http://slow.example.com # Timeouts

# wget — download with resume
wget https://example.com/file.zip                # Download
wget -c https://example.com/large-file.zip       # Resume partial download
wget -r -l 2 -np https://example.com/docs/       # Recursive mirror (depth 2, no parent)
wget --mirror --page-requisites --convert-links \
  https://docs.example.com                        # Full site mirror

# ss (modern netstat replacement)
ss -tulpn                      # All listening TCP/UDP ports with processes
ss -tn                         # TCP connections, numeric
ss -tua                        # All TCP and UDP sockets
ss -t state established        # Only established TCP connections
ss -t state time-wait          # Connections in TIME_WAIT
ss -s                          # Socket summary statistics
ss -4                          # IPv4 only
ss -O src :80                  # Filter by source port 80
ss -O dport = :8080            # Filter by destination port 8080

# netstat (older but still common)
netstat -tulpn                 # Listening ports
netstat -an | grep :8080       # Check port 8080
netstat -s                     # Network statistics by protocol
netstat -i                     # Interface statistics

# tcpdump — packet capture
sudo tcpdump -i eth0           # Capture all traffic on eth0
sudo tcpdump -i eth0 port 8080 # Capture port 8080 traffic
sudo tcpdump -i eth0 host 10.0.1.5  # Traffic to/from specific IP
sudo tcpdump -i eth0 -w capture.pcap  # Write to file for Wireshark
sudo tcpdump -r capture.pcap   # Read capture file
sudo tcpdump -i eth0 -nn -X \ 
  'tcp[tcpflags] & tcp-syn != 0'  # Show SYN packets with hex dump
sudo tcpdump -i eth0 -c 100   # Capture only 100 packets and stop

# DNS resolution
nslookup api.myapp.com         # DNS lookup
dig api.myapp.com              # Detailed DNS query
dig api.myapp.com +short       # Short answer (just IP)
host api.myapp.com             # Simple lookup

# Connectivity
nc -zv google.com 80           # Check if port 80 is open
nc -zv localhost 8080          # Check local service
nc -z -w 3 10.0.1.10 1-1000   # Scan first 1000 ports (3s timeout)
mtr google.com                 # Traceroute + ping (continuous)
iperf3 -c server.example.com   # Bandwidth test
```

## grep/sed/awk — Log Parsing Examples

```bash
# grep — search patterns

# Find all ERROR lines in logs
grep "ERROR" app.log

# Count occurrences
grep -c "ERROR" app.log                      # Count ERROR lines
grep -c "ERROR" *.log                        # Count per file

# Show context
grep -B 5 "NullPointerException" app.log      # 5 lines before match
grep -A 10 "Stacktrace:" app.log              # 10 lines after match  
grep -C 3 "ERROR" app.log                    # 3 lines around match

# Recursive search in directory
grep -rn "TODO\|FIXME\|HACK" src/            # Find all TODOs in source
grep -rL "class .*Controller" src/           # Files WITHOUT pattern

# Extended regex
grep -E "ERROR|FATAL" app.log                # Multiple patterns
grep -E "^2026-05-[0-9]{2}" app.log          # Lines matching date pattern
grep -P "\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}" app.log  # Perl regex (IPs)

# Invert match
grep -v "INFO\|DEBUG" app.log                # Show only WARN+ level lines

# sed — stream editing

# Replace in-place (with backup)
sed -i.bak 's/localhost:8080/prod-server:443/g' config.properties

# Print specific lines
sed -n '100,200p' large-file.log              # Print lines 100-200

# Delete lines matching pattern
sed '/^#/d' config.properties                # Remove comments
sed '/^$/d' file.txt                         # Remove empty lines

# Multiple operations
sed -e 's/ERROR/CRITICAL/g' -e '/DEBUG/d' app.log

# awk — column-based processing

# Print specific columns
awk '{print $1, $4, $7}' access.log          # Date, time, URL path

# Filter and format
awk '$9 >= 500 {print $1, $7, $9}' access.log  # Failed requests (5xx)

# Summarize with associative arrays
awk '{status[$9]++} END {for (s in status) print s, status[s]}' access.log

# Log parsing examples

# Top 10 IPs by request count
awk '{ips[$1]++} END {for (ip in ips) print ips[ip], ip | "sort -rn | head -10"}' access.log

# Average response time per endpoint
awk '{url=$7; time=$NF; total[url]+=time; count[url]++} 
     END {for (url in total) print url, total[url]/count[url] | "sort -k2 -rn | head -10"}' \
     access.log

# Parse structured JSON log (with jq)
cat app.log | grep "ERROR" | jq '{timestamp: ."@timestamp", message: .message, traceId: .trace_id}'

# Find slowest API endpoints
# If log has format: [TIMESTAMP] [LEVEL] [THREAD] LOGGER - METHOD URL STATUS TIME
grep -E "\[INFO\]" app.log | 
  awk -F'[][]' '{print $4}' |  # Extract URL/method area
  sort | uniq -c | sort -rn | head -10

# Monitor errors in real time
tail -f app.log | grep --line-buffered "ERROR" | while read line; do
    echo "$(date): ERROR detected — $line" >> error_alerts.log
done
```

---

1. Navigate the file system using only terminal commands.
2. Write a bash script that backs up a directory with timestamp.
3. Use `grep` to find all TODO comments in a Java project.
4. Write a one-liner that counts Java files by directory.
5. Create a startup script for a Spring Boot JAR that sets JVM options and handles signals.
