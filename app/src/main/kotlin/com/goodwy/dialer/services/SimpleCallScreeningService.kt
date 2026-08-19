package com.goodwy.dialer.services

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import com.goodwy.commons.extensions.baseConfig
import com.goodwy.commons.extensions.getMyContactsCursor
import com.goodwy.commons.extensions.isNumberBlocked
import com.goodwy.commons.helpers.BLOCKING_TYPE_REJECT
import com.goodwy.commons.helpers.BLOCKING_TYPE_SILENCE
import com.goodwy.commons.helpers.ContactLookupResult
import com.goodwy.commons.helpers.SimpleContactsHelper
import com.goodwy.commons.helpers.isQPlus
import com.goodwy.dialer.activities.OverlayCallActivity
import com.goodwy.dialer.extensions.config
import com.goodwy.dialer.models.Events
import org.greenrobot.eventbus.EventBus

class SimpleCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart

        if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
            showOverlay(number)
        }

        when {
            !baseConfig.blockingEnabled -> {
                respondToCall(callDetails, isBlocked = false)
            }

            number != null && isNumberBlocked(number) && baseConfig.blockingEnabled -> {
                if (baseConfig.doNotBlockContactsAndRecent) {
                    if (number in config.recentOutgoingNumbers) respondToCall(callDetails, isBlocked = false)
                    else {
                        val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
                        val result = SimpleContactsHelper(this).existsSync(number, privateCursor)
                        respondToCall(callDetails, isBlocked = result == ContactLookupResult.NotFound)
                    }
                } else {
                    respondToCall(callDetails, isBlocked = true)
                }
            }

            number != null && baseConfig.blockUnknownNumbers && baseConfig.blockingEnabled -> {
                if (number in config.recentOutgoingNumbers) respondToCall(callDetails, isBlocked = false)
                else {
                    val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
                    val result = SimpleContactsHelper(this).existsSync(number, privateCursor)
                    respondToCall(callDetails, isBlocked = result == ContactLookupResult.NotFound)
                }
            }

            number == null && baseConfig.blockHiddenNumbers && baseConfig.blockingEnabled -> {
                respondToCall(callDetails, isBlocked = true)
            }

            else -> {
                respondToCall(callDetails, isBlocked = false)
            }
        }
    }

    private fun showOverlay(number: String?) {
        val intent = Intent(this, OverlayCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("caller_number", number)
        }
        startActivity(intent)
    }

    private fun respondToCall(callDetails: Call.Details, isBlocked: Boolean) {
        val response = if (isBlocked) {
            if (isQPlus() && baseConfig.blockingType == BLOCKING_TYPE_SILENCE) {
                CallResponse.Builder()
                    .setSilenceCall(true)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(baseConfig.blockingType == BLOCKING_TYPE_REJECT)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build()
            }
        } else {
            CallResponse.Builder()
                .build()
        }

        respondToCall(callDetails, response)

        if (isBlocked) EventBus.getDefault().post(Events.RefreshCallLog)
    }
}
