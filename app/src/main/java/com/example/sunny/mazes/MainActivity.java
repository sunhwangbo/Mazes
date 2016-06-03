package com.example.sunny.mazes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    RelativeLayout frame;
    GridLayout mazeframe;
    MyView[][] maze;
    Maze mazegenerator;
    char[][] mazedata;
    DrawingSurface background;
    int x = 40, y = 60;  // start position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame = (RelativeLayout) findViewById(R.id.frame);
        background = new DrawingSurface(this);
        frame.addView(background);
        mazeframe = new GridLayout(this);
        mazeframe.setColumnCount(21);
        mazegenerator = new Maze(5); // generate maze
        mazedata = mazegenerator.getMazeData(); // return maze data,  wall is 'X' , route is ' '
        maze = new MyView[21][11];
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 21; j++) {
                maze[j][i] = new MyView(this);
                mazeframe.addView(maze[j][i]);
                if (mazedata[j][i] == ' ')  // route maze set visibility
                    maze[j][i].setVisibility(View.INVISIBLE);
            }
        }
        frame.addView(mazeframe);

    }

    public void generateMaze() {  // make new maze
        mazegenerator = new Maze(5);
        mazedata = mazegenerator.getMazeData();
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 21; j++) {
                if (mazedata[j][i] == ' ')
                    maze[j][i].setVisibility(View.INVISIBLE);
                else
                    maze[j][i].setVisibility(View.VISIBLE);
            }
        }
    }
    // recycle code that used in class
    public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
        Canvas cacheCanvas;
        Bitmap backBuffer;
        int width, height;
        Paint paint;
        Context context;
        SurfaceHolder mHolder;

        public DrawingSurface(Context context) {
            super(context);
            this.context = context;
            setLayoutParams(new ViewGroup.LayoutParams(420, 440));
            init();
        }

        public DrawingSurface(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
            init();
        }

        private void init() {
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            width = getWidth();
            height = getHeight();
            cacheCanvas = new Canvas();
            backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //back buffer
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.WHITE);
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(5);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            draw();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        int lastX, lastY;
        boolean isStarting = false; // if touch position is player , set true , otherwise false

        public boolean getFailure(int x, int y) { // if current position is valid , return false , otherwise return
                if ((lastX <= 405 && lastX >= 15 && lastY <= 425 && lastY >= 15)&&maze[x / 20][y / 40].getVisibility() == VISIBLE || maze[(x + 10) / 20][y / 40].getVisibility() == VISIBLE||
                maze[(x - 10) / 20][y / 40].getVisibility() == VISIBLE ||maze[x / 20][(y-10) / 40].getVisibility() == VISIBLE||maze[x / 20][(y+10) / 40].getVisibility() == VISIBLE)
                    return true;
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            int action = event.getAction();
            lastX = (int) event.getX();
            lastY = (int) event.getY();

            Log.i("maze", lastX + " " + lastY + " current cell = " + lastX / 20 + " " + lastY / 40);
            if (getFailure(lastX, lastY) && isStarting) { // player crushed
                cacheCanvas.drawColor(Color.WHITE);
                x = 40;
                y = 60;
                isStarting = false;
                draw();
            } else {
                if ((lastX - x) * (lastX - x) + (lastY - y) * (lastY - y) <= 15 * 15) { // if check touch position is player
                    isStarting = true;
                }
                if (isStarting) {
                    cacheCanvas.drawColor(Color.WHITE);
                    x = (int) event.getX();
                    y = (int) event.getY();
                    switch (action & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        case MotionEvent.ACTION_UP:
                            if (lastX / 20 >= 18 && lastY / 40 >= 9) { // game success
                                generateMaze(); // remake maze
                                cacheCanvas.drawColor(Color.WHITE); //initialize canvas
                                x = 40;// start position
                                y = 60;
                                isStarting = false;
                                draw();
                                Toast.makeText(getApplicationContext(), "Congratulation!", Toast.LENGTH_SHORT).show();
                            }
                            isStarting = false;
                            break;
                    }
                    draw(); // SurfaceView에 그리는 function을 직접 제작 및 호출
                    return true;
                }
            }
            return false;
        }

        public void draw() {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
                paint.setColor(Color.RED);
                cacheCanvas.drawRect(20, 40, 60, 80, paint); // start mark draw
                paint.setColor(Color.BLUE);
                cacheCanvas.drawRect(360, 360, 400, 400, paint); // end mark draw
                paint.setColor(Color.BLACK);
                cacheCanvas.drawCircle(x, y, 15, paint); //player darw
                canvas.drawBitmap(backBuffer, 0, 0, paint);

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (mHolder != null) mHolder.unlockCanvasAndPost(canvas);
            }
        }

    } // class DrawingSurface

    protected class MyView extends View {  // each maze cell
        public MyView(Context context) {
            super(context);
            setLayoutParams(new ViewGroup.LayoutParams(20, 40));
        }

        public void onDraw(Canvas canvas) {
            Paint pnt = new Paint();
            pnt.setColor(Color.GRAY);
            canvas.drawColor(Color.BLACK);
            canvas.drawRect(0, 0, 20, 40, pnt);
        }
    }
}
