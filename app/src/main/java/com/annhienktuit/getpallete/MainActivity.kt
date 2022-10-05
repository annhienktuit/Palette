package com.annhienktuit.getpallete

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.annhienktuit.getpallete.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissionAllowance()
        loadDefaultImage()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            allowFullScreen()
            pickImage(binding)
        } else {
            legacyPickImage(binding)
        }
    }

    private fun legacyPickImage(binding: ActivityMainBinding) {
        binding.btnPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun allowFullScreen() {
        val controller = window.insetsController
        controller?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsets.Type.statusBars())
    }

    private fun loadDefaultImage() {
        val bitmap = (binding.ivImage.drawable as BitmapDrawable).bitmap
        val builder = Palette.Builder(bitmap)
        builder.generate { palette ->
            if (palette != null) {
                try {
                    setButtonColor(palette)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setButtonColor(palette: Palette) {
        val dominantColor = palette.getDominantColor(Color.BLACK)
        val lightVibrantColor = palette.getLightVibrantColor(Color.BLACK)
        val vibrantColor = palette.getVibrantColor(Color.BLACK)
        val darkVibrant = palette.getDarkVibrantColor(Color.BLACK)
        val lightMuted = palette.getLightMutedColor(Color.BLACK)
        val darkMuted = palette.getDarkMutedColor(Color.BLACK)
        val muted = palette.getMutedColor(Color.BLACK)

        binding.btnDominant.setBackgroundColor(dominantColor)
        binding.btnLightVibrant.setBackgroundColor(lightVibrantColor)
        binding.btnVibrant.setBackgroundColor(vibrantColor)
        binding.btnDarkVibrant.setBackgroundColor(darkVibrant)
        binding.btnLightMuted.setBackgroundColor(lightMuted)
        binding.btnDarkMuted.setBackgroundColor(darkMuted)
        binding.btnMuted.setBackgroundColor(muted)

        palette.dominantSwatch?.titleTextColor?.let { binding.btnDominant.setTextColor(it) }
        palette.lightVibrantSwatch?.titleTextColor?.let { binding.btnLightVibrant.setTextColor(it) }
        palette.vibrantSwatch?.titleTextColor?.let { binding.btnVibrant.setTextColor(it) }
        palette.darkVibrantSwatch?.titleTextColor?.let { binding.btnDarkVibrant.setTextColor(it) }
        palette.lightMutedSwatch?.titleTextColor?.let { binding.btnLightMuted.setTextColor(it) }
        palette.darkMutedSwatch?.titleTextColor?.let { binding.btnDarkMuted.setTextColor(it) }
        palette.mutedSwatch?.titleTextColor?.let { binding.btnMuted.setTextColor(it) }
        binding.btnDominant.text = "Dominant(${getHex(dominantColor)})"
        binding.btnLightVibrant.text = "Light Vibrant(${getHex(lightVibrantColor)})"
        binding.btnVibrant.text = "Vibrant(${getHex(vibrantColor)})"
        binding.btnDarkVibrant.text = "Dark Vibrant(${getHex(darkVibrant)})"
        binding.btnLightMuted.text = "Light Muted(${getHex(lightMuted)})"
        binding.btnDarkMuted.text = "Dark Muted(${getHex(darkMuted)})"
        binding.btnMuted.text = "Muted(${getHex(muted)})"
        binding.background.setBackgroundColor(ColorUtils.blendARGB(muted, Color.WHITE, 0.5f))
    }

    private fun getHex(@ColorInt color: Int): String {
        if (color == Color.BLACK) return "Cannot GET or Black"
        return String.format("#%06X", 0xFFFFFF and color)
    }

    private fun pickImage(binding: ActivityMainBinding) {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                getImageAndShow(uri)
            }
        binding.btnPicker.setOnClickListener {
            pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("GetImageBelow11", data!!.data.toString())
        if (resultCode == RESULT_OK) {
            Log.i("GetImageBelow11", data.data.toString())
            getImageAndShow(data.data)
        } else {
            Toast.makeText(
                this@MainActivity,
                "Error while getting image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getImageAndShow(uri: Uri?) {
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            Glide.with(this).load(uri).into(binding.ivImage)
            Glide.with(this).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val builder = Palette.Builder(resource)
                    builder.generate { palette ->
                        if (palette != null) {
                            try {
                                setButtonColor(palette)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error on extracting palette",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun checkPermissionAllowance() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                2
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                3
            )

        }
    }

    companion object {
        private const val PICK_IMAGE_CODE = 1
    }
}