// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda

import cats._, arrow._, data._, implicits._
import cats.mtl._
import cats.mtl.implicits._
import cats.effect._

import scala.collection.immutable.List
import scala.concurrent.duration._

import logic._
import interpreters._
import http._
import http.interpreters._
import http.oauth2._
import http.oauth2.interpreters._
import time._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    if (args.contains("--machines")) auth("machines")
    else agents(BearerToken("<invalid>", Epoch(0)))
  }.attempt.map {
    case Right(_) => ExitCode.Success
    case Left(err) =>
      java.lang.System.err.println(err)
      ExitCode.Error
  }

  // implement your own configuration reader
  def readConfig[A](filename: String): IO[A] =
    scala.sys.error("unimplemented")

  def putStrLn(str: String): IO[Unit] =
    scala.sys.error("unimplemented")

  // performs the OAuth 2.0 dance to obtain refresh tokens
  def auth(name: String): IO[Unit] = {
    for {
      config    <- readConfig[ServerConfig](name + ".server")
      sleeper   = new SleepIO
      ui        <- BlazeUserInteraction(sleeper)
      auth      = new AuthModule(config)(ui)
      codetoken <- auth.authenticate
      clock     = new LocalClockIO
      client    <- BlazeJsonClient[IO]
      access    = new AccessModule(config)(client, clock)
      token     <- access.access(codetoken)
      _         <- putStrLn(s"got token: ${token._1}")
    } yield ()
  }

  def liftIoK[F[_]](implicit L: LiftIO[F]): IO ~> F = FunctionK.lift(L.liftIO)

  // runs the app, requires that refresh tokens are provided
  def agents(bearer: BearerToken): IO[Unit] = {
    type Middle[a] = StateT[IO, BearerToken, a]
    type Inner[a] = StateT[Middle, WorldView, a]

    for {
      config  <- readConfig[AppConfig]("application.conf")
      blazeIO <- BlazeJsonClient[IO]
      blaze = blazeIO.mapK[Middle](liftIoK)
      _ <- {
        val bearerClient = new BearerJsonClientModule[Middle](bearer)(blaze)
        val drone        = new DroneModule[Middle](bearerClient)
        val clock        = (new LocalClockIO).mapK[Middle](liftIoK)
        val refresh      = new RefreshModule[Middle](config.machines.server)(blaze, clock)
        val oauthClient =
          new OAuth2JsonClientModule[Middle](config.machines.token)(blaze, clock, refresh)
        val machines = new GoogleMachinesModule[Middle](oauthClient)
        val agents   = new DynAgentsModule[Middle](drone, machines)
        for {
          start <- agents.initial
          sleeper = (new SleepIO).mapK[Inner](liftIoK)
          fagents = agents.mapK[Inner](StateT.liftK)
          _ <- step(fagents, sleeper).foreverM[Unit].runA(start)
        } yield ()
      }.runA(bearer)
    } yield ()
  }

  private def step[F[_]: Monad](
    A: DynAgents[F],
    S: Sleep[F]
  )(
    implicit
    F: MonadState[F, WorldView]
  ): F[Unit] =
    for {
      old     <- F.get
      updated <- A.update(old)
      changed <- A.act(updated)
      _       <- F.set(changed)
      _       <- S.sleep(10.seconds)
    } yield ()

  final case class OAuth2Config(
    token: RefreshToken,
    server: ServerConfig
  )
  final case class AppConfig(
    drone: BearerToken,
    machines: OAuth2Config
  )
}
