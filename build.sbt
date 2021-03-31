scalaVersion in ThisBuild := "2.12.13"
scalacOptions in ThisBuild ++= Seq(
  "-language:_",
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-deprecation"
)

val commonDeps = Seq(
  "org.typelevel"        %% "cats-kernel"     % "2.4.2",
  "org.typelevel"        %% "cats-core"       % "2.4.2",
  "org.typelevel"        %% "cats-effect"     % "2.4.0",
  "com.lihaoyi"          %% "sourcecode"      % "0.2.4",
  "com.chuusai"          %% "shapeless"       % "2.3.3",
  "eu.timepit"           %% "refined"         % "0.9.21"
)

val http4sVersion = "0.18.26"
val exampleDeps = Seq(
  "org.typelevel"         %% "simulacrum"          % "1.0.1",
  "org.typelevel"         %% "jawn-parser"         % "1.1.0",
  "org.typelevel"         %% "cats-mtl-core"       % "0.7.1",
  "com.propensive"        %% "magnolia"            % "0.17.0",
  "org.typelevel"         %% "cats-free"           % "2.4.2",
  "com.propensive"        %% "contextual"          % "1.2.1",
  "org.scalatest"         %% "scalatest"           % "3.2.6" % "test,it",
  "org.http4s"            %% "http4s-dsl"          % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % http4sVersion
)

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  libraryDependencies ++= commonDeps
)

lazy val snippets = (project in file(".")).settings(
  commonSettings
)

lazy val example = (project in file("example")).settings(
  commonSettings,
  libraryDependencies ++= exampleDeps
)

scalacOptions in (Compile, console) -= "-Xfatal-warnings"
initialCommands in (Compile, console) := Seq(
  "cats._, cats.implicits._, cats.data._"
).mkString("import ", ",", "")
