package com.example.videowallpaper

import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {
        private var player: MediaPlayer? = null
        private var holder: SurfaceHolder? = null
        private var loadedUri: String? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            this.holder = holder
            start()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (!visible) { player?.pause(); return }
            val prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
            if (prefs.getString(MainActivity.KEY_URI, null) != loadedUri) start()
            else {
                applyVolume()
                player?.start()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            release()
            this.holder = null
        }

        override fun onDestroy() { release() }

        private fun start() {
            release()
            val h = holder ?: return
            val uriText = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                .getString(MainActivity.KEY_URI, null) ?: return
            loadedUri = uriText
            try {
                player = MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.parse(uriText))
                    setSurface(h.surface)
                    isLooping = true
                    setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    setOnPreparedListener { applyVolume(); if (isVisible) it.start() }
                    prepareAsync()
                }
            } catch (e: Exception) {
                release() // vídeo apagado ou sem permissão: mantém a tela preta
            }
        }

        private fun applyVolume() {
            val mute = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                .getBoolean(MainActivity.KEY_MUTE, true)
            val v = if (mute) 0f else 1f
            player?.setVolume(v, v)
        }

        private fun release() {
            player?.release()
            player = null
        }
    }
}
