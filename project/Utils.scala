import sbt.*
import sbt.Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.*
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoKeys.*
import sbtbuildinfo.BuildInfoKey
import _root_.io.github.sbt.tzdb.TzdbPlugin
import _root_.io.github.sbt.tzdb.TzdbPlugin.autoImport.zonesFilter

object Utils {

  private def copyAll(location: File, outDir: File): List[File] = IO.listFiles(location).toList
    .map { file =>
      val (name, ext) = file.baseAndExt
      val out: File   = outDir / (name + "." + ext)
      IO.copyFile(file, out)
      out
    }

  private def linkJS(feProject: Project): Def.Initialize[Task[File]] = Def.taskIf {
    if (isRelease) {
      val _ = (feProject / Compile / fullLinkJS).value
      (feProject / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
    } else {
      val _ = (feProject / Compile / fastLinkJS).value
      (feProject / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
    }
  }

  def copyJsToAssets(feProject: Project): Def.Initialize[Task[List[File]]] = Def.task[List[File]](
    copyAll(linkJS(feProject).value, (Compile / resourceManaged).value / "assets")
  )

  private lazy val isRelease: Boolean = sys.env.contains("RELEASE")

  implicit class RichProject(private val project: Project) extends AnyVal {

    def root(c: CompositeProject*): Project = project.in(file("."))
      .aggregate(c.flatMap(_.componentProjects).map(_.project): _*)

    def jsEsProject: Project = project.enablePlugins(ScalaJSPlugin).enablePlugins(BuildInfoPlugin)
      .enablePlugins(TzdbPlugin).settings(
        scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
        buildInfoKeys := List(
          BuildInfoKey
            .action("host")(if (isRelease) "https://conservami.org" else "http://localhost:8080")
        ),
        zonesFilter   := { (z: String) => z == "Europe/Rome" }
      )

    def jvmDocker: Project = project.enablePlugins(DockerPlugin, JavaAppPackaging).settings(
      dockerBaseImage    := "eclipse-temurin:17-jre",
      dockerExposedPorts := 8080 :: Nil,
      dockerExecCommand  := "podman" :: Nil,
      dockerBuildOptions ++= List("--platform", "linux/amd64")
    )
  }

}
