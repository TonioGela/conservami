package dev.toniogela.conservami.components

import tyrian.*
import tyrian.Html.*
import dev.toniogela.conservami.pages.Page

object Header {

  def view = header(`class` := "header-container")(
    renderLogo(),
    div(`class` := "header-nav")(ul(`class` := "header-links")(
      renderNavLink("Elenco Soci", Page.Urls.home),
      renderNavLink("Registra Nuovo Socio", Page.Urls.create)
    ))
  )

  private val logoImage: String = "/assets/img/logo.png"

  private def renderLogo() =
    a(href := "/")(img(`class` := "home-logo", src := logoImage, alt := "Conservami"))

  private def renderNavLink(text: String, location: String) =
    li(`class` := "nav-item")(a(href := location, `class` := "nav-link")(text))
}
