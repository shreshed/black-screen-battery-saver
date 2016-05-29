package busu.com.blackscreenbatterysaver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by adibusu on 5/28/16.
 */
public class ViewPortView extends View {

    private RectF hole;

    private RectF[] blacks;

    private Paint paintBlack;

    final float DEFAULT_HEIGHT_PERCENTAGE = 0.3f;

    final int TOP = 0;
    final int BOTTOM = 1;
    final int LEFT = 2;
    final int RIGHT = 3;
    final int CENTER = 4;

    //TOP, BOTTOM or CENTER
    private int currentPosition = CENTER;

    public ViewPortView(Context context) {
        super(context);
        init();
    }

    void init() {
        hole = new RectF();
        blacks = new RectF[4];
        for (int i = 0; i < 4; i++) {
            blacks[i] = new RectF();
        }
        paintBlack = new Paint();
        paintBlack.setColor(Color.BLACK);
        paintBlack.setStyle(Paint.Style.FILL);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickedX >= 0f && lastClickedY >= 0f) {
                    onClicked();
                }
            }
        });
    }

    private void onClicked() {
        changeHolePosition(lastClickedY < hole.top);
        applyNewHolePosition();
    }

    private void changeHolePosition(boolean hasToMoveUpwards) {
        if (hasToMoveUpwards) {
            if (currentPosition == CENTER) {
                currentPosition = TOP;
            } else if (currentPosition == BOTTOM) {
                currentPosition = CENTER;
            }
        } else {
            if (currentPosition == CENTER) {
                currentPosition = BOTTOM;
            } else if (currentPosition == TOP) {
                currentPosition = CENTER;
            }
        }
    }

    private void commitPositionToHole() {
        float holeTop = hole.top;
        switch (currentPosition) {
            case TOP:
                holeTop = 0f;
                break;
            case CENTER:
                holeTop = (getHeight() - hole.height()) / 2.0f;
                break;
            case BOTTOM:
                holeTop = getHeight() - hole.height();
                break;
        }
        final float holeHeight = hole.height();
        hole.top = holeTop;
        hole.bottom = holeTop + holeHeight;
    }

    private void adjustBlacks() {
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        //
        blacks[TOP].set(0, 0, parentWidth, hole.top);
        blacks[BOTTOM].set(0, hole.bottom, parentWidth, parentHeight);
        blacks[LEFT].set(0, hole.top, hole.left, hole.height());
        blacks[RIGHT].set(hole.right, hole.top, parentWidth, hole.height());
    }

    /**
     * Applies a new height to the hole, but does no check if the percentage is valid
     * Always follow it by a #commitPositionToHole or, even better, #applyNewHolePosition
     *
     * @param percentageOfViewHeight
     */
    private void setHoleHeight(float percentageOfViewHeight) {
        final float holeHeight = getHeight() * percentageOfViewHeight;
        //always position TOP, next should come always code that positions the hole vertically
        hole.set(0, 0, getWidth(), holeHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0 && w != oldw && h != oldh) {
            setHoleHeight(DEFAULT_HEIGHT_PERCENTAGE);
            applyNewHolePosition();
        }
    }

    private void applyNewHolePosition() {
        commitPositionToHole();
        adjustBlacks();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (RectF black : blacks) {
            canvas.drawRect(black, paintBlack);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (hole.contains(event.getX(), event.getY())) {
            return false;
        }
        super.onTouchEvent(event);
        recordPositionOfClick(event);
        return true;
    }

    private float lastClickedX, lastClickedY;

    private void recordPositionOfClick(MotionEvent motionEvent) {
        lastClickedX = lastClickedY = -1f;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                lastClickedX = motionEvent.getX();
                lastClickedY = motionEvent.getY();
                break;
        }
    }


}
