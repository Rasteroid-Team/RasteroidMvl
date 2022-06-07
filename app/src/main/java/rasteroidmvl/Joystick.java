package rasteroidmvl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Joystick extends View implements Runnable {

    private static final int DEFAULT_LOOP_INTERVAL = 50;
    private static final int DEFAULT_COLOR_BUTTON = Color.BLACK;
    private static final int DEFAULT_COLOR_BORDER = Color.TRANSPARENT;
    private static final int DEFAULT_ALPHA_BORDER = 255;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_SIZE = 200;
    private static final int DEFAULT_WIDTH_BORDER = 3;
    private static final boolean DEFAULT_AUTO_RECENTER_BUTTON = true;
    private static final boolean DEFAULT_BUTTON_STICK_TO_BORDER = false;

    private Paint mPaintCircleButton;
    private Paint mPaintCircleBorder;
    private Paint mPaintBackground;
    private Paint mPaintBitmapButton;
    private Bitmap mButtonBitmap;
    private float mButtonSizeRatio;
    private float mBackgroundSizeRatio;

    private int mPosX = 0;
    private int mPosY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;

    private int mFixedCenterX = 0;
    private int mFixedCenterY = 0;

    private boolean mAutoReCenterButton;
    private boolean mButtonStickToBorder;
    private boolean enabled;

    private int mButtonRadius;
    private int mBorderRadius;
    private int mBorderAlpha;
    private float mBackgroundRadius;

    private OnMoveListener moveListener;

    private long mLoopInterval = DEFAULT_LOOP_INTERVAL;
    private Thread mThread = new Thread(this);

    /*
     * The allowed direction of the button is define by the value of this parameter:
     * - a negative value for horizontal axe
     * - a positive value for vertical axe
     * - zero for both axes
     */
    public static int BUTTON_DIRECTION_BOTH = 0;
    private int mButtonDirection = 0;

    /**
     * Constructor that is called when inflating a Joystick from XML.
     * @param context The Context that the Joystick is using.
     * @param attrs The attributes of the XML that is inflating the Joystick.
     */
    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Joystick,
                0, 0);

        int buttonColor;
        int borderColor;
        int backgroundColor;
        int borderWidth;
        Drawable buttonDrawable;
        try {
            buttonColor = styledAttributes.getColor(R.styleable.Joystick_buttonColor, DEFAULT_COLOR_BUTTON);
            borderColor = styledAttributes.getColor(R.styleable.Joystick_borderColor, DEFAULT_COLOR_BORDER);
            mBorderAlpha = styledAttributes.getInt(R.styleable.Joystick_borderAlpha, DEFAULT_ALPHA_BORDER);
            backgroundColor = styledAttributes.getColor(R.styleable.Joystick_backgroundColor, DEFAULT_BACKGROUND_COLOR);
            borderWidth = styledAttributes.getDimensionPixelSize(R.styleable.Joystick_borderWidth, DEFAULT_WIDTH_BORDER);
            mAutoReCenterButton = styledAttributes.getBoolean(R.styleable.Joystick_autoReCenterButton, DEFAULT_AUTO_RECENTER_BUTTON);
            mButtonStickToBorder = styledAttributes.getBoolean(R.styleable.Joystick_buttonStickToBorder, DEFAULT_BUTTON_STICK_TO_BORDER);
            buttonDrawable = styledAttributes.getDrawable(R.styleable.Joystick_buttonImage);
            enabled = styledAttributes.getBoolean(R.styleable.Joystick_enabled, true);
            mButtonSizeRatio = styledAttributes.getFraction(R.styleable.Joystick_buttonSizeRatio, 1, 1, 0.25f);
            mBackgroundSizeRatio = styledAttributes.getFraction(R.styleable.Joystick_backgroundSizeRatio, 1, 1, 0.75f);
            mButtonDirection = styledAttributes.getInteger(R.styleable.Joystick_buttonDirection, BUTTON_DIRECTION_BOTH);
        } finally {
            styledAttributes.recycle();
        }

        mPaintCircleButton = new Paint();
        mPaintCircleButton.setAntiAlias(true);
        mPaintCircleButton.setColor(buttonColor);
        mPaintCircleButton.setStyle(Paint.Style.FILL);

        if (buttonDrawable != null) {
            if (buttonDrawable instanceof BitmapDrawable) {
                mButtonBitmap = ((BitmapDrawable) buttonDrawable).getBitmap();
                mPaintBitmapButton = new Paint();
            }
        }

        mPaintCircleBorder = new Paint();
        mPaintCircleBorder.setAntiAlias(true);
        mPaintCircleBorder.setColor(borderColor);
        mPaintCircleBorder.setStyle(Paint.Style.STROKE);
        mPaintCircleBorder.setStrokeWidth(borderWidth);

        if (borderColor != Color.TRANSPARENT) {
            mPaintCircleBorder.setAlpha(mBorderAlpha);
        }

        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setColor(backgroundColor);
        mPaintBackground.setStyle(Paint.Style.FILL);
    }

    /**
     * Get the center of the joystick
     */
    private void initPosition() {
        mFixedCenterX = mCenterX = mPosX = getWidth() / 2;
        mFixedCenterY = mCenterY = mPosY = getWidth() / 2;
    }


    /**
     * Draw the background, the border and the button of the Joystick. The button of the Joystick
     * can be either an image or a paint.
     * @param canvas the canvas on which the Joystick will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawCircle(mFixedCenterX, mFixedCenterY, mBackgroundRadius, mPaintBackground);
        canvas.drawCircle(mFixedCenterX, mFixedCenterY, mBorderRadius, mPaintCircleBorder);

        if (mButtonBitmap != null) {
            canvas.drawBitmap(
                    mButtonBitmap,
                    mPosX + mFixedCenterX - mCenterX - mButtonRadius,
                    mPosY + mFixedCenterY - mCenterY - mButtonRadius,
                    mPaintBitmapButton
            );
        }
        else {
            canvas.drawCircle(
                    mPosX + mFixedCenterX - mCenterX,
                    mPosY + mFixedCenterY - mCenterY,
                    mButtonRadius,
                    mPaintCircleButton
            );
        }
    }


    /**
     * Overrided method, this is called when the size of this view has changed.
     * Here we get the center of the view and the radius to draw all the shapes.
     * The radius is based on the smallest value between width and height.
     * @param width int, current width of this view.
     * @param height int, current height of this view.
     * @param oldWidth int, old width of this view.
     * @param oldHeight int, old height of this view.
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        initPosition();
        int d = Math.min(width, height);
        mButtonRadius = (int) (d / 2 * mButtonSizeRatio);
        mBorderRadius = (int) (d / 2 * mBackgroundSizeRatio);
        mBackgroundRadius = mBorderRadius - (mPaintCircleBorder.getStrokeWidth() / 2);

        if (mButtonBitmap != null) {
            mButtonBitmap = Bitmap.createScaledBitmap(mButtonBitmap, mButtonRadius * 2,mButtonRadius * 2, true);
        }
    }

    /**
     * Overrided method, set the measured values to resize the view to a certain width and height
     * @param widthMeasureSpec width
     * @param heightMeasureSpec height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }

    /**
     * Overrided method, checks that the measure passed by parameter is not 0 (UNSPECIFIED). If it's 0 it gets the
     * default size value.
     * @param measureSpec measure to check.
     * @return int the measure checked.
     */
    private int measure(int measureSpec) {
        int measure=DEFAULT_SIZE;
        if (MeasureSpec.getMode(measureSpec) != MeasureSpec.UNSPECIFIED) {
            measure=MeasureSpec.getSize(measureSpec);
        }
        return measure;
    }

    /**
     * Overrided method, handle touch screen motion event. Move the joystick button according to the
     * finger coordinate. The button movement depends too on the class flags state.
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled) {

            setPositions(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                onJoystickReleased();
            }else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onJosytickSelected();
            }

            double abs = Math.sqrt((mPosX - mCenterX) * (mPosX - mCenterX)
                    + (mPosY - mCenterY) * (mPosY - mCenterY));

            if (abs > mBorderRadius || (mButtonStickToBorder && abs != 0)) {
                mPosX = (int) ((mPosX - mCenterX) * mBorderRadius / abs + mCenterX);
                mPosY = (int) ((mPosY - mCenterY) * mBorderRadius / abs + mCenterY);
            }

            if (!mAutoReCenterButton) {
                if (moveListener != null) {
                    moveListener.onMove(getAngle(), getStrength());
                }
            }
            invalidate();
        }
        return true;
    }

    private void onJosytickSelected() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }

        mThread = new Thread(this);
        mThread.start();

        if (moveListener != null) {
            moveListener.onMove(getAngle(), getStrength());
        }
    }

    /**
     * Called when the joystick is not longer pressed. Stops the listener and re-center the button of
     * the joystick if the re-center flag is true.
     */
    private void onJoystickReleased() {
        mThread.interrupt();

        if (mAutoReCenterButton) {
            int angle = getAngle();
            resetButtonPosition();
            if (moveListener != null) {
                moveListener.onMove(angle, getStrength());
            }
        }
    }

    /**
     * To move the button according to the finger coordinate (Can be limited to one axis)
     * @param event MotionEvent
     */
    private void setPositions(MotionEvent event) {
        if (mButtonDirection < 0){
            mPosY = mCenterY;
        } else {
            mPosY = (int) event.getY();
        }

        if (mButtonDirection > 0){
            mPosX = mCenterX;
        } else {
            mPosX = (int) event.getX();
        }
    }

    /**
     * Process the angle following the 360° counter-clock protractor rules.
     * @return the angle of the button
     */
    private int getAngle() {
        int angle = (int) Math.toDegrees(Math.atan2(mCenterY - mPosY, mPosX - mCenterX));
        if (angle < 0){
            angle+=360;
        }

        return angle;
    }

    /**
     * Process the strength as a percentage of the distance between the center and the border.
     * @return the strength of the button
     */
    private int getStrength() {
        return (int) (100 * Math.sqrt((mPosX - mCenterX)
                * (mPosX - mCenterX) + (mPosY - mCenterY)
                * (mPosY - mCenterY)) / mBorderRadius);
    }

    /**
     * Reset the button position to the center.
     */
    public void resetButtonPosition() {
        mPosX = mCenterX;
        mPosY = mCenterY;
    }

    /**
     * Sets a new OnMoveListener interface object with the default loop interval.
     * @param listener The object that that will receive the onMove call
     */
    public void setOnMoveListener(OnMoveListener listener) {
        moveListener = listener;
        mLoopInterval = DEFAULT_LOOP_INTERVAL;
    }

    /**
     * Sets a new OnMoveListener interface object with a loop interval passed by parameter.
     * @param listener The callback that will run
     * @param loopInterval Refresh rate to be invoked in milliseconds
     */
    public void setOnMoveListener(OnMoveListener listener, int loopInterval) {
        moveListener = listener;
        mLoopInterval = loopInterval;
    }

    /**
     * Enable or disable the joystick. The button won't move and onMove won't be called.
     * @param enabled boolean to enable or disable the jostick.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Overrided method, sends the value of the move to the listener each x miliseconds, where x is
     * the value assigned to mLoopInterval.
     */
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (moveListener != null) {
                        moveListener.onMove(getAngle(), getStrength());
                    }
                }
            });

            try {
                Thread.sleep(mLoopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
