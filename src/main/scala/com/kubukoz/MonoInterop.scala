package com.kubukoz

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.implicits._
import cats.~>
import reactor.core.publisher.Mono

object MonoInterop {

  def monoFromF[F[_]: Async]: Resource[F, F ~> Mono] =
    Dispatcher[F].map { dispatcher =>
      new (F ~> Mono) {
        def apply[A](fa: F[A]): Mono[A] = Mono.create { finish =>
          val cancel = dispatcher.unsafeRunCancelable {
            fa
              .redeemWith(
                e => Sync[F].delay(finish.error(e)),
                v => Sync[F].delay(finish.success(v))
              )
          }

          finish.onCancel(() => cancel())
        }
      }
    }

  def fFromMono[F[_]: Async]: Mono ~> F = new (Mono ~> F) {

    def apply[A](fa: Mono[A]): F[A] = Async[F].async { cb =>
      val cancel = fa
        .doOnSuccess(a => cb(Right(a)))
        .doOnError(e => cb(Left(e)))
        .subscribe()

      Sync[F].delay(cancel.dispose()).some.pure[F]
    }

  }

}
