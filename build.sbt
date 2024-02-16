import Utils.*

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  organization      := "dev.toniogela",
  scalaVersion      := "3.4.0",
  semanticdbEnabled := true,
  scalafixOnCompile := true,
  scalafmtOnCompile := true
))

lazy val root = project.root(
  domain,
  backend
  // frontend
)

lazy val domain = crossProject(
  JVMPlatform
  // JSPlatform
).crossType(CrossType.Pure).in(file("modules/domain")).settings(
  name                := "conservami-domain",
  libraryDependencies :=
    List("io.circe" %%% "circe-core" % "0.14.6", "io.circe" %%% "circe-refined" % "0.14.6")
)

lazy val backend = project.jvmDocker.in(file("modules/backend")).dependsOn(domain.jvm).settings(
  name       := "conservami-backend",
  run / fork := true,
  // Compile / resourceGenerators += copyJsToAssets(frontend),
  libraryDependencies ++= List(
    "com.monovore"        %%% "decline-effect"      % "2.4.1",
    "org.http4s"          %%% "http4s-ember-server" % "1.0.0-M40",
    "org.http4s"          %%% "http4s-dsl"          % "1.0.0-M40",
    "org.typelevel"       %%% "log4cats-slf4j"      % "2.6.0",
    "ch.qos.logback"        % "logback-classic"     % "1.5.0",
    "org.tpolecat"        %%% "skunk-core"          % "0.6.3",
    "com.github.geirolz"   %% "fly4s-core"          % "1.0.0",
    "com.disneystreaming" %%% "weaver-cats"         % "0.8.3"     % Test,
    "com.disneystreaming" %%% "weaver-scalacheck"   % "0.8.3"     % Test,
    "org.http4s"          %%% "http4s-ember-client" % "1.0.0-M40" % Test,
    "org.typelevel"       %%% "log4cats-noop"       % "2.6.0"     % Test
  )
)

// lazy val frontend = project.jsEsProject.in(file("modules/frontend")).settings(
//   name                            := "conservami-frontend",
//   libraryDependencies             := List("io.indigoengine" %%% "tyrian-io" % "0.8.0"),
//   scalaJSUseMainModuleInitializer := true,
//   Test / test                     := {}
// )
