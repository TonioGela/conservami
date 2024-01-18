package dev.toniogela.conservami

import cats.data.*
import cats.syntax.all.*
import java.util.UUID
import io.circe.Codec
import io.circe.refined.*
import java.time.*
import eu.timepit.refined.api.*
import eu.timepit.refined.string.MatchesRegex

import User.*
import io.github.arainko.ducktape.*
import java.time.format.DateTimeFormatter

type FiscalCodeRegex  = MatchesRegex["""^[A-Z]{6}\d{2}[ABCDEHLMPRST]{1}\d{2}[A-Z]\d{3}[A-Z]$"""]
type EmailRegex       = MatchesRegex["""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$"""]
type PhoneNumberRegex = MatchesRegex["""^\+?[0-9]{1,4}[-./\s]?[0-9]{1,15}$"""]

type ItalianFiscalCode = String Refined FiscalCodeRegex

object ItalianFiscalCode
    extends Distilled[ItalianFiscalCode, String]("Il codice fiscale è mal formattato")

type Email = String Refined EmailRegex

object Email extends Distilled[Email, String]("L'email è mal formattata")

type PhoneNumber = String Refined PhoneNumberRegex

object PhoneNumber extends Distilled[PhoneNumber, String]("Il numero di telefono è mal formattato")

class Distilled[FTP, T](val errorMessage: String)(implicit rt: RefinedType.AuxT[FTP, T])
    extends RefinedTypeOps[FTP, T]:
  override def from(t: T): Either[String, FTP] = super.from(t).leftMap(_ => errorMessage)
  def fromNel(t: T): EitherNel[String, FTP]    = from(t).leftMap(NonEmptyList.one)

val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

final case class User(
    id: UUID,
    name: String,
    surname: String,
    birthPlace: String,
    birthDate: LocalDate,
    fiscalCode: ItalianFiscalCode,
    residence: String,
    phoneNumber: PhoneNumber,
    email: Email,
    profession: Option[String],
    memberSince: LocalDate,
    membershipCardNumber: String,
    donation: Int,
    pdfDocument: Array[Byte]
) derives Codec.AsObject

final case class UserView(
    id: UUID,
    name: String,
    surname: String,
    birthPlace: String,
    birthDate: LocalDate,
    fiscalCode: ItalianFiscalCode,
    residence: String,
    phoneNumber: PhoneNumber,
    email: Email,
    profession: Option[String],
    memberSince: LocalDate,
    membershipCardNumber: String,
    donation: Int
) derives Codec.AsObject

object UserView:
  def from(user: User): UserView = user.to[UserView]

final case class UserAdd(
    name: String,
    surname: String,
    birthPlace: String,
    birthDate: LocalDate,
    fiscalCode: ItalianFiscalCode,
    residence: String,
    phoneNumber: PhoneNumber,
    email: Email,
    profession: Option[String],
    membershipCardNumber: String,
    donation: Int
) derives Codec.AsObject
