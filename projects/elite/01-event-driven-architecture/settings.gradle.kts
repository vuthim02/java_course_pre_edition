rootProject.name = "event-driven-parent"

include(
    "event-sourcing-module",
    "kafka-module",
    "outbox-module",
    "saga-module"
)
