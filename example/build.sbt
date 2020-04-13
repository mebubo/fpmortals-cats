scalaVersion in ThisBuild := "2.12.11"
scalacOptions in ThisBuild ++= Seq(
  "-language:_",
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "org.typelevel"        %% "simulacrum"      % "1.0.0",
  "org.typelevel"        %% "cats-core"       % "2.1.1",
  "org.typelevel"        %% "mouse"           % "0.24",
  "org.typelevel"        %% "cats-mtl-core"   % "0.7.1",
  "org.typelevel"        %% "cats-effect"     % "2.1.2",
  "org.typelevel"        %% "kittens"         % "2.0.0",
  "eu.timepit"           %% "refined-cats"    % "0.9.13"
)

val http4sVersion = "0.18.16"
libraryDependencies ++= Seq(
  "com.chuusai"           %% "shapeless"           % "2.3.3",
  "com.lihaoyi"           %% "sourcecode"          % "0.1.4",
  "com.propensive"        %% "contextual"          % "1.1.0",
  "org.scalatest"         %% "scalatest"           % "3.0.5" % "test,it",
  "com.github.pureconfig" %% "pureconfig"          % "0.9.1",
  "org.http4s"            %% "http4s-dsl"          % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % http4sVersion
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

scalacOptions in (Compile, console) -= "-Xfatal-warnings"
initialCommands in (Compile, console) := Seq(
  "cats._, cats.implicits._, cats.data._"
).mkString("import ", ",", "")
