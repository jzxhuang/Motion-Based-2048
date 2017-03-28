package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;


public class LightSensorEventManager extends SensorEventManager
{
    public LightSensorEventManager(Sensor sensor, TextView outputView)
    {
        super(sensor, outputView);
    }

    public void onSensorChanged (SensorEvent se)
    {
        super.onSensorChanged(se);
        if(output != null)
            output.setText("LIGHT SENSOR: " + sensorValues + "\nLIGHT SENSOR ABSOLUTE MAX: " + sensorValuesMax);
    }
}