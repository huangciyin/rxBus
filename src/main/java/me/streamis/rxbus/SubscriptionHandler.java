package me.streamis.rxbus;

import org.vertx.java.core.Handler;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

/**
 *
 */
abstract class SubscriptionHandler<R, T> implements Observable.OnSubscribe<R>, Subscription, Handler<T> {

  private Observer observer;

  public void execute() {
  }

  @Override
  public void call(Subscriber<? super R> subscriber) {
    this.observer = subscriber;
    execute();
  }

  @Override
  public void handle(T message) {
    fireNext((R) message);
  }

  protected void fireNext(R next) {
    if (observer != null) observer.onNext(next);
  }

  protected void fireResult(R res) {
    if (observer != null) {
      observer.onNext(res);
      observer.onCompleted();
    }
  }

  protected void fireError(Throwable t) {
    if (observer != null) observer.onError(t);
  }

  @Override
  public void unsubscribe() {
    if (observer != null) observer.onCompleted();
    observer = null;
  }

  @Override
  public boolean isUnsubscribed() {
    return false;
  }
}
