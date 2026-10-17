package com.planday.app.data.local.model

enum class TaskStatus {
    TODO,
    DONE,
    SKIPPED,
    NEEDS_NEW_DAY
}

enum class TaskPriority(val level: Int) {
    P1(1),
    P2(2),
    P3(3)
}

enum class ReminderType {
    HEADS_UP_NOTIFICATION,
    FULL_SCREEN_ALARM
}

enum class ReminderStatus {
    SCHEDULED,
    FIRED,
    DISMISSED,
    SNOOZED,
    MISSED
}

enum class ReminderLogEvent {
    SCHEDULED,
    REBOOT_RESCHEDULED,
    TIMEZONE_RESCHEDULED,
    FIRED,
    ACKNOWLEDGED,
    SNOOZED,
    MISSED
}
