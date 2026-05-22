package com.hackathon.hunter.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hackathon.hunter.MainActivity
import com.hackathon.hunter.data.local.dao.HackathonDao
import com.hackathon.hunter.data.local.entity.HackathonEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var hackathonDao: HackathonDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // No registration endpoint specified in requirements, logging is sufficient.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data.isEmpty()) return

        val id = data["id"]?.toIntOrNull() ?: return
        val platform = data["platform"] ?: return
        val platformId = data["platform_id"] ?: ""
        val title = data["title"] ?: return
        val description = data["description"]
        val url = data["url"] ?: ""
        val rulesUrl = data["rules_url"]
        val prizeType = data["prize_type"] ?: "fiat"
        val prizeCurrency = data["prize_currency"] ?: "USD"
        val prizeValue = data["prize_value"]?.toDoubleOrNull() ?: 0.0
        val isOnline = data["is_online"]?.toBoolean() ?: false
        val startDate = data["start_date"]
        val endDate = data["end_date"]
        val isVietnamEligible = data["is_vietnam_eligible"]?.toBoolean() ?: true
        val reportCount = data["report_count"]?.toIntOrNull() ?: 0
        val createdAt = data["created_at"]

        serviceScope.launch {
            // 1. Fetch reported logs
            val reportedLogs = hackathonDao.getReportedLogs()

            // 2. Fetch filter settings from SharedPreferences
            val prefs = getSharedPreferences("hackathon_hunter_prefs", Context.MODE_PRIVATE)
            val minPrizeValue = prefs.getFloat("pref_min_prize_value", 0.0f).toDouble()
            val filterPrizeType = prefs.getString("pref_prize_type", "all") ?: "all"
            val vietnamOnly = prefs.getBoolean("pref_vietnam_only", true)
            val onlineOnly = prefs.getBoolean("pref_online_only", false)
            
            val activePlatforms = prefs.getStringSet(
                "pref_platforms",
                setOf("devpost", "devfolio", "hackerearth", "gitcoin", "dorahacks", "bewater")
            ) ?: emptySet()

            // 3. Evaluate criteria locally using helper
            val shouldNotify = FCMFilterEvaluator.shouldNotify(
                id = id,
                platform = platform,
                prizeType = prizeType,
                prizeValue = prizeValue,
                isOnline = isOnline,
                isVietnamEligible = isVietnamEligible,
                reportedLogs = reportedLogs,
                minPrizeValue = minPrizeValue,
                filterPrizeType = filterPrizeType,
                vietnamOnly = vietnamOnly,
                onlineOnly = onlineOnly,
                activePlatforms = activePlatforms
            )
            if (!shouldNotify) return@launch

            // 4. Save to Room database cache
            val entity = HackathonEntity(
                id = id,
                platform = platform,
                platformId = platformId,
                title = title,
                description = description,
                url = url,
                rulesUrl = rulesUrl,
                prizeType = prizeType,
                prizeCurrency = prizeCurrency,
                prizeValue = prizeValue,
                isOnline = isOnline,
                startDate = startDate,
                endDate = endDate,
                isVietnamEligible = isVietnamEligible,
                reportCount = reportCount,
                isReportedByUser = false,
                isBookmarked = false,
                createdAt = createdAt
            )
            hackathonDao.insertOrUpdate(listOf(entity))

            // 5. Trigger System notification
            showNotification(entity)
        }
    }

    private fun showNotification(hackathon: HackathonEntity) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "hackathon_hunter_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hackathon Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for matching hackathon opportunities"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("hackathon_id", hackathon.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            hackathon.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prizeDetails = "${hackathon.prizeValue} ${hackathon.prizeCurrency}"
        val format = if (hackathon.isOnline) "Online" else "In-Person"
        val eligibility = if (hackathon.isVietnamEligible) "🇻🇳 Việt Nam được tham gia" else "🚫 Không dành cho Việt Nam"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Cuộc thi mới: ${hackathon.title}")
            .setContentText("Giải thưởng: $prizeDetails | Lọc: $format | $eligibility")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Nền tảng: ${hackathon.platform.uppercase()}\n" +
                "Giải thưởng: $prizeDetails (${hackathon.prizeType.uppercase()})\n" +
                "Hình thức: $format\n" +
                "Độ hợp lệ: $eligibility\n\n" +
                (hackathon.description ?: "")
            ))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(hackathon.id, notification)
    }
}
