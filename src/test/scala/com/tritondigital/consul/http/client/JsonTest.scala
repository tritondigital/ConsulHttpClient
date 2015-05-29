package com.tritondigital.consul.http.client

import org.scalatest.{Matchers, WordSpec}

class JsonTest extends WordSpec with Matchers {

  val fullJson =
      """
        |[
        | {
        |   "Node": {
        |     "Node": "ash-alabs04.sv.stw",
        |     "Address": "10.30.101.47"
        |   },
        |   "Service": {
        |     "ID": "registrator.ash-alabs04.sv.stw:loving_brattain:9000",
        |     "Service": "elixir-profile-enricher",
        |     "Tags": null,
        |     "Address": "",
        |     "Port": 32770
        |   },
        |   "Checks": [
        |     {
        |       "Node": "ash-alabs04.sv.stw",
        |       "CheckID": "serfHealth",
        |       "Name": "Serf Health Status",
        |       "Status": "passing",
        |       "Notes": "",
        |       "Output": "Agent alive and reachable",
        |       "ServiceID": "",
        |       "ServiceName": ""
        |     }
        |   ]
        | },
        | {
        |   "Node": {
        |     "Node": "ash-alabs05.sv.stw",
        |     "Address": "10.30.101.48"
        |   },
        |   "Service": {
        |     "ID": "registrator.ash-alabs05.sv.stw:tender_perlman:9000",
        |     "Service": "elixir-profile-enricher",
        |     "Tags": null,
        |     "Address": "",
        |     "Port": 32771
        |   },
        |   "Checks": [
        |     {
        |       "Node": "ash-alabs05.sv.stw",
        |       "CheckID": "serfHealth",
        |       "Name": "Serf Health Status",
        |       "Status": "passing",
        |       "Notes": "",
        |       "Output": "Agent alive and reachable",
        |       "ServiceID": "",
        |       "ServiceName": ""
        |     }
        |   ]
        | }
        |]
      """.stripMargin

  val requiredJson =
    """
      |[
      | {
      |   "Node": {
      |     "Address": "10.30.101.47"
      |   },
      |   "Service": {
      |     "Port": 32770
      |   },
      | },
      | {
      |   "Node": {
      |     "Address": "10.30.101.48"
      |   },
      |   "Service": {
      |     "Port": 32771
      |   },
      | }
      |]
    """.stripMargin

  "Json" should {
    "extract no nodes from an empty json" in {
      Json.extractNodes("[]") should be ('empty)
    }

    "extract no nodes from an empty String" in {
      Json.extractNodes("") should be ('empty)
    }

    "extract all the nodes from a full json" in {
      Json.extractNodes(fullJson) should contain allOf(Node("10.30.101.47", 32770), Node("10.30.101.48", 32771))
    }

    "extract all the nodes from a json with only the required fields" in {
      Json.extractNodes(requiredJson) should contain allOf(Node("10.30.101.47", 32770), Node("10.30.101.48", 32771))
    }
  }

}
