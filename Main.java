import java.io.*;
import java.util.*;
public class Main
{
	public static void main(String[] args)
	{
		if (args[0].equalsIgnoreCase("-train"))
		{
			NeuralNet network = new NeuralNet();
			
			File dir1 = new File("./src/Male");
			File dir2 = new File("./src/Female");
			crossValidate(dir1,dir2,network);
			saveWeights(network);
		}
		else if (args[0].equalsIgnoreCase("-test"))
		{
			NeuralNet network = initializeWeightsFromFile();
			createMatrixFromNetwork(network);
			//this function is commented out because it was only used for the
			//network visualizations for problem 4. It isn't necessary for testing
			File dir = new File("./src/Test");
			File[] testSet = dir.listFiles();
			for(int i = 0; i < testSet.length; i++)
			{
				makeNetwork(testSet[i], network);
				double result = network.networkOutput();
				//System.out.println("Result is " + result);
				if( result >= .5)
				{
					//double confidence = Math.abs(((.9 - result)/.9) * 100);
					System.out.println(testSet[i] + " Male " + (1 - Math.abs(result - .9)));
				}
				else
				{
					//double confidence = Math.abs(((.1 - result)/.1) * 100);
					System.out.println(testSet[i] + " Female " + (1 - Math.abs(result - .1)));
				}
			}
		}
	}
	public static void makeNetwork(File file, NeuralNet network)
	{
		//double[][] grayValues = new double[128][120]; // dont need an arrayList since this is constant 
		//File dir = new File("./src/Female");  // this file path is due to eclipse
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			int arrayIndex = 0;
			while((line = br.readLine()) != null) // for each line
			{

				String tmp[] = line.split(" ");
				for (int k = 0; k < tmp.length; k++) // for each character
				{
					network.inputList.get(arrayIndex).setInput((Double.parseDouble(tmp[k]) / 255));
					arrayIndex++;
					//network.inputList.add(new inputLayerNode((Double.parseDouble(tmp[k]) / 255),network.hiddenLayerSize));
					//System.out.println(grayValues[j][k]);
				}
				//System.out.println(line);
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static class NeuralNet
	{
		final int numInputs = 128 * 120;
		final int hiddenLayerSize = 12; // was 12
		final double learningRate = .35; // small so we don't overstep the minimum
		ArrayList<inputLayerNode> inputList;
		ArrayList<hiddenLayerNode> hiddenList;
		outPutLayerNode outPutNode;

		public NeuralNet()
		{

			//final int numOutputs = 1; // male or female
			inputList = new ArrayList<inputLayerNode>(numInputs);
			hiddenList = new ArrayList<hiddenLayerNode>(hiddenLayerSize);
			initializeInput(inputList);
			initializeHidden(hiddenList);
			initializeWeights();

		}
		public double networkOutput()
		{
			for (int j = 0; j < hiddenLayerSize; j++)
			{
				double sum = 0;
				for (int i = 0; i < inputList.size(); i++) // calculate outputs of input units
				{
					sum += inputList.get(i).input * inputList.get(i).weight[j];
				}
				hiddenList.get(j).setOutput(1 / (1 + Math.exp((-1)*(sum))));
			}
			double outputSum = 0;
			for (int i = 0; i < hiddenLayerSize; i++)
			{
				outputSum += hiddenList.get(i).output * hiddenList.get(i).weight;
			}
			//System.out.println(1 / (1 + Math.exp((-1)*(outputSum))));
			return 1 / (1 + Math.exp((-1)*(outputSum)));
		}
		private void initializeInput(ArrayList<inputLayerNode> inputList)
		{
			for(int i = 0;i<numInputs;i++)
			{
				inputList.add(new inputLayerNode(0.0,hiddenLayerSize));
			}
		}
		private void initializeHidden(ArrayList<hiddenLayerNode> hiddenList)
		{
			for(int i = 0;i<hiddenLayerSize;i++)
			{
				hiddenList.add(new hiddenLayerNode(0.0));
			}
		}
		private void initializeWeights()
		{
			for(int j = 0; j < 128*120; j++)
			{
				for(int k = 0; k < this.hiddenLayerSize; k++)
				{
					this.inputList.get(j).weight[k] = (Math.random() - .5)/10;
				}
			}
			for (int j = 0; j < this.hiddenLayerSize; j++)
			{
				this.hiddenList.get(j).weight = (Math.random() - .5)/10;
			}
		}
	}
	public static class inputLayerNode
	{
		double[] weight;
		double input;
		public inputLayerNode(double input, int hiddenLayerSize)
		{
			this.input = input;
			weight = new double[hiddenLayerSize];
		}
		public void setInput(double input)
		{
			this.input = input;
		}

	}
	public static class hiddenLayerNode
	{
		double weight;
		double output;
		public hiddenLayerNode(double weight)
		{
			this.weight = weight;
			output = 0;
		}
		public void setOutput(double output)
		{
			this.output = output;
		}
		public void setWeight(double weight)
		{
			this.weight = weight;
		}
		//double[] weight;
		//double input;
		double sigmoidOutput;
		double error;
	}
	public static class outPutLayerNode
	{
		double input;
		double sigmoidOutput;
		double error;
	}
	public static class FilePair{
		File file;
		double trueval;
		public FilePair(File file, double trueval)
		{
			this.file = file;
			this.trueval = trueval;
		}
	}

	public static void crossValidate(File maleDir, File femaleDir, NeuralNet network)
	{
		//Implements 5-fold cross validation
		FilenameFilter textFilter = new FilenameFilter()
		{ //only look at .txt files to disregard the "a" and "b" files
			public boolean accept(File dir, String name) 
			{
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".txt"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		ArrayList<File> malefiles = new ArrayList<File>(Arrays.asList(maleDir.listFiles(textFilter)));
		ArrayList<File> femalefiles = new ArrayList<File>(Arrays.asList(femaleDir.listFiles(textFilter)));
        ArrayList<FilePair> trainingset = new ArrayList<FilePair>();
        for(int i = 0; i<malefiles.size(); i++)
        {
        	trainingset.add(new FilePair(malefiles.get(i),0.9));
        }
        for(int i = 0; i<femalefiles.size(); i++)
        {
        	trainingset.add(new FilePair(femalefiles.get(i),0.1));
        }
		Collections.shuffle(trainingset); //randomize training data order
		ArrayList<ArrayList<FilePair>> sets = new ArrayList<ArrayList<FilePair>>();
		//divide into 5 sets
		int step = 0;
		if(trainingset.size()%5 == 0)
		{
			step = trainingset.size()/5;
		}
		else{
			step = trainingset.size()/5+1;
		}
		for (int start = 0; start < trainingset.size(); start += step) {
			int end = Math.min(start+step, trainingset.size());
			sets.add(new ArrayList<FilePair>(trainingset.subList(start, end)));
		}
		for(int i = 0; i<sets.size();i++)//i is the test set
		{
			
				for(int j = 0;j<sets.size();j++)//training sets
				{
					if(j==i)
					{
						continue;
					}
					for(int k = 0; k<sets.get(j).size();k++)//train on the training set
					{
						//System.out.println("training set " + j + " : " + sets.get(j).get(k).file);
						makeNetwork(sets.get(j).get(k).file,network);
						BackPropagationLearning(network,sets.get(j).get(k).trueval);
					}
				}
			int correct = 0;
			int wrong = 0;
			for(int k = 0; k<sets.get(i).size();k++)//test on the testing set
			{

				makeNetwork(sets.get(i).get(k).file,network);
				double result = network.networkOutput();
				//System.out.println("testing set " + i + " : " + sets.get(i).get(k).file + "  output: " + result);
				if(sets.get(i).get(k).trueval == 0.1){
					if(result < 0.5)
					{
						correct++;
					}
					else
					{
						wrong++;
					}
				}
				else
				{
					if(result > 0.5)
					{
						correct++;
					}
					else
					{
						wrong++;
					}
				}
			}
			System.out.println("Got " + correct + " correct and " + wrong + " WRONG for test set " + i);
			correct = 0;
			wrong = 0;
			for(int j = 0;j<sets.size();j++)//testing on training sets to see training accuracy
			{
				if(j==i)
				{
					continue;
				}
				for(int k = 0; k<sets.get(j).size();k++)//train on the training set
				{
					makeNetwork(sets.get(j).get(k).file,network);
					double result = network.networkOutput();
					//System.out.println("testing set " + j + " : " + sets.get(j).get(k).file + "  output: " + result);
					if(sets.get(j).get(k).trueval == 0.1){
						if(result < 0.5)
						{
							correct++;
						}
						else
						{
							wrong++;
						}
					}
					else
					{
						if(result > 0.5){
							correct++;
						}
						else
						{
							wrong++;
						}
					}
				}
			}
			System.out.println("Got " + correct + " correct and " + wrong + " WRONG for the training set data");
		}
	}
	
	public static void saveWeights (NeuralNet network)
	{
		double weights;
		String line = "";
		try
		{
			PrintWriter writer = new PrintWriter("weights.txt", "UTF-8");
			for(int j = 0; j < network.numInputs; j++)
			{
				line = "";
				for(int k = 0; k < network.hiddenLayerSize; k++)
				{
					weights = network.inputList.get(j).weight[k];
					line += String.valueOf(weights) + " ";
				}
				writer.println(line);
			}
			for (int k = 0; k < network.hiddenLayerSize; k++)
			{
				weights = network.hiddenList.get(k).weight;
				line = String.valueOf(weights);
				writer.println(line);
			}
			writer.close();
		}
		catch(UnsupportedEncodingException uee)
		{
			System.out.println("Unsupported encoding exception");
		}
		catch(FileNotFoundException fnfe)
		{
			System.out.println("File not found exception");
		}
	}
	public static NeuralNet initializeWeightsFromFile()
	{
		NeuralNet network = new NeuralNet();
		File weightFile = new File("./weights.txt");
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(weightFile));
			String line = null;
			for(int k = 0; (line = br.readLine()) != null; k++) // for each line in the file
			{
				if(k < network.numInputs) // looks good so far
				{// if we are still looking at lines containing output vectors for input nodes
					String tmp[] = line.split(" "); //tmp array representing line
					for(int l = 0; l < tmp.length; l++)
					{// iterate through the tmp array assigning the values in it to the input node's n weights
						network.inputList.get(k).weight[l] = Double.parseDouble(tmp[l]);
					}
					//read in input layer weights
				}
				else // k > network.numInputs so we're at the output weights
				{
					network.hiddenList.get(k - network.numInputs).weight = Double.parseDouble(line);
					//read in hidden layer weights
				}
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return(network);
	}
	public static NeuralNet BackPropagationLearning(NeuralNet network, double trueValue)
	{	
		double networkOutput = network.networkOutput();
		double hiddenOutput = 0;
		double deltaK = networkOutput * (1 - networkOutput) * (trueValue - networkOutput);
		for (int i = 0; i < network.hiddenLayerSize; i++)
		{
			hiddenOutput = network.hiddenList.get(i).output;
			network.hiddenList.get(i).error = hiddenOutput * (1 - hiddenOutput) * network.hiddenList.get(i).weight * deltaK;
		}
		for(int j = 0; j < 128*120; j++)
		{
			for(int k = 0; k < network.hiddenLayerSize; k++)
			{
				double weightJI = network.learningRate * network.hiddenList.get(k).error * network.inputList.get(j).input;
				network.inputList.get(j).weight[k] += weightJI;
			}
		} // updating weights for input layer
		for (int j = 0; j < network.hiddenLayerSize; j++)
		{
			double weightJI = network.learningRate * deltaK * network.hiddenList.get(j).output;
			network.hiddenList.get(j).weight += weightJI;
		}
		return(network);
	}
	public static void createMatrixFromNetwork(NeuralNet network)
	{
		int[][] matrix = new int[128][120];
		int xCounter;
		int yCounter;
		double globalMin = 0;
		double globalMax = 0;
		for (int i = 0; i < network.hiddenLayerSize; i++)
		{
			int totalCounter = 0;
			for (xCounter = 0; xCounter < 128; xCounter++)
			{
				for (yCounter = 0; yCounter < 120; yCounter++)
				{ 
					double temp = (network.inputList.get(totalCounter).weight[i]*255 + 16.28227667238654) * 7.5; // this weird constant is to equalize values to 0 so we can set them 0-255
					matrix[xCounter][yCounter] = (int)((double)temp); 
					if (matrix[xCounter][yCounter] < globalMin)
					{
						globalMin = matrix[xCounter][yCounter];
					}
					if (matrix[xCounter][yCounter] > globalMax)
					{
						globalMax = matrix[xCounter][yCounter];
					}
					totalCounter++;
				}
			}
			try
			{
				PrintWriter writer = new PrintWriter("visualizationForHiddenNode"+i+".txt", "UTF-8");
				for (int xCounter2 = 0; xCounter2 < 128; xCounter2++)
				{
					String line = "";
					for (int yCounter2 = 0; yCounter2 < 120; yCounter2++)
					{
						if (yCounter2 != 119)
						{
							line += matrix[xCounter2][yCounter2] + ",";
						}
						else if (yCounter2 == 119)
						{
							line += matrix[xCounter2][yCounter2];
						}
					}
					writer.println(line);
				}
				writer.close();
			}
			catch(UnsupportedEncodingException uee)
			{
				System.out.println("Unsupported encoding exception");
			}
			catch(FileNotFoundException fnfe)
			{
				System.out.println("File not found exception");
			}			
		}
	}/**/
}