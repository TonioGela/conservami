package dev.toniogela.conservami

import java.util.UUID
import io.circe.Encoder
import java.time.*

final case class User(
    id: UUID,
    name: String,
    surname: String,
    birthPlace: String,
    birthDate: OffsetDateTime,
    fiscalCode: String,
    residence: String,
    phoneNumber: String,
    email: String,
    profession: String,
    memberSince: ZonedDateTime,
    membershipCardNumber: String,
    pdfDocument: Array[Byte]
) derives Encoder.AsObject
