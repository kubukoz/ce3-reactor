package com.kubukoz

import java.time.Duration

import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.IOApp
import reactor.core.publisher.Mono

import MonoInterop._

object Demo extends IOApp.Simple {

  val io = {
    IO.println("hello") *>
      IO.sleep(500.millis) *>
      IO.println("world")
  }.onCancel(IO(println("cancelled")))

  val mono = Mono
    .fromSupplier(() => println("hello"))
    .then(Mono.delay(Duration.ofMillis(500)))
    .then(Mono.fromSupplier(() => println("world")))
    .doOnCancel(() => println("cancelled"))

  def run: IO[Unit] = {
    val runMono = MonoInterop
      .fFromMono[IO]
      .apply {
        mono
      }
      .timeout(200.millis)

    val runIO = MonoInterop.monoFromF[IO].use { toMono =>
      MonoInterop.fFromMono[IO].apply {
        toMono(io).timeout(Duration.ofMillis(200))
      }
    }

    runMono.attempt *> runIO.attempt.void
  }

}
