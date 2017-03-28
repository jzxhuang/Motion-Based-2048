package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import jzxhuang.ca.uwaterloo.ca.Motion2048.R;

public class GameBlock extends GameBlockTemplate
{
    private int xPrev, yPrev; //These two variables hold the previous position of the block until the movement is complete
    private int xCurr, yCurr;   //These two variables hold the current position of the block

    private int coordOffset = 0;    //Offset from the edge of the screen
    public GameLoopTask.Direction blockDirection = GameLoopTask.Direction.NO_MOVEMENT;

    public int boardWidth;  //The width of the height of the board
    public int boardHeight;

    private int width = 0;
    private int height = 0;

    private int velocity = 0;   //Velocity of the block (for animation)
    private final int ACC = 4;  //Acceleration constant of the block

    //New Lab 4 Code
    private GameLoopTask myGL;
    private RelativeLayout rl;
    private TextView numTV;
    public int blockNumber;     //holds the value of the block
    private boolean remove;     //checks if the block needs to merge
    public boolean delete;      //checks if the block needs to be deleted
    private int[] tvOffset = new int[2];    //Offset for textview

    private int targetX = 0;    //Target coordinates of the block
    private int targetY = 0;

    public boolean finished;    //checks if the block is finished moving

    private GameBlock occupant; //used for collision detection and merging

    public GameBlock(Context context, RelativeLayout rl, int x, int y, int offset, int width, int height, GameLoopTask gL)    //Constructor, creates the block and scales it
    {
        super(context);
        this.myGL=gL;
        this.rl=rl;
        boardWidth = width;
        boardHeight = height;
        coordOffset = offset;

        tvOffset[0] = width/16+offset/2;
        tvOffset[1] = coordOffset;
        numTV = new TextView(context);
        //Generate either 2 or 4
        Random myRandomGen = new Random();
        blockNumber = (myRandomGen.nextInt(2) + 1) * 2;

        //Initialize textview
        numTV.setText(String.format("%d", blockNumber));
        numTV.setTextSize(40.0f);
        numTV.setTextColor(Color.BLACK);

        setImageResource(R.drawable.gameblock);

        rl.addView(this);   //add block to relative layout

        occupant = null;
        finished = false;
        delete = false;
    }

    //Sets the target coordinate of the block. Called every time the direction changes
    @Override
    public void setTarget()
    {
        int testCoord;
        int numOfOccupants; //number of blocks in the way

        switch(blockDirection){
            case UP:
                testCoord = coordOffset/2;      //test the furthest tile possible
                numOfOccupants = 0;
                while(testCoord!=yCurr){        //while the current position is not at the test tile
                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if (myGL.isOccupied(xCurr, testCoord) != null){ //check if the test tile is occupied. if yes, increase number of "occupants"
                        numOfOccupants++;
                        occupant = myGL.isOccupied(xCurr, testCoord);       //get the occupied block and save it to a variable
                    }
                    testCoord += boardHeight/4; //check the next tile
                }
                //merging code
                if(occupant!=null && occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
                    targetY =coordOffset/2 + (numOfOccupants-1)*boardHeight/4;     //set target to same slot as occupied
                    this.remove = true;             //set boolean variable remove to true
                }
                else{   //otherwise calculate target coordinate as normal
                    this.occupant = null;
                    targetY = coordOffset/2 + numOfOccupants*boardHeight/4;
                }
//                //test code for remove
//                if(occupant!=null && occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
//                    targetY = coordOffset/2 + (numOfOccupants-1)*boardHeight/4;     //set target to same slot as occupied
//                    this.remove = true;             //set boolean variable remove to true
//                }
//                if(numOfOccupants == 1){    //one other block
//                    if(occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
//                        targetY = coordOffset/2;     //set target to same slot as occupied
//                        this.remove = true;             //set boolean variable remove to true
//                        this.numTV.setText("!!!");
//                    }
//                    else{
//                        this.occupant = null;
//                        targetY = coordOffset/2 + numOfOccupants*boardHeight/4; //set target tile based on if the test tiles were occupied
//                    }
//                }
//                else if(numOfOccupants == 2){   //if there are two other blocks in the column
//                    if(this.yCurr == (coordOffset/2 + boardHeight/2)){      //In the 3rd spot down
//                        if(myGL.isOccupied(xCurr, coordOffset/2).blockNumber == occupant.blockNumber) {    //if the two blocks above will merge
//                            targetY = coordOffset / 2 + boardHeight / 4;    //set target to second tile
//                            this.occupant = null;
//                            this.numTV.setText(":)");
//                        }
//                        else{       //if the two blocks above don't merge
//                            if(this.blockNumber == occupant.blockNumber){   //if 2nd and 3rd merge
//                                targetY = coordOffset/2 + boardHeight/4;
//                                this.remove = true;
//                            }
//                            else {           // if 2nd and 3rd don't merge
//                                this.occupant = null;
//                                targetY = coordOffset / 2 + numOfOccupants * boardHeight / 4; //set target tile based on if the test tiles were occupied
//                            }
//                        }
//                    }
//                    else if(this.yCurr == (coordOffset/2+boardHeight*3/4 )) { //last spot
//                        if(myGL.isOccupied(xCurr, coordOffset/2 + boardHeight/2) == null || myGL.isOccupied(xCurr, coordOffset/2 + boardHeight/4) == null) {   //2nd or 3rd tile is empty. 3 situations
//                            if (myGL.isOccupied(xCurr, coordOffset / 2).blockNumber != occupant.blockNumber) {      //if other two don't merge
//                                if (this.blockNumber == occupant.blockNumber) {       //if this and occupant value are equal
//                                    targetY = coordOffset / 2 + boardHeight / 4;        //set to 2nd tile, merge
//                                    this.remove = true;
//                                } else {          //if unequal
//                                    targetY = coordOffset / 2 + boardHeight / 2;
//                                    this.occupant = null;           //set target to 3rd tile
//                                }
//                            } else if (myGL.isOccupied(xCurr, coordOffset / 2).blockNumber == occupant.blockNumber) { //if they do merge
//                                targetY = coordOffset / 2 + boardHeight / 4; //set target to 2nd tile
//                                this.occupant = null;
//                            }
//                        }
//                        else{           //first tile is empty
//                            if (myGL.isOccupied(xCurr, coordOffset/2 + boardHeight/4).blockNumber != occupant.blockNumber){
//                                if (this.blockNumber == occupant.blockNumber) {       //if this and occupant value are equal
//                                    targetY = coordOffset / 2 + boardHeight / 4;        //set to 2nd tile, merge
//                                    this.remove = true;
//                                } else {          //if unequal
//                                    targetY = coordOffset / 2 + boardHeight / 2;
//                                    this.occupant = null;           //set target to 3rd tile
//                                }
//                            }
//                            else{
//                                targetY = coordOffset/2 + boardHeight/4;
//                                this.occupant = null;
//                            }
//                        }
//                    }
//                }
//                else if(numOfOccupants == 3){       //3 tiles
//                    if(myGL.isOccupied(xCurr, coordOffset/2).blockNumber == myGL.isOccupied(xCurr, coordOffset/2 + boardHeight/2).blockNumber){     //if first two will merge
//                        if(occupant.blockNumber == this.blockNumber){       //if last two also merge (i.e. two pairs of merges
//                            targetY = coordOffset/2 + boardHeight/4;
//                            this.remove = true;
//                        }
//                        else{
//                            targetY = coordOffset/2 + boardHeight/2; //3rd tile
//                            this.occupant = null;
//                            this.numTV.setText("???");
//                        }
//                    }
//                    else if (myGL.isOccupied(xCurr, coordOffset/2 + boardHeight/4).blockNumber == occupant.blockNumber){ //if 2nd and 3rd merge
//                        targetY = coordOffset/2 + boardHeight/2;    //3rd tile
//                        this.occupant = null;
//                        this.remove = false;
//                        this.numTV.setText("WHEE");
//                    }
//                    else{               //if neither 1st/2nd or 2nd/3rd merge
//                        if(this.blockNumber == occupant.blockNumber){ //if 3rd and 4th merge
//                            targetY = coordOffset/2 + boardHeight/2;    //3rd tile
//                            this.remove = true;}
//                        else{
//                            targetY = coordOffset/2 + boardHeight*3/4;
//                            this.occupant = null;
//                        }
//                    }
//                }
//                else{       //0 occupants
//                    this.occupant = null;
//                    targetY = coordOffset/2 + numOfOccupants*boardHeight/4; //set target tile based on if the test tiles were occupied
//                }
            break;

            case DOWN:
                testCoord = boardHeight*3/4 + coordOffset/2;
                numOfOccupants = 0;
                while(testCoord!=yCurr){
                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if (myGL.isOccupied(xCurr, testCoord) != null){
                        numOfOccupants++;
                        occupant = myGL.isOccupied(xCurr, testCoord);       //get the occupied block and save it to a variable
                    }
                    testCoord -= boardHeight/4;
                }
                //merging code
                if(occupant!=null && occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
                    targetY = boardHeight*3/4 + coordOffset/2 - (numOfOccupants-1)*boardHeight/4;     //set target to same slot as occupied
                    this.remove = true;             //set boolean variable remove to true
                }
                else{
                    this.occupant = null;
                    targetY = boardHeight*3/4 + coordOffset/2 - numOfOccupants*boardHeight/4;
                }
            break;

            case RIGHT:
                testCoord = boardWidth*3/4 + coordOffset/2;      //test the furthest tile possible
                numOfOccupants = 0;
                while(testCoord!=xCurr){        //while the current position is not at the test tile
                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if (myGL.isOccupied(testCoord, yCurr) != null){ //check if the test tile is occupied. if yes, increase number of "occupants"
                        numOfOccupants++;
                        occupant = myGL.isOccupied(testCoord, yCurr);       //get the occupied block and save it to a variable
                    }
                    testCoord -= boardWidth/4; //check the next tile
                }
                //merging code
                if(occupant!=null && occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
                    targetX = boardWidth*3/4 + coordOffset/2 - (numOfOccupants-1)*boardHeight/4;     //set target to same slot as occupied
                    this.remove = true;             //set boolean variable remove to true
                }
                else{
                    this.occupant = null;
                    targetX = boardHeight*3/4 + coordOffset/2 - numOfOccupants*boardHeight/4;
                }
            break;

            case LEFT:
                testCoord = coordOffset/2;      //test the furthest tile possible
                numOfOccupants = 0;
                while(testCoord!=xCurr){        //while the current position is not at the test tile
                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if (myGL.isOccupied(testCoord, yCurr) != null){ //check if the test tile is occupied. if yes, increase number of "occupants"
                        numOfOccupants++;
                        occupant = myGL.isOccupied(testCoord, yCurr);       //get the occupied block and save it to a variable
                    }
                    testCoord += boardWidth/4; //check the next tile
                }
                //merging code
                if(occupant!=null && occupant.blockNumber == this.blockNumber){       //if closest occupied slot has same number
                    targetX = coordOffset/2 + (numOfOccupants-1)*boardHeight/4;     //set target to same slot as occupied
                    this.remove = true;             //set boolean variable remove to true
                }
                else{
                    this.occupant = null;
                    targetX = coordOffset/2 + numOfOccupants*boardHeight/4; //set target tile based on if the test tiles were occupied
                }
            break;

            default:

            break;
        }
    }

    //Moves the block. Called on a timer (game timer)
    @Override
    public void move()
    {
        if (blockDirection == GameLoopTask.Direction.NO_MOVEMENT) return;

        switch (blockDirection)
        {
            case DOWN:
                if(targetY + height < boardHeight) {     //check that the block is not already at the bottom edge
                    if (yCurr < targetY) {               //while block has not reached the target location
                        if (yCurr + velocity >= targetY) {   //if next movement will move the block past or equal to the target location
                            yCurr = targetY;     //set block to target and previous location to target
                            yPrev = targetY;
                            velocity = 0;       //reset velocity
                            //merging code
                            if (this.remove == true){
                                occupant.blockNumber*=2;    //change value of occupant
                                occupant.numTV.setText(String.format("%d", occupant.blockNumber));
                                occupant.numTV.bringToFront();

                                rl.removeView(this.numTV);  //remove block from layout
                                rl.removeView(this);
                                this.delete = true;         //can be removed from LL
                            }
                        } else {                        //else move block and increase speed
                            yCurr += velocity;      //new coordinates of the block
                            velocity += ACC;        //accelerate
                        }
                    }
                }
                break;
            case UP:    //the same logic from the first switch case applies except in different directions
                if(yCurr - coordOffset/2 > 0) {
                    if (yCurr > targetY) {
                        if (yCurr - velocity <= targetY) {
                            yCurr = targetY;
                            yPrev = targetY;
                            velocity = 0;
                            //test code for removing
                            if (this.remove == true){
                                occupant.blockNumber*=2;
                                occupant.numTV.setText(String.format("%d", occupant.blockNumber));
                                occupant.numTV.bringToFront();

                                rl.removeView(this.numTV);
                                rl.removeView(this);
                                this.delete = true;
                            }
                        } else {
                            yCurr -= velocity;
                            velocity += ACC;
                        }
                    }
                }
                break;
            case RIGHT:
                if (targetX + width < boardWidth) {
                    if (xCurr < targetX) {
                        if ((xCurr + velocity) >= targetX) {
                            xCurr = targetX;
                            xPrev = targetX;
                            velocity = 0;
                            //merging code
                            if (this.remove == true){
                                occupant.blockNumber*=2;
                                occupant.numTV.setText(String.format("%d", occupant.blockNumber));
                                occupant.numTV.bringToFront();

                                rl.removeView(this.numTV);
                                rl.removeView(this);
                                this.delete = true;
                            }
                        }
                        else {
                            xCurr += velocity;
                            velocity += ACC;
                        }
                    }
                }
                break;
            case LEFT:
                if (xCurr - coordOffset/2 > 0) {
                    if (xCurr > targetX) {
                        if ((xCurr - velocity) <= targetX) {
                            xCurr = targetX;
                            xPrev = targetX;
                            velocity = 0;
                            //merging code
                            if (this.remove == true){
                                occupant.blockNumber*=2;
                                occupant.numTV.setText(String.format("%d", occupant.blockNumber));
                                occupant.numTV.bringToFront();

                                rl.removeView(this.numTV);
                                rl.removeView(this);
                                this.delete = true;
                            }
                        } else {
                            xCurr -= velocity;
                            velocity += ACC;
                        }
                    }
                }
                break;
            default:
                break;
        }
        this.setX(xCurr);       //set coordinate of the block
        this.setY(yCurr);

        numTV.setX(xCurr + tvOffset[0]);    //set coordinate of TV
        numTV.setY(yCurr + tvOffset[1]);

        if(velocity == 0) {       //after movement reset the direction to none
            blockDirection = GameLoopTask.Direction.NO_MOVEMENT;
            this.finished = true;   //done moving
        }
    }

    public void updateCoordinates(int x, int y)     //used when block is created
    {
        int newX = x + coordOffset / 2;     //determine the coordinates of the block taking into account the offset
        int newY = y + coordOffset / 2;

        if (newX < 0 || newX + width > boardWidth || newY < 0 || newY + height > boardHeight )  //check the coordinates are not off the grid
            return;
        //Store the initial coordinates of the block
        xPrev = newX;
        yPrev = newY;
        xCurr = newX;
        yCurr = newY;

        setX(newX);     //set coordinates of block
        setY(newY);

        numTV.setX(xCurr + tvOffset[0]);    //set position of TV
        numTV.setY(yCurr + tvOffset[1]);
        rl.addView(numTV);  //add TV to relative layout
        numTV.bringToFront();

        blockDirection = GameLoopTask.Direction.NO_MOVEMENT;        //initialize direction to none
    }

    public void removeViews(){
        this.rl.removeAllViews();
        return;
    }

    public void updateSize(int width, int height, int x, int y)   //Scales the size of the block dynamically. used when the block is created
    {
        getLayoutParams().width = width;
        getLayoutParams().height = height;
        updateCoordinates(x,y);
    }

    public int[] getCoord(){    //returns the coordinates of the current block
        int[] coord = new int[2];
        coord[0] = xCurr;
        coord[1] = yCurr;
        return coord;
    }

    public int[] getTarget(){   //returns the target coordinates of the current block
        int[] coord = new int[2];
        coord[0] = targetX;
        coord[1] = targetY;
        return coord;
    }
}
