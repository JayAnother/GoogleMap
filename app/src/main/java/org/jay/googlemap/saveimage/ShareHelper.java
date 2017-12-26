package org.jay.googlemap.saveimage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 2017/11/2.
 */

public class ShareHelper {

    public static final String PACKAGENAME_MMS = "com.android.mms";

    public static final String PACKAGENAME_FACEBOOK = "com.facebook.katana";

    public static final String PACKAGENAME_INSTAGRAM = "com.instagram.android";

    public static final String PACKAGENAME_EMAIL = "com.android.email";

    public static final String PACKAGENAME_TWITTER = "com.twitter.android";

    public static final String INSTAGRAM = "Instagram";

    public static final String FACEBOOK = "Facebook";

    public static final String TWITTER = "Twitter";

    public static final String EMAIL = "Email";

    public static final String MESSAGE = "Message";

    private Activity activity;

    private String resolveInfoActivityInfoName = "";

    private String title = "";

    private String body = "";

    public ShareHelper(Activity activity) {
        this.activity = activity;
    }

    //根据包名获取要分享的Intent
    public void getSomeIntent(String packageNameStr, Uri uri, String title, String body) {
        this.title = title;
        this.body = body;

        //获取可分享的Intent(一个包名可能有多项分享)
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(share, 0);
        List<LabeledIntent> labeledIntents = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (packageName.contains(packageNameStr)) {
                resolveInfoActivityInfoName = resolveInfo.activityInfo.name;
                Intent intent = new Intent();
                if (packageName.contains(PACKAGENAME_EMAIL)
                    || packageName.contains(PACKAGENAME_MMS)
                    || packageName.contains(PACKAGENAME_TWITTER)) {
                    needBodyText(intent);
                }
                intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                intent.setPackage(packageName);
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("image/*");
                LabeledIntent labeledIntent = new LabeledIntent(intent, packageName,
                    resolveInfo.loadLabel(packageManager),
                    resolveInfo.getIconResource());
                labeledIntents.add(labeledIntent);
            }
        }
        //大于1个显示 LabeledIntent,等于1个直接执行跳转
        if (labeledIntents.size() > 1) {
            Intent openInChooser = Intent.createChooser(labeledIntents.remove(0), "Share");
            LabeledIntent[] extraIntent = labeledIntents
                .toArray(new LabeledIntent[labeledIntents.size()]);
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntent);
            activity.startActivity(openInChooser);
        } else if (labeledIntents.size() == 1) {
            Intent intent = new Intent();
            String packName = labeledIntents.get(0).getPackage();
            intent.setComponent(new ComponentName(packName, resolveInfoActivityInfoName));
            intent.setPackage(packName);
            if (packName.contains(PACKAGENAME_EMAIL)
                || packName.contains(PACKAGENAME_MMS)
                || packName.contains(PACKAGENAME_TWITTER)) {
                needBodyText(intent);
            }
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setType("image/*");
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "mei you", Toast.LENGTH_SHORT).show();
            if (PACKAGENAME_INSTAGRAM.equals(packageNameStr)) {
                showDialog(INSTAGRAM);
            } else if (PACKAGENAME_FACEBOOK.equals(packageNameStr)) {
                showDialog(FACEBOOK);
            } else if (PACKAGENAME_TWITTER.equals(packageNameStr)) {
                showDialog(TWITTER);
            } else if (PACKAGENAME_EMAIL.equals(packageNameStr)) {
                showDialog(EMAIL);
            } else if (PACKAGENAME_MMS.equals(packageNameStr)) {
                showDialog(MESSAGE);
            }

        }
    }

    private AlertDialog mAlertDialog;

    private void showDialog(String app) {
    }

    private void needBodyText(Intent intent) {
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, body);
    }

}
