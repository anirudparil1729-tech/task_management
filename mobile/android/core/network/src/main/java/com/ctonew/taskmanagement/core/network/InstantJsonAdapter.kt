package com.ctonew.taskmanagement.core.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantJsonAdapter {
  @ToJson
  fun toJson(value: Instant): String = value.toString()

  @FromJson
  fun fromJson(value: String): Instant {
    try {
      return Instant.parse(value)
    } catch (_: Throwable) {
      // fall through
    }

    try {
      return OffsetDateTime.parse(value).toInstant()
    } catch (_: Throwable) {
      // fall through
    }

    // FastAPI/Pydantic may return a naive datetime without an offset.
    return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)
  }
}

class LocalDateJsonAdapter {
  @ToJson
  fun toJson(value: LocalDate): String = value.toString()

  @FromJson
  fun fromJson(value: String): LocalDate = LocalDate.parse(value)
}
