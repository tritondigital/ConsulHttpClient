package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncCompletionHandler, Response, AsyncHttpClient}

import scala.concurrent.{Promise, Future}

class FutureRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {

  def withConsul(): FutureRequestBuilder = this

  def execute(): Future[Response] = {
    val promise = Promise[Response]()
    requestBuilder.execute(new CompletePromise(promise))
    promise.future
  }

  class CompletePromise(promise: Promise[Response]) extends AsyncCompletionHandler[Response] {

    override def onCompleted(response: Response): Response = {
      promise.success(response)
      response
    }

    override def onThrowable(throwable: Throwable): Unit = promise.failure(throwable)

  }

}
