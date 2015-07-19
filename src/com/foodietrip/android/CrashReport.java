package com.foodietrip.android;

import org.acra.annotation.ReportsCrashes;
import org.acra.*;

import android.app.Application;
@ReportsCrashes(formKey = "",
                formUri = "http://proposal.twbbs.org/~proposal/api/index.php/crashreport/submit",
                formUriBasicAuthLogin = "WeLoveXiaoMao",
                formUriBasicAuthPassword = "kGiMqjLmc89dZ",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.DIALOG,
                resToastText = R.string.crash_toast_text,
                resDialogText = R.string.crash_dialog_text,
                resDialogIcon = R.drawable.ic_launcher,
                resDialogTitle = R.string.crash_dialog_title,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
                resDialogOkToast = R.string.crash_ok_toast)
public class CrashReport extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
	    ACRA.init(this);
	}
}
