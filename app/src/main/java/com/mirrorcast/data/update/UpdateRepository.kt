package com.mirrorcast.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.mirrorcast.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository for checking app updates
 */
class UpdateRepository(
    private val context: Context,
    private val apiService: GitHubApiService
) {
    /**
     * Get current app version code
     */
    fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            BuildConfig.VERSION_CODE
        }
    }

    /**
     * Get current app version name
     */
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo: PackageInfo = context.packageManager
                .getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (e: PackageManager.NameNotFoundException) {
            BuildConfig.VERSION_NAME
        }
    }

    /**
     * Check for updates from GitHub Releases
     * Returns UpdateInfo if update is available, null otherwise
     */
    suspend fun checkForUpdate(): UpdateInfo? {
        return try {
            // Parse GitHub repository from BuildConfig
            val repoInfo = BuildConfig.GITHUB_REPO.split("/")
            if (repoInfo.size != 2) {
                return null // Invalid repository format
            }
            
            val owner = repoInfo[0]
            val repo = repoInfo[1]
            
            // Get latest release
            val latestRelease = apiService.getLatestRelease(owner, repo)
            
            // Find APK asset
            val apkAsset = latestRelease.assets.find { 
                it.name.endsWith(".apk", ignoreCase = true) && 
                it.name.contains("prod", ignoreCase = true) &&
                it.name.contains("release", ignoreCase = true)
            } ?: return null
            
            // Extract version code from release notes or tag name
            // Release notes format: "**Version Code:** 2" or similar
            // Tag format: "prod-v1.0.0-20240101-120000" or similar
            val versionName = extractVersionName(latestRelease.tagName, latestRelease.name)
            val versionCode = extractVersionCodeFromReleaseNotes(latestRelease.body) 
                ?: extractVersionCode(latestRelease.tagName, latestRelease.name)
            
            val currentVersionCode = getCurrentVersionCode()
            val currentVersionName = getCurrentVersionName()
            
            // Check if update is available
            // Compare version codes, but also check version names as fallback
            val hasUpdate = versionCode > currentVersionCode || 
                          (versionCode == currentVersionCode && versionName != currentVersionName)
            
            if (hasUpdate) {
                UpdateInfo(
                    versionCode = versionCode,
                    versionName = versionName,
                    downloadUrl = apkAsset.downloadUrl,
                    releaseNotes = latestRelease.body,
                    releaseName = latestRelease.name
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract version code from release notes
     * Looks for "Version Code: X" pattern in release notes
     */
    private fun extractVersionCodeFromReleaseNotes(releaseNotes: String): Int? {
        val pattern = """(?i)version\s*code\s*:?\s*(\d+)""".toRegex()
        val match = pattern.find(releaseNotes)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Extract version code from tag name or release name
     * Tries to parse version code from various formats
     * Tag format examples: "prod-v1.0.0-20240101-120000", "prod-v1.1-20240101-120000"
     */
    private fun extractVersionCode(tagName: String, releaseName: String): Int {
        // First, try to extract from build.gradle.kts versionCode if available in release notes
        // But we'll use a more reliable method: parse from version name and convert
        
        // Try to extract version name first
        val versionName = extractVersionName(tagName, releaseName)
        
        // Parse version name (e.g., "1.0.0", "1.1", "2.0.1")
        val versionParts = versionName.split(".").mapNotNull { it.toIntOrNull() }
        if (versionParts.isNotEmpty()) {
            val major = versionParts.getOrNull(0) ?: 0
            val minor = versionParts.getOrNull(1) ?: 0
            val patch = versionParts.getOrNull(2) ?: 0
            // Convert to version code: major * 10000 + minor * 100 + patch
            // This ensures version code increases with each release
            return major * 10000 + minor * 100 + patch
        }
        
        // Fallback: try to find version pattern in tag or release name
        val versionPattern = """v?(\d+)\.(\d+)\.?(\d*)""".toRegex()
        val match = versionPattern.find(tagName) ?: versionPattern.find(releaseName)
        if (match != null) {
            val major = match.groupValues[1].toIntOrNull() ?: 0
            val minor = match.groupValues[2].toIntOrNull() ?: 0
            val patch = match.groupValues[3].toIntOrNull() ?: 0
            return major * 10000 + minor * 100 + patch
        }
        
        // Last resort: return a high number to ensure update is detected
        // This should not happen if version naming is consistent
        return Int.MAX_VALUE
    }

    /**
     * Extract version name from tag name or release name
     */
    private fun extractVersionName(tagName: String, releaseName: String): String {
        val versionPattern = """v?(\d+\.\d+\.?\d*)""".toRegex()
        val match = versionPattern.find(tagName) ?: versionPattern.find(releaseName)
        return match?.value?.removePrefix("v") ?: "Unknown"
    }

    companion object {
        /**
         * Create Retrofit service for GitHub API
         */
        fun createApiService(): GitHubApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(GitHubApiService::class.java)
        }
    }
}

/**
 * Update information data class
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val releaseName: String
)

