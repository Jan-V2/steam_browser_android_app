package john.vanderholt.sale_selector;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import kotlin.Function;
import kotlin.Unit;


public class OnSwipeTouchListener  implements OnTouchListener {

    private final GestureDetector gestureDetector;
    private Runnable right_swipe;
    private Runnable left_swipe;
    private Runnable click;

    public OnSwipeTouchListener (Context ctx, Runnable right_swipe, Runnable left_swipe, Runnable click){
        this.left_swipe = left_swipe;
        this.right_swipe = right_swipe;
        this.click = click;
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i("swipe", "in ontouch");
        return gestureDetector.onTouchEvent(event);
    }



    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 30;
        private static final int SWIPE_VELOCITY_THRESHOLD = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i("swipe", "in onfling");

            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        Log.e("swipe", "right" );
        right_swipe.run();
    }

    public void onSwipeLeft() {
        Log.e("swipe", "left" );
        this.left_swipe.run();
    }

    public void onSwipeTop() {
        Log.e("swipe", "top" );
    }

    public void onSwipeBottom() {
        Log.e("swipe", "bot" );
    }
}