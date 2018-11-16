package me.jfenn.sunrisesunsetviewsample;

import android.os.Bundle;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import me.jfenn.sunrisesunsetview.SunriseSunsetView;

public class MainActivity extends AppCompatActivity implements SunriseSunsetView.SunriseListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SunriseSunsetView view = findViewById(R.id.sunView);
        view.setListener(this);
    }

    @Override
    public void onSunriseChanged(SunriseSunsetView view, long sunriseMillis) {
        Toast.makeText(this, "Sunrise set to " + new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(sunriseMillis)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSunsetChanged(SunriseSunsetView view, long sunsetMillis) {
        Toast.makeText(this, "Sunset set to " + new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(sunsetMillis)), Toast.LENGTH_SHORT).show();
    }
}
