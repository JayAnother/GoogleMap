package org.jay.googlemap.mapanimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import org.jay.googlemap.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private IMapController mapController;
    private Marker mMarker;

    private static List<GeoPoint> mPoints = new ArrayList<>();
    static {
        mPoints.add(new GeoPoint(48.011679, 37.857809));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        mMapView = findViewById(R.id.map);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);

        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.performClick();
                    mMapView.performClick();

                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();

                    IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels(x, y);

                    GeoPoint geoPoint = new GeoPoint(iGeoPoint.getLatitude(), iGeoPoint.getLongitude());

                    MarkerAnimation.animateMarkerToGB(mMapView, mMarker, geoPoint, new LatLngInterpolator.Linear());
                }

                return true;
            }
        });

        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        mapController = mMapView.getController();
        mapController.setZoom(20);
        GeoPoint startPoint = new GeoPoint(mPoints.get(0));
        mapController.setCenter(startPoint);

        mMarker = new Marker(mMapView);
        mMarker.setPosition(startPoint);
        mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMapView.getOverlays().add(mMarker);

        mMarker.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        mMarker.setTitle("Start point");
        mMapView.invalidate();
    }

}
