package com.vectormax.mobile.launcher

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import java.io.File
import android.widget.Toast
import android.app.DownloadManager
import android.content.*
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import android.content.pm.PackageManager
import android.os.StrictMode
import androidx.core.content.FileProvider
import android.database.Cursor
import android.os.Environment
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi


class MainActivity : AppCompatActivity() {
    val defaultFileName:String="kuali.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        setContentView(R.layout.activity_main)
        registerReceiver(onDownloadComplete,IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        if(!runAppIfInstalled()){
            beginDownload()
        }
    }

    private fun runAppIfInstalled(): Boolean {

        val isInstalled = isPackageInstalled("com.vectormax.tvinput.kuali", getPackageManager())
        Log.d("shimi", "in runAppIfInstalled isInstalled = " + isInstalled)
        if(isInstalled){
            //val pm = getPackageManager()
            //val launchIntent = pm.getLaunchIntentForPackage("com.vectormax.tvinput.kuali")
            val launchIntent  = Intent(Intent.ACTION_MAIN);
                launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                val cn = ComponentName("com.vectormax.tvinput.kuali","com.vectormax.tvinput.ui.MainActivity");
                launchIntent.setComponent(cn);

            Log.d("shimi", "in isInstalled launchIntent = " + launchIntent.toString())
            startActivity(launchIntent)
            finish()
            return true
        }else {
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete);
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("shimi", "in onActivityResult requestCode  = "+requestCode+"  resultCode = "+resultCode)

        if (requestCode == 1234 && resultCode == Activity.RESULT_OK) {
            installAPK(getApkFile(defaultFileName))
        } else if (requestCode == 5555 ) {
            runAppIfInstalled()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
    }

    /**
     * Install APK using PackageInstaller
     * @param apkFile  File object of APK
     */
    private fun installAPK(apkFile: File) {//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
            }
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        startActivityForResult(intent, 5555);
        val fileSize = Integer.parseInt((apkFile.length() / 1024).toString())
        Log.d("shimi", "installAPK apkFile = "+apkFile.absoluteFile+"  fileSize = "+fileSize)
    }


    /**
     * Uninstall APK using PackageInstaller
     * @param apkPackageName
     */
    private fun uninstallAPK(apkPackageName: String) {
        val intent = Intent("android.intent.action.DELETE")
        intent.data = Uri.parse("package:$apkPackageName")
        startActivity(intent)
    }


    private fun beginDownload() = runWithPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE){

        val file = getApkFile( defaultFileName)
        //val file = File(getExternalFilesDir(null), "kuali-apk.apk")
        val fileSize = Integer.parseInt((file.length() / 1024).toString())
        if(file.exists() && fileSize > 0 ){
            Log.d("shimi", "in file.exists()  = "+file.isDirectory+"  "+file.name+"  fileSize = "+fileSize)
            installAPK(file)
        }else{
           /*
           Create a DownloadManager.Request with all the information necessary to start the download
           */
            val request =
                DownloadManager.Request(Uri.parse("https://nyc.vectormax.com:4061/config/app-kuali-release.apk"))
                    .setTitle("Downloading App")// Title of the Download Notification
                    .setDescription("Downloading kuali app")// Description of the Download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                    //.setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                    //.setRequiresCharging(false)// Set if charging is required to begin the download
                    .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                    .setAllowedOverRoaming(true)// Set if download is allowed on roaming network
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, defaultFileName)
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)// downloadIDenqueue puts the download request in the queue.
            Log.d("shimi", "in beginDownload else  = "+downloadID)
        }
    }

    private fun getApkFile(fileName:String): File {
        val path = Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_DOWNLOADS + File.separator + fileName
        return File(path)
    }


    private var downloadID: Long = 0
    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            Log.d("shimi", "in onReceive downloadID = "+downloadID +"   id = "+id)
            var fileName:String=""
            if (downloadID == id) {
                installAPK(getApkFile(defaultFileName))
//                val extras: Bundle = intent.getExtras()!!
//                val manager:DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                val query :DownloadManager.Query  =  DownloadManager.Query()
//                query.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
//                val c:Cursor = manager.query(query);
//                if (c.moveToFirst()) {
//                    var status:Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                        val downloadFileLocalUri:String=c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
//                        if (downloadFileLocalUri != null) {
//                            val mFile = File(Uri.parse(downloadFileLocalUri).path!!)
//                            fileName=mFile.name
//                        }
//                    }
//                }
//                c.close()
//                 progressBar.visibility = View.GONE
//                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
//                val file = getApkFile(fileName) //File(getExternalFilesDir(null), "kuali-apk.apk")
//                installAPK(file)
            }
        }
    }

    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private fun openDownloadedAttachment(context: Context, downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
                openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType)
            }
        }
        cursor.close()
    }

    /**
     * Used to open the downloaded attachment.
     *
     *
     * 1. Fire intent to open download file using external application.
     *
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private fun openDownloadedAttachment(
        context: Context,
        attachmentUri: Uri?,
        attachmentMimeType: String
    ) {
        var attachmentUri = attachmentUri
        if (attachmentUri != null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
                // FileUri - Convert it to contentUri.
                val file = File(attachmentUri.path!!)
                attachmentUri =
                    FileProvider.getUriForFile(this, "com.freshdesk.helpdesk.provider", file)
            }

            val openAttachmentIntent = Intent(Intent.ACTION_VIEW)
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType)
            openAttachmentIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.startActivity(openAttachmentIntent)
            } catch (e: ActivityNotFoundException) {

            }

        }
    }


    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }
}
