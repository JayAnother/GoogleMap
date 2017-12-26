package org.jay.googlemap.map;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aerisweather.aeris.communication.AerisEngine;
import com.aerisweather.aeris.maps.AerisMapsEngine;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.jay.googlemap.DensityUtils;
import org.jay.googlemap.R;
import org.jay.googlemap.map.model.MyItem;
import org.jay.googlemap.map.model.Person;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.jay.googlemap.R.id.map;

public class BasicMapDemoActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        ClusterManager.OnClusterClickListener<Person>,
        ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterItemClickListener<Person>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    //    private static final String WEATHER_MAP_URL_FORMAT = "http://mw1.google.com/mw-planetary/lunar/lunarmaps_v1/clem_bw/%d/%d/%d.jpg";
    private static final String WEATHER_MAP_URL_FORMAT = "http://maps.aerisapi.com/BKbUpNcSpYy8n3mr9CHMn_lRy8R3daQ41ttKGxuwYjJ70mOJ2KdyWoeGZMFPiR/radar,satellite-global/%d/%d/%d/latest.png";
    private static int duration = 10000;
    @BindView(R.id.btn)
    Button mBtn;
    @BindView(R.id.location)
    Button mLocation;
    @BindView(R.id.iv_compass)
    ImageView mIvCompass;
    @BindView(R.id.reset)
    Button mReset;
    private List<Marker> markers = new ArrayList<Marker>();
    public GoogleMap mMap;
    private LatLng mFrom;
    private Marker mMarker01;
    private Marker mMarker02;
    private boolean mIsGestureMove;
    private ValueAnimator mValueAnimator;

    private ClusterManager<MyItem> mClusterManager1;
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);




    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterIconGenerator.setBackground(getDrawable(R.drawable.bg_transparent));
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            Bitmap bitmap= BitmapFactory.decodeResource(getResources(),person.profilePhoto);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, DensityUtils.dip2px(BasicMapDemoActivity.this,40), DensityUtils.dip2px(BasicMapDemoActivity.this,47), false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            Drawable drawable=null;
            for (Person p : cluster.getItems()) {
                 drawable = getResources().getDrawable(p.profilePhoto);
                break;
            }
            mClusterImageView.setImageDrawable(drawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map_demo);
        ButterKnife.bind(this);
        initAerisWeather();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        supportMapFragment.getMapAsync(this);
    }

    private void initAerisWeather() {
        AerisEngine.initWithKeys(this.getString(R.string.aerisapi_client_id), this.getString(R.string.aerisapi_client_secret), this);
        AerisMapsEngine.getInstance(this).getDefaultPointParameters().setLightningParameters("dt:-1", 500, null, null);

    }

    boolean flag = false;

    @OnClick(R.id.iv_compass)
    public void onCompassViewClicked() {

        if (!flag) {
            CameraPosition pos = CameraPosition.builder(mMap.getCameraPosition()).bearing(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
        } else {
            mMarker01.setRotation(0);
        }
        flag = !flag;

    }

    @Override
    public void onCameraMove() {
        float bearing = mMap.getCameraPosition().bearing;
        rotationCompass(360 - bearing);
    }

    private void rotationCompass(float bearing) {
        mIvCompass.setRotation(bearing);
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
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        // Add a marker in qingdao and move the camera 青岛 北纬36°.0′ 东经120°.3′
        LatLng sydney = new LatLng(36, 100);
        mMarker02 = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in QingDao"));

//        LatLng sydney1 = new LatLng(36, 0);
//        mMarker04 = mMap.addMarker(new MarkerOptions().position(sydney1).title("Marker in QingDao"));

        mFrom = new LatLng(36.1, 100.1);

        float track = 0;
        MarkerOptions mFromMarkerOptions = new MarkerOptions()
                .position(mFrom)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .rotation(track)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_plane));
        mMarker01 = mMap.addMarker(mFromMarkerOptions);

        CameraPosition pos = CameraPosition
                .builder(mMap.getCameraPosition())
                .bearing(track)
                .target(mMarker01.getPosition())
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        markers.add(mMarker01);
        markers.add(mMarker02);

//        addLiteOverlay();
//        addMarkers();





        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker01.getPosition(), 9.5f));


//        mClusterManager = new ClusterManager<MyItem>(this, mMap);
//        mMap.setOnCameraIdleListener(mClusterManager);
//
//        try {
//            readItems();
//        } catch (JSONException e) {
//            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
//        }
//        mClusterManager = new ClusterManager<Person>(this, mMap);
//        mClusterManager.setRenderer(new PersonRenderer());
//        mMap.setOnCameraIdleListener(mClusterManager);
//        mMap.setOnMarkerClickListener(mClusterManager);
//        mMap.setOnInfoWindowClickListener(mClusterManager);
//        mClusterManager.setOnClusterClickListener(this);
//        mClusterManager.setOnClusterInfoWindowClickListener(this);
//        mClusterManager.setOnClusterItemClickListener(this);
//        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
//
//        addPinCluster();

    }

    private void addPinCluster() {
        addItems();
        mClusterManager.cluster();
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Person person) {
        Toast.makeText(this, ""+person.name, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person person) {

    }


    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }

    private void addItems() {
        for (int i = 0; i < 10; i++) {
            mClusterManager.addItem(new Person(position(), "wiki"+i, R.drawable.ic_map_geography));
        }
    }











//    private void readItems() throws JSONException {
//        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
//        List<MyItem> items = new MyItemReader().read(inputStream);
//        mClusterManager.addItems(items);
//    }

    private final List<Marker> mMarkerRainbow = new ArrayList<Marker>();

    private void addMarkers() {
        int numMarkersInRainbow = 12;
        for (int i = 0; i < numMarkersInRainbow; i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            -30 + 10 * Math.sin(i * Math.PI / (numMarkersInRainbow - 1)),
                            135 - 10 * Math.cos(i * Math.PI / (numMarkersInRainbow - 1))))
                    .title("Marker " + i)
                    .icon(BitmapDescriptorFactory.defaultMarker(i * 360 / numMarkersInRainbow)));
            mMarkerRainbow.add(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "" + marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    private void addLiteOverlay() {
        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                String s = String.format(Locale.US, WEATHER_MAP_URL_FORMAT, zoom, x, y);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };

        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
    }

    @OnClick(R.id.location)
    public void onLocationViewClicked() {
        if(mMap==null) return;
        mIsGestureMove=false;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mMarker01.getPosition()));
//        mClusterManager.getClusterMarkerCollection().clear();
//        mClusterManager.getMarkerCollection().clear();

    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
//        addPinCluster();
        animateMarker(mMarker01, mMarker02.getPosition());
//        animateMarkerToGB(mMarker01, mMarker02.getPosition(), new LatLngInterpolator.LinearFixed());
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
                    handler.postDelayed(this, 16);
                } else {
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
            mValueAnimator = ValueAnimator.ofFloat(0, 1f);
            mValueAnimator.setDuration(duration);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        final LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        LatLng latLng=new LatLng(formatDouble(newPosition.latitude),formatDouble(newPosition.longitude));
                        index[0]++;
                        float bearing = computeRotation(v, 0, 0);
                            marker.setPosition(latLng);
                        Log.d("jay", ": [getPosition]="+ marker.getPosition().toString());
                            if (!mIsGestureMove) {
                                CameraPosition cameraPosition1 = new CameraPosition.Builder()
                                        .target(latLng).build();
                                mMap.moveCamera(CameraUpdateFactory
                                        .newCameraPosition(cameraPosition1));
                                Log.d("jay", "---------------: [getCameraPosition]="+ mMap.getCameraPosition().toString());
                            }

//                        marker.setPosition(newPosition);
//                        mMap.moveCamera(
//                                CameraUpdateFactory.newLatLng(newPosition));
//

                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            mValueAnimator.start();
        }
    }

    public static float computeRotation(float fraction, float startRotation, float endRotation) {
        float normalizeEnd = endRotation - startRotation; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + startRotation;
        return (result + 360) % 360;
    }
    public static double formatDouble(double d) {
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        ddf1.setMaximumFractionDigits(6);
        String s = ddf1.format(d);
        return Double.parseDouble(s);
    }

    @OnClick(R.id.reset)
    public void onResetViewClicked() {
        if(mValueAnimator!=null&&mValueAnimator.isRunning()){
            mValueAnimator.cancel();
        }
        mMarker01.setPosition(mFrom);
        mMap.moveCamera(
                CameraUpdateFactory.newLatLng(mFrom));
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == REASON_GESTURE) {
            mIsGestureMove = true;
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
