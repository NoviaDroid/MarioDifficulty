/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

import java.util.Random;

/**
 *
 * @author stathis
 * We know. We can see that in the commit history. Also we don't really care.
 */
public class paramsPCG {
    
    public int width;
    public int height;
    public int seed;
    
    
    public int ODDS_STRAIGHT ; //(1-10)
    public int ODDS_HILL_STRAIGHT ;//(1-10)
    public int ODDS_TUBES ;//(1-10)
    public int ODDS_JUMP ;//(1-10)
    public int ODDS_CANNONS ;//(1-10)
    public int difficulty;
    
    
    public int GAP_SIZE ; //(2-5)
    public int MAX_COINS ;//(10-100)
    public int MAX_ENEMIES;
    
    
    public double reward;
    
    Random randomGenerator = new Random();
   
    
    public  paramsPCG(){
        
        width = 105; // 7 sections, including beginning&end, at 15 blocks each
        height = 15;
        
        newSeed();
        
        ODDS_STRAIGHT = 2  ; //(1-5)
        ODDS_HILL_STRAIGHT = 2;//(1-5)
        ODDS_TUBES = 2 ;//(1-5)
        ODDS_JUMP = 2;//(1-5)
        ODDS_CANNONS = 2;//(1-5)
        difficulty = 2;//(1-5)
        
        GAP_SIZE = 5; //(2-5)
        MAX_COINS = 5;//(10-100)
        MAX_ENEMIES = 20;
        
        
        //hill climbing params
        reward = 0;
       
    }  
    
    
    public void newSeed()
    {
        seed = randomGenerator.nextInt(100);
    }

    public paramsPCG copy()
    {
        paramsPCG c = new paramsPCG();
        
        c.width = width;
        c.height = height;
        c.ODDS_STRAIGHT = ODDS_STRAIGHT;
        c.ODDS_HILL_STRAIGHT = ODDS_HILL_STRAIGHT;
        c.ODDS_TUBES = ODDS_TUBES;
        c.ODDS_JUMP = ODDS_JUMP;
        c.ODDS_CANNONS = ODDS_CANNONS;
        c.GAP_SIZE = GAP_SIZE;
        c.MAX_COINS = MAX_COINS;
        c.MAX_ENEMIES = MAX_ENEMIES;
        c.difficulty = difficulty;
        c.seed = seed;
        c.reward = reward;
        
        return c;
    }
    
    public void randomizeParameters(){
        ODDS_STRAIGHT = randomGenerator.nextInt(5)+1; //(1-5)
        ODDS_HILL_STRAIGHT = randomGenerator.nextInt(5)+1;//(1-5)
        ODDS_TUBES = randomGenerator.nextInt(5)+1;//(1-5)
        ODDS_JUMP = randomGenerator.nextInt(5)+1;//(1-5)
        ODDS_CANNONS = randomGenerator.nextInt(5)+1;//(1-5)
        GAP_SIZE = randomGenerator.nextInt(4)+2;//(2-5)
    }
    
    public void incrementAll(){
        ODDS_STRAIGHT = ODDS_STRAIGHT%5+1; //(1-5)
        ODDS_HILL_STRAIGHT = ODDS_HILL_STRAIGHT%5+1;//(1-5)
        ODDS_TUBES = ODDS_TUBES%5+1;//(1-5)
        ODDS_JUMP = ODDS_JUMP%5+1;//(1-5)
        ODDS_CANNONS = ODDS_CANNONS%5+1;//(1-5)
        GAP_SIZE = (GAP_SIZE -1)%4+2;//(2-5)
    }
    
    public void clampValues(){
        if (ODDS_STRAIGHT > 5){
            ODDS_STRAIGHT = 5; //(1-5)
        } else if (ODDS_STRAIGHT < 1){
            ODDS_STRAIGHT = 1; //(1-5)
        }
        if (ODDS_HILL_STRAIGHT > 5){
            ODDS_HILL_STRAIGHT = 5; //(1-5)
        } else if (ODDS_HILL_STRAIGHT < 1){
            ODDS_HILL_STRAIGHT = 1; //(1-5)
        }
        if (ODDS_TUBES > 5){
            ODDS_TUBES = 5; //(1-5)
        } else if (ODDS_TUBES < 1){
            ODDS_TUBES = 1; //(1-5)
        }
        if (ODDS_JUMP > 5){
            ODDS_JUMP = 5; //(1-5)
        } else if (ODDS_JUMP < 1){
            ODDS_JUMP = 1; //(1-5)
        }
        if (ODDS_CANNONS > 5){
            ODDS_CANNONS = 5; //(1-5)
        } else if (ODDS_CANNONS < 1){
            ODDS_CANNONS = 1; //(1-5)
        }
        if (GAP_SIZE > 5){
            GAP_SIZE = 5; //(2-5)
        } else if (GAP_SIZE < 2){
            GAP_SIZE = 2; //(2-5)
        }
    }
    
    public int[] getSettingsInt(){
        return new int[]{   ODDS_STRAIGHT,
                            ODDS_HILL_STRAIGHT,
                            ODDS_TUBES,
                            ODDS_JUMP,
                            ODDS_CANNONS,
                            GAP_SIZE};
    }
    
    public double[] getSettingsDouble(){
        return new double[]{ODDS_STRAIGHT,
                            ODDS_HILL_STRAIGHT,
                            ODDS_TUBES,
                            ODDS_JUMP,
                            ODDS_CANNONS,
                            GAP_SIZE};
    }
    
    public void setSettingsInt(int[] settings){
        ODDS_STRAIGHT = settings[0];
        ODDS_HILL_STRAIGHT = settings[1];
        ODDS_TUBES = settings[2];
        ODDS_JUMP = settings[3];
        ODDS_CANNONS = settings[4];
        GAP_SIZE = settings[5];
        clampValues();
    }
    
    public void adjustSettingsInt(int[] settings){
        ODDS_STRAIGHT += settings[0];
        ODDS_HILL_STRAIGHT += settings[1];
        ODDS_TUBES += settings[2];
        ODDS_JUMP += settings[3];
        ODDS_CANNONS += settings[4];
        GAP_SIZE += settings[5];
        clampValues();
    }
}
