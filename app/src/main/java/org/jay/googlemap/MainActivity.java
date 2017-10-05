package org.jay.googlemap;

import org.jay.googlemap.module.DemoDetails;
import org.jay.googlemap.module.DemoDetailsList;
import org.jay.googlemap.widget.FeatureView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    AlertDialog dialog;

    @BindView(R.id.image)
    ImageView btn_settings;

    @BindView(R.id.list)
    ListView list;

    @BindView(R.id.empty)
    TextView mEmpty;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @OnClick(R.id.image)
    public void onViewClicked() {
        chooseLanguage();
    }

    /**
     * A custom array adapter that shows a {@link FeatureView} containing details about the demo.
     */
    private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {

        /**
         * @param demos An array containing the details of the demos to be displayed.
         */
        public CustomArrayAdapter(Context context, DemoDetails[] demos) {
            super(context, R.layout.feature, R.id.title, demos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }

            DemoDetails demo = getItem(position);

            featureView.setTitleId(demo.titleId);
            featureView.setDescriptionId(demo.descriptionId);

            Resources resources = getContext().getResources();
            String title = resources.getString(demo.titleId);
            String description = resources.getString(demo.descriptionId);
            featureView.setContentDescription(title + ". " + description);

            return featureView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ListAdapter adapter = new CustomArrayAdapter(this, DemoDetailsList.DEMOS);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setEmptyView(findViewById(R.id.empty));

        //读取SharedPreferences数据，初始化语言设置
        setLanguage();
    }

    private void chooseLanguage() {
        //点击设置按钮进入语言设置
        //创建单选框
        final AlertDialog.Builder builder = new
            AlertDialog.Builder(MainActivity.this);
        builder.setSingleChoiceItems(new String[]{"Auto", "简体中文"},
            getSharedPreferences("language", Context.MODE_PRIVATE).getInt("language", 0),
            new DialogInterface.OnClickListener() {
                //点击单选框某一项以后
                public void onClick(DialogInterface dialogInterface, int i) {

                    //将选中项存入SharedPreferences，以便重启应用后读取设置
                    SharedPreferences preferences = getSharedPreferences("language",
                        Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("language", i);
                    editor.apply();
                    dialog.dismiss();

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                                /* 重新在新的任务栈开启新应用
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent
                                .FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                android.os.Process.killProcess(android.os.Process.myPid()); */
                }
            });

        dialog = builder.create();
        dialog.show();
    }

    private void setLanguage() {

        //读取SharedPreferences数据，默认选中第一项
        SharedPreferences preferences = getSharedPreferences("language", Context.MODE_PRIVATE);
        int language = preferences.getInt("language", 0);

        //根据读取到的数据，进行设置
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        switch (language) {
            case 0:
                configuration.setLocale(Locale.getDefault());
                break;
            case 1:
                configuration.setLocale(Locale.CHINESE);
                break;
            default:
                break;
        }

        resources.updateConfiguration(configuration, displayMetrics);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DemoDetails demo = (DemoDetails) parent.getAdapter().getItem(position);
        startActivity(new Intent(this, demo.activityClass));
    }
}
