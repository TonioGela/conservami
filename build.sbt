import Utils.*

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  organization      := "dev.toniogela",
  scalaVersion      := "3.4.2",
  semanticdbEnabled := true,
  scalafixOnCompile := true,
  scalafmtOnCompile := true
))

lazy val root = project.root(domain, backend, frontend)

lazy val domain = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure)
  .in(file("modules/domain")).settings(
    name                := "conservami-domain",
    libraryDependencies := List(
      "io.circe"          %%% "circe-core"    % "0.14.9",
      "io.circe"          %%% "circe-refined" % "0.14.9",
      "io.github.arainko" %%% "ducktape"      % "0.2.4"
    )
  )

lazy val backend = project.jvmDocker.in(file("modules/backend")).dependsOn(domain.jvm).settings(
  name       := "conservami-backend",
  run / fork := true,
  Compile / resourceGenerators += copyJsToAssets(frontend),
  libraryDependencies ++= List(
    "org.typelevel"       %%% "cats-effect"                % "3.5.4",
    "co.fs2"              %%% "fs2-core"                   % "3.10.2",
    "co.fs2"              %%% "fs2-io"                     % "3.10.2",
    "com.monovore"        %%% "decline-effect"             % "2.4.1",
    "org.http4s"          %%% "http4s-ember-server"        % "0.23.27",
    "org.http4s"          %%% "http4s-dsl"                 % "0.23.27",
    "org.http4s"          %%% "http4s-circe"               % "0.23.27",
    "org.typelevel"       %%% "log4cats-slf4j"             % "2.7.0",
    "ch.qos.logback"        % "logback-classic"            % "1.5.6",
    "org.tpolecat"        %%% "skunk-core"                 % "0.6.4",
    "com.github.geirolz"   %% "fly4s"                      % "1.0.7",
    "org.flywaydb"          % "flyway-database-postgresql" % "10.17.0",
    "org.postgresql"        % "postgresql"                 % "42.7.3",
    "com.github.librepdf"   % "openpdf"                    % "2.0.2",
    "com.disneystreaming" %%% "weaver-cats"                % "0.8.4"   % Test,
    "com.disneystreaming" %%% "weaver-scalacheck"          % "0.8.4"   % Test,
    "org.http4s"          %%% "http4s-ember-client"        % "0.23.27" % Test,
    "org.typelevel"       %%% "log4cats-noop"              % "2.7.0"   % Test
  )
)

lazy val frontend = project.jsEsProject.in(file("modules/frontend")).dependsOn(domain.js).settings(
  name                                      := "conservami-frontend",
  libraryDependencies                       := List(
    "io.indigoengine"   %%% "tyrian-io"       % "0.11.0",
    "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
    "io.circe"          %%% "circe-parser"    % "0.14.9",
    "com.armanbilge"    %%% "fs2-dom"         % "0.2.1"
  ),
  Compile / scalaJSUseMainModuleInitializer := true,
  Compile / mainClass                       := Some("dev.toniogela.conservami.Conservami"),
  Test / test                               := {}
)
