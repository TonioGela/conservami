package dev.toniogela.conservami.components

import tyrian.*
import tyrian.Html.*
import dev.toniogela.conservami.pages.Page

object Header {

  def view = nav(`class` := "navbar navbar-expand-lg navbar-light bg-light")(
    div(`class` := "container-fluid")(
      navBarBrand,
      navBarButton,
      div(`class` := "collapse navbar-collapse", id := "navbarSupportedContent")(
        ul(`class` := "navbar-nav ms-auto mb-2 mb-lg-0")(
          renderNavLink("Registra Nuovo Socio", Page.Urls.create)
        )
      )
    )
  )

  private val logoImage: String = "/assets/img/logo.png"

  private val navBarBrand: Html[Nothing] = a(`class` := "navbar-brand mb-0 h1", href := "/")(
    img(
      src     := logoImage,
      width   := "40",
      height  := "40",
      `class` := "d-inline-block align-text-middle"
    ),
    text(" Conservami")
  )

  private val navBarButton: Html[Nothing] = button(
    `class` := "navbar-toggler",
    `type`  := "button",
    Attribute("data-bs-toggle", "collapse"),
    Attribute("data-bs-target", "#navbarSupportedContent"),
    Attribute("aria-controls", "navbarSupportedContent"),
    Attribute("aria-expanded", "false"),
    Attribute("aria-label", "Toggle navigation")
  )(span(`class` := "navbar-toggler-icon")())

  private def renderNavLink(text: String, location: String) =
    li(`class` := "nav-item")(a(href := location, `class` := "nav-link")(text))
}
