package com.example.videowallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    companion object {
        const val PREFS = "wallpaper"
        const val KEY_URI = "video_uri"
        const val KEY_MUTE = "mute"
        private const val PICK = 1
    }

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val pad = (24 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        root.addView(TextView(this).apply {
            text = "Vídeo Wallpaper"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        })
        status = TextView(this).apply { setPadding(0, pad / 2, 0, pad) }
        root.addView(status)

        root.addView(Button(this).apply {
            text = "Escolher vídeo"
            setOnClickListener {
                startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "video/*"
                        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, PICK
                )
            }
        })
        root.addView(Switch(this).apply {
            text = "Tocar com som"
            isChecked = !prefs.getBoolean(KEY_MUTE, true)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, pad / 2, 0, pad / 2)
            setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean(KEY_MUTE, !on).apply() }
        })
        root.addView(Button(this).apply {
            text = "Aplicar como wallpaper"
            setOnClickListener {
                if (prefs.getString(KEY_URI, null) == null) {
                    Toast.makeText(context, "Escolha um vídeo primeiro.", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(context, VideoWallpaperService::class.java)
                    ))
                }
            }
        })
        setContentView(root)
        refreshStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri = data?.data ?: return
        if (requestCode != PICK || resultCode != RESULT_OK) return
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URI, uri.toString()).apply()
        refreshStatus()
    }

    private fun refreshStatus() {
        val has = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_URI, null) != null
        status.text = if (has) "Vídeo escolhido. Toque em Aplicar para ativar." else "Nenhum vídeo escolhido."
    }
}
