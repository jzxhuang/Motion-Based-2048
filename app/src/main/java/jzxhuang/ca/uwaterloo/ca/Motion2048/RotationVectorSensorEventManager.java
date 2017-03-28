package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;

public class RotationVectorSensorEventManager extends SensorEventManager
{

    public RotationVectorSensorEventManager(Sensor sensor, TextView outputView)
    {
        super(sensor, outputView);
    }

    public void onSensorChanged (SensorEvent se)
    {
        super.onSensorChanged(se);
        if(output != null)
            output.setText("ROTATION VECTOR: " + sensorValues + "\nROTATION VECTOR ABSOLUTE MAX: " + sensorValuesMax);
    }
}