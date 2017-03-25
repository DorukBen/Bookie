package com.karambit.bookie.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.karambit.bookie.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orcan on 3/23/17.
 */

public class IntentHelper {
    public static void openEmailClient(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"));
        PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(emailIntent, 0);
        if (resolveInfos.size() > 0) {
            ResolveInfo info = resolveInfos.get(0);
            // First create an intent with only the package name of the first registered email app
            // and build a picked based on it
            Intent intentChooser = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName);
            Intent openInChooser = Intent.createChooser(intentChooser, context.getString(R.string.choose_email_app));

            // Then create a list of LabeledIntent for the rest of the registered email apps
            List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
            for (int i = 1; i < resolveInfos.size(); i++) {
                // Extract the label and repackage it in a LabeledIntent
                info = resolveInfos.get(i);
                String packageName = info.activityInfo.packageName;
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                intentList.add(new LabeledIntent(intent, packageName, info.loadLabel(packageManager), info.icon));
            }

            LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
            // Add the rest of the email apps to the picker selection
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
            context.startActivity(openInChooser);
        }
    }
}
