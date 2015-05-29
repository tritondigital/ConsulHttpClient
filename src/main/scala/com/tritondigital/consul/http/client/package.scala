package com.tritondigital.consul.http

import com.ning.http.client.AsyncHttpClient

import scala.language.implicitConversions

package object client {

  implicit def BoundRequestBuilderToConsulRequestBuilder(value: AsyncHttpClient#BoundRequestBuilder) = new ConsulRequestBuilder(value)

}
