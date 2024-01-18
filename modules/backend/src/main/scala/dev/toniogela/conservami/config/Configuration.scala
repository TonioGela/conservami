package dev.toniogela.conservami.config

import com.monovore.decline.Opts
import cats.syntax.all.*
import fly4s.data.*
import org.http4s.Uri
import com.monovore.decline.Argument
import org.http4s.Uri.Scheme
import cats.data.NonEmptyList

final case class PgCredentials(
    host: String,
    port: Int,
    user: String,
    password: Option[String],
    database: String,
    options: Option[String]
) {

  /* For some reasons the scheme that flyway expects should be postgresql and not postgres */
  lazy val jdbcUrl: String = options
    .fold(s"jdbc:postgresql://$host:${port.toString}/$database")(opts =>
      s"jdbc:postgresql://$host:${port.toString}/$database?$opts"
    )
}

object Configuration:

  private given Argument[PgCredentials] = Argument.from("database_url")(url =>
    Uri.fromString(url).toValidatedNel.leftMap(_.map(_.message)).ensure(NonEmptyList.one(
      "The schema of the url must be \"postgres\""
    ))(_.scheme.equals(Scheme.fromString("postgres").toOption)).andThen(uri =>
      (
        uri.authority.map(_.host.value).toValidNel("Unable to extract the host from the url"),
        uri.authority.flatMap(_.port).toValidNel("Unable to extract the port from the url"),
        uri.authority.flatMap(_.userInfo).map(_.username)
          .toValidNel("Unable to extract the user from the url"),
        uri.authority.flatMap(_.userInfo).map(_.password)
          .toValidNel("Unable to extract the password from the url"),
        uri.path.segments.headOption.map(_.toString)
          .toValidNel("Unable to extract the database name from the url"),
        uri.query.renderString.some.filterNot(_.isBlank).validNel[String]
      ).mapN(PgCredentials.apply)
    )
  )

  val pgOpts: Opts[PgCredentials] = Opts.env[PgCredentials](
    "DATABASE_URL",
    "The database url connection in this shape: \"postgres://{username}:{password}@{hostname}:{port}/{database}?{options}\""
  )

  val fly4s: Fly4sConfig = Fly4sConfig(
    table = "migrations",
    defaultSchemaName = "conservami".some,
    locations = Locations("db"),
    baselineOnMigrate = true,
    cleanDisabled = false
  )

end Configuration
