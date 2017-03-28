package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Jeff on 2017-03-23.
 */

public abstract class GameBlockTemplate extends ImageView {
    public GameBlockTemplate(Context myContext){super(myContext);}
    public abstract void setTarget();
    public abstract void move();
}
