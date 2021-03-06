package ch.idsia.mario.engine;

//import org.apache.log4j.Logger;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.NDC;

import Architect.paramsPCG;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.idsia.mario.MarioInterface.GamePlay;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.sprites.BulletBill;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Shell;
import ch.idsia.mario.engine.sprites.Sprite;

import ch.idsia.mario.engine.level.*;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.FlowerEnemy;
import ch.idsia.scenarios.Play;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class DataRecorder {

	public boolean recording = true;
	public ArchLevel level;
	private boolean []keys, keyPressed;
	public LevelScene levelScene;

	/**
	 * Time variables to record
	 */
	private int timeStart, timeEnd;
	private int completionTime; //counts only the current run on the level, excluding death games
	public int totalTime; //sums all the time, including from previous games if player died

	/**
	 * Jump variables to record
	 */
	private int totalJumpTime, startJumpTime, endJumpTime;
	private int timesJumped;
	public boolean isInAir;

	/**
	 * Duck variables to record
	 */
	private int totalDuckTime, startDuckTime, endDuckTime;
	private int timesDucked;

	/**
	 * Running variables to record
	 */
	private int totalRunTime, startRunTime, endRunTime;
	private int timesRun;

	/**
	 * Switching variables to record
	 */
	private int totalRightTime, totalLeftTime, startRightTime,  endRightTime, startLeftTime, endLeftTime;
	private int direction;

	/**
	 * How many kills the player has
	 */
	private int[] kills;
	private int fireKills;
	private int suicideKills;
	private int stompKills;
	private int shellKills;

	/**
	 * How many times dit the player die to the specific cause (monster or jump)
	 */
	private int[] deaths;

	/**
	 * How many coins did the player collect
	 */
	private int collectedCoins;

	/**
	 * How many blocks did the player destroy
	 */
	private int blocksEmptyDestroyed, blocksCoinDestroyed, blocksPowerDestroyed;

	/**
	 * How many shells the player has kicked
	 */
	private int shellsUnleashed;

	/**
	 * Power up time, how much time mario spent in what form
	 */
	private int totalLittleTime = 0;
	private int startLittleTime = 0;
	private int endLittleTime = 0;

	private int totalLargeTime = 0;
	private int startLargeTime = 0;
	private int endLargeTime = 0;

	private int totalFireTime = 0;
	private int startFireTime = 0;
	private int endFireTime = 0;

	private int switchedPower = 0;

	private boolean levelWon;
	public String detailedLog = "";

	public String getDetailedLog(){
		return detailedLog;
	}
	
	public DataRecorder(LevelScene levelScene, ArchLevel level, boolean []keys){
		this.levelScene = levelScene;
		this.level = level;
		this.keys = keys;

		keyPressed = new boolean[keys.length];

		reset();
	}

	public void reset(){
		kills = new int[7];
		deaths = new int[10]; //added one for the hole death and one for time death and one for shell

		//time reset
		completionTime = 0;
		timeStart = 0;
		timeEnd = 0;
		totalTime = 0;

		//jump reset
		timesJumped = 0;
		totalJumpTime = 0;
		startJumpTime = 0;
		endJumpTime = 0;
		isInAir = false;

		//duck reset
		timesDucked = 0;
		totalDuckTime = 0;
		startDuckTime = 0;
		endDuckTime = 0;

		//run reset
		timesRun = 0;
		totalRunTime = 0;
		startRunTime = 0;
		endRunTime = 0;

		//switch reset
		totalRightTime = 0;
		totalLeftTime = 0;
		startRightTime = 0;
		startLeftTime = 0;
		endRightTime = 0;
		endLeftTime = 0;

		//coins reset
		collectedCoins = 0;

		//blocks reset
		blocksEmptyDestroyed = 0;
		blocksCoinDestroyed = 0;
		blocksPowerDestroyed = 0;

		//shell reset
		shellsUnleashed = 0;

		//kill types
		fireKills = 0;
		suicideKills = 0;
		stompKills = 0;
		shellKills = 0;

		//power up types
		totalLittleTime = 0;
		startLittleTime = 0;
		endLittleTime = 0;

		totalLargeTime = 0;
		startLargeTime = 0;
		endLargeTime = 0;

		totalFireTime = 0;
		startFireTime = 0;
		endFireTime = 0;

		switchedPower = 0;

		levelWon = false;
	}

	public void tickRecord(){
		keysRecord();
	}

	public void levelWon(){
		levelWon = true;
	}

	public boolean getLevelWon(){
		return levelWon;
	}

	public void startTime(){
		if(timeStopped == true){
			timeStopped = false;
			timeStart = 2982 - levelScene.timeLeft;
                        detailedLog += "StartTime = "+ timeStart;
                        detailedLog += "\n";
		}
	}

	private boolean timeStopped = true;
	private long endGRight;

	public void endTime(){
		if(timeStopped == false){
			timeStopped = true;

			timeEnd = 2982 - levelScene.timeLeft;
			totalTime += timeEnd-timeStart;
			//;//System.out.println("******************************************");
			//;//System.out.println("tt:" + totalTime);
			//;//System.out.println("******************************************");
			completionTime = timeEnd-timeStart;
			detailedLog += "Totaltime = "+completionTime;
			detailedLog += "\n";
		}
	}

	/**
	 * Closes all of the recording, this should commit the data?
	 */
	public void stopRecord(){
		if(recording){
			recording = false;

			//time at current point
			recordJumpLand();
			endTime();

			switch(direction){
				case 1:
					endRightMoveRecord();
				break;
				case -1:
					endLeftMoveRecord();
				break;
			}

			if(levelScene.mario.running){
				endRunningRecord();
			}

			if(levelScene.mario.ducking){
				endDuckRecord();
			}

			if(Mario.large && !Mario.fire){
				endLargeRecord();
			}

			if(Mario.fire){
				endFireRecord();
			}

			if(!Mario.fire && !Mario.large){
				endLittleRecord();
			}
		}
	}

	public void startRightMoveRecord(){
		startRightTime = 2982 - levelScene.timeLeft;
		direction = 1;
	}

	public void startLeftMoveRecord(){
		startLeftTime = 2982 - levelScene.timeLeft;
		direction = -1;
	}

	public void endRightMoveRecord(){
		endRightTime = 2982 - levelScene.timeLeft;

		totalRightTime += endRightTime - startRightTime;
		detailedLog += "RightMove: StTime = "+startRightTime +" EdTime = "+totalRightTime;
		detailedLog += "\n";
	}

	public void endLeftMoveRecord(){
            
		endLeftTime = 2982 - levelScene.timeLeft;
		totalLeftTime += endLeftTime - startLeftTime;
		detailedLog += "LeftMove: StTime = "+startLeftTime +" EdTime = "+totalLeftTime;
		detailedLog += "\n";
		

	}

	public void startDuckRecord(){
		if(!levelScene.mario.ducking){
			timesDucked++;

			startDuckTime = 2982 - levelScene.timeLeft;

			//;//System.out.println("START DUCK");
		}
	}

	public void endDuckRecord(){
		if(levelScene.mario.ducking){
			endDuckTime = 2982 - levelScene.timeLeft;

			totalDuckTime += endDuckTime - startDuckTime;

			//;//System.out.println("END DUCK");
			
			//;//System.out.println("END DUCK");
			
			detailedLog += "Duck: StTime = "+startDuckTime +" EdTime = "+endDuckTime;
			detailedLog += "\n";

		}
	}

	private boolean littleRecording = false;

	public void startLittleRecord(){
		if(!littleRecording){
			littleRecording = true;

			switchedPower++;

			//;//System.out.println("------------------- "+switchedPower+" -------------------");

			startLittleTime = 2982 - levelScene.timeLeft;

			//;//System.out.println("LITTLE START: " + startLittleTime);
		}
	}

	public void endLittleRecord(){
		if(littleRecording){
			littleRecording = false;
			endLittleTime = 2982 - levelScene.timeLeft;

			totalLittleTime += endLittleTime - startLittleTime;

			//;//System.out.println("LITTLE END: "+endLittleTime);
			//;//System.out.println("TOTAL LITTLE END: " + totalLittleTime);
			//;//System.out.println("LITTLE END: "+endLittleTime);
			detailedLog += "LittleState: StTime = "+startLittleTime +" EdTime = "+endLittleTime;
			detailedLog += "\n";
		}
	}

	public void startLargeRecord(){
		switchedPower++;

		//;//System.out.println("------------------- "+switchedPower+" -------------------");

		startLargeTime = 2982 - levelScene.timeLeft;

		//;//System.out.println("LARGE START");
	}

	public void endLargeRecord(){
		endLargeTime = 2982 - levelScene.timeLeft;

		totalLargeTime += endLargeTime - startLargeTime;

		//;//System.out.println("LARGE END");
		
		detailedLog += "LargeState: StTime = "+startLargeTime +" EdTime = "+endLargeTime;
		detailedLog += "\n";
		
	
	}

	public void startFireRecord(){
		switchedPower++;

		//;//System.out.println("------------------- "+switchedPower+" -------------------");

		startFireTime = 2982 - levelScene.timeLeft;

		//;//System.out.println("FIRE START");
	}

	public void endFireRecord(){
		endFireTime = 2982 - levelScene.timeLeft;

		totalFireTime += endFireTime - startFireTime;

		//;//System.out.println("FIRE END");
		detailedLog += "FireState: StTime = "+startFireTime +" EdTime = "+endFireTime;
		detailedLog += "\n";
	}

	public void startRunningRecord(){
		if(!levelScene.mario.running){
			timesRun++;

			startRunTime = 2982 - levelScene.timeLeft;

			//System.out.println("START RUN");
		}
	}

	public void endRunningRecord(){
		if(levelScene.mario.running){

			

			endRunTime = 2982 - levelScene.timeLeft;

			totalRunTime += endRunTime - startRunTime;
			//System.out.println("END RUN" + totalRunTime);
			detailedLog += "RunState: StTime = "+startRunTime +" EdTime = "+endRunTime;
			detailedLog += "\n";			
			
		}
	}

	public void fireKillRecord(Sprite sprite){
		killRecord(sprite);
		int enemyType = 0;
		if(sprite instanceof FlowerEnemy){
			detailedLog += "FireKill:  EnemyType = FlowerEnemy  time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";		
		}
		else if(sprite instanceof BulletBill){// cannon shot
		}
		else if(sprite instanceof Shell){
		}
		else if(sprite instanceof Enemy){
			Enemy enemy = (Enemy)sprite;
			detailedLog += "FireKill:  EnemyType ="+ enemy.type +"time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
		}
		fireKills++;
		//;//System.out.println(" fire kill ");
	}

	public void shellKillRecord(Sprite sprite){
		killRecord(sprite);
		if(sprite instanceof FlowerEnemy){
			detailedLog += "ShellKill:  EnemyType = FlowerEnemy time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
		}
		else if(sprite instanceof BulletBill){//cannon shot
		}
		else if(sprite instanceof Shell){
			detailedLog += "ShellKill:  EnemyType = Turtle time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
		}
		else if(sprite instanceof Enemy){
			Enemy enemy = (Enemy)sprite;
			detailedLog += "ShellKill:  EnemyType = "+enemy.type+" time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
		}
		
		shellKills++;
		//;//System.out.println(" shell kill ");
	}

	public void killSuicideRecord(Sprite sprite){
		killRecord(sprite);
		suicideKills++;
		//;//System.out.println(" suicide ");
	}

	public void killStompRecord(Sprite sprite){
		killRecord(sprite);
		if(sprite instanceof FlowerEnemy){
			detailedLog += "StompKill:  EnemyType = FlowerEnemy time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
			
		}
		else if(sprite instanceof BulletBill){// cannon shot
			detailedLog += "StompKill:  EnemyType = BulletBill time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";				
		}
		else if(sprite instanceof Shell){
//			levelScene.levelRecorder.enemyR.interact.add(new DataEntry(EnemyRecorder.GREEN_TURTLE, EnemyRecorder.UNLEASHED, 2982 - levelScene.timeLeft,x,y));
					}
		else if(sprite instanceof Enemy){
			Enemy enemy = (Enemy)sprite;
			detailedLog += "StompKill:  EnemyType = "+enemy.type+" time = "+ (2982 - levelScene.timeLeft);
			detailedLog += "\n";	
			
		}
		stompKills++;
		//;//System.out.println(" stomp ");
	}

	public void killRecord(Sprite sprite){
		//something goes wrong with the type of the flower enemy, this is special case
		if(sprite instanceof FlowerEnemy){

			kills[SpriteTemplate.JUMP_FLOWER]++;
		}
		else if(sprite instanceof BulletBill){
			kills[5]++;
		}
		else if(sprite instanceof Shell){
			//not sure what to do with shells
		}
		else if(sprite instanceof Enemy){
			Enemy enemy = (Enemy)sprite;

			kills[enemy.type]++;
		}

		printKills();
	}

	public void blockEmptyDestroyRecord(){
		
		blocksEmptyDestroyed++;
	}

	public void blockCoinDestroyRecord(){
		detailedLog += "BlockCoinDestroy:  time = "+ (2982 - levelScene.timeLeft);
		detailedLog += "\n";	
		blocksCoinDestroyed++;
	}

	public void blockPowerDestroyRecord(){
		detailedLog += "BlockPowerDestroy:  time = "+ (2982 - levelScene.timeLeft);
		detailedLog += "\n";	
		blocksPowerDestroyed++;
	}

	public void dieRecord(Sprite sprite){
		if(sprite instanceof FlowerEnemy){
			detailedLog += "Die:  EnemyType = FlowerEnemy ";
			detailedLog += "\n";	
			deaths[SpriteTemplate.JUMP_FLOWER]++;
		}
		else if(sprite instanceof BulletBill){
			detailedLog += "Die:  EnemyType = BulletBill ";
			detailedLog += "\n";
			deaths[5]++;
		}
		else if(sprite instanceof Shell){
			//not sure what to do with shells
			detailedLog += "Die:  EnemyType = TurtleShell ";
			detailedLog += "\n";
			deaths[9]++;
		}
		else if(sprite instanceof Enemy){
			Enemy enemy = (Enemy)sprite;
			deaths[enemy.type]++;
			detailedLog += "Die:  EnemyType = "+enemy.type;
			detailedLog += "\n";
			
		}


	}

	public void dieTimeRecord(){
		//time
		deaths[7]++;

	}

	public void dieJumpRecord(){
		//jump
		deaths[8]++;
		detailedLog += "Die:  Gap ";
		detailedLog += "\n";

	}

	public void shellUnleashedRecord(){
		shellsUnleashed++;
		//;//System.out.println(" shell unleased");
		detailedLog += "UnleashShell:  time = "+ (2982 - levelScene.timeLeft);
		detailedLog += "\n";
	}

	private void keysRecord(){
		if(keys[Mario.KEY_LEFT] && !keyPressed[Mario.KEY_LEFT]){
			keyPressed[Mario.KEY_LEFT] = true;
		}
		else if(!keys[Mario.KEY_LEFT]){
			keyPressed[Mario.KEY_LEFT] = false;
		}
	}

	public void recordJump(){
		if(isInAir){
			timesJumped++;
			startJumpTime = 2982 -levelScene.timeLeft;
		}
	}

	public void recordJumpLand(){
		if(isInAir){
			isInAir = false;
			endJumpTime = 2982-levelScene.timeLeft;

			totalJumpTime += endJumpTime - startJumpTime;
				
			detailedLog += "Jump:  StTime = "+ startJumpTime +" EdTime = "+endJumpTime;
			detailedLog += "\n";
		}
	}

	public void recordCoin(){
		detailedLog += "CollectCoin:  time = "+ (2982 - levelScene.timeLeft);
		detailedLog += "\n";
		collectedCoins++;
	}

	private int convertTime(int time){
		return (int)Math.floor((time+15-1)/15);
	}

	public void printAll(){

		printKills();
		printTime();
		printJump();
		printDuck();
		printRun();
		printSwitching();

		//;//System.out.println("total fire: " + convertTime( totalFireTime) + " total large: " + convertTime(totalLargeTime) + " total little: " + convertTime(totalLittleTime));
	}

	private void printSwitching(){
		//printStart("Switch Variables");
		//;//System.out.println("Time Spent Moving Right: " + convertTime(totalRightTime) + " ("+Math.round((double)convertTime(totalRightTime)/(double)convertTime(totalTime)*(double)100)+"%)");
		//;//System.out.println("Time Spent Moving Left: " + convertTime(totalLeftTime) + " ("+Math.round((double)convertTime(totalLeftTime)/(double)convertTime(totalTime)*(double)100)+"%)");
		//;//System.out.println("Time Spent Standing Still: " + (convertTime(totalTime)-convertTime(totalLeftTime)-convertTime(totalRightTime)) + " ("+Math.round((double)(convertTime(totalTime)-convertTime(totalLeftTime)-convertTime(totalRightTime))/(double)convertTime(totalTime)*(double)100)+"%)");

		//printEnd();
	}

	private void printJump(){
		//printStart("Jump Variables");
		//;//System.out.println("Number of Times Jumped: " + timesJumped);
		//;//System.out.println("Time Spent Jumping: " + convertTime(totalJumpTime) + " ("+Math.round((double)convertTime(totalJumpTime)/(double)convertTime(totalTime)*(double)100)+"%)");
		//printEnd();
	}

	private void printRun(){
		//printStart("Run Variables");
		//;//System.out.println("Number of Times Run: " + timesRun);
		//;//System.out.println("Time Spent Running: " + convertTime(totalRunTime) + " ("+Math.round((double)convertTime(totalRunTime)/(double)convertTime(totalTime)*100)+"%)");
		//printEnd();
	}

	private void printDuck(){
		//printStart("Duck Variables");
		//;//System.out.println("Number of Times Ducked: " + timesDucked);
		//;//System.out.println("Time Spent Ducking: " + convertTime(totalDuckTime) + " ("+Math.round((double)convertTime(totalDuckTime)/(double)convertTime(totalTime)*(double)100)+"%)");
		//printEnd();
	}

	private void printTime(){
		//printStart("Time Variables");
		//;//System.out.println("Total Completion Time: " + convertTime(totalTime));
		//;//System.out.println("Total Last Time: " + convertTime(completionTime));
		//printEnd();
	}

	private void printDeaths(){
		//printStart("Player Died Against");
		int deathsTotal = 0;

		for(int i=0;i<deaths.length;++i){
			String type = "";

			switch(i){
				case SpriteTemplate.RED_TURTLE:
					type = "Red Koopa";
				break;
				case SpriteTemplate.GREEN_TURTLE:
					type = "Green Koopa";
				break;
				case SpriteTemplate.GOOMPA:
					type = "Goompa";
				break;
				case SpriteTemplate.ARMORED_TURTLE:
					type = "Spikey Turtle";
				break;
				case SpriteTemplate.JUMP_FLOWER:
					type = "Jumping Flower";
				break;
				case SpriteTemplate.CANNON_BALL:
					type = "Cannon Ball";
				break;
				case SpriteTemplate.CHOMP_FLOWER:
					type = "Chomping Flower";
				break;
				case 7:
					type = "Time";
				break;
				case 8:
					type = "Hole";
				break;
				case 9:
					type = "Shell";
				break;
			}

			//;//System.out.println(type + " " + deaths[i] + " times.");

			deathsTotal+= deaths[i];
		}

		//;//System.out.println("\nPlayer died a total of " + deathsTotal + " times");

		//printEnd();

	}

	private void printKills(){
		//printStart("Player Has Killed");

		for(int i=0;i<kills.length;++i){
			String type = "";

			switch(i){
				case SpriteTemplate.RED_TURTLE:
					type = "Red Koopa";

				break;
				case SpriteTemplate.GREEN_TURTLE:
					type = "Green Koopa";
				break;
				case SpriteTemplate.GOOMPA:
					type = "Goompa";
				break;
				case SpriteTemplate.ARMORED_TURTLE:
					type = "Spikey Turtle";
				break;
				case SpriteTemplate.JUMP_FLOWER:
					type = "Jumping Flower";
				break;
				case SpriteTemplate.CANNON_BALL:
					type = "Cannon Ball";
				break;
				case SpriteTemplate.CHOMP_FLOWER:
					type = "Chomping Flower";
				break;
			}

			int percentage = 0;

		}

		//printEnd();
	}

	private void printStart(String title){
		title = " "+title+" ";

		int tweak = 0;
		if(title.length()%2!=0) //unequal number
			tweak = 1;


		for(int i=0;i<50/2-title.length()/2;++i)
			;//System.out.print(">");

		;//System.out.print(title);

		for(int i=0;i<50/2-title.length()/2 - tweak;++i)
			;//System.out.print("<");

		;//System.out.print("\n");

	}

	private void printEnd(){
		//;//System.out.println("------------------- "+switchedPower+" -------------------");

		for(int i=0;i<50;++i)
			;//System.out.print("-");

		;//System.out.print("\n");
	}

	public GamePlay fillGamePlayMetrics(int segment_diff, boolean verbose , double[] reward_weights,paramsPCG m, ArchLevel level  ) throws IllegalArgumentException, IllegalAccessException{
                //fillGamePlayMetrics
                //-at the moment, only called at swapping to new level segment
                //-should also be called in LevelSceneTest.winActions() + deathActions()
                GamePlay gpm = new GamePlay();
                gpm.completionTime = getCompletionTime();
		gpm.totalTime = getTotalTime();////sums all the time, including from previous games if player died

                gpm.jumpsNumber = getTimesJumped();
		gpm.timeSpentDucking = getTotalDuckTime();
		gpm.duckNumber = getTimesDucked();
		gpm.timeSpentRunning = getTotalRunTime();
		gpm.timesPressedRun = getTimesRun();
		gpm.timeRunningRight = getTotalRightTime();
		gpm.timeRunningLeft =  getTotalLeftTime();
                
                gpm.coinsCollected =  getCoinsCollected();
                gpm.totalCoins = level.COINS;
                
                gpm.emptyBlocksDestroyed = getBlocksEmptyDestroyed();
		gpm.totalEmptyBlocks = level.BLOCKS_EMPTY;
		gpm.coinBlocksDestroyed = getBlocksCoinDestroyed();
		gpm.totalCoinBlocks = level.BLOCKS_COINS;
		gpm.powerBlocksDestroyed = getBlocksPowerDestroyed();
		gpm.totalpowerBlocks = level.BLOCKS_POWER;
		gpm.kickedShells =  getShellsUnleashed(); //kicked
		gpm.enemyKillByFire = getKillsFire(); //Number of Kills by Shooting Enemy
		gpm.enemyKillByKickingShell = getKillsShell(); //Number of Kills by Kicking Shell on Enemy
		gpm.totalEnemies = level.ENEMIES;

		gpm.totalTimeLittleMode = getTotalLittleTime(); //Time Spent Being Small Mario
		gpm.totalTimeLargeMode = getTotalLargeTime(); //Time Spent Being Large Mario
		gpm.totalTimeFireMode = getTotalFireTime(); //Time Spent Being Fire Mario
		gpm.timesSwichingPower = getSwitchedPower(); //Number of Times Switched Between Little, Large or Fire Mario
		gpm.aimlessJumps = J(); //aimless jumps
		gpm.percentageBlocksDestroyed = nb(); //percentage of all blocks destroyed
		gpm.percentageCoinBlocksDestroyed = ncb(); //percentage of coin blocks destroyed
		gpm.percentageEmptyBlockesDestroyed = neb(); //percentage of empty blocks destroyed
		gpm.percentagePowerBlockDestroyed = np(); //percentage of power blocks destroyed
                
                gpm.timesOfDeathByFallingIntoGap = dg(); //number of death by falling into a gap
		gpm.timesOfDeathByRedTurtle = deaths[SpriteTemplate.RED_TURTLE];
		gpm.timesOfDeathByGreenTurtle = deaths[SpriteTemplate.GREEN_TURTLE];
		gpm.timesOfDeathByGoomba = deaths[SpriteTemplate.GOOMPA];
		gpm.timesOfDeathByArmoredTurtle = deaths[SpriteTemplate.ARMORED_TURTLE];
		gpm.timesOfDeathByJumpFlower = deaths[SpriteTemplate.JUMP_FLOWER];
		gpm.timesOfDeathByCannonBall = deaths[SpriteTemplate.CANNON_BALL];
		gpm.timesOfDeathByChompFlower = deaths[SpriteTemplate.CHOMP_FLOWER];
                
		gpm.RedTurtlesKilled = kills[SpriteTemplate.RED_TURTLE];
		gpm.GreenTurtlesKilled = kills[SpriteTemplate.GREEN_TURTLE];
		gpm.GoombasKilled = kills[SpriteTemplate.GOOMPA];
		gpm.ArmoredTurtlesKilled = kills[SpriteTemplate.ARMORED_TURTLE];
		gpm.JumpFlowersKilled = kills[SpriteTemplate.JUMP_FLOWER];
		gpm.CannonBallKilled = kills[SpriteTemplate.CANNON_BALL];
		gpm.ChompFlowersKilled = kills[SpriteTemplate.CHOMP_FLOWER];
               
                //Verbose debugging output
                if (verbose) {
                    ;//System.out.println("");
                    ;//System.out.println("fillGamePlayMetrics() called, metric for this segment are...");

                    ;//System.out.println("-completionTime: " + getCompletionTime() );
                    ;//System.out.println("-totalTime: " + getTotalTime() );

                    ;//System.out.println("-jumpsNumber: " + getTimesJumped() );
                    ;//System.out.println("-timeSpentDucking: " + getTotalDuckTime() );
                    ;//System.out.println("-duckNumber: " + getTimesDucked() );
                    ;//System.out.println("-timeSpentRunning: " + getTotalRunTime() );
                    ;//System.out.println("-timesPressedRun: " + getTimesRun() );
                    ;//System.out.println("-timeRunningRight: " + getTotalRightTime() );
                    ;//System.out.println("-timeRunningLeft: " + getTotalLeftTime() );

                    ;//System.out.println("-coinsCollected: " + getCoinsCollected());
                    ;//System.out.println("-totalCoins: " + level.COINS);            

                    ;//System.out.println("-emptyBlocksDestroyed: " + getBlocksEmptyDestroyed() );
                    ;//System.out.println("-totalEmptyBlocks: " + level.BLOCKS_EMPTY );
                    ;//System.out.println("-coinBlocksDestroyed: " + getBlocksCoinDestroyed() );
                    ;//System.out.println("-totalCoinBlocks: " + level.BLOCKS_COINS );
                    ;//System.out.println("-powerBlocksDestroyed: " + getBlocksPowerDestroyed() );
                    ;//System.out.println("-totalpowerBlocks: " + level.BLOCKS_POWER );
                    ;//System.out.println("-kickedShells: " + getShellsUnleashed() );
                    ;//System.out.println("-enemyKillByFire: " + getKillsFire() );
                    ;//System.out.println("-enemyKillByKickingShell: " + getKillsShell() );
                    ;//System.out.println("-totalEnemies: " + level.ENEMIES );

                    ;//System.out.println("-totalTimeLittleMode: " + getTotalLittleTime());
                    ;//System.out.println("-totalTimeLargeMode: " + getTotalLargeTime());
                    ;//System.out.println("-totalTimeFireMode: " + getTotalFireTime());
                    ;//System.out.println("-timesSwichingPower: " + getSwitchedPower());
                    ;//System.out.println("-aimlessJumps: " + J());
                    ;//System.out.println("-percentageBlocksDestroyed: " + nb());
                    ;//System.out.println("-percentageCoinBlocksDestroyed: " + ncb());
                    ;//System.out.println("-percentageEmptyBlockesDestroyed: " + neb());
                    ;//System.out.println("-percentagePowerBlockDestroyed: " + np());               

                    ;//System.out.println("-timesOfDeathByFallingIntoGap: " + dg() );               
                    ;//System.out.println("-timesOfDeathByRedTurtle: " + deaths[SpriteTemplate.RED_TURTLE] );               
                    ;//System.out.println("-timesOfDeathByGreenTurtle: " + deaths[SpriteTemplate.GREEN_TURTLE] );               
                    ;//System.out.println("-timesOfDeathByGoomba: " + deaths[SpriteTemplate.GOOMPA]);               
                    ;//System.out.println("-timesOfDeathByArmoredTurtle: " + deaths[SpriteTemplate.ARMORED_TURTLE] );               
                    ;//System.out.println("-timesOfDeathByJumpFlower: " + deaths[SpriteTemplate.JUMP_FLOWER] );               
                    ;//System.out.println("-timesOfDeathByCannonBall: " + deaths[SpriteTemplate.CANNON_BALL] );               
                    ;//System.out.println("-timesOfDeathByChompFlower: " + deaths[SpriteTemplate.CHOMP_FLOWER] );               

                    ;//System.out.println("-RedTurtlesKilled: " + kills[SpriteTemplate.RED_TURTLE]);
                    ;//System.out.println("-GreenTurtlesKilled: " + kills[SpriteTemplate.GREEN_TURTLE]);
                    ;//System.out.println("-GoombasKilled: " + kills[SpriteTemplate.GOOMPA]);
                    ;//System.out.println("-ArmoredTurtlesKilled: " + kills[SpriteTemplate.ARMORED_TURTLE]);
                    ;//System.out.println("-JumpFlowersKilled: " + kills[SpriteTemplate.JUMP_FLOWER]);
                    ;//System.out.println("-CannonBallKilled: " + kills[SpriteTemplate.CANNON_BALL]);
                    ;//System.out.println("-ChompFlowersKilled: " + kills[SpriteTemplate.CHOMP_FLOWER]);
                }
                
                //Write metric to file in weird hexformat
		gpm.write("player.txt");
		
		// for Architect
		gpm.k_T = kT();
		gpm.t_L = tL();
		gpm.k_P = kP();
		gpm.t_r = tr();
		gpm.d_j = dj();
		gpm.n_c = nc();
		gpm.n_I = nI();
		gpm.t_s = ts();
		gpm.k_f = kf();

                //Create string of POMDP metrics to be written to log file
                //detailedLog += "StartTime = "+ timeStart;
                //detailedLog += "\n";
                //First the in-game metrics as above
                String POMDPmetrics = "";
                POMDPmetrics += getCompletionTime() + ", ";
                POMDPmetrics += getTotalTime() + ", ";
                
                POMDPmetrics += getTimesJumped() + ", ";
                POMDPmetrics += getTotalDuckTime() + ", ";
                POMDPmetrics += getTimesDucked() + ", ";
                POMDPmetrics += getTotalRunTime() + ", ";
                POMDPmetrics += getTimesRun() + ", ";
                POMDPmetrics += getTotalRightTime() + ", ";
                POMDPmetrics += getTotalLeftTime() + ", ";
                
                POMDPmetrics += getCoinsCollected() + ", ";
                POMDPmetrics += level.COINS + ", ";
                
                POMDPmetrics += getBlocksEmptyDestroyed() + ", ";
                POMDPmetrics += level.BLOCKS_EMPTY + ", ";
                POMDPmetrics += getBlocksCoinDestroyed() + ", ";
                POMDPmetrics += level.BLOCKS_COINS + ", ";
                POMDPmetrics += getBlocksPowerDestroyed() + ", ";
                POMDPmetrics += level.BLOCKS_POWER + ", ";
                POMDPmetrics += getShellsUnleashed() + ", ";
                POMDPmetrics += getKillsFire() + ", ";
                POMDPmetrics += getKillsShell() + ", ";
                POMDPmetrics += level.ENEMIES + ", ";
                
                POMDPmetrics += getTotalLittleTime() + ", ";
                POMDPmetrics += getTotalLargeTime() + ", ";
                POMDPmetrics += getTotalFireTime() + ", ";
                POMDPmetrics += getSwitchedPower() + ", ";
                POMDPmetrics += J() + ", ";
                POMDPmetrics += nb() + ", ";
                POMDPmetrics += ncb() + ", ";
                POMDPmetrics += neb() + ", ";
                POMDPmetrics += np() + ", ";
                
                POMDPmetrics += dg() + ", ";
                POMDPmetrics += deaths[SpriteTemplate.RED_TURTLE] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.GREEN_TURTLE] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.GOOMPA] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.ARMORED_TURTLE] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.JUMP_FLOWER] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.CANNON_BALL] + ", ";
                POMDPmetrics += deaths[SpriteTemplate.CHOMP_FLOWER] + ", ";
                
                POMDPmetrics += kills[SpriteTemplate.RED_TURTLE] + ", ";
                POMDPmetrics += kills[SpriteTemplate.GREEN_TURTLE] + ", ";
                POMDPmetrics += kills[SpriteTemplate.GOOMPA] + ", ";
                POMDPmetrics += kills[SpriteTemplate.ARMORED_TURTLE] + ", ";
                POMDPmetrics += kills[SpriteTemplate.JUMP_FLOWER] + ", ";
                POMDPmetrics += kills[SpriteTemplate.CANNON_BALL] + ", ";
                POMDPmetrics += kills[SpriteTemplate.CHOMP_FLOWER] + ", ";
                
                POMDPmetrics += (int) m.ODDS_STRAIGHT + ", ";
                POMDPmetrics += (int)m.ODDS_HILL_STRAIGHT + ", ";
                POMDPmetrics += (int) m.ODDS_TUBES + ", ";
                POMDPmetrics +=(int)m.ODDS_JUMP + ", ";
                POMDPmetrics += (int) m.ODDS_CANNONS + ", ";
                POMDPmetrics += m.difficulty + ", ";
               
                //Third the expressed appropriateness of this difficulty level, as expressed by the user
                //POMDPmetrics += randomNumber(0,2) + ", "; //currently random for sake of completion
                //POMDPmetrics += 0 + ", "; //currently 0 as it will be hand entried with actual data
                //%OLD-@ATTRIBUTE appropriateness {0,1}
                //POMDPmetrics += "?, "; //currently ? as it will be hand entried with actual data
                //NEW
                //@ATTRIBUTE label-engagement {1,2,3,4,5,6,7}
                //@ATTRIBUTE label-frustration {1,2,3,4,5,6,7}
                //@ATTRIBUTE label-challenge {1,2,3,4,5,6,7}
                //POMDPmetrics += "?, "; //currently ? as it will be hand entried with actual data
                //POMDPmetrics += "?, "; //currently ? as it will be hand entried with actual data
                //POMDPmetrics += "?, "; //currently ? as it will be hand entried with actual data
                POMDPmetrics += randomNumber(1, 8) + ", "; //currently random for testing purposes - should be input with actual data in training process
                POMDPmetrics += randomNumber(1, 8) + ", "; //currently random for testing purposes - should be input with actual data in training process
                
                
                // define some gimmick tasks
                // coin task coins_per_level - coins_collected
                
                // coin task
                // float custom_task = level.COINS/10;
                
                // block destroy task
                //float custom_task = ((level.BLOCKS_EMPTY + level.BLOCKS_COINS + level.BLOCKS_POWER)/3) / 10;
                
                // enemy task
                //float custom_task = level.ENEMIES/10;
                
                // shell task
                //float custom_task =  m.MAX_TURTLES/10;
                
                // cannon task
                //float custom_task = m.ODDS_CANNONS/10;
                
                // gap task
                //float custom_task = m.ODDS_JUMP/10;
//                double[] rewards = {0.0 ,0.33 ,1 ,0.33 ,0.0 };
//                double custom_task = 0.0;
//                for(int i = 0 ; i < reward_weights.length; i++)
//                    custom_task += reward_weights[i]*rewards[i];
                int reward_label = level.getCustomRewards(Play.taskType(), gpm);
                
//                //System.out.println("Rewards is:" + reward_label);
//                //System.out.println(reward_label);
                if(reward_label>5)reward_label = 5;
                if(reward_label<1)reward_label = 1;
//                double custom_task = rewards[reward_label-1];
                //System.out.println("END RUN" + totalRunTime);
//                System.out.println("Rewards is:");
//                System.out.println(custom_task);
                
                POMDPmetrics += reward_label + ", "; //currently random for testing purposes - should be input with actual data in training process
                
                //Add date + time stamp
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                POMDPmetrics += timeStamp;
                
                //Write observation string without ending \n to gpm.POMDPmetrics
                gpm.POMDPmetrics = POMDPmetrics;                
                ;//System.out.println("");
                ;//System.out.println("POMDPmetrics coming up...");
                ;//System.out.println(POMDPmetrics);
                
                POMDPmetrics += "\n"; //new line
                           
		//Write metrics relevant for POMDP to sander.txt file
                writePOMDP(POMDPmetrics);
                
		//Write detailedLog that lists jump actions and other barely relevant stuff
                //;//System.out.println(detailedLog);
		//write(detailedLog);
		return gpm;
	}

	private void write(String detailedLogName) {
		try {
			FileWriter file = new FileWriter(new File("DetailedInfo.txt"));
			file.write(detailedLogName);
			file.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

	private void writePOMDP(String detailedLogName) {
            try
            {
                String filename= "MarioPOMDP-testinstances.arff";
                FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                fw.write(detailedLogName); //appends the string to the file
                fw.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }          
            /*
                try {
			FileWriter file = new FileWriter(new File("POMDPmetrics.txt"));
			file.write(detailedLogName);
			file.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
                */
	}        

        private int randomNumber(int low, int high){
		return new Random(new Random().nextLong()).nextInt(high-low)+low;
	}		
               
	public int getCompletionTime(){
		return convertTime(completionTime);
	}

	public int getTotalTime(){
		return convertTime(totalTime);
	}

	public int getTotalJumpTime(){
		return convertTime(totalJumpTime);
	}

	public int getTimesJumped(){
		return timesJumped;
	}

	public int getTotalDuckTime(){
		return convertTime(totalDuckTime);
	}

	public int getTimesDucked(){
		return timesDucked;
	}

	public int getTotalRunTime(){
		return convertTime(totalRunTime);
	}

	public int getTimesRun(){
		return timesRun;
	}

	public int getTotalRightTime(){
		return convertTime(totalRightTime);
	}

	public int getTotalLeftTime(){
		return convertTime(totalLeftTime);
	}

	public int getCoinsCollected(){
		return collectedCoins;
	}

	public int getBlocksEmptyDestroyed(){
		return blocksEmptyDestroyed;
	}

	public int getBlocksCoinDestroyed(){
		return blocksCoinDestroyed;
	}

	public int getBlocksPowerDestroyed(){
		return blocksPowerDestroyed;
	}

	public int getKills(int monster){
		return kills[monster];
	}

	public int getDeaths(int cause){
		return deaths[cause];
	}

	public int getShellsUnleashed(){
		return shellsUnleashed;
	}

	public int getKillsStomp(){
		return stompKills;
	}

	public int getKillsFire(){
		return fireKills;
	}

	public int getKillsShell(){
		return shellKills;
	}

	public  int getKillsSuicide(){
		return suicideKills;
	}

	public int getTotalLittleTime(){
		return convertTime(totalLittleTime);
	}

	public int getTotalLargeTime(){
		return convertTime(totalLargeTime);
	}

	public int getTotalFireTime(){
		return convertTime(totalFireTime);
	}

	public int getSwitchedPower(){
		return switchedPower;
	}


	/**
	 * The total time taken to complete a level
	 * @return
	 */
	public double tc(){
		return (double)getTotalTime();
	}

	public double tL(){
		//;//System.out.println("measure 1:" + getTotalTime());
		//;//System.out.println("measure 2:" + getTotalLeftTime());
		
		return (double)getTotalLeftTime()/(double)getTotalTime();
	}

	/**
	 * time in large form
	 * @return
	 */
	public double tl(){
		return (double)getTotalLargeTime()/(double)getTotalTime();
	}

	/**
	 * Time in tiny mario form
	 * @return
	 */
	public double tt(){
		return (double)getTotalLittleTime()/(double)getTotalTime();
	}

	/**
	 * Time spent running in percent
	 * @return
	 */
	public double tr(){
		return (double)getTotalRunTime()/(double)getTotalTime();
	}

	/**
	 * Time spent in powerup form
	 * @return
	 */
	public double tp(){
		return 1-((double)getTotalLittleTime()/(double)getTotalTime());
	}

	/**
	 * Time spent in fire mario form
	 * @return
	 */
	public double tf(){
		return (double)getTotalFireTime()/(double)getTotalTime();
	}

	public double tR(){
		return (double)getTotalRightTime()/(double)getTotalTime();
	}

	public double ks(){
		if(getKillsStomp()+getKillsFire() == 0){
			return 0;
		}
		else
			return (double)getKillsStomp()/(double)(getKillsStomp()+getKillsFire());
	}

	public double kf(){
		if(getKillsFire() == 0)
			return 0;
		else
			return (double)getKillsFire()/(double)(getKillsFire()+getKillsStomp());
	}
	
	/**
	 * 
	 *  for Architect
	 * @return
	 */
	
	public double kT(){
		if(getKillsFire()+getKillsStomp()+getKillsShell() == 0)
			return 0;
		else
			return ((getKillsFire()+getKillsStomp()+getKillsShell())/(double)(level.ENEMIES));
	}
	
	public double kP(){
		if(getKillsFire()+getKillsStomp()+getKillsShell() == 0)
			return 0;
		else
			return (double)(getKillsFire()+getKillsStomp()+getKillsShell()-(double)(dop()));
	}

	public double tll(){
		return getCompletionTime();
	}

	public double ts(){
		
		double temp;
		//;//System.out.println("left : " + getTotalLeftTime());
		//;//System.out.println("right : " + getTotalRightTime());
		//;//System.out.println("Jump : " + getTotalJumpTime());
		//;//System.out.println("total : " + getTotalTime());
		
		temp = 1- ( (  (double)getTotalLeftTime()  +  (double)getTotalRightTime() )/((double)getTotalTime()) );
		//;//System.out.println("inside : " + temp);

		return temp;
		
	}

	/**
	 * This is also called J' but cant be called that due to special character
	 * @return double of aimless jumps
	 */
	public double J(){
		return getTimesJumped()-getKillsStomp()-getBlocksEmptyDestroyed()-getBlocksCoinDestroyed()-getBlocksPowerDestroyed();
	}

	public double nm(){
		return getSwitchedPower();
	}

	public double nd(){
		return getTimesDucked();
	}
	
	/** FOR ARCHITECT
	 * 
	 * @return
	 */
	

	
	/**
	 * Percentage of all blocks destroyed
	 * @return
	 */
	public double nc()
	{
		
			return (double)collectedCoins / (double)(level.COINS + level.BLOCKS_COINS);
		

	}
	
	
	public double nb(){
		double n = 0;
		if( level.BLOCKS_EMPTY != 0)
			n+= getBlocksEmptyDestroyed()/level.BLOCKS_EMPTY;
		if(level.BLOCKS_POWER != 0)
			n+= getBlocksPowerDestroyed()/level.BLOCKS_POWER;
		if( level.BLOCKS_COINS != 0)
			n+= getBlocksCoinDestroyed()/level.BLOCKS_COINS;

		return n;
	}

	public double ncb(){
		if( level.BLOCKS_COINS != 0)
			return getBlocksCoinDestroyed()/level.BLOCKS_COINS;
		else
			return 0;
	}
	
	public double nI(){
		if( level.BLOCKS_COINS + level.BLOCKS_POWER + level.COINS != 0)
			return (double)(getCoinsCollected() +  getBlocksPowerDestroyed() + getBlocksCoinDestroyed()) / (double)( level.BLOCKS_COINS + level.BLOCKS_POWER + level.COINS);
		else
			return 0;
	}

	public double neb(){
		if( level.BLOCKS_EMPTY != 0)
			return getBlocksEmptyDestroyed()/level.BLOCKS_EMPTY;
		else
			return 0;
	}

	public double np(){
		if( level.BLOCKS_POWER != 0)
			return (double)getBlocksPowerDestroyed()/(double)level.BLOCKS_POWER;
		else
			return 0;
	}

	/**
	 * Deaths by falling into gaps
	 * @return
	 */
	public double dg(){
		return getDeaths(8);
	}

	/**
	 * Percentage of deaths by falling into gaps
	 * @return
	 */
	public double dj(){
		int tDeaths = 0;

		for(int i=0;i<deaths.length;++i){
			tDeaths += deaths[i];
		}

		if(tDeaths<=0)
			return 0;
		else
			return dg()/tDeaths;
	}
	/**
	 * should be do but cannot use constrained word
	 * @return
	 */
	public double dop(){
		int deaths = 0;

		for(int i=0;i<=6;++i)
			deaths += getDeaths(i);

		return deaths+getDeaths(9); //remember the shell
	}

	public static double normalize(double v, double min, double max){
		double out = (v-min)/(max-min);

		if(out > 1)
			return 1;
		else if(out < 0)
			return 0;
		else
			return out;
	}
}
