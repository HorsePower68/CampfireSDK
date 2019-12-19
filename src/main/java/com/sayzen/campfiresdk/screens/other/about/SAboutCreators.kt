package com.sayzen.campfiresdk.screens.other.about

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.widgets.WidgetMenu

class SAboutCreators : Screen(R.layout.screen_other_abount_creators){

    private val vCopyLink:View = findViewById(R.id.vCopyLink)

    private val vMoreZeon: View = findViewById(R.id.vMoreZeon)
    private val vMoreSaynok: View = findViewById(R.id.vMoreSaynok)
    private val vMoreEgor: View = findViewById(R.id.vMoreEgor)
    private val vMoreTurbo: View = findViewById(R.id.vMoreTurbo)

    private val vPhotoZeon: ViewAvatarTitle = findViewById(R.id.vPhotoZeon)
    private val vPhotoSaynok: ViewAvatarTitle = findViewById(R.id.vPhotoSaynok)
    private val vPhotoEgor: ViewAvatarTitle = findViewById(R.id.vPhotoEgor)
    private val vPhotoTurbo: ViewAvatarTitle = findViewById(R.id.vPhotoTurbo)


    init {
        vMoreZeon.setOnClickListener {
            WidgetMenu()
                    .add(R.string.app_campfire){_, _ -> SProfile.instance(1, Navigator.TO)  }
                    .add(R.string.app_email){_, _ -> ToolsIntent.startMail("zeooon@ya.ru")  }
                    .add(R.string.app_vkontakte){_, _ -> ControllerLinks.openLink("https://vk.com/zeooon")   }
                    .asSheetShow()
        }

        vMoreSaynok.setOnClickListener {
            WidgetMenu()
                    .add(R.string.app_campfire){_, _ -> SProfile.instance(2720, Navigator.TO)  }
                    .add(R.string.app_email){_, _ -> ToolsIntent.startMail("saynokdeveloper@gmail.com")  }
                    .add(R.string.app_vkontakte){_, _ -> ControllerLinks.openLink("https://vk.com/saynok")   }
                    .asSheetShow()
        }

        vMoreEgor.setOnClickListener {
            WidgetMenu()
                    .add(R.string.app_campfire){_, _ -> SProfile.instance(9447, Navigator.TO)  }
                    .add(R.string.app_email){_, _ -> ToolsIntent.startMail("georgepro036@gmail.com")  }
                    .add(R.string.app_vkontakte){_, _ -> ControllerLinks.openLink("https://vk.com/id216069359")   }
                    .asSheetShow()
        }

        vMoreTurbo.setOnClickListener {
            WidgetMenu()
                    .add(R.string.app_campfire){_, _ -> SProfile.instance(8083, Navigator.TO)  }
                    .add(R.string.app_email){_, _ -> ToolsIntent.startMail("turboRO99@gmail.com")  }
                    .add(R.string.app_vkontakte){_, _ -> ControllerLinks.openLink("https://vk.com/turboa99")   }
                    .asSheetShow()
        }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_CREATORS.asWeb())
            ToolsToast.show(R.string.app_copied)
        }

        ImageLoader.load(API_RESOURCES.DEVELOPER_ZEON).into(vPhotoZeon)
        ImageLoader.load(API_RESOURCES.DEVELOPER_SAYNOK).into(vPhotoSaynok)
        ImageLoader.load(API_RESOURCES.DEVELOPER_EGOR).into(vPhotoEgor)
        ImageLoader.load(API_RESOURCES.DEVELOPER_TURBO).into(vPhotoTurbo)
    }

}