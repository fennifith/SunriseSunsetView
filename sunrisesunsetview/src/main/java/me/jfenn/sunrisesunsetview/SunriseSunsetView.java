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

    private Paint sunrisePaint;
    private Paint sunsetPaint;
    private Paint linePaint;

    private AnimatedFloat dayStart;
    private AnimatedFloat dayEnd;

    private Float moveBeginStart;
    private Float moveBeginEnd;

    private SunriseListener listener;

    public SunriseSunsetView(Context context) {
        super(context);
        init();
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunriseSunsetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dayStart = new AnimatedFloat(0.25f);
        dayEnd = new AnimatedFloat(0.75f);

        sunrisePaint = new Paint();
        sunrisePaint.setAntiAlias(true);
        sunrisePaint.setStyle(Paint.Style.FILL);
        sunrisePaint.setColor(Color.BLACK);

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
    }

    /**
     * Set the sunrise time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period. Change in values will not be animated.
     *
     * @param dayStartMillis            The sunrise time, in milliseconds.
     */
    public void setDayStart(long dayStartMillis) {
        setDayStart(dayStartMillis, false);
    }

    /**
     * Set the sunrise time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayStartMillis            The sunrise time, in milliseconds.
     * @param animate                   Whether to animate the change in
     *                                  values.
     */
    public void setDayStart(long dayStartMillis, boolean animate) {
        dayStartMillis %= DAY_LENGTH;
        if (animate)
            dayStart.to((float) dayStartMillis / DAY_LENGTH);
        else dayStart.setCurrent((float) dayStartMillis / DAY_LENGTH);
    }

    /**
     * Calculate the sunrise time, in milliseconds. Returned values
     * will not range beyond a 24 hour period.
     *
     * @return The sunrise time, in milliseconds.
     */
    public long getDayStart() {
        return (long) (dayStart.getTarget() * DAY_LENGTH);
    }

    /**
     * Set the sunset time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period. Change in values will not be animated.
     *
     * @param dayEndMillis              The sunset time, in milliseconds.
     */
    public void setDayEnd(long dayEndMillis) {
        setDayEnd(dayEndMillis, false);
    }

    /**
     * Set the sunset time, in milliseconds. Values can range
     * beyond the period of a day; they are modulated by a 24 hour
     * period.
     *
     * @param dayEndMillis              The sunset time, in milliseconds.
     * @param animate                   Whether to animate the change in
     *                                  values.
     */
    public void setDayEnd(long dayEndMillis, boolean animate) {
        dayEndMillis %= DAY_LENGTH;
        if (animate)
            dayEnd.to((float) dayEndMillis / DAY_LENGTH);
        else dayEnd.setCurrent((float) dayEndMillis / DAY_LENGTH);
    }

    /**
     * Calculate the sunset time, in milliseconds. Returned values
     * will not range beyond a 24 hour period.
     *
     * @return The sunset time, in milliseconds.
     */
    public long getDayEnd() {
        return (long) (dayEnd.getTarget() * DAY_LENGTH);
    }

    /**
     * Specify an interface to receive updates when the sunrise/sunset
     * times are modified by the user. Methods in this interface are only
     * called when the view is interacted with; calling setDayEnd or
     * setDayStart will not result in this interface being notified.
     *
     * @param listener                  An interface to receive updates
     *                                  when the sunrise/sunset times
     *                                  are modified.
     */
    public void setListener(SunriseListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        dayStart.next(true);
        dayEnd.next(true);

        float scaleX = getWidth() / 23f;
        float scaleY = getHeight() / 2f;
        float interval = (dayEnd.val() - dayStart.val()) / 2;
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
        canvas.drawRect(0, 0, (int) scaleX * hour, (int) scaleY, sunrisePaint);
        canvas.drawRect(0, (int) scaleY, (int) scaleX * hour, canvas.getHeight(), sunsetPaint);
        canvas.drawRect((int) scaleX * hour, 0, canvas.getWidth(), canvas.getHeight(), linePaint);

        if (!dayStart.isTarget() || !dayEnd.isTarget())
            postInvalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float horizontalDistance = event.getX() / getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveBeginStart = null;
                moveBeginEnd = null;
                if (!dayStart.isTarget() || !dayEnd.isTarget())
                    break;

                if (Math.abs(horizontalDistance - dayStart.val()) < Math.abs(horizontalDistance - dayEnd.val()))
                    moveBeginStart = dayStart.val() - horizontalDistance;
                else moveBeginEnd = dayEnd.val() - horizontalDistance;

                break;
            case MotionEvent.ACTION_MOVE:
                if (moveBeginStart != null && horizontalDistance < dayEnd.getTarget()) {
                    dayStart.to(Math.min(1, Math.max(0, moveBeginStart + horizontalDistance)));
                } else if (moveBeginEnd != null && horizontalDistance > dayStart.getTarget()) {
                    dayEnd.to(Math.min(1, Math.max(0, moveBeginEnd + horizontalDistance)));
                }

                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    if (moveBeginStart != null)
                        listener.onSunriseChanged(this, getDayStart());
                    else if (moveBeginEnd != null)
                        listener.onSunsetChanged(this, getDayEnd());
                }

                moveBeginStart = null;
                moveBeginEnd = null;
                break;
        }
        return false;
    }

    public interface SunriseListener {
        void onSunriseChanged(SunriseSunsetView view, long sunriseMillis);
        void onSunsetChanged(SunriseSunsetView view, long sunsetMillis);
    }
}
