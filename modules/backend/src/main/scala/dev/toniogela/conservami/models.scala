package dev.toniogela.conservami

import dev.toniogela.conservami.User.*
import io.github.arainko.ducktape.*
import java.util.UUID
import java.time.LocalDate
import java.time.ZoneId

final case class PersistenceUserAdd(
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
):
  def toUser(uuid: UUID): User = this.into[User].transform(Field.const(_.id, uuid))

object PersistenceUserAdd:

  def from(userAdd: UserAdd, pdf: Array[Byte]): PersistenceUserAdd = userAdd
    .into[PersistenceUserAdd].transform(
      Field.const(_.pdfDocument, pdf),
      Field.const(_.memberSince, LocalDate.now(ZoneId.of("Europe/Rome")))
    )
