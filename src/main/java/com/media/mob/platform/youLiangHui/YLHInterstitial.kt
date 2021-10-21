package com.media.mob.platform.youLiangHui

import com.media.mob.bean.request.MediaRequestParams
import com.media.mob.bean.request.MediaRequestResult
import com.media.mob.helper.logger.MobLogger
import com.media.mob.media.interstitial.IInterstitial
import com.media.mob.media.interstitial.InterstitialWrapper
import com.media.mob.platform.IPlatform
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener
import com.qq.e.comm.util.AdError

class YLHInterstitial : InterstitialWrapper() {

    private val classTarget = YLHInterstitial::class.java.simpleName

    /**
     * 广告平台名称
     */
    override val platformName: String = IPlatform.PLATFORM_YLH

    /**
     * 优量汇插屏广告对象
     */
    private var interstitialAD: UnifiedInterstitialAD? = null

    /**
     * 展示插屏广告
     */
    override fun show() {
        interstitialAD?.show()
    }

    /**
     * 广告销毁
     */
    override fun destroy() {
        interstitialAD?.destroy()
        interstitialAD = null
    }

    fun requestInterstitial(mediaRequestParams: MediaRequestParams<IInterstitial>) {
        interstitialAD = UnifiedInterstitialAD(
            mediaRequestParams.activity,
            mediaRequestParams.tacticsInfo.thirdSlotId,
            object : UnifiedInterstitialADListener {

                /**
                 * 插屏半屏广告加载失败
                 */
                override fun onNoAD(error: AdError?) {
                    MobLogger.e(
                        classTarget,
                        "优量汇插屏广告请求失败: Code=${error?.errorCode ?: -1}, Message=${error?.errorMsg ?: "Unknown"}"
                    )

                    mediaRequestParams.mediaPlatformLog.handleRequestFailed(
                        error?.errorCode ?: -1,
                        error?.errorMsg ?: "Unknown"
                    )

                    mediaRequestParams.mediaRequestResult.invoke(
                        MediaRequestResult(
                            null, 60006,
                            "优量汇插屏广告请求失败: Code=${error?.errorCode ?: -1}, Message=${error?.errorMsg ?: "Unknown"}"
                        )
                    )

                    destroy()
                }

                /**
                 * 插屏半屏广告加载完毕
                 */
                override fun onADReceive() {
                    MobLogger.e(classTarget, "优量汇插屏广告加载完毕")
                }

                /**
                 * 插屏半屏广告视频素材缓存完毕
                 */
                override fun onVideoCached() {
                    MobLogger.e(classTarget, "优量汇插屏广告视频素材缓存完毕")
                }

                /**
                 * 插屏半屏广告展开
                 */
                override fun onADOpened() {
                    MobLogger.e(classTarget, "优量汇插屏广告展开")
                }

                /**
                 * 插屏半屏广告曝光
                 */
                override fun onADExposure() {
                    MobLogger.e(classTarget, "优量汇插屏广告曝光")

                    invokeMediaShowListener()

                    reportMediaActionEvent("show", mediaRequestParams.tacticsInfo, mediaRequestParams.mediaRequestLog)
                }

                /**
                 * 插屏半屏广告点击
                 */
                override fun onADClicked() {
                    MobLogger.e(classTarget, "优量汇插屏广告点击")

                    invokeMediaClickListener()

                    reportMediaActionEvent("click", mediaRequestParams.tacticsInfo, mediaRequestParams.mediaRequestLog)
                }

                /**
                 * 插屏半屏广告点击离开应用
                 */
                override fun onADLeftApplication() {
                    MobLogger.e(classTarget, "优量汇插屏广告点击离开应用")
                }

                /**
                 * 插屏半屏广告关闭
                 */
                override fun onADClosed() {
                    MobLogger.e(classTarget, "优量汇插屏广告关闭")

                    invokeMediaCloseListener()
                }

                /**
                 * 插屏半屏广告渲染成功
                 */
                override fun onRenderSuccess() {
                    MobLogger.e(classTarget, "优量汇插屏广告渲染成功")

                    mediaRequestParams.mediaPlatformLog.handleRequestSucceed()

                    mediaRequestParams.mediaRequestResult.invoke(MediaRequestResult(this@YLHInterstitial))
                }

                /**
                 * 插屏半屏广告渲染失败
                 */
                override fun onRenderFail() {
                    MobLogger.e(classTarget, "优量汇插屏弹窗广告渲染失败")

                    mediaRequestParams.mediaPlatformLog.handleRequestFailed(-1, "优量汇插屏弹窗广告渲染失败")

                    mediaRequestParams.mediaRequestResult.invoke(MediaRequestResult(null, 60006, "优量汇插屏弹窗广告渲染失败"))

                    destroy()
                }
            })

        mediaRequestParams.mediaPlatformLog.insertRequestTime()

        interstitialAD?.loadAD()
    }
}