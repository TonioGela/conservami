package dev.toniogela.conservami

import cats.data.*
import cats.syntax.all.*
import java.util.UUID
import io.circe.Encoder
import io.circe.refined.*
import java.time.*
import eu.timepit.refined.api.*
import eu.timepit.refined.string.MatchesRegex

import User.*

final case class User private (
    id: UUID,
    name: String,
    surname: String,
    birthPlace: String,
    birthDate: ZonedDateTime,
    fiscalCode: ItalianFiscalCode,
    residence: String,
    phoneNumber: PhoneNumber,
    email: Email,
    profession: String,
    memberSince: ZonedDateTime,
    membershipCardNumber: String,
    donation: Int,
    pdfDocument: Array[Byte]
) derives Encoder.AsObject

object User:

  def from(
      id: UUID,
      name: String,
      surname: String,
      birthPlace: String,
      birthDate: ZonedDateTime,
      fiscalCode: String,
      residence: String,
      phoneNumber: String,
      email: String,
      profession: String,
      memberSince: ZonedDateTime,
      membershipCardNumber: String,
      donation: Int
  ): EitherNel[String, User] = (
    UUID.randomUUID().rightNel[String], // TODO this will need to come from a different source
    name.rightNel[String],
    surname.rightNel[String],
    birthPlace.rightNel[String],
    birthDate.rightNel[String],
    ItalianFiscalCode.fromNel(fiscalCode),
    residence.rightNel[String],
    PhoneNumber.fromNel(phoneNumber),
    Email.fromNel(email),
    profession.rightNel[String],
    memberSince.rightNel[String],
    membershipCardNumber.rightNel[String],
    donation.rightNel[String],
    Array.emptyByteArray.rightNel[String]
  ).parMapN(User.apply)

  private type FiscalCodeRegex =
    MatchesRegex["""^[A-Z]{6}\d{2}[ABCDEHLMPRST]{1}\d{2}[A-Z]\d{3}[A-Z]$"""]
  private type EmailRegex = MatchesRegex["""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$"""]
  private type PhoneNumberRegex = MatchesRegex["""^\+?[0-9]{1,4}[-./\s]?[0-9]{1,15}$"""]

  type ItalianFiscalCode = String Refined FiscalCodeRegex

  object ItalianFiscalCode
      extends Distilled[ItalianFiscalCode, String]("Il codice fiscale è mal formattato")

  type Email = String Refined EmailRegex

  object Email extends Distilled[Email, String]("L'email è mal formattata")

  type PhoneNumber = String Refined PhoneNumberRegex

  object PhoneNumber
      extends Distilled[PhoneNumber, String]("Il numero di telefono è mal formattato")

  class Distilled[FTP, T](val errorMessage: String)(implicit rt: RefinedType.AuxT[FTP, T])
      extends RefinedTypeOps[FTP, T]:
    override def from(t: T): Either[String, FTP] = super.from(t).leftMap(_ => errorMessage)
    def fromNel(t: T): EitherNel[String, FTP]    = from(t).leftMap(NonEmptyList.one)

end User
