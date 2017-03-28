package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import android.widget.TextView;

import static java.lang.Boolean.TRUE;

public class AccelerometerSensorEventManager extends SensorEventManager
{
    //FSM threshold constants
    final float[] THRESH_RIGHT = {0.5f, 1.5f, -0.5f};   //Right movement is rise > drop on x-axis
    final float[] THRESH_LEFT = {-0.5f, -1.5f, 0.5f};   //Left movement is drop > rise on x-axis
    final float[] THRESH_DOWN = {-0.5f, -1.5f, 0.5f};   //Down movement is drop > rise on z-axis
    final float[] THRESH_UP = {0.5f, 1.5f, -0.5f};      //Up movement is rise > drop on z-axis
    final float THRESH_Y= -0.4f;                        //All movements have same y-signature - a drop on the y-axis

    private boolean yFlag = false;                              //Flag for if the y-signature is satisfied

    private final int SAMPLEDEFAULT = 35;           //Default FSM counter (max amount of data points for a single motion)
    private int sampleCounter = SAMPLEDEFAULT;      //Counter for data points
    private GameLoopTask gameLoopTask;

    //FSM states and Signatures
    private enum myState {
        WAIT, riseRight, fallRight, riseLeft, fallLeft, riseUp, fallUp, riseDown, fallDown, determined
    }
    ;
    private myState state = myState.WAIT;       //default state is WAIT

    private enum mySig {right, left, up, down, unknown};

    private mySig signature = mySig.unknown;    //default signature is unknown

    public AccelerometerSensorEventManager(Sensor sensor, TextView outputView, GameLoopTask gameLoopTask) {
        super(sensor, outputView);
        this.gameLoopTask = gameLoopTask;
    }

    public void onSensorChanged(SensorEvent se) {
        super.onSensorChanged(se);
        float[][] historyReading = getHistoryReading();     //get the past 100 values
        if(gameLoopTask.endGame == TRUE){
            gameLoopTask.setDirection(GameLoopTask.Direction.NO_MOVEMENT);
            return;
        }

        FSM(historyReading);        //Call the FSM function
        if(sampleCounter <= 0){     //After allotted sample counter is used up, determine the signature and output appropriately
            if(state == myState.determined){
                if(signature == mySig.left) {
                    output.setText("LEFT");
                    gameLoopTask.setDirection(GameLoopTask.Direction.LEFT);
                }
                else if(signature == mySig.right) {
                    output.setText("RIGHT");
                    gameLoopTask.setDirection(GameLoopTask.Direction.RIGHT);
                }
                else if(signature == mySig.up) {
                    output.setText("UP");
                    gameLoopTask.setDirection(GameLoopTask.Direction.UP);
                }
                else if (signature == mySig.down) {
                    output.setText("DOWN");
                    gameLoopTask.setDirection(GameLoopTask.Direction.DOWN);
                }
                else
                    output.setText("NO MOVEMENT"); {
                    gameLoopTask.setDirection(GameLoopTask.Direction.NO_MOVEMENT);
                }
            }
            else{
                state = myState.WAIT;
                output.setText("NO MOVEMENT");
                gameLoopTask.setDirection(GameLoopTask.Direction.NO_MOVEMENT);
            }
            output.setTextColor(Color.WHITE);         //Large font size
            sampleCounter = SAMPLEDEFAULT;  //Reset counter
            state = myState.WAIT;           //Set state back to wait
        }
    }

    private void FSM(float[][] values) {
        //Calculate the change in the last two values on x, y, z-axes
        float delX = values[99][0] - values[98][0];
        float delY = values[99][1] - values [98][1];
        float delZ = values[99][2] - values[98][2];
        switch (state) {
            case WAIT:
                yFlag = false;                      //set y-sig to false
                sampleCounter = SAMPLEDEFAULT;      //reset counter and signature
                signature = mySig.unknown;
                //Look for a match and change state
                if (delX > THRESH_RIGHT[0])
                    state = myState.riseRight;
                else if (delX < THRESH_LEFT[0])
                    state = myState.fallLeft;
                else if (delZ > THRESH_UP[0])
                    state = myState.riseUp;
                else if (delZ < THRESH_DOWN[0])
                    state = myState.fallDown;
                //Log.d("state", state.toString());
                break;
            case riseRight:     //rise in x-axis of right movement (1st part)
                if (delX <= 0) {    //if slope is negative and value is above threshold
                    if (values[99][0] >= THRESH_RIGHT[1]) {
                        state = myState.fallRight;      //change state to drop in x-axis of right movement
                    }
                    else            //else reset
                        state = myState.determined;
                }
                break;
            case fallRight:     //rise in x-axis of right movement (2nd part)
                if(delY >= 0){  //check if y-signature matches
                    if(values[99][1] <= THRESH_Y)
                        yFlag = true;
                    else {
                        state = myState.determined;
                        Log.d("FAIL", "YCHECK");
                    }
                }
                if (delX >= 0) {    //if slope is positive, y-signature matches, and x-value is below threshold, recognize as right movement
                    if (values[99][0] <= THRESH_RIGHT[2] && yFlag == true) {
                        signature = mySig.right;
                    }
                    state = myState.determined;
                }
                break;
            case fallLeft:      //fall in x-axis of left movement (1st part)
                if (delX >= 0) {
                    if (values[99][0] <= THRESH_LEFT[1]) {
                        state = myState.riseLeft;
                    } else {
                        state = myState.determined;
                    }
                }
                break;
            case riseLeft:      //rise in x-axis of left movement (2nd part)
                if(delY >= 0){  //check if y-signature matches
                    if(values[99][1] <= THRESH_Y)
                        yFlag = true;
                    else {
                        state = myState.determined;
                        Log.d("FAIL", "YCHECK");
                    }
                }
                if (delX <= 0) {    //if y-signature matches and x-value is above threshold, recognize as left movement
                    if (values[99][0] >= THRESH_LEFT[2] && yFlag == true) {
                        signature = mySig.left;
                    }
                    state = myState.determined;
                }
                break;
            case fallDown:      //fall in z-axis of down movement (1st part)
                if (delZ >= 0){
                    if (values[99][2] <= THRESH_DOWN[1])
                        state= myState.riseDown;
                    else
                        state = myState.determined;
                }
                break;
            case riseDown:      //rise in z-axis of down movement (2nd part)
                if(delY >= 0){  //check if y-signature matches
                    if(values[99][1] <= THRESH_Y)
                        yFlag = true;
                    else {
                        state = myState.determined;
                        Log.d("FAIL", "YCHECK");
                    }
                }
                if (delZ <= 0){ //if y-signature matches and z-value is below threshold, recognize as down movement
                    if (values[99][2] >= THRESH_DOWN[2] && yFlag == true) {
                            signature = mySig.down;
                            state = myState.determined;
                    }
                    else
                        state = myState.determined;
                }
                break;
            case riseUp:        //rise in z-axis of up movement (1st part)
                if (delZ <= 0){
                    if (values[99][2] >= THRESH_UP[1]) {
                        state = myState.fallUp;
                    }
                    else
                        state = myState.determined;
                }
                break;
            case fallUp:        //fall in z-axis of up movement (2nd part)
                if(delY >= 0){  //check if y-sginature matches
                    if(values[99][1] <= THRESH_Y)
                        yFlag = true;
                    else {
                        state = myState.determined;
                        Log.d("FAIL", "YCHECK");
                    }
                }
                if (delZ >= 0){ //if y-signature matches and value is below threshold, recognize as up movement
                    if (values[99][2] <= THRESH_UP[2] && yFlag==true) {
                        signature = mySig.up;
                        state = myState.determined;
                    }
                    else
                        state = myState.determined;
                }
                break;
            case determined:    //determined state (either invalid signature or recognized signature)
                //Log.d("FSM: ", "State DETERMINED " + signature.toString());
                //output.setText(signature.toString());
                break;
            default:
                state = myState.WAIT;
                break;
        }
        sampleCounter--;
    }
}