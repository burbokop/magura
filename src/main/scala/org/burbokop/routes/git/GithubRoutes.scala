package org.burbokop.routes.git

import org.burbokop.utils.SttpUtils
import org.burbokop.models.git.{GithubBranch, GithubRelease}
import sttp.client3.{HttpURLConnectionBackend, UriContext, asByteArray, asString, basicRequest}

object GithubRoutes {
  def getRepository(user: String, repo: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo")
      .response(asString)
      .send(HttpURLConnectionBackend())
  }

  def getBranch(user: String, repo: String, branch: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo/branches/$branch")
      .response(SttpUtils.as[GithubBranch])
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

  def getRepositoryReleases(user: String, repo: String) = {
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri"https://api.github.com/repos/$user/$repo/releases")
      .response(SttpUtils.as[List[GithubRelease]])
      .send(HttpURLConnectionBackend())
  }
}
