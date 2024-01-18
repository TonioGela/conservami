package dev.toniogela.conservami.core

import tyrian.*
import tyrian.Html.*
import cats.syntax.all.*
import java.time.LocalDate
import dev.toniogela.conservami.*

trait FormField[T, M, X](kind: String, uid: String, name: String, message: String => M) {
  def validation: T => Either[String, X]

  def render(t: T): Html[M] = {

    val (inputClass, errorDiv) = validation(t).fold(
      e => ("form-control is-invalid", div(`class` := "form-text")(e)),
      _ => ("form-control is-valid", div(`class` := "form-text")())
    )

    val inputAttributes: List[Attr[M]] = {
      val default = List(`class` := inputClass, id := uid, onInput(message))
      kind match {
        case "number" => (`type` := kind) :: (min     := 0) :: (value := "0") :: default
        case "money"  => (`type` := "number") :: (min := 0) :: (value := "0") :: default
        case x        => (`type` := x) :: default
      }
    }

    val inputGroupFields = {
      val default = List(span(`class` := "input-group-text")(name), input(inputAttributes))
      kind match {
        case "money" => default :+ span(`class` := "input-group-text")("€")
        case _       => default
      }
    }

    div(`class` := "mb-3")(div(`class` := "input-group")(inputGroupFields*), errorDiv)

  }
}

case class StringField[M](uid: String, name: String)(message: String => M)
    extends FormField[Option[String], M, Option[String]]("text", uid, name, message) {

  def validation: Option[String] => Either[String, Option[String]] = _.asRight[String]
}

case class NonEmptyStringField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, String]("text", uid, name, message) {

  def validation: String => Either[String, String] =
    s => Either.cond(!s.isBlank, s.trim, s"Il campo $name non puo essere vuoto")
}

case class PositiveNumberField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, Int]("number", uid, name, message) {

  def validation: String => Either[String, Int] =
    s => s.toIntOption.filter(_ >= 0).toRight(s"Il campo $name deve essere un numero non negativo")
}

case class DateField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, LocalDate]("date", uid, name, message) {

  def validation: String => Either[String, LocalDate] = s =>
    Either.catchNonFatal(LocalDate.parse(s, dateTimeFormat))
      .leftMap(_ => s"La data $s essere inserita nel formato corretto")
}

case class FiscalCodeField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, ItalianFiscalCode]("text", uid, name, message) {

  def validation: String => Either[String, ItalianFiscalCode] = ItalianFiscalCode.from
}

case class PhoneNumberField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, PhoneNumber]("text", uid, name, message) {

  def validation: String => Either[String, PhoneNumber] = PhoneNumber.from
}

case class EmailField[M](uid: String, name: String)(message: String => M)
    extends FormField[String, M, Email]("email", uid, name, message) {

  def validation: String => Either[String, Email] = Email.from
}
