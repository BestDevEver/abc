import ReleaseTransformations._

lazy val commonSettings = Seq(
  organization := "com.rklaehn",
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.5", "2.11.7"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
    "com.rklaehn" %%% "sonicreducer" % "0.3.0",
    "org.spire-math" %%% "algebra" % "0.3.1",
    "org.spire-math" %%% "cats" % "0.3.0",
    "org.scalatest" %%% "scalatest" % "3.0.0-M7" % "test",
    "org.spire-math" %%% "algebra-std" % "0.3.1" % "test",
    "org.spire-math" %%% "algebra-laws" % "0.3.1" % "test"
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature"
  ),
  licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("http://github.com/rklaehn/radixtree")),

  // release stuff
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  publishTo <<= version { v =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <scm>
      <url>git@github.com:rklaehn/radixtree.git</url>
      <connection>scm:git:git@github.com:rklaehn/radixtree.git</connection>
    </scm>
      <developers>
        <developer>
          <id>r_k</id>
          <name>R&#xFC;diger Klaehn</name>
          <url>http://github.com/rklaehn/</url>
        </developer>
      </developers>
  ,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    ReleaseStep(action = Command.process("package", _)),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges))

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project.in(file("."))
  .aggregate(
    coreJVM, collectionJVM, lawsJVM, testsJVM,
    coreJS, collectionJS, lawsJS, testsJS,
    instrumentedTests, jmhBenchmarks
  )
  .settings(name := "root")
  .settings(commonSettings: _*)
  .settings(noPublish: _*)

lazy val core = crossProject.crossType(CrossType.Pure).in(file("core"))
  .settings(name := "abc")
  .settings(commonSettings: _*)

lazy val laws = crossProject.crossType(CrossType.Pure).in(file("laws"))
  .settings(name := "abc-laws")
  .settings(commonSettings: _*)
  .dependsOn(core)

lazy val collection = crossProject.crossType(CrossType.Pure).in(file("collection"))
  .settings(name := "abc-collection")
  .settings(commonSettings: _*)
  .settings(noPublish: _*)
  .dependsOn(core)

lazy val tests = crossProject.crossType(CrossType.Pure).in(file("tests"))
  .settings(name := "abc-tests")
  .settings(commonSettings: _*)
  .settings(noPublish: _*)
  .dependsOn(core, laws)

lazy val instrumentedTests = project.in(file("instrumentedTests"))
  .settings(name := "instrumentedTests")
  .settings(commonSettings: _*)
  .settings(instrumentedTestSettings: _*)
  .settings(noPublish: _*)
  .dependsOn(coreJVM)

lazy val jmhBenchmarks = project.in(file("jmhBenchmarks"))
  .settings(name := "jmhBenchmarks")
  .settings(commonSettings:_*)
  .dependsOn(coreJVM, collectionJVM)
  .settings(noPublish: _*)
  .enablePlugins(JmhPlugin)

lazy val thymeBenchmarks = project.in(file("thymeBenchmarks"))
  .settings(name := "thymeBenchmarks")
  .settings(commonSettings:_*)
  .dependsOn(coreJVM, collectionJVM)
  .settings(libraryDependencies +=
    "ichi.bench" % "thyme" % "0.1.1" from "https://github.com/Ichoran/thyme/raw/9ff531411e10c698855ade2e5bde77791dd0869a/Thyme.jar")
  .settings(noPublish: _*)

lazy val instrumentedTestSettings = {
  def makeAgentOptions(classpath:Classpath) : String = {
    val jammJar = classpath.map(_.data).filter(_.toString.contains("jamm")).head
    s"-javaagent:$jammJar"
  }
  Seq(
    javaOptions in Test <+= (dependencyClasspath in Test).map(makeAgentOptions),
      libraryDependencies += "com.github.jbellis" % "jamm" % "0.3.0" % "test",
      fork := true
    )
}


// abc-jvm is JVM-only
lazy val abcJVM = project.in(file(".abcJVM"))
  .aggregate(coreJVM, lawsJVM, testsJVM)
  .dependsOn(coreJVM, lawsJVM, testsJVM % "test-internal -> test")
  .settings(moduleName := "cats-jvm")
  .settings(commonSettings:_*)
  .settings(commonJvmSettings:_*)

lazy val coreJVM = core.jvm

lazy val coreJS = core.js

lazy val lawsJVM = laws.jvm

lazy val lawsJS = laws.js

lazy val collectionJVM = collection.jvm

lazy val collectionJS = collection.js

lazy val testsJVM = tests.jvm

lazy val testsJS = tests.js
