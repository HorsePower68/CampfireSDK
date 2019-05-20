package com.sayzen.campfiresdk

import android.app.Activity
import android.content.Intent
import com.dzen.campfire.api.models.notifications.Notification
import com.google.firebase.messaging.RemoteMessage
import com.sayzen.devsupandroidgoogle.GoogleNotifications
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsThreads

object ControllerNotifications {

    val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"

    private val groupId_app = ToolsNotifications.instanceGroup(1, R.string.app_name)
    private val chanelOther =
        ToolsNotifications.instanceChanel(3).setName(R.string.app_name).setGroupId(groupId_app).init()
    private var activityClass: Class<Activity>? = null
    private var logoWhite: Int = 0
    private var logoColored: Int = 0

    val TYPE_NOTIFICATIONS = 1
    val TYPE_CHAT = 2

    var token: String = ""

    fun init(activityClass: Class<Activity>, logoWhite:Int, logoColored:Int) {
        this.activityClass = activityClass
        this.logoWhite = logoWhite
        this.logoColored = logoColored
        GoogleNotifications.init({ token: String? -> onToken(token) }, { message: RemoteMessage -> onMessage(message) })
    }

    //
    //  Message
    //

    private fun onMessage(message: RemoteMessage) {

        info("ControllerNotifications onMessage $message")

        if (!message.data.containsKey("my_data")) return

        val notification = Notification.instance(Json(message.data["my_data"]!!))
        ToolsThreads.main { EventBus.post(EventNotification(notification)) }

        val parser = parser(notification)

        if (parser != null && parser.canShow()) {
            val text = parser.asString(false)
            info("ControllerNotifications text $text")
            if (text.isNotEmpty()) {

                val icon = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) logoWhite else logoColored
                val intent = Intent(SupAndroid.appContext, activityClass)
                val title = ToolsResources.s(R.string.app_name)
                val tag = tag(notification.id)

                intent.putExtra(EXTRA_NOTIFICATION, notification.json(true, Json()).toString())

                parser.post(icon, intent, text, title, tag)
            }
        }
    }

    fun tag(notificationId: Long) = "id_$notificationId"

    fun doAction(notificationJson: String): Boolean {
        val n = Notification.instance(Json(notificationJson))
        val nn = parser(n)
        if (nn != null) {
            nn.doAction()
            return true
        }
        return false
    }

    //
    //  Token
    //

    private fun onToken(token: String?) {
        ControllerNotifications.token = token ?: ""
    }

    //
    //  Notifications
    //

    fun parser(n: Notification): Parser? {
        return when (n) {
            else -> null
        }
    }

    abstract class Parser(open val n: Notification) {

        abstract fun post(icon: Int, intent: Intent, text: String, title: String, tag: String)

        abstract fun asString(html: Boolean): String

        abstract fun canShow(): Boolean

        abstract fun doAction()

        abstract fun getIcon(): Int

    }


}