package com.dabee.tpquickplacebykakaosearchapi.activities

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this,"e09c24c3cdb65dd57a484ff0812f078c")
    }
}