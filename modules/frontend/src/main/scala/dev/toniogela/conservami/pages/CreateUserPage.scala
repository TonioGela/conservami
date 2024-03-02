package dev.toniogela.conservami.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import cats.syntax.all.*
import dev.toniogela.conservami.*
import dev.toniogela.conservami.core.*
import dev.toniogela.conservami.pages.Page.*
import dev.toniogela.conservami.pages.CreateUserPage.*
import java.util.UUID

//TODO! I can use a card maybe
final case class CreateUserPage(
    id: Option[UUID] = None,
    username: String = "",
    surname: String = "",
    birthPlace: String = "",
    birthDate: String = "",
    fiscalCode: String = "",
    residence: String = "",
    phoneNumber: String = "",
    email: String = "",
    profession: Option[String] = None,
    donation: String = "0",
    error: Option[String] = None
) extends Page {

  val url: String = Urls.create

  override def initCommand: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = msg match
    case UpdateState("username", username) => this.copy(error = None, username = username).andNone
    case UpdateState("surname", surname)   => this.copy(error = None, surname = surname).andNone
    case UpdateState("birthPlace", birthPlace)   => this.copy(error = None, birthPlace = birthPlace)
        .andNone
    case UpdateState("birthDate", birthDate)     => this.copy(error = None, birthDate = birthDate)
        .andNone
    case UpdateState("fiscalCode", fiscalCode)   => this.copy(error = None, fiscalCode = fiscalCode)
        .andNone
    case UpdateState("residence", residence)     => this.copy(error = None, residence = residence)
        .andNone
    case UpdateState("phoneNumber", phoneNumber) => this
        .copy(error = None, phoneNumber = phoneNumber).andNone
    case UpdateState("email", email)             => this.copy(error = None, email = email).andNone
    case UpdateState("profession", profession)   => this
        .copy(error = None, profession = profession.some).andNone
    case UpdateState("donation", donation) => this.copy(error = None, donation = donation).andNone
    case AttemptCreation                   => (
        nome.validation(username),
        cognome.validation(surname),
        luogoNascita.validation(birthPlace),
        dataNascita.validation(birthDate),
        codiceFiscale.validation(fiscalCode),
        residenza.validation(residence),
        numeroTel.validation(phoneNumber),
        emailF.validation(email),
        professione.validation(profession),
        "A0001".asRight[String],
        donazione.validation(donation)
      ).mapN(UserAdd.apply).fold(
        error => (this.copy(error = error.some), Cmd.None),
        userAdd =>
          id.fold((this, Endpoint.createUser[IO](userAdd)))(uuid =>
            (this, Endpoint.alterUser(uuid)[IO](userAdd))
          )
      )

    case UserCreated          =>
      // TODO! I can redirect to Individual User Page
      (this.copy(error = None), Cmd.emit(InternalRedirect(UserListPage())))
    case UserCreationError(e) => (this.copy(error = e.some), Cmd.None)
    case _                    => (this, Cmd.None)

  private val fields = List(
    nome.render(username),
    cognome.render(surname),
    luogoNascita.render(birthPlace),
    dataNascita.render(birthDate),
    codiceFiscale.render(fiscalCode),
    residenza.render(residence),
    numeroTel.render(phoneNumber),
    emailF.render(email),
    professione.render(profession),
    donazione.render(donation),
    button(`type` := "button", `class` := "btn btn-primary mb-3", onClick(AttemptCreation))(
      "Crea Socio"
    ),
    error.fold(div())(e => div(`class` := "alert alert-danger", role := "alert")(e))
  )

  override def view: Html[Page.Msg] = div(`class` := "form-section")(div(`class` := "top-section")(
    h1("Crea socio"),
    form(name := "create-user", `class` := "form", onSubmit(NoOp))(fields*)
  ))

}

object CreateUserPage:
  val nome    = NonEmptyStringField("username", "Nome")(UpdateState("username", _))
  val cognome = NonEmptyStringField("surname", "Cognome")(UpdateState("surname", _))

  val luogoNascita  =
    NonEmptyStringField("birthPlace", "Luogo di Nascita")(UpdateState("birthPlace", _))
  val dataNascita   = DateField("birthDate", "Data di Nascita")(UpdateState("birthDate", _))
  val codiceFiscale = FiscalCodeField("fiscalCode", "Codice Fiscale")(UpdateState("fiscalCode", _))
  val residenza     = NonEmptyStringField("residence", "Residenza")(UpdateState("residence", _))

  val numeroTel   =
    PhoneNumberField("phoneNumber", "Numero di Telefono")(UpdateState("phoneNumber", _))
  val emailF      = EmailField("email", "Email")(UpdateState("email", _))
  val professione = StringField("profession", "Professione")(UpdateState("profession", _))
  val donazione   = PositiveNumberField("donation", "Donazione")(UpdateState("donation", _))

  sealed trait Msg                                          extends Page.Msg
  final case class UpdateState(`for`: String, data: String) extends Msg
  case object AttemptCreation                               extends Msg
  case object UserCreated                                   extends Msg
  final case class UserCreationError(error: String)         extends Msg
