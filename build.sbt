scalaVersion in ThisBuild := "2.12.11"
scalacOptions in ThisBuild ++= Seq(
  "-language:_",
  "-Ypartial-unification",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "org.typelevel"        %% "simulacrum"      % "1.0.0",
  "org.typelevel"        %% "cats-core"       % "2.1.1",
  "org.typelevel"        %% "mouse"           % "0.24",
  "eu.timepit"           %% "refined-cats"    % "0.9.13",
  "com.chuusai"          %% "shapeless"       % "2.3.3",
  "org.scalaz"           %% "scalaz-effect"   % "7.2.25",
  "org.scalaz"           %% "scalaz-ioeffect" % "2.10.1",
  "eu.timepit"           %% "refined-scalaz"  % "0.9.13",
  "com.lihaoyi"          %% "sourcecode"      % "0.1.4",
  "io.estatico"          %% "newtype"         % "0.4.2"
)

val derivingVersion = "1.0.0"
libraryDependencies ++= Seq(
  "org.scalaz" %% "deriving-macro" % derivingVersion,
  compilerPlugin("org.scalaz" %% "deriving-plugin" % derivingVersion),
  "org.scalaz" %% "scalaz-deriving"            % derivingVersion,
  "org.scalaz" %% "scalaz-deriving-magnolia"   % derivingVersion,
  "org.scalaz" %% "scalaz-deriving-scalacheck" % derivingVersion
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

scalacOptions in (Compile, console) -= "-Xfatal-warnings"
