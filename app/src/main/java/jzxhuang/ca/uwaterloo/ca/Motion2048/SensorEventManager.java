package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import static java.lang.Math.abs;

public class SensorEventManager implements SensorEventListener
{
    //Declare variables
    TextView output;
    Sensor sensor;      //the type of sensor (subclass)
    float maxX, maxY, maxZ;     //storing maximums
    float[] sensorMaxValuesArray = new float [3];
    String sensorValues, sensorValuesMax;

    private final float FILTER_CONSTANT = 13.0f;            //The filter constant for the LPF
    private float [][] historyReading = new float[100][3];  //2-D array with the last 100 readings of the accelerometer

    public SensorEventManager() {
        // unused
    }

    public SensorEventManager(Sensor sensor, TextView outputView){
        output = outputView;
        output.setTextColor(Color.BLACK);
        this.sensor = sensor;
    }

    public void onAccuracyChanged (Sensor s, int i){
        // unused
    }

    public void onSensorChanged (SensorEvent se) {
        if (se.sensor.getType() == sensor.getType()) {
            //call LPF function
            insertHistoryReadings(se.values);

            sensorValues = String.format("(%.3f, %.3f, %.3f)",      //output to textview
                    se.values[0], se.values[1], se.values[2]);
            //Calculate max values
            if (abs(se.values[0]) > maxX)
                maxX = abs(se.values[0]);
            if (se.values[1] > maxY)
                maxY = abs(se.values[1]);
            if (se.values[2] > maxZ)
                maxZ = abs(se.values[2]);
            //update max values
            updateMaxValues();
        }
    }

    public void reset() //This resets the max values
    {
        maxX = maxY = maxZ = 0;
        updateMaxValues();
    }

    private void updateMaxValues()      //This updates the max values (stored in array for easier access)
    {
        sensorMaxValuesArray[0] = maxX;
        sensorMaxValuesArray[1] = maxY;
        sensorMaxValuesArray[2] = maxZ;

        sensorValuesMax = String.format("(%.3f, %.3f, %.3f)", maxX, maxY, maxZ);
    }

    private void insertHistoryReadings(float[] values){         //Stores and filters past 100 readings
        for(int i=1; i<100; i++){
            historyReading[i-1][0]= historyReading[i][0];
            historyReading[i-1][1]= historyReading[i][1];
            historyReading[i-1][2]= historyReading[i][2];
        }
        //Low-Pass filter implementation
        historyReading[99][0]+= (values[0]-historyReading[99][0])/FILTER_CONSTANT;
        historyReading[99][1]+= (values[1]-historyReading[99][1])/FILTER_CONSTANT;
        historyReading[99][2]+= (values[2]-historyReading[99][2])/FILTER_CONSTANT;
    }
    public float[][] getHistoryReading(){       //Returns 2-D array with previous 100 readings
        return historyReading;
    }
}
