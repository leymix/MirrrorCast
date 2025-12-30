package com.mirrorcast.data.update

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * GitHub Releases API service
 */
interface GitHubApiService {
    /**
     * Get latest release for a repository
     * @param owner Repository owner (username or organization)
     * @param repo Repository name
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}

