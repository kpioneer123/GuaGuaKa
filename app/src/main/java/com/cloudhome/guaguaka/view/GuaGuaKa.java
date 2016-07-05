package com.cloudhome.guaguaka.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.cloudhome.guaguaka.R;

/**
 * Created by yangguangbaoxian on 2016/7/4.
 */
public class GuaGuaKa extends View {


    private Paint mOutterPaint ;
    private Path mPath; //手指划屏幕的路径
    private Canvas mCanvas;
    private Bitmap mBitmap;//使用mOutterPaint在mBitmap上绘制


    private int mLastX;
    private int mLastY;
    private Bitmap mOutterBitmap;
    //-------------------------------原始图层


    private String mText;

    private Paint  mBackPaint;

    /**
     * 记录刮奖信息文本的宽和高
     */
      private Rect mTextBound;
    private int mTextSize;
    private int mTextColor;
    //判断遮盖层区域是否消除达到阈值
    private volatile boolean mComplete = false;

    /**
     * 刮刮卡刮完的回调
     */

    public interface OnGuaGuaKaCompleteListener {
        void onComplete();
    }

    private OnGuaGuaKaCompleteListener mListener;

    public void setOnGuaGuaKaCompleteListener(OnGuaGuaKaCompleteListener mListener) {
        this.mListener = mListener;
    }



    public GuaGuaKa(Context context) {
        this(context,null);

    }


    public GuaGuaKa(Context context, AttributeSet attrs) {

        this(context,attrs,0);

    }


    /**三个参数的构造方法需要在程序中显示的时候去调用
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public GuaGuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

        //获得自定义属性
        TypedArray a = null;
        try {
            a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa, defStyleAttr, 0);
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.GuaGuaKa_text:
                        mText = a.getString(attr);
                        break;
                    case R.styleable.GuaGuaKa_textColor:
                        mTextColor = a.getColor(attr, 0x000000);
                        break;
                    case R.styleable.GuaGuaKa_textSize:
                        //将22sp转为像素值
                        mTextSize = (int) a.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
                        break;
                }
            }
        } finally {
            a.recycle();
        }
    }

    public void setText(String mText)
    {
        this.mText = mText;
        //获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText,0,mText.length(),mTextBound);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width  = getMeasuredWidth();
        int height = getMeasuredHeight();

        //初始化我们的bitmap
        mBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //设置绘制path画笔的一些属性
        setUpOutPaint();

        setUpBackPaint();

        mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height), null);//画“谢谢参与”bitmap
    }


    /**
     * 设置我们绘制获奖信息的画笔属性
     */
    private void setUpBackPaint() {

        mBackPaint.setColor(mTextColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        //获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText,0,mText.length(),mTextBound);

    }

    /**
     * 设置绘制path画笔的一些属性
     */
    private void setUpOutPaint() {
        //设置绘制path画笔的一些属性
        mOutterPaint.setColor(Color.RED);
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStrokeWidth(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;

                mPath.moveTo(mLastX,mLastY);
                break;
            case MotionEvent.ACTION_MOVE:

                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if(dx > 3||dy >3)
                {
                    mPath.lineTo(x,y);
                }

                mLastX = x;
                mLastY = y;


                break;
            case  MotionEvent.ACTION_UP:

              new Thread(mRunnable).start();


                break;

            default:
                break;
        }
          invalidate();
        return true;

    }

    private Runnable mRunnable =new Runnable() {
        @Override
        public void run() {

            int w = getWidth();
            int h = getHeight();

            float wipeArea   = 0;
            float totalArae  = w*h;

            Bitmap bitmap = mBitmap;

            int[] mPixels = new int[w*h];

            //获得Bitmap上所有像素的信息
            bitmap.getPixels(mPixels,0,w,0,0,w,h);

            for(int i=0; i<w ; i++)
            {
                for (int j = 0;j<h;j++)
                {
                    int index = j*w+i;

                    if(mPixels[index] ==0)
                    {
                        wipeArea ++;
                    }
                }
            }

            if(wipeArea >  0 && totalArae > 0)
            {
                int percent = (int)(wipeArea * 100/totalArae);

                Log.e("TAG",percent + "");

                if(percent > 60)
                {
                    //清除掉图层区域

                    mComplete = true;

                    postInvalidate();
                }
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {


        //canvas.drawBitmap(bitmap,0,0,null);
        canvas.drawText(mText,getWidth()/2-mTextBound.width()/2,getHeight()/2 +mTextBound.height()/2,mBackPaint);

        if(mComplete)
        {
            if(mListener!=null)
            {
                mListener.onComplete();
            }

        }
        if(!mComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    private void drawPath() {

        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath,mOutterPaint);
    }

    /**
     * 进行一些初始化操作
     */
    private void init() {
        mTextSize = 30;
        mOutterPaint = new Paint();
        mBackPaint = new Paint();
        mPath = new Path();

        mText = "谢谢惠顾!";
        mTextBound = new Rect();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());
        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.fg_guaguaka);

    }
}