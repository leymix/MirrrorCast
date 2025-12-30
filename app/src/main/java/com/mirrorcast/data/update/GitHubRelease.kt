package com.mirrorcast.data.update

import com.google.gson.annotations.SerializedName

/**
 * GitHub Releases API response model
 */
data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("body")
    val body: String,
    
    @SerializedName("published_at")
    val publishedAt: String,
    
    @SerializedName("assets")
    val assets: List<ReleaseAsset>
)

data class ReleaseAsset(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("browser_download_url")
    val downloadUrl: String,
    
    @SerializedName("size")
    val size: Long
)

