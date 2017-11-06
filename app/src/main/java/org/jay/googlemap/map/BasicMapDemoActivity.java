package org.jay.googlemap.map;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jay.googlemap.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.jay.googlemap.R.id.map;

public class BasicMapDemoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static int duration = 5000;
    @BindView(R.id.btn)
    Button mBtn;
    @BindView(R.id.location)
    Button mLocation;
    private List<Marker> markers = new ArrayList<Marker>();
    public GoogleMap mMap;
    private LatLng mFrom;
    private Marker mMarker01;
    private Marker mMarker02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map_demo);
        ButterKnife.bind(this);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        supportMapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setPadding(0, 200, 0, 0);
        // Add a marker in qingdao and move the camera 青岛 北纬36°.0′ 东经120°.3′
        LatLng sydney = new LatLng(36, -80);
        mMarker02 = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in QingDao"));

//        LatLng sydney1 = new LatLng(36, 0);
//        mMarker04 = mMap.addMarker(new MarkerOptions().position(sydney1).title("Marker in QingDao"));

        mFrom = new LatLng(36, 150);

        float track = 235;
        MarkerOptions mFromMarkerOptions = new MarkerOptions()
                .position(mFrom)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .rotation(track)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        mMarker01 = mMap.addMarker(mFromMarkerOptions);

        CameraPosition pos = CameraPosition
                .builder(mMap.getCameraPosition())
                .bearing(track)
                .target(mMarker01.getPosition())
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

//        mTo = new LatLng(36, 50);
//        mMarker03 = mMap.addMarker(new MarkerOptions().position(mTo).title("Marker in QingDao"));

        markers.add(mMarker01);
        markers.add(mMarker02);


    }

    @OnClick(R.id.location)
    public void onLocationViewClicked() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mMarker01.getPosition()));

    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
//        animateMarker(mMarker01, mMarker02.getPosition());
        animateMarkerToGB(mMarker01, mMarker02.getPosition(), new LatLngInterpolator.LinearFixed());
//        animateMarkerToICS(mMarker01, mMarker02.getPosition(), new LatLngInterpolator.LinearFixed());
    }


    void animateMarkerToGB(final Marker marker, final LatLng endP, final LatLngInterpolator latLngInterpolator) {
        final Handler handler = new Handler();
        final LatLng startP = marker.getPosition();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();
        final float durationInMs = duration;
        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
                marker.setPosition(latLngInterpolator.interpolate(v, startP, endP));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 1);
                }else{
                    Log.d("jay", "run: []= wanle");
                }
            }
        });
    }


    void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startP = marker.getPosition();
        final LatLng endP = finalPosition;
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                final LatLng newPosition = latLngInterpolator.interpolate(v, startP, endP);
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLng(newPosition));
            }
        });
        animator.start();
    }


    //
    public void animateMarker(final Marker marker, final LatLng destination) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = destination;
            final int[] index = {0};
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(duration); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        final LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        index[0]++;
                        Log.d("jay", "onAnimationUpdate: [animation]=" + newPosition + index[0]);
//                        if (index[0] % 5 == 0) {
//                            marker.setPosition(newPosition);
//                            mMap.moveCamera(
//                                    CameraUpdateFactory.newLatLng(newPosition));
//                        }

                        marker.setPosition(newPosition);
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLng(newPosition));
//

                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }
//
//
//    private interface LatLngInterpolator {
//        LatLng interpolate(float fraction, LatLng a, LatLng b);
//
//        class LinearFixed implements LatLngInterpolator {
//            @Override
//            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
//                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
//                double lngDelta = b.longitude - a.longitude;
//                // Take the shortest path across the 180th meridian.
//                if (Math.abs(lngDelta) > 180) {
//                    lngDelta -= Math.signum(lngDelta) * 360;
//                }
//                double lng = lngDelta * fraction + a.longitude;
//                return new LatLng(lat, lng);
//            }
//        }
//    }


    /**
     * 经纬度用南纬是负，北纬是正，东经是正，西经是负；
     *
     东经正数，西经为负数。
     经度是地球上一个地点离一根被称为本初子午线的南北方向走线以东或以西的度数。本初子午线的经度是0°，地球上其它地点的经度是向东到180°或向西到180°。
     不像纬度有赤道作为自然的起点，经度没有自然的起点，做为本初子午线的那条线是人选出来的。
     英国的制图学家使用经过伦敦格林尼治天文台的子午线作为起点，过去其它国家或人也使用过其它的子午线做起点，比如罗马、哥本哈根、耶路撒冷、圣彼德堡、比萨、巴黎和费城等。
     在1884年的国际本初子午线大会上格林尼治的子午线被正式定为经度的起点。东经180°即西经180°，约等同于国际换日线，国际换日线的两边，日期相差一日。　　
     经度是指通过某地的经线面与本初子午面所成的二面角。
     在本初子午线以东的经度叫东经，在本初子午线以西的叫西经。
     东经用“E”表示，西经用“W”表示。　　
     经度的每一度被分为60分，每一分被分为60秒。
     一个经度因此一般看上去是这样的：东经23°27′ 30"或西经23°27′ 30"。
     更精确的经度位置中秒被表示为分的小数，比如：东经23°27.500′，但
     也有使用度和它的小数的：东经23.45833°。有时西经被写做负数：-23.45833°。但偶尔也有人把东经写为负　　

     北纬为正数，南纬为负数。　　
     纬度 是指某点与地球球心的连线和地球赤道面所成的线面角，其数值在0至90度之间。
     位于赤道以北的点的纬度叫北纬，记为N；位于赤道以南的点的纬度称南纬，记为S。　　
     纬度数值在0至30度之间的地区称为低纬度地区；
     纬度数值在30至60度之间的地区称为中纬度地区；
     纬度数值在60至90度之间的地区称为高纬度地区。　　
     赤道、南回归线、北回归线、南极圈和北极圈是特殊的纬线。数，但这相当不常规。
     */
}
