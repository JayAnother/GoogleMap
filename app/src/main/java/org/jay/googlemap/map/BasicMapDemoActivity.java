package org.jay.googlemap.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jay.googlemap.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BasicMapDemoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map_demo);
        SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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

        // Add a marker in qingdao and move the camera 青岛 北纬36°.0′ 东经120°.3′
        LatLng sydney = new LatLng(36, 120);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in QingDao"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.d("jay", "onPointerCaptureChanged: [hasCapture]="+hasCapture);
    }

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
