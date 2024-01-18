package dev.toniogela.conservami.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import cats.syntax.all.*
import dev.toniogela.conservami.pages.Page.Msg
import dev.toniogela.conservami.UserView
import dev.toniogela.conservami.pages.IndividualUserPage.*
import dev.toniogela.conservami.core.Endpoint
import java.time.format.DateTimeFormatter

final case class IndividualUserPage(
    userId: String,
    state: Either[String, UserView] = Left("Nessun utente con questo identificativo")
) extends Page {

  val url: String = s"/user/$userId"

  override def initCommand: Cmd[IO, Page.Msg] = Endpoint.getUser(userId)(())

  override def update(msg: Msg): (Page, Cmd[IO, Page.Msg]) = msg match
    case UserRetriveSuccess(user)  => (this.copy(state = user.asRight[String]), Cmd.None)
    case UserRetriveFailure(error) => (this.copy(state = error.asLeft[UserView]), Cmd.None)
    case _                         => (this, Cmd.None)

  // TODO! Take a look at a bootstrap user profile page, they are funny

  override def view: Html[Page.Msg] = state.fold(
    div(_),
    uv =>
      div(
        table(`class` := "table")(
          thead(tr(th(uv.name), th(uv.surname))),
          tbody(
            tr(th("Tessera #"), td(uv.membershipCardNumber)),
            tr(th("Email"), td(uv.email.value)),
            tr(th("Telefono"), td(uv.phoneNumber.value)),
            tr(th("Codice Fiscale"), td(uv.fiscalCode.value)),
            tr(th("Residenza"), td(uv.residence)),
            tr(
              th("Membro dal: "),
              td(uv.memberSince.format(DateTimeFormatter.ofPattern("dd/MM/yyy")))
            ),
            tr(
              th("Data di Nascita"),
              td(uv.birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyy")))
            ),
            tr(th("Luogo di Nascita"), td(uv.birthPlace)),
            tr(th("Professione"), td(uv.profession.orEmpty)),
            tr(th("Donazione"), td(s"${uv.donation.toString} €"))
          )
        ),
        `object`(
          data    := s"/api/user/$userId/pdf",
          `type`  := "application/pdf",
          width   := "100%",
          `style` := "min-width:500px;",
          height  := "500px"
        )(p("Impossibile mostrare il PDF in pagina"), a(href := s"/user/$userId/pdf")("Scaricalo."))
      )
  )
}

object IndividualUserPage:
  trait Msg                                      extends Page.Msg
  case class UserRetriveSuccess(users: UserView) extends Msg
  case class UserRetriveFailure(error: String)   extends Msg
