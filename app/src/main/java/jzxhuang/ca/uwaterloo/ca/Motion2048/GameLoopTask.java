package jzxhuang.ca.uwaterloo.ca.Motion2048;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

public class GameLoopTask extends TimerTask
{
    enum Direction { UP, DOWN, LEFT, RIGHT, NO_MOVEMENT }

    private Activity activity;
    private Context context;
    private RelativeLayout layout;
    private Direction currentDirection = Direction.NO_MOVEMENT;

    public boolean exist;       //prevents it from being created more than once onWindowChange
    public boolean endGame;     //checks if the game is complete (win or lose)

    public LinkedList<GameBlock> gbLL = new LinkedList<>();     //linked list that stores all the game blocks
    private Random randomGen;   //randomly generated number

    private TextView output;
    //Constructor: Passes in main activity, context, textview and the relativelayout
    GameLoopTask(Activity activity, Context context, RelativeLayout layout, TextView output)
    {
        this.activity = activity;
        this.context = context;
        this.layout = layout;
        this.output = output;
        endGame = false;
    }

    public void  createBlock()  //creates an instance of the block.
    {
        //-------- THIS CODE GENERATES BLOCK AT AN EMPTY TILE -----------
        int[] test = new int[2];        //tests if the tile is empty
        int emptyTiles = -1;            //number of empty tiles
        int offset = (int) (layout.getWidth()*0.037);       //offset
        boolean[][] valid = new boolean[4][4];                          //[row][column], false = empty, true = occupied
        for(GameBlock gb : gbLL) {      //traverse through linked list of game blocks
            emptyTiles = 0;
            for (int i = 0; i<4; i++){
                test[0] = 0;
                test[1] = offset/2 + layout.getHeight()*i/4;            //y coord (row)
                for (int j=0; j<4; j++){
                    test[0] = offset/2 + layout.getWidth()*j/4;         //x coord (column)
                    if(isOccupied(test[0], test[1])!= null){            //if tile is occupied, set true
                        valid[i][j] = true;
                    }
                    else{   //else set falses
                        valid[i][j] = false;
                        emptyTiles++;                                   //count number of empty tiles
                    }
                }
            }
        }
        if(emptyTiles == -1)emptyTiles = 16;    //if the for loop is never entered (i.e. no game blocks)
        if(emptyTiles == 0){                    //if no empty blocks. gg!
            endGame = true;
            output.setText("GAME OVER");
            output.setTextColor(Color.RED);
            return;
        }

        randomGen = new Random();
        //generate a random tile until an empty one is found
        boolean check = false;      //checks if the randomly generated location is empty
        int x = randomGen.nextInt(16);
        x = x%4;
        int y = randomGen.nextInt(16);
        y = y%4;
        while(check == false) {     //if empty move through tiles until an empty spot is found
            if (valid[y][x] == false)
                check = true;
            else {
                if (x < 3) {
                    x++;
                } else if (x == 3 & y < 3) {
                    x = 0;
                    y++;
                } else {
                    x = 0;
                    y = 0;
                }
            }
        }
        //-------- END OF CODE THAT GENERATES BLOCK AT AN EMPTY TILE -----------
        int[] myCoord = {x*layout.getWidth()/4, y*layout.getHeight()/4};    //set initial coordinates of the block to be created

        //int blockSizeOffset = (int) (layout.getWidth() * 0.037);
        int blockSize = (layout.getWidth() / 4) - offset;

        GameBlock newBlock = new GameBlock(context, layout, myCoord[0], myCoord[1], offset, layout.getWidth(), layout.getHeight(), this);   //create new block with the initial coordinates
        newBlock.updateSize(blockSize, blockSize, myCoord[0], myCoord[1]);  //update the block size

        gbLL.add(newBlock); //add block to LL
    }

    public void setDirection(Direction newDirection)
    {
        if (newDirection == GameLoopTask.Direction.NO_MOVEMENT) return; //if no movement
        //if(this.currentDirection != newDirection){
            this.currentDirection = newDirection;
            for(GameBlock gb : gbLL) {
                gb.blockDirection = newDirection;
                gb.setTarget();
            }
        //}
    }

    public GameBlock isOccupied(int coordX, int coordY){        //checks if a coordinate is occupied and returns the instance of the block if occupied
        int[] checkCoord = new int[2];
        for(GameBlock gb : gbLL){   //traverse through LL
            checkCoord = gb.getCoord(); //get coord and compare if they are equal
            if(checkCoord[0] == coordX && checkCoord[1] == coordY){
                Log.d("Game Loop Report: ", "Occupant Found!");
                return gb;
            }
        }
        return null;    //if no matches return null
    }

    public GameBlock targetOccupied(int coordX, int coordY){    //checks if the target coordinates are occupied.
        int[] checkCoord = new int[2];
        for(GameBlock gb : gbLL){
            checkCoord = gb.getTarget();
            if(checkCoord[0] == coordX && checkCoord[1] == coordY){
                Log.d("Game Loop Report: ", "Occupant Found!");
                return gb;
            }
        }
        return null;
    }

    public void restart(){
        for(GameBlock gb : gbLL){
            gb.removeViews();
        }
        gbLL.clear();
    }

    @Override
    public void run()
    {
        activity.runOnUiThread(
                new Runnable()
                {
                    public void run() {
                        boolean create = false;     //creates new block when this is true. waits for all blocks to finish moving
                        int index = -1;
                        if(endGame)                 //if game is over do nothing
                            return;

                        for( GameBlock gb : gbLL) { //traverse LL
                            gb.move();              //move blocks
                            if(gb.delete == true) { //if block needs to be deleted get the index
                                index = gbLL.indexOf(gb);
                            }
                            if(gb.blockNumber == 32) { //win condition
                                endGame = true;
                                output.setText("YOU WIN!!!");
                                output.setTextColor(Color.BLUE);
                            }
                        }
                        if(index >= 0)      //remove block that needs to be deleted
                            gbLL.remove(index);
                        for( GameBlock gb: gbLL){
                            if(gb.finished == false){   //checks if all blocks are done moving
                                create = false;
                                break;
                            }
                            else{
                                create = true;
                            }
                        }
                        if (create == true) {       //if blocks are done moving create a new block
                            createBlock();
                            for (GameBlock gb : gbLL) {
                                gb.finished = false;
                            }
                        }
                    }
                }
        );
    }
}
