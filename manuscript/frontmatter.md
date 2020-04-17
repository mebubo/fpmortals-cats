{frontmatter}


# About This Book

This book is for the typical Scala developer, probably with a Java background,
who is both sceptical and curious about the **Functional Programming** (FP)
paradigm. This book justifies every concept with practical examples, including
writing a web application.

This book uses [Typelevel Cats 2.1](https://typelevel.org/cats/), the most popular Functional Programming
framework for Scala. Typelevel has a wealth of accessible and idiomatic learning
resources in a welcoming and safe environment.

This book is designed to be read from cover to cover, in the order presented,
with a rest between chapters. Earlier chapters encourage coding styles that we
will later discredit: similar to how we learn Newton's theory of gravity as
children, and progress to Riemann / Einstein / Maxwell if we become students of
physics.

A computer is not necessary to follow along, but studying the Cats source code
is encouraged. Some of the more complex code snippets are available with [the
book's source code](https://github.com/turt13/fpmortals-cats/) and those who want practical exercises are encouraged to
(re-)implement Cats (and the example application) using the partial descriptions
presented in this book.


# Copyright Notice

This book is an updated and revised edition of "Functional Programming for
Mortals" by Sam Halliday.

Like the original, this book uses the [Creative Commons Attribution ShareAlike
4.0 International](https://creativecommons.org/licenses/by-sa/4.0/legalcode) (CC BY-SA 4.0) license.

All original code snippets in this book and the example application
`drone-dynamic-agents` are provided under the [Hippocratic License 2.1](https://firstdonoharm.dev/): an
Ethical Source license that specifically prohibits the use of software to
violate universal standards of human rights.


# Practicalities

To set up a project that uses the libraries presented in this book, use a recent
version of Scala with FP-specific features enabled (e.g. in `build.sbt`):

{lang="text"}
~~~~~~~~
  scalaVersion in ThisBuild := "2.12.11"
  scalacOptions in ThisBuild ++= Seq(
    "-language:_",
    "-Ypartial-unification",
    "-Xfatal-warnings"
  )
  
  libraryDependencies ++= Seq(
    "org.typelevel"        %% "simulacrum"      % "1.0.0",
    "org.typelevel"        %% "cats-core"       % "2.1.1",
  )
  
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
~~~~~~~~

In order to keep our snippets short, we will omit the `import`
section. Unless told otherwise, assume that all snippets have the
following imports:

{lang="text"}
~~~~~~~~
  import cats._, cats.data._, cats.implicits._
  import simulacrum._
~~~~~~~~


