package org.burbokop.magura.routes.git

import org.burbokop.magura.utils.SttpUtils
import org.burbokop.magura.models.git.{GithubBranch, GithubRelease}
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
    val uri = uri"https://api.github.com/repos/$user/$repo/branches/$branch"
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri)
      .response(SttpUtils.asThrowable[GithubBranch](uri))
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
    val uri = uri"https://api.github.com/repos/$user/$repo/releases"
    basicRequest
      .header("Accept", "application/vnd.github.v3+json")
      .get(uri)
      .response(SttpUtils.asThrowable[List[GithubRelease]](uri))
      .send(HttpURLConnectionBackend())
  }
}
