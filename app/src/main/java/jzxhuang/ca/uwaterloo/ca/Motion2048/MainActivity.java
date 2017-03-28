package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;

import jzxhuang.ca.uwaterloo.ca.Motion2048.R;

public class MainActivity extends AppCompatActivity
{
    LinearLayout ll;
    RelativeLayout rl;  //Initialize RelativeLayout
    GameLoopTask gameLoop;  //Variable for gameloop object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        //Initialize RelativeLayout
        rl = (RelativeLayout) findViewById(R.id.label2);
        ll = (LinearLayout)findViewById(R.id.label1);
        final TextView directionTextView = new TextView(getApplicationContext());

        //Get dimensions for relative layout and game board
        Display display = getWindowManager().getDefaultDisplay();
        rl.getLayoutParams().width = display.getWidth();
        rl.getLayoutParams().height = display.getWidth();
        rl.setBackgroundResource(R.drawable.gameboard);

        gameLoop = new GameLoopTask(this, getApplicationContext(), rl, directionTextView);  //create instance of gameloop task

        //Initialize sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelSensor = sensorManager.getDefaultSensor((Sensor.TYPE_LINEAR_ACCELERATION));

        //Registering sensor
        final SensorEventManager accelSensorManager = new AccelerometerSensorEventManager(accelSensor, directionTextView, gameLoop);
        sensorManager.registerListener(accelSensorManager, accelSensor, SensorManager.SENSOR_DELAY_GAME);

        //Add direction text to relative layout
        directionTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);    // Center text
        //rl.addView(directionTextView);
        //directionTextView.setTextColor(Color.WHITE);
        directionTextView.setTextSize(50f);
        ll.addView(directionTextView);
        directionTextView.bringToFront();

        //Initialize Timer
        Timer gameLoopTimer = new Timer();
        gameLoopTimer.schedule(gameLoop, 0, 50);

        //Restart game
        Button reset = new Button(getApplicationContext());
        reset.setText("RESTART");
        reset.setTextSize(30.0f);
        reset.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //Call reset methods
                gameLoop.restart();
                gameLoop.createBlock();
                gameLoop.endGame = false;
                directionTextView.setText("RESTARTED GAME");
                directionTextView.setTextColor(Color.MAGENTA);
            }
        });
        ll.addView(reset);

        //Create a block
        Button create = new Button(getApplicationContext());
        create.setText("CREATE");
        create.setTextSize(30f);
        create.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                gameLoop.createBlock();
                directionTextView.setText("CREATED BLOCK");
                directionTextView.setTextColor(Color.MAGENTA);
            }
        });
        ll.addView(create);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {    //create first block after window completes loading (so we can use getLayoutParams())
        super.onWindowFocusChanged(hasFocus);

        if(gameLoop.exist==false){      //create block only once
            gameLoop.createBlock();
            gameLoop.exist = true;
        }
    }
}