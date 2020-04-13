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

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

scalacOptions in (Compile, console) -= "-Xfatal-warnings"
initialCommands in (Compile, console) := Seq(
  "cats._, cats.implicits._, cats.data._"
).mkString("import ", ",", "")
