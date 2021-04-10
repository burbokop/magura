package org.burbokop.routes

import sttp.client3.{HttpURLConnectionBackend, UriContext, asByteArray, asString, basicRequest}

object GithubRoutes {
  def getRepository(user: String, repo: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo")
      .response(asString)
      .send(HttpURLConnectionBackend())
  }

  def getRepositoryContent(user: String, repo: String, path: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo/contents/$path")
      .response(asString)
      .send(HttpURLConnectionBackend())
  }

  def downloadRepositoryZip(user: String, repo: String, ref: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo/zipball/$ref")
      .response(asByteArray)
      .send(HttpURLConnectionBackend())
  }

}
