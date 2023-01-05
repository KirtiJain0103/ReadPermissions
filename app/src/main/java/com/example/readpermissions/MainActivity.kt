package com.example.readpermissions

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.readpermissions.databinding.ActivityMainBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var permission : String
    var deniedWithNeverAskAgain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    onPermissionGranted()
                    Log.d("onCreate: ", "granted")
                } else {
                    if (deniedWithNeverAskAgain)
                        onPermissionDeniedWithDoNotAskAgain()
                    else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permission
                        )
                    ) {
                        deniedWithNeverAskAgain = true
                    }
                }

            }

        binding.downloadButton.setOnClickListener {
            permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            requestPermissionLauncher.launch(permission)

        }
    }




        private fun onPermissionDeniedWithDoNotAskAgain(){

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Allow permission to use this app Go to settings")
            builder.setPositiveButton("OK") { _, _ ->

                //Handling sdk version of <30

                intent = if(Build.VERSION.SDK_INT<30)
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                else
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)

                val uri = Uri.fromParts("package",this.packageName,null)
                intent.data = uri

                startActivity(intent)

            }
            builder.show()
        }

    private fun onPermissionGranted(){
        // Declaring a Bitmap local
        var mImage: Bitmap?

        // Declaring a webpath as a string
        val mWebPath = "https://media.geeksforgeeks.org/wp-content/uploads/20210224040124/JSBinCollaborativeJavaScriptDebugging6-300x160.png"


        // Declaring and initializing an Executor and a Handler
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        myExecutor.execute {
            mImage = mLoad(mWebPath)
            myHandler.post {
               binding.image.setImageBitmap(mImage)
                if(mImage!=null){
                    mSaveMediaToStorage(mImage)
                }
            }
        }

    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    // Function to convert string to URL
    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var os: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var directory = arrayOf<File>()
            directory = this.externalMediaDirs
            for (i in directory.indices) {
                if (directory[i]!!.name.contains(this.packageName)) {
                    val imagesDir  = directory[i]!!.absolutePath
                    val image = File(imagesDir,filename)
                    os = FileOutputStream(image)
                }
            }
        }

        os.also {   bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Saved to Android/media" , Toast.LENGTH_SHORT).show()
           }

    }


}

