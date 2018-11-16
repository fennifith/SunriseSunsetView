package me.jfenn.sunrisesunsetviewsample;

import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import me.jfenn.sunrisesunsetview.SunriseSunsetView;

public class MainActivity extends AppCompatActivity implements SunriseSunsetView.SunriseListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SunriseSunsetView view = findViewById(R.id.sunView);
        view.setSunriseColor(ContextCompat.getColor(this, R.color.colorPrimary));
        view.setSunsetColor(0xFF424242);
        view.setListener(this);
    }

    @Override
    public void onSunriseChanged(SunriseSunsetView view, long sunriseMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(sunriseMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(sunriseMillis) % 60;
        Toast.makeText(this, String.format(Locale.getDefault(), "Sunrise set to %02d:%02d", hours, minutes), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSunsetChanged(SunriseSunsetView view, long sunsetMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(sunsetMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(sunsetMillis) % 60;
        Toast.makeText(this, String.format(Locale.getDefault(), "Sunrise set to %02d:%02d", hours, minutes), Toast.LENGTH_SHORT).show();
    }
}
