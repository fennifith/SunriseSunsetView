package me.jfenn.sunrisesunsetview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

import androidx.annotation.Nullable;
import me.jfenn.androidutils.anim.AnimatedFloat;

public class SunriseSunsetView extends View implements View.OnTouchListener {

    private static final long DAY_LENGTH = 86400000L;

    private Paint paint;
    private Paint sunsetPaint;
    private Paint linePaint;

    private AnimatedFloat dayStart;
    private AnimatedFloat dayEnd;

    private boolean movingStart;
    private boolean movingEnd;

    private SunriseListener listener;

    public SunriseSunsetView(Context context) {
        this(context, null, 0);
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        sunsetPaint = new Paint();
        sunsetPaint.setAntiAlias(true);
        sunsetPaint.setStyle(Paint.Style.FILL);
        sunsetPaint.setColor(Color.BLACK);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        linePaint.setAlpha(20);

        setOnTouchListener(this);
        setClickable(true);
        setFocusable(true);

        dayStart = new AnimatedFloat(0.333f);
        dayEnd = new AnimatedFloat(0.666f);
    }

    public void setDayStart(long dayStartMillis) {
        setDayStart(dayStartMillis, false);
    }

    public void setDayStart(long dayStartMillis, boolean animate) {
        dayStartMillis %= DAY_LENGTH;
        if (animate)
            dayStart.to((float) dayStartMillis / DAY_LENGTH);
        else dayStart.setCurrent((float) dayStartMillis / DAY_LENGTH);
    }

    public void setDayEnd(long dayEndMillis) {
        setDayEnd(dayEndMillis, false);
    }

    public void setDayEnd(long dayEndMillis, boolean animate) {
        dayEndMillis %= DAY_LENGTH;
        if (animate)
            dayEnd.to((float) dayEndMillis / DAY_LENGTH);
        else dayEnd.setCurrent((float) dayEndMillis / DAY_LENGTH);
    }

    public void setListener(SunriseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dayStart.next(true);
        dayEnd.next(true);

        float scaleX = canvas.getWidth() / 23f;
        float scaleY = canvas.getHeight() / 2f;
        float interval = dayStart.val() / 2;
        float interval2 = (1 - dayEnd.val() + dayStart.val()) / 2;
        float start = dayStart.val() - (1 - dayEnd.val() + dayStart.val());
        interval *= 24 * scaleX;
        interval2 *= 24 * scaleX;
        start *= 24 * scaleX;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Path path = new Path();
        path.moveTo(start, scaleY);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);
        path.rQuadTo(interval2, scaleY * ((interval2 / interval + 1) / 2), interval2 * 2, 0);
        path.rQuadTo(interval, -scaleY * ((interval / interval2 + 1) / 2), interval * 2, 0);

        canvas.clipPath(path);
        canvas.drawRect(0, 0, (int) scaleX * hour, (int) scaleY, paint);
        canvas.drawRect(0, (int) scaleY, (int) scaleX * hour, canvas.getHeight(), sunsetPaint);
        canvas.drawRect((int) scaleX * hour, 0, canvas.getWidth(), canvas.getHeight(), linePaint);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float horizontalDistance = event.getX() / getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!dayStart.isTarget() || !dayEnd.isTarget()) {
                    movingStart = false;
                    movingEnd = false;
                    break;
                }

                if (Math.abs(horizontalDistance - dayStart.val()) < Math.abs(horizontalDistance - dayEnd.val())) {
                    movingStart = true;
                    movingEnd = false;
                } else {
                    movingStart = false;
                    movingEnd = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (movingStart && horizontalDistance < dayEnd.getTarget()) {
                    dayEnd.to(horizontalDistance);
                } else if (movingEnd && horizontalDistance > dayStart.getTarget()) {
                    dayStart.to(horizontalDistance);
                }

                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                movingStart = false;
                movingEnd = false;

                if (listener != null)
                    listener.onSunriseChanged((long) (dayStart.getTarget() * DAY_LENGTH), (long) (dayEnd.getTarget() * DAY_LENGTH));

                break;
        }
        return false;
    }

    public interface SunriseListener {
        void onSunriseChanged(long sunriseMillis, long sunsetMillis);
    }
}
