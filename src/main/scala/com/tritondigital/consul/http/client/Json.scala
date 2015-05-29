package com.tritondigital.consul.http.client

import org.json4s._
import org.json4s.native.JsonMethods._

object Json {

  implicit val formats = DefaultFormats

  def extractNodes(body: String): List[Node] = {
    val json = parse(body)
    json.children.map { child =>
      Node((child \ "Node" \ "Address").extract[String], (child \ "Service" \ "Port").extract[Int])
    }
  }

}
