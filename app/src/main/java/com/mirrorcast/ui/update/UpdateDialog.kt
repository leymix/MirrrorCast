package com.mirrorcast.ui.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mirrorcast.data.update.UpdateInfo
import java.io.File

/**
 * Update dialog that shows when a new version is available
 */
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Yeni Güncelleme Mevcut!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sürüm ${updateInfo.versionName} yayınlandı",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (updateInfo.releaseNotes.isNotEmpty()) {
                    Text(
                        text = "Değişiklikler:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = updateInfo.releaseNotes.take(500), // Limit to 500 chars
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Daha Sonra")
                    }
                    
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İndir ve Kur")
                    }
                }
            }
        }
    }
}

/**
 * Download and install APK
 */
fun downloadAndInstallApk(context: Context, downloadUrl: String, versionName: String) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri)
        
        // Set destination
        val fileName = "MirrorCast-${versionName}.apk"
        val destination = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        
        request.setDestinationUri(Uri.fromFile(destination))
        request.setTitle("MirrorCast Güncelleme")
        request.setDescription("Yeni sürüm indiriliyor...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)
        
        // Start download
        val downloadId = downloadManager.enqueue(request)
        
        // Open download manager to show progress
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse("content://downloads/my_downloads"),
            "vnd.android.document/directory"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback: Open browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * Install APK file
 */
fun installApk(context: Context, apkUri: Uri) {
    val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
    }
    
    context.startActivity(intent)
}

