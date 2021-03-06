package ch.idsia.mario.engine;

import java.awt.GraphicsConfiguration;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

import Architect.*;

import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.Evaluation;

import ch.idsia.mario.engine.level.ArchLevel;
import ch.idsia.mario.engine.level.BgLevelGenerator;
//import dk.itu.mario.engine.sonar.FixedSoundSource;
import ch.idsia.mario.engine.sprites.CoinAnim;
import ch.idsia.mario.engine.sprites.FireFlower;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Mushroom;
import ch.idsia.mario.engine.sprites.Particle;
import ch.idsia.mario.engine.sprites.Sprite;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.BgRenderer;
import ch.idsia.mario.engine.DataRecorder;
import ch.idsia.mario.engine.LevelRenderer;
import static ch.idsia.mario.engine.LevelScene.recorder;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.*;
import ch.idsia.mario.engine.ResourcesManager;
import ch.idsia.scenarios.Play;
import java.util.logging.Logger;





public class LevelSceneTest extends LevelScene{

	ArrayList<Double> switchPoints;
	
	public ArrayList<double[]> valueArrayList = new ArrayList(0);//means of the gaussians, will contain all unique vectors used
	public ArrayList<double[]> rewardList = new ArrayList(0);//contains all rewards in same order as valueArrayList, corresponding to each vector, list for each vector
	public double[] vectorModel = new double[0];//appropriateness for vectors values in same order as valueArrayList, corresponding to each vector, one for each vector
	//moved to parent class for rendering;
	//public double [][] valueList = {startVector};//means of the gaussians, will contain all playvectors created, possibly including multiple of same
	private int newVectorInterval = 1;//interval for new vectors; i.e. 5 will create 5 vectors before setting selecting best vector
	private int newVectorCount = 0; //counter for newVectorInterval
	private boolean normalDiffMethods = false;//boolean to toggle normal difficulty calculations
	
	public boolean recording = false;
	public boolean l2 = true;
	public boolean l3 = false;
	public ArchLevel level2;
	public ArchLevel level2_reset;
	public ArchLevel level3;
	public ArchLevel level3_reset;
	public boolean gameover = false;

	//General variables
	public boolean verbose = false;
        private int swaps = 0;
	//Variables for Random Forest classification
	public RandomForest RF = new RandomForest();
	public Instances RF_trainingInstances;
	public Instances RF_testInstances;

        public int levelWidth = 200;

	public Architect arch;


	public LevelSceneTest(GraphicsConfiguration graphicsConfiguration,
			MarioComponent renderer, long seed, int levelDifficulty, int type){
		super(graphicsConfiguration,renderer,seed,levelDifficulty,type);
		
	}

	public void init() {
		try
		{
			Level.loadBehaviors(new DataInputStream(ResourcesManager.class.getResourceAsStream("tiles.dat")));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			//System.exit(0);
		}

		//System.out.println("");
		//System.out.println("----------------------------------------");
		//System.out.println("---------- Initialising game -----------");
		//System.out.println("----------------------------------------");
               
		//valueArrayList.add(startVector);//add start vector for gaussian
		
		//Track planned difficuly levels for each level segment
		currentLevelSegment = 0;
		nextSegmentAlreadyGenerated = false;

		//Load training instances from ARFF file
		boolean verbose = false;
                loadTrainingInstances(verbose);
                loadTestInstances(verbose);
		//Fill kernels with training data
		//fillKernels();

		//Init player model with average rewards based on historic data (i.e., from training instances)
		//System.out.println("");
		//System.out.println("Initialising player model with training instances...");                       
		//Loop through training instances
		for (int i=0; i < RF_trainingInstances.numInstances(); i++) {
			//Calculate reward for selected instance, add reward to appropriate player models, and update display of average accumulate reward
			Instance trainingInstance = selectTrainingInstance(i);
			boolean doBernoulliRewards = false;
			boolean isTrainingInstance = true;
			verbose = false;
			updateReward(trainingInstance, doBernoulliRewards, isTrainingInstance, verbose); //update reward in playerModelDiff1,4,7
			updatePlayerModel();
			//displayReceivedRewards();
		}
		
		//m.DIFFICULTY = arch.message.DIFFICULTY*3+1; //arch.message.DIFFICULTY is initialised with 1 in Architect\state
		//m.DIFFICULTY = setAction(); //set action using Softmax
		//m.state = getDifficulty(); //function return m.DIFFICULTY
		//m.state[1] = randomNumber(0,3); //initial appropriateness measurement is random at the moment
		//System.out.println("");
		//System.out.println("Initialising game...");
		////System.out.println("-levelDifficulty: " + levelDifficulty);
		////System.out.println("-arch.message.DIFFICULTY: " + arch.message.DIFFICULTY);
		////System.out.println("-m.DIFFICULTY: " + m.DIFFICULTY);
		////System.out.println("-m.state: " + m.state);
		//System.out.println("-initialising two level segments with play vector: " + Arrays.toString(this.valueList[valueList.length-1]) );

		//TEST - Set next action using Softmax
		//setAction(); //sets m.bestAction

		//TEST - Get probability of appropriateness
		//String observation_str = "32, 32, 24, 0, 0, 28, 2, 19, 5, 23, 30, 1, 5, 5, 5, 1, 1, 1, 0, 0, 4, 23, 0, 0, 2, 16.0, 2.0, 1.0, 0.0, 1.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1";
		//getProbsAppropriateness(observation_str, true);

		//TEST - Update reward in player models
		////System.out.println("-reward function has been initialised"); //is now initialised to 0 in setPlayerModel()
		//String observation_str = "32, 32, 24, 0, 0, 28, 2, 19, 5, 23, 30, 1, 5, 5, 5, 1, 1, 1, 0, 0, 4, 23, 0, 0, 2, 16.0, 2.0, 1.0, 0.0, 1.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1";
		//updateReward(observation_str);                       

		//Generate base level?
		if(level==null)
			/*if(isCustom){
		        		CustomizedLevelGenerator clg = new CustomizedLevelGenerator();
		        		GamePlay gp = new GamePlay();
		        		gp = gp.read("player.txt");
		        		currentLevel = (Level)clg.generateLevel(gp);

		        		//You can use the following commands if you want to benefit from
		        		//	the interface containing detailed information
		        		String detailedInfo = FileHandler.readFile("DetailedInfo.txt");

		              }
			        	else*/
			//levelDifficulty = 1;
			//plannedDifficultyLevels.add(levelDifficulty); //as this level segment seems not to be used, only add difficulties of actually created segments
			currentLevel = new RandomLevel(levelWidth, 15, levelSeed, levelDifficulty, levelType); //it's my impression this level segment is not directly used, perhaps overwritten elsewhere?
                        //width used to be 200
		
			level = currentLevel;
		
			
	                      

		paused = false;
		Sprite.spriteContext = this;
		sprites.clear();

		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(100);
		//level = LevelGenerator.createLevel(320, 15, levelSeed+randomInt, levelDifficulty, levelType);
		//randomInt = randomGenerator.nextInt(100);
		//level2 = new CustomizedLevel(100, 15, levelSeed+randomInt , levelDifficulty , levelType,arch.message);

		//levelDifficulty = m.DIFFICULTY; - this is good, but let's use the same function everywhere to get the difficulty, so:
		//levelDifficulty = getDifficulty();
		//levelDifficulty = 1;
		//plannedDifficultyLevels.add(levelDifficulty);
                arch.params_new.seed = randomInt;
                level2 = new ArchLevel(arch.params_new);//using second constructor!
		//level = new ArchLevel(100, 15, levelSeed+randomInt, levelDifficulty, levelType, arch.message);
		plannedDifficultyLevels.add(level2.DIFFICULTY_sander);

		//level2 = new ArchLevel(100, 15, levelSeed+randomInt , levelDifficulty , levelType,arch.message);
		//level2 = new RandomLevel(100, 15, levelSeed+randomInt , levelDifficulty , levelType);
                randomInt = randomGenerator.nextInt(100);
                arch.params_new.seed = randomInt;
		
                //arch.params_new.seed = randomInt;
		// level3 = new CustomizedLevel(100, 15, levelSeed+randomInt , levelDifficulty,levelType,arch.message);
		//level3 = new RandomLevel(100, 15, levelSeed+randomInt , levelDifficulty , levelType);
		//plannedDifficultyLevels.add(levelDifficulty);
		level3 = new ArchLevel(arch.params_new);//using second constructor!
		//level3 = new ArchLevel(100, 15, levelSeed+randomInt, levelDifficulty, levelType, arch.message);
		plannedDifficultyLevels.add(level3.DIFFICULTY_sander);

		fixborders();
		conjoin();

		try {
			level2_reset = level2.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			level3_reset = level3.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
		for (int i = 0; i < 2; i++)
		{
			int scrollSpeed = 4 >> i;
		int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
		int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
		Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
		bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
		}

		double oldX = 0;
		if(mario!=null)
			oldX = mario.x;

		mario = new Mario(this);
		sprites.add(mario);
		startTime = 1;
		timeLeft = 200*15;

		tick = 0;

		/*
		 * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
		 */
		 switchPoints = new ArrayList<Double>();

		//first pick a random starting waypoint from among ten positions
		 int squareSize = 16; //size of one square in pixels
		 int sections = 10;

		 double startX = 32; //mario start position
		 double endX = level.getxExit()*squareSize; //position of the end on the level
		 //if(!isCustom && recorder==null)

		 recorder = new DataRecorder(this,level2,keys);
		 ////System.out.println("\n enemies LEFT : " + recorder.level.COINS); //SANDER disable
		 ////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_COINS);
		 ////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_POWER);
		 gameStarted = false;
	}                     

	public void loadTrainingInstances(boolean verbose) {
		try {
			//Load training instances into data
			//System.out.println("");
			//System.out.println("Loading training instances into RandomForest classifier...");
                       BufferedReader reader = new BufferedReader(
			new FileReader("traindata/MarioPOMDP-traininginstances.arff"));
			Instances data = new Instances(reader);
			reader.close();
			// setting class attribute
			data.setClassIndex(data.numAttributes() - 2);            //2nd to last attribute is used for classification (last is timestamp)

			//Filter out timestamp string data
			String[] options = new String[2];
			options[0] = "-R";                                       // "range"
			options[1] = "48";                                       // last timestamp attribute
			Remove remove = new Remove();                            // new instance of filter
			remove.setOptions(options);                              // set options
			remove.setInputFormat(data);                             // inform filter about dataset **AFTER** setting options
			RF_trainingInstances = Filter.useFilter(data, remove);   // apply filter

			//Build RandomForest classifier
			String[] options_RF = new String[1];
			options_RF[0] = "-D";          // debug output
			//RandomForest RF = new RandomForest(); //declared as public
			//RF.setOptions(options_RF);
			RF.buildClassifier(RF_trainingInstances);

			if (verbose) {
				//Get classification of example data
				////System.out.println(RF.toString());
				Evaluation eTest = new Evaluation(data);
				//eTest.evaluateModel(RF, RF_trainingInstances);
				//eTest.crossValidateModel(RF, data, 10, new Random());
				int folds = 10;
				Random rand = new Random(0);  // using seed = 0 (should be 1?)
				eTest.crossValidateModel(RF, RF_trainingInstances, folds, rand);

				// Print the result à la Weka explorer:
				String strSummary = eTest.toSummaryString();
				//System.out.println(strSummary);
				////System.out.println(eTest.toClassDetailsString());

				// Get the confusion matrix
				//double[][] cmMatrix = eTest.confusionMatrix();                                
				////System.out.println(eTest.toMatrixString());
			}

			//System.out.println("-done loading " + RF_trainingInstances.numInstances() + " training instance(s)");
		}
		catch (Exception e) {
			//Error reading file
			//System.out.println("ERROR!!! - In function loadTrainingInstances()...");
			//System.out.println("-" + e);
		}                            
	}

	public void loadTestInstances(boolean verbose) {
		try {
			//Load test instances into data
			//System.out.println("");
			//System.out.println("Loading test instances...");
			BufferedReader reader = new BufferedReader(
			new FileReader("traindata/MarioPOMDP-testinstances.arff"));
			Instances data = new Instances(reader);
			reader.close();
			// setting class attribute
			data.setClassIndex(data.numAttributes() - 2);        //2nd to last attribute is used for classification (last is timestamp)

			//Filter out string data
			String[] options = new String[2];
			options[0] = "-R";                                   // "range"
			options[1] = "48";                                   // last timestamp attribute
			Remove remove = new Remove();                        // new instance of filter
			remove.setOptions(options);                          // set options
			remove.setInputFormat(data);                 // inform filter about dataset **AFTER** setting options
			RF_testInstances = Filter.useFilter(data, remove);   // apply filter                                

			//System.out.println("-done loading " + RF_testInstances.numInstances() + " test instance(s)");
		}
		catch (Exception e) {
			//Error reading file
			//System.out.println("ERROR!!! - In function loadTestInstances()...");
			//System.out.println("-" + e);
		}                            
	}

	public Instance selectTestInstance() {
		//Select last instance from loaded set of Test Instances

		//Create test instance
		//Instance testInstance = new Instance(newDataTest.firstInstance());
		//Instance testInstance = new Instance(newDataTest.instance(0));
		Instance testInstance = new Instance(RF_testInstances.lastInstance());
		////System.out.println("-selecting last instance in test set RF_testInstances, done");

		// Specify that the instance belong to the training set 
		// in order to inherit from the set description                                
		testInstance.setDataset(RF_trainingInstances);
		//System.out.println("-selected last instance in test set: " + testInstance.toString() );

		return testInstance;
	}

	public Instance selectTrainingInstance(int index) {
		//Select last instance from loaded set of Test Instances

		//Create test instance
		//Instance trainingInstance = new Instance(RF_trainingInstances.firstInstance());
		//Instance trainingInstance = new Instance(RF_trainingInstances.lastInstance());
		Instance trainingInstance = new Instance(RF_trainingInstances.instance(index));                           

		// Specify that the instance belong to the training set 
		// in order to inherit from the set description                                
		trainingInstance.setDataset(RF_trainingInstances);
		//System.out.println("-processing instance # " + index + " in training set: " + trainingInstance.toString() );

		return trainingInstance;
	}

	public double[] classifyInstance(Instance testInstance, boolean verbose) {
		try {
			//Classify one particular instance from loaded set of Test Instances

			//Create test instance
			//Instance testInstance = new Instance(newDataTest.firstInstance());
			//Instance testInstance = new Instance(newDataTest.instance(0));
			//Instance testInstance = new Instance(RF_testInstances.lastInstance());
			////System.out.println("-selecting last instance in test set RF_testInstances, done");

			// Specify that the instance belong to the training set 
			// in order to inherit from the set description                                
			//testInstance.setDataset(RF_trainingInstances);

			// Get the likelihood of each classes 
			// fDistribution[0] is the probability of being positive
			// fDistribution[1] is the probability of being negative 
			double[] fDistribution = RF.distributionForInstance(testInstance);
                            
			if (verbose) {
				//System.out.println("");
				//System.out.println("Classifying selected test instance...");                               
				//System.out.println("-probability of instance being appropriate     (1): " + fDistribution[1]);
				//System.out.println("-probability of instance being non-appropriate (0): " + fDistribution[0]);
				//System.out.println("-returning appropriateness probability of: " + fDistribution[1]);
			}
//                        for(int i = 0 ; i< fDistribution.length;i++)
//                               System.out.print(fDistribution[i] + "|");
			return fDistribution;
		}
		catch (Exception e) {
			//Error reading file
			//System.out.println("ERROR!!! - In function classifyInstance()...");
			//System.out.println("-" + e);
                        return new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
			//return  empty;//dummy value
		}                            
	}

                public double getInstanceReward(Instance selectedInstance, boolean doBernoulliRewards, boolean isTrainingInstance, boolean verbose)
                {                         
                               //Query random forest classifier for probability distribution
                               double[] probDist = classifyInstance(selectedInstance, false);                          
                
                //Determine Expected Reward from the returned Probability Distribution
                //Note that the following 5-point Likert scale was used
                //1 - Extremely unchallenging for me
                //2 - Somewhat unchallenging for me
                //3 - Appropriately challenging for me
                //4 - Somewhat challenging for me
                //5 - Extremely challenging for me
                double[] rewardsPerClass = new double[]{0, 1d/3d, 1, 1d/3d, 0}; //rewards per class as defined in paper, section 4.2 Phase 1.
                double expectedReward = (probDist[0] * rewardsPerClass[0]) +
                                        (probDist[1] * rewardsPerClass[1]) +
                                        (probDist[2] * rewardsPerClass[2]) +
                                        (probDist[3] * rewardsPerClass[3]) +
                                        (probDist[4] * rewardsPerClass[4]);
                       
                if (verbose) {
                    System.out.println("");
                    System.out.println("getInstanceReward called()...");
                    System.out.println("-probability of instance being class 1: " + probDist[0]);
                    System.out.println("-probability of instance being class 2: " + probDist[1]);
                    System.out.println("-probability of instance being class 3: " + probDist[2]);
                    System.out.println("-probability of instance being class 4: " + probDist[3]);
                    System.out.println("-probability of instance being class 5: " + probDist[4]);
                    System.out.println("-the expected reward for this level segment was determined to be: " + expectedReward );
                    System.out.println("-done");
                }
                   
//                           //Determine rewards according to Bernoulli scheme / proportional reward
//                            double reward = 0.0;
//                           if (doBernoulliRewards) {
//                                           if (verbose) System.out.println("-returning reward of 1 with probablity of " + probChallenging + ", else reward of 0 (Bernoulli rewards)");
//                                           boolean returnBernoulliReward;
//                                           if ( Math.random() <= probChallenging ) returnBernoulliReward = true;
//                                           else returnBernoulliReward = false;
//                                           if (verbose) System.out.println("-boolean returnBernoulliReward: " + returnBernoulliReward);
//                                           if (returnBernoulliReward) reward = 1.0;
//                                           else reward = 0.0;                               
//                           }
//                           else {
//                                           if (verbose) System.out.println("-returning reward " + probChallenging + " (regular non-Bernoulli rewards)");
//                                           reward = probChallenging;
//                           }
//                           if (verbose) System.out.println("-done");
 
                return expectedReward;
        }
	public int getDifficulty()
	{
		//return m.DIFFICULTY; //this is outdated - lets return the actual values as also displaye on the screen
		//    public int currentLevelSegment;
		//    public ArrayList plannedDifficultyLevels = new ArrayList(0);                       
		return (int) plannedDifficultyLevels.get(currentLevelSegment);
	}

	public int getAppropriateness()
	{
		return randomNumber(0,3);
	}

	public void updateReward(Instance selectedInstance, boolean doBernoulliRewards, boolean isTrainingInstance, boolean verbose)
	{                          
		//Update reward in the vector playerModel[]
		if (verbose);////System.out.println("");
		if (verbose);////System.out.println("updateReward called()");

		//Determine difficulty level associated to this instance
		int difficultyLevel;
		if (isTrainingInstance) {
			difficultyLevel = Integer.parseInt(selectedInstance.toString(45)); //in this attribute the difficulty level is stored
		} 
		else {
			difficultyLevel = getDifficulty();  //now: m.DIFFICULTY. perhaps it should be m.state ?
		}
		if (verbose);////System.out.println("-calculating reward for previous level segment with difficulty level: " + difficultyLevel );
		//double probsAppro = getProbsAppropriateness_ObservationStr(observation_str, false);
		double probsAppro = classifyInstance(selectedInstance, false)[1];                           
		if (verbose);////System.out.println("-difficulty of level segment was deemed appropriate with a probability of: " + probsAppro );

		//Determine rewards according to Bernoulli scheme / proportional reward
		double reward = 0.0;
		if (doBernoulliRewards) {
			if (verbose);////System.out.println("-returning reward of 1 with probablity of " + probsAppro + ", else reward of 0 (Bernoulli rewards)");
			boolean returnBernoulliReward;
			if ( Math.random() <= probsAppro ) returnBernoulliReward = true;
			else returnBernoulliReward = false;

			if (verbose);////System.out.println("-boolean returnBernoulliReward: " + returnBernoulliReward);
			if (returnBernoulliReward) reward = 1.0;
			else reward = 0.0;                                
		}
		else {
			if (verbose);////System.out.println("-returning reward " + probsAppro + " (regular non-Bernoulli rewards)");
			reward = probsAppro;
		}

		if (verbose);////System.out.println("-adding reward of " + reward + " to arraylist playerModelDiff" + difficultyLevel);
		switch (difficultyLevel) {
		case 1: playerModelDiff1.add(reward); break;
		case 4: playerModelDiff4.add(reward); break;
		case 7: playerModelDiff7.add(reward); break;
		default:;////System.out.println("-ERROR! Cannot add reward to concerning playerModelDiff1,4,7 due to incorrect input of difficultyLevel"); break;
		}
		//Note, updating the display of average rewards is performed by updatePlayerModel()
		//int index = getPlayerModelIndex(difficultyLevel);
		////System.out.println("-updating playerModel[" + index + "] with reward: " + reward);
		//playerModel[index] += reward;
		if (verbose);////System.out.println("-done");

		//OLD
		//Increase reward proportionally to appropriateness of current difficulty level to the specific player
		//As determed by probabilities in player model
		/*
                           ;////System.out.println("");
                           ;////System.out.println("updateReward called()");
                            double reward = getPlayerModelElement(m.DIFFICULTY);
                           ;////System.out.println("-increasing reward by: " + reward);
                            m.REWARD += reward;
                           ;////System.out.println("-new reward is now: " + m.REWARD);
		 */

		//OLD OLD
		/*
                            if (m.state == 1) { //SANDER UPDATE - NOT CORRECT AT THE MOMENT
                                //Appropriate difficulty - Increase reward
                                int rangeMin = 0;
                                int rangeMax = 1;
                                Random r = new Random();
                                double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                               ;////System.out.println("Increasing reward by: " + randomValue);
                                m.REWARD += randomValue;
                               ;////System.out.println("New cummulative reward: " + m.REWARD);
                            }
                            else {
                                //No appropriate difficulty - Do not increase reward for this level block
                                m.REWARD += 0;                              
                            }
		 */
	}
	
	public void updateRewardVectors(Instance selectedInstance, boolean doBernoulliRewards, boolean isTrainingInstance, boolean verbose)
	{   	//this is the method updateRewards, adjusted for the vectors of the gaussian              
		//Update reward in the vector rewardList[]
		if (verbose);////System.out.println("");
		if (verbose);////System.out.println("updateRewardVectors called()");
	
		//Determine difficulty level associated to this instance
		double [] playVector = valueList[valueList.length-1].clone();//get last vector used
	
		if (verbose);////System.out.println("-calculating reward for previous level segment play vector: " + Arrays.toString(playVector) );
		//double probsAppro = getProbsAppropriateness_ObservationStr(observation_str, false);
		double probsAppro = classifyInstance(selectedInstance, false)[1];
		//probsAppro = getAbandonmentProbability();
		if (verbose);////System.out.println("-difficulty of play vector was deemed appropriate with a probability of: " + probsAppro );
	
		//Determine rewards according to Bernoulli scheme / proportional reward
		double reward = 0.0;
		if (doBernoulliRewards) {
			if (verbose);////System.out.println("-returning reward of 1 with probablity of " + probsAppro + ", else reward of 0 (Bernoulli rewards)");
			boolean returnBernoulliReward;
			if ( Math.random() <= probsAppro ) returnBernoulliReward = true;
			else returnBernoulliReward = false;
	
			if (verbose);////System.out.println("-boolean returnBernoulliReward: " + returnBernoulliReward);
			if (returnBernoulliReward) reward = 1.0;
			else reward = 0.0;                                
		}
		else {
			if (verbose);////System.out.println("-returning reward " + probsAppro + " (regular non-Bernoulli rewards)");
			reward = probsAppro;
		}
	
		if (verbose);////System.out.println("-adding reward of " + reward + " to play vector" + Arrays.toString(playVector));
		
		addRewardToList(playVector, reward);//adding to rewardList and ValueArrayList
		//Note, updating the display of average rewards is performed by updatePlayerModel()
		//int index = getPlayerModelIndex(difficultyLevel);
		//System.out.println("-updating vectorModel[" + Arrays.toString(playVector) + "] with reward: " + reward);
		//playerModel[index] += reward;
		if (verbose);////System.out.println("-done");
	
	}

	public double[] setVectorAction() 
	{
			//this is the method setAction adjusted for the vectors of the gaussian
	       //Select the next action using the accumulated rewards as given in the player models
	       //Now: select according to softMax probabilities
	      ;////System.out.println("");
	      ;////System.out.println("setVectorAction() called...");
	       //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	       ////System.out.println("setAction() called at " + timeStamp + "...");

	       //Determine the softMax temperature on the basis of the probability of user abandonment
	       double abandonmentProbability = getAbandonmentProbability();
	       softMax_temperature = (1 - abandonmentProbability);
	      ;////System.out.println("-probability of abandonment: " + abandonmentProbability );
	      ;////System.out.println("-calling softmax with temperature of: " + softMax_temperature);
	       
	       //Select action according to softMax probabilities
	       double[] softmaxProbs = softmax(this.vectorModel);
	       for (int i = 0; i < softmaxProbs.length;i++)
	       {
	    	   String vector = Arrays.toString(valueArrayList.get(i));
	    	  ;////System.out.println("-probability of vector "+vector+" (with average accumulated reward " + this.vectorModel[i] + ") being selected is: " + softmaxProbs[i]);
   
	       }
	      
	       double p = Math.random();
	       double cumulativeProbability = 0.0;
	       double[] selectedAction = new double[valueArrayList.get(0).length];
	       for (int i = 0; i < softmaxProbs.length; i++) {
	           cumulativeProbability += softmaxProbs[i];
	           if (p <= cumulativeProbability) {
	               selectedAction = valueArrayList.get(i);
	               break;
	           }
	       }
	       //return the selected vector
	       return selectedAction;

	}
	
	public void updateVectorModel()
	{
		///Update vectorModel[] with actual average (!) rewards, using rewardList as input   
		
		double [] newVectorModel = new double[this.rewardList.size()];
		for(int i = 0;i < this.rewardList.size();i++)
		{
			double[] rewards = this.rewardList.get(i);
			if (rewards.length > 1)
			{
				double average = 0;
				for(int j = 0; j< rewards.length;j++)
				{
					average += rewards[j];
				}
				average /= rewards.length;
				newVectorModel[i] = average;
			}
			else
			{
				newVectorModel[i] = rewards[0];
			}
		}
		this.vectorModel = newVectorModel;
	}
	
	private void addRewardToList(double[] playVector, double reward) 
	{
		//method to add new or existing rewards to rewardList[][]
		
		int index = deepContainsArray(valueArrayList,playVector);
		if (index != -1)						//if it's already been added before, append the reward
		{	
			double[] rewards = rewardList.get(index);
			rewards = Arrays.copyOf(rewards,rewards.length+1);
			rewards[rewards.length-1] = reward;
			rewardList.set(index, rewards);
		}
		else								//if it isn't, add both the vector and the reward
		{
			valueArrayList.add(playVector); //add to ArrayList too!
			double [] rewardArray =  {reward};
			rewardList.add(rewardArray);
		}
		
	}
	
	public int deepContainsArray(ArrayList<double[]> arraylist, double[] element)
	{
		//quick-method to check if element is in ArrayList<double[]>. There is sure to be a better way, however i have not found it yet.
		//returns the index if found,  or -1 if not found
		for (int i = 0; i<arraylist.size();i++)
		{
			if(Arrays.equals(arraylist.get(i), element))
			return i;
		}
		return -1;
	}

	public static double [][] addToArray(double [][] array, double [] element)
	{	//quick method to add array to array of arrays
		double [][] newarray = new double [array.length+1][array[0].length];
		for (int i = 0;i < array.length;i++)
		{	
			newarray[i] = array[i];
			
		}
			newarray[array.length] = element;
			return newarray;
			
	}
	
	public void newchunk()
	{
		if(!gameover)
		{
			//System.out.println("-newchunck called");
                        
                        
                        //Note: Using other constructor of ArchLevel, using recorder and valueList as inputs
			level2 = new ArchLevel(arch.params_new);
			nextSegmentAlreadyGenerated = true;
			//System.out.println("-setting nextSegmentAlreadyGenerated to: " + nextSegmentAlreadyGenerated);

			try {
				level2_reset = level3_reset.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				level3_reset = level2.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else
		{
			level.xExit = 105;
		}

	}


	private int convertTime(int time){
		return (int)Math.floor((time+15-1)/15);
	}

	public void swap()
	{

		int k = 0;
		//The background info should change aswell                       

		if(mario.x > (level2.width*16 + level3.width*16* 0.78))
		{
			recorder.endTime();
			//Swapping level segment
                       ;////System.out.println("");
                       ;////System.out.println("----------------------------------------");
                       ;////System.out.println("-------- Swapping level segment --------");
                       ;////System.out.println("----------------------------------------");
                       //System.out.println("actual reward:" + level3.COINS);
                        //Write to log + get observation string for calculating appropriateness
                        //String observation_str = "32, 32, 24, 0, 0, 28, 2, 19, 5, 23, 30, 1, 5, 5, 5, 1, 1, 1, 0, 0, 4, 23, 0, 0, 2, 16.0, 2.0, 1.0, 0.0, 1.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1";
                        level2.playValues = this.valueList[0];
                        level3.playValues = this.valueList[0];
                        Instance classifyTestInstance = selectTestInstance();
                        arch.reward_weights = classifyInstance(classifyTestInstance,false);
//                        for(int i = 0 ;i < arch.reward_weights.length; i++)
//                            System.out.print(arch.reward_weights[i] + " || ");
                        
                            try {
                                arch.Observations = recorder.fillGamePlayMetrics(getDifficulty(), verbose  , arch.reward_weights,arch.params_new, level3); //write metrics at swapping to new level segment
                            } catch (IllegalArgumentException ex) {
                                Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            } catch (IllegalAccessException ex) {
                                Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            }
                            
                        
                       
                        //arch.small_count += 1;
                        //Load test instances and select last instance for classification
                        //Update in which level segment the player currently is
                        arch.reward_label = level3.getCustomRewards(Play.taskType(), arch.Observations);
                        //System.out.println("LABEL IS:" + arch.reward_label);
                        currentLevelSegment++;                                        
                        //Somehow update the next levels parameters(ex. hill climbing)
                        arch.update();
                        
                        nextSegmentAlreadyGenerated = false;
                        
                        //Update planned difficulty for the upcoming level segment
                        plannedDifficultyLevels.add(levelDifficulty); //more efficient code as if statement has become redundant
                        recorder.levelScene.resetTime();
                        
                        
                        


			for (int i = 0; i < level.width; i++)
			{
				if(i < level2.width)
				{
					level2.map[i] = level.map[i];
					// level2.data[i] = level.data[i];
					level2.spriteTemplates[i] = level.spriteTemplates[i];

				}
				else
				{
					level3.map[k] = level.map[i];
					// level3.data[k] = level.data[i];
					level3.spriteTemplates[k] = level.spriteTemplates[i];
					k++;
				}

			}

			newchunk();
			fixborders();
			k = 0;


			for (int i = 0; i <level.width; i++)
			{
				if(i < level3.width)
				{
					level.map[i] = level3.map[i];
					// level.data[i] = level3.data[i];

					level.spriteTemplates[i] = level3.spriteTemplates[i];

				}
				else
				{
					level.map[i] = level2.map[k];
					// level.data[i] = level2.data[k];
					level.spriteTemplates[i] = level2.spriteTemplates[k];
					k++;
				}

			}
			for(int i = 0 ; i < sprites.size() ; i++)
			{
				//if(sprites.get(i).x < level2.width)sprites.get(i).release();
				sprites.get(i).x = sprites.get(i).x - level2.width*16;
			}
			

		}
                
                // here u are in level 3
                        recorder.reset();
                        recorder.startTime();
                        recorder.level = level3;


	}
	// }

	private void displayReceivedVectorRewards() 
	{	//print rewardlist
		//System.out.println("");
	   ;////System.out.println("displayReceivedVectorRewards() called...");
		for (int i = 0; i < rewardList.size();i++);
		//System.out.printf("Vector "+Arrays.toString(valueArrayList.get(i))+" : "+Arrays.toString(rewardList.get(i))+"\n");
		
	}

	public void save()
	{
		try {
			level2_reset = level2.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			level3_reset = level3.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void conjoin()
	{
		//fixborders();
		//INSERTED THIS CODE
		//to conjoin two levels into one
		int width = level2.width + level3.width;
		int height = level2.height;
		Level level4 = new Level(width, height);
		level4.map = new byte[width][height];
		// level4.data = new byte[width][height];
		level4.xExit = width - 5;
		int k = 0;
		for (int i = 0; i < width; i++)
		{
			if(i < level2.width)
			{
				level4.map[i] = level2.map[i].clone();
				//level4.data[i] = level2.data[i];
				level4.spriteTemplates[i] = level2.spriteTemplates[i];

			}
			else
			{
				level4.map[i] = level3.map[k].clone();
				//level4.data[i] = level3.data[k];
				level4.spriteTemplates[i] = level3.spriteTemplates[k];
				k++;
			}

		}
		level = level4;
	}


	public void fixborders()
	{    	    	
		for( int i = 0 ; i<15 ; i++)
		{
			level2.map[0][i] = (byte)(0);
			level2.map[level2.width-1][i] = (byte)(0);

			//if(level2.map[level2.width-1][i] == )
			if (level2.map[level2.width-2][i] == (byte)(-127))
			{
				level2.map[level2.width-2][i] = (byte)(-126);
			}

			if (level2.map[level2.width-2][i] == (byte)(-111))
			{
				level2.map[level2.width-2][i] = (byte)(-110);
			}

			if (level2.map[1][i] == (byte)(-127))
			{
				//change to corner
				level2.map[1][i] = (byte)(-128);

			}
			if(level2.map[1][i] == (byte)(-111))
			{
				level2.map[1][i] = (byte)(-112);
			}

			level3.map[0][i] = (byte)(0);
			level3.map[level3.width-1][i] = (byte)(0);

			//if(level2.map[level2.width-1][i] == )
			if (level3.map[level3.width-2][i] == (byte)(-127))
			{
				level3.map[level3.width-2][i] = (byte)(-126);
			}

			if (level3.map[level3.width-2][i] == (byte)(-111))
			{
				level3.map[level3.width-2][i] = (byte)(-110);
			}

			if (level3.map[1][i] == (byte)(-127))
			{
				//change to corner
				level3.map[1][i] = (byte)(-128);

			}
			if(level3.map[1][i] == (byte)(-111))
			{
				level3.map[1][i] = (byte)(-112);
			}
		}
	}

	public void tick(){
		swap();
		super.tick();

		if(recorder != null && !gameStarted){
			recorder.startLittleRecord();
			recorder.startTime();
			gameStarted = true;
		}
		if(recorder != null)
			recorder.tickRecord();
	}

	public void winActions() throws IllegalArgumentException, IllegalAccessException{
		if (recorder != null)
			recorder.fillGamePlayMetrics(getDifficulty(), verbose ,arch.reward_weights, arch.params_new, level3); //write metrics at winning the game (currently never reached in infinite setup)

		//marioComponent.win();
	}

	public void deathActions() throws IllegalArgumentException, IllegalAccessException{
		//Reset general mario stuff
//		if(Mario.lives <=0){ //has no more lives
//			if(recorder != null) 
//				recorder.fillGamePlayMetrics( getDifficulty(), verbose); //write metrics at game over
//			//marioComponent.lose();
//		}
//		else // mario still has lives to play :)--> have a new beginning
		{
                        System.out.println("here");
			if(recorder != null) 
				recorder.fillGamePlayMetrics( getDifficulty(), verbose ,arch.reward_weights, arch.params_new, level3); //write metrics at regular death with still lives left
			//Mario.lives--;
                        reset();
		}
	}

	public void bump(int x, int y, boolean canBreakBricks){
		byte block = level.getBlock(x, y);

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
		{
			bumpInto(x, y - 1);
			level.setBlock(x, y, (byte) 4);

			if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
			{
				//sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
				if (!Mario.large)
				{
					addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
				}
				else
				{
					addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
				}

				if(recorder != null){
					recorder.blockPowerDestroyRecord();
				}
			}
			else
			{
				//TODO should only record hidden coins (in boxes)
				if(recorder != null){
					recorder.blockCoinDestroyRecord();
				}

				Mario.getCoin();
				//sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
				addSprite(new CoinAnim(x, y));
			}
		}

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
		{
			bumpInto(x, y - 1);
			if (canBreakBricks)
			{
				if(recorder != null){
					recorder.blockEmptyDestroyRecord();
				}

				//sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
				level.setBlock(x, y, (byte) 0);
				for (int xx = 0; xx < 2; xx++)
					for (int yy = 0; yy < 2; yy++)
						addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
			}

		}
	}

	public void bumpInto(int x, int y)
	{
		byte block = level.getBlock(x, y);
		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
		{
			Mario.getCoin();
			//sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
			level.setBlock(x, y, (byte) 0);
			addSprite(new CoinAnim(x, y + 1));


			//TODO no idea when this happens... maybe remove coin count
			if(recorder != null)
				recorder.recordCoin();
		}

		for (Sprite sprite : sprites)
		{
			sprite.bumpCheck(x, y);
		}
	}

	private int randomNumber(int low, int high){
		return new Random(new Random().nextLong()).nextInt(high-low)+low;
	}

	private int toBlock(float n){
		return (int)(n/16);
	}

	private int toBlock(double n){
		return (int)(n/16);
	}

	private float toReal(int b){
		return b*16;
	}



	public void reset() {
		//System.out.println("");
		//System.out.println("----------------------------------------");
		//System.out.println("------------ Resetting game ------------");
		//System.out.println("----------------------------------------");                               

		//Always reset POMDP stuff
		playerModelDiff1.clear();
		playerModelDiff4.clear();
		playerModelDiff7.clear();
		if(normalDiffMethods)
		{	
		updatePlayerModel();
		displayReceivedRewards();
		}
		int temp_diffsegment1;
		int temp_diffsegment2;
		if (currentLevelSegment == 0) {
			//System.out.println("-you died in the first segment, resetting to how you just started");
			temp_diffsegment1 = (int) plannedDifficultyLevels.get(0);
			temp_diffsegment2 = (int) plannedDifficultyLevels.get(1);
		}
		else {
			//System.out.println("-nextSegmentAlreadyGenerated:" + nextSegmentAlreadyGenerated);
			if (nextSegmentAlreadyGenerated) {
				//because the next segment is already generated (and so the previous does not exist anymore),
				temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment);
				temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment+1);
			}
			else {
				//because the next segment is not yet generated
				temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment-1);
				temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment);
			}
		}
		plannedDifficultyLevels.clear();

		//System.out.println("-resetting to: " + temp_diffsegment1 + ", " + temp_diffsegment2);
		plannedDifficultyLevels.add(temp_diffsegment1);
		plannedDifficultyLevels.add(temp_diffsegment2);
		currentLevelSegment = 0;

		paused = false;
		Sprite.spriteContext = this;
		sprites.clear();

		try {
			level2 = level2_reset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			level3 = level3_reset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

                fixborders();
		conjoin();

		layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
		for (int i = 0; i < 2; i++)
		{
			int scrollSpeed = 4 >> i;
		int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
		int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
		Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
		bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
		}

		double oldX = 0;
		if(mario!=null)
			oldX = mario.x;

		mario = new Mario(this);
		sprites.add(mario);
		startTime = 1;

		timeLeft = 200*15;

		tick = 0;

		/*
		 * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
		 */
		 switchPoints = new ArrayList<Double>();

		//first pick a random starting waypoint from among ten positions
		int squareSize = 16; //size of one square in pixels
		int sections = 10;

		double startX = 32; //mario start position
		double endX = level.getxExit()*squareSize; //position of the end on the level
		//if(!isCustom && recorder==null)
                level2.playValues = this.valueList[0];
			recorder = new DataRecorder(this,level3,keys);
			////System.out.println("\n enemies LEFT : " + recorder.level.COINS); //Sander disable
			////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_COINS);
			////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_POWER);
			gameStarted = false;
	}
        
        
        
   public double[] softmax(double[] input) {
        //Return vector with softMax probabilities
        //double softMax_temperature = 1.0; //set globablly
        double output[] = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double div = 0.0;
            for (int j = 0; j < input.length; j++) {
                div += Math.exp( input[j] / softMax_temperature );
            }
            output[i] = Math.exp( input[i] / softMax_temperature ) / div;
        }
        return output;
   }    

   public double getAbandonmentProbability() {
       //Return probability of the user abandoning the game
       return Math.random();
   }  
   
  
      
   public void displayReceivedRewards() {
       //Display received rewards, as stored in playerModelDiff1,4,7
       //Display rewards stored in playerModelDiff1
       //for (int i = 0; i < playerModelDiff1.toString())
      ;////System.out.println("");
      ;////System.out.println("displayReceivedRewards() called...");
      ;////System.out.println("-playerModelDiff1: " + playerModelDiff1.toString());
      ;////System.out.println("-playerModelDiff4: " + playerModelDiff4.toString());
      ;////System.out.println("-playerModelDiff7: " + playerModelDiff7.toString());
   }
   
   public void updatePlayerModel() {
       //Update playerModel[] with actual average (!) rewards, using playerModelDiff1,4,7 as input     
       //Add some test data
       //playerModelDiff4.add(0.0);
       //playerModelDiff4.add(1.0);
       //playerModelDiff4.add(1.0);
       
       //Update playerModel[0] - difficulty 1
       double average = 0.0;
       for (int i = 0; i < playerModelDiff1.size(); i++) {
           average += (double) playerModelDiff1.get(i);
       }
       if ( playerModelDiff1.size() > 0 )
           average = average / playerModelDiff1.size();
       playerModel[0] = average;

       //Update playerModel[1] - difficulty 4
       average = 0.0;
       for (int i = 0; i < playerModelDiff4.size(); i++) {
           average += (double) playerModelDiff4.get(i);
       }
       if ( playerModelDiff4.size() > 0 )
           average = average / playerModelDiff4.size();
       playerModel[1] = average;

       //Update playerModel[2] - difficulty 7
       average = 0.0;
       for (int i = 0; i < playerModelDiff7.size(); i++) {
           average += (double) playerModelDiff7.get(i);
       }
       if ( playerModelDiff7.size() > 0 )
           average = average / playerModelDiff7.size();
       playerModel[2] = average;
   }
   
   
   public double getReward()
   {
       Random randomGenerator = new Random();
       return (double)randomGenerator.nextInt(10);
   }
   

}
