package com.tritondigital.consul.http.client

import java.io.{File, InputStream}
import java.lang.Boolean
import java.net.InetAddress
import java.util

import com.ning.http.client.cookie.Cookie
import com.ning.http.client.multipart.Part
import com.ning.http.client._
import com.ning.http.client.uri.Uri

class ConsulRequest(request: Request, node: Node) extends Request {

  def getMethod: String = request.getMethod

  def getUri: Uri = new Uri(request.getUri.getScheme, request.getUri.getUserInfo, node.ip, node.port, request.getUri.getPath, request.getUri.getQuery)

  def getUrl: String = getUri.toUrl

  def getInetAddress: InetAddress = request.getInetAddress

  def getLocalAddress: InetAddress = request.getLocalAddress

  def getHeaders: FluentCaseInsensitiveStringsMap = request.getHeaders

  def getCookies: util.Collection[Cookie] = request.getCookies

  def getByteData: Array[Byte] = request.getByteData

  def getCompositeByteData: util.List[Array[Byte]] = request.getCompositeByteData

  def getStringData: String = request.getStringData

  def getStreamData: InputStream = request.getStreamData

  def getBodyGenerator: BodyGenerator = request.getBodyGenerator

  def getContentLength: Long = request.getContentLength

  def getFormParams: util.List[Param] = request.getFormParams

  def getParts: util.List[Part] = request.getParts

  def getVirtualHost: String = request.getVirtualHost

  def getQueryParams: util.List[Param] = request.getQueryParams

  def getProxyServer: ProxyServer = request.getProxyServer

  def getRealm: Realm = request.getRealm

  def getFile: File = request.getFile

  def getFollowRedirect: Boolean = request.getFollowRedirect

  def getRequestTimeout: Int = request.getRequestTimeout

  def getRangeOffset: Long = request.getRangeOffset

  def getBodyEncoding: String = request.getBodyEncoding

  def getConnectionPoolPartitioning: ConnectionPoolPartitioning = request.getConnectionPoolPartitioning

  def getNameResolver: NameResolver = request.getNameResolver

}
