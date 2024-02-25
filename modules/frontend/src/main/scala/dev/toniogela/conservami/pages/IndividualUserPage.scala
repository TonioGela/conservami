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
      div(`class` := "container")(div(`class` := "row")(
        div(`class` := "col-lg-4")(div(`class` := "card text-bg-primary mb-3")(
          div(`class` := "card-body")(
            h3(`class` := "card-title")(s"${uv.name} ${uv.surname}"),
            p(`class` := "card-text")(s"Tessera: ${uv.membershipCardNumber}")
          ),
          ul(`class` := "list-group list-group-flush")(
            li(`class` := "list-group-item")(
              text("Email: "),
              a(href := s"mailto:${uv.email.value}")(uv.email.value)
            ),
            li(`class` := "list-group-item")(
              text("Telefono: "),
              a(href := s"tel:${uv.phoneNumber.value}")(uv.phoneNumber.value)
            )
          )
        )), // TODO! Bottoni modifica ed elimina qui sotto la card
        div(`class` := "col-lg-8")(
          div(`class` := "card mb-3")(
            div(`class` := "card-body")(h4(`class` := "card-title mb-0")("Dati")),
            ul(`class` := "list-group list-group-flush")(
              li(`class` := "list-group-item")(
                s"Socio/a dal: ${uv.memberSince.format(DateTimeFormatter.ofPattern("dd/MM/yyy"))}"
              ),
              li(`class` := "list-group-item")(s"Donazione: ${uv.donation} €"),
              li(`class` := "list-group-item")(
                s"Professione: ${uv.profession.getOrElse("Non specificata")}"
              ),
              li(`class` := "list-group-item")(s"Codice Fiscale: ${uv.fiscalCode.value}"),
              li(`class` := "list-group-item")(
                s"Data di Nascita: ${uv.birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyy"))}"
              ),
              li(`class` := "list-group-item")(s"Residenza: ${uv.residence}")
            )
          ),
          div(`class` := "card mb-3")(
            div(`class` := "card-body")(h4(`class` := "card-title mb-0")("Documenti")),
            div(`class` := "accordion accordion-flush m-1", id := "documentAccordion")(
              div(`class` := "accordion-item")(h2(`class` := "accordion-header")(
                button(
                  `class` := "accordion-button",
                  `type`  := "button",
                  Attribute("data-bs-toggle", "collapse"),
                  Attribute("data-bs-target", "#collapseOne")
                )("Modulo Iscrizione"),
                div(
                  id      := "collapseOne",
                  `class` := "accordion-collapse collapse",
                  Attribute("data-bs-parent", "#accordionExample")
                )(div(`class` := "accordion-body")(
                  `object`(
                    data    := s"/api/user/$userId/pdf",
                    `type`  := "application/pdf",
                    width   := "100%",
                    `style` := "min-width:500px;",
                    height  := "500px"
                  )(
                    p("Impossibile mostrare il PDF in pagina"),
                    a(href := s"/user/$userId/pdf")("Scaricalo.")
                  )
                ))
              ))
            )
          )
        )
      ))
  )

}

object IndividualUserPage:
  trait Msg                                      extends Page.Msg
  case class UserRetriveSuccess(users: UserView) extends Msg
  case class UserRetriveFailure(error: String)   extends Msg
