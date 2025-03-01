package com.media.mob.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.media.mob.Constants
import com.media.mob.R
import com.media.mob.helper.formatFileSize
import com.media.mob.helper.thread.runBackgroundThread
import com.media.mob.helper.thread.runMainThread
import com.media.mob.network.resource.ResourceLoader
import com.media.mob.platform.youLiangHui.helper.DownloadConfirmHelper
import com.media.mob.platform.youLiangHui.helper.bean.ApkInfo
import com.media.mob.widget.dialog.permission.MediaPermissionDialog

class MediaMobConfirmActivity : AppCompatActivity() {

    companion object {
        const val CONFIRM_APP_INFO_URL = "mob_confirm_app_info_url"
    }

    private var url: String? = null

    private var infoView: View? = null
    private var loadView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.mob_activity_confirm)

        url = intent.getStringExtra(CONFIRM_APP_INFO_URL)

        if (url.isNullOrEmpty()) {
            finish()
            return
        }

        initialView()
    }

    private fun initialView() {
        infoView = findViewById<RelativeLayout>(R.id.rl_confirm_info)
        loadView = findViewById<RelativeLayout>(R.id.rl_confirm_loading)

        infoView?.visibility = View.INVISIBLE
        loadView?.visibility = View.VISIBLE

        runBackgroundThread {
            DownloadConfirmHelper.requestDownloadApkInfo(url) {
                runMainThread {
                    refreshInfoView(it)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshInfoView(apkInfo: ApkInfo?) {
        if (apkInfo == null) {
            finish()
            return
        }

        loadView?.visibility = View.INVISIBLE
        infoView?.visibility = View.VISIBLE

        val icon: ImageView? = infoView?.findViewById(R.id.iv_confirm_icon)

        ResourceLoader.loadResource({ result ->
            if (result != null) {
                runMainThread {
                    val bitmap = BitmapFactory.decodeByteArray(result, 0, result.size)
                    icon?.setImageBitmap(bitmap)
                }
            }
        }, apkInfo.icon)

        val close: ImageView? = infoView?.findViewById(R.id.iv_confirm_close)
        close?.setOnClickListener {
            DownloadConfirmHelper.downloadConfirmCallBack?.onCancel()
            finish()
        }

        val title: TextView? = infoView?.findViewById(R.id.tv_confirm_name)
        title?.text = apkInfo.appName

        val company: TextView? = infoView?.findViewById(R.id.tv_confirm_company)
        company?.text = apkInfo.authorName

        val information: TextView? = infoView?.findViewById(R.id.tv_confirm_info)
        information?.text = "版本: ${apkInfo.versionName}  大小: ${apkInfo.fileSize.formatFileSize()}"

        val agreement: TextView? = infoView?.findViewById(R.id.tv_confirm_agreement)
        agreement?.setOnClickListener {
            val intent = Intent(Constants.application, MediaMobWebActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(MediaMobWebActivity.WEB_URL, apkInfo.agreement)
            startActivity(intent)
        }

        val permissions = DownloadConfirmHelper.analysisPermissionList(apkInfo.permissions)

        val permission: TextView? = infoView?.findViewById(R.id.tv_confirm_permission)

        if (permissions.isNullOrEmpty()) {
            permission?.visibility = View.GONE
        } else {
            permission?.visibility = View.VISIBLE

            permission?.setOnClickListener {
                MediaPermissionDialog(this, permissions).show()
            }
        }

        val confirm: Button? = infoView?.findViewById(R.id.bu_confirm_action)

        confirm?.setOnClickListener {
            DownloadConfirmHelper.downloadConfirmCallBack?.onConfirm()
            finish()
        }
    }

    override fun onBackPressed() {
        DownloadConfirmHelper.downloadConfirmCallBack?.onCancel()
        super.onBackPressed()
    }
}