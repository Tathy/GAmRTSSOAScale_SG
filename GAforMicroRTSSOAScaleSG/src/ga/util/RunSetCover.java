package ga.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import SetCoverSampling.ConfigurationsSC;
import SetCoverSampling.GameSampling;
import SetCoverSampling.IndividualFitness;
import SetCoverSampling.StateAction;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import rts.GameState;
import rts.PlayerAction;
import util.sqlLite.Log_Facade;


public class RunSetCover {
	
	private final String dirPathPlayer = System.getProperty("user.dir").concat("/logs_game/logs_states/");
    //private final String dirPathPlayer = "logs_game/logs_states/";
    
    private final static String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
    //private final static String pathTableScripts = "Table/";
    public HashMap<String, List<Integer>> dataH=new HashMap<String, List<Integer>>();
	public RunSetCover()
	{
		ScriptsTable st=new ScriptsTable(pathTableScripts);
		ArrayList<String> basicFunctions= st.allBasicFunctions();
		File[] files = new File(dirPathPlayer).listFiles();	
		
		presampling(files,basicFunctions);
		

	}

	public  void presampling(File[] files, ArrayList<String> allCommands) {
	    for (File file : files) {
	            //System.out.println("Directory: " + file.getName());
	            //sampling(file.listFiles()); // Calls same method again.
	    	
	    		//For player0 //we should interchange player here in order to avoid influence of the map side
	    		String pathPlayer=file.getAbsolutePath()+"/player1";
	    		File filePlayer=new File(pathPlayer);
					
	        	samplingByFiles(filePlayer.getName(), filePlayer.listFiles(), allCommands, pathPlayer);	        	
	    }
	}
	
	public  void samplingByFiles(String folderLeader, File[] Files, ArrayList<String> allCommands, String pathPlayer)
	{
		new File(pathPlayer+"/sampling").mkdirs();
		GameSampling game = new GameSampling();
		Random rand = new Random();
		int numberStatesSampled=ConfigurationsSC.NUM_STATES_SAM;
		int stateForSampling=0;

		ArrayList<String> statesforSampling = new ArrayList<>();
		StateAction sa=new StateAction();
		int totalActionsAllStates=0;
		for (int i=0;i<Files.length;i++)
		{
			//System.out.println("new state "+i);
			//System.out.println("sa "+Files[i].getName());
//			try {
//				TimeUnit.SECONDS.sleep(2);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			try {
				sa = readFile(Files[i].getPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			

			GameState gsSimulator = GameState.fromJSON(sa.getState(),game.utt);
			String []listactionsAllStates=unitActionSplitted(sa.getAction());
			totalActionsAllStates=totalActionsAllStates+listactionsAllStates.length;
			
//			if (gsSimulator.canExecuteAnyAction(0)){
//				for (int j = 0; j < ConfigurationsSC.TOTAL_SCRIPTS; j++) {
//
//
//					gsSimulator = GameState.fromJSON(statesforSampling.get(i),game.utt);
//					//System.out.println(gsSimulator.toString());
//
//					PlayerAction pa= game.generateActionbyScript(gsSimulator, j, 0);
//					try {
//						Writer writer = new FileWriter("samplings/"+folderLeader+"_state_"+stateForSampling+"_idLogs_"+pathLog+"_player_0"+".txt",true);
//						writer.write(pa.getActions().toString());
//						writer.write("\n");
//						writer.flush();
//						writer.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					System.gc(); // forço o garbage para tentar liberar memoria....
//
//
//				}
//			}

			if (gsSimulator.canExecuteAnyAction(1)){
				for (int j = 0; j < allCommands.size(); j++) {

						ArrayList<PlayerAction> paL= game.generateActionbyScriptByString(gsSimulator,allCommands.get(j) , 1);
//						System.out.println("actions script "+pa.getActions().toString());
//						System.out.println("actions state "+sa.getAction());						
//						System.out.println(Arrays.toString(parts));
//						System.out.println(parts[k]);
//						System.out.println(pa.getActions().toString());		
						fitnessUnitAction(paL, sa,j);
//						try {
//							Writer writer = new FileWriter(pathPlayer+"/sampling/"+Files[i].getName()+".txt",true);
//							writer.write(pa.getActions().toString());
//							writer.write("\n");
//							writer.flush();
//							writer.close();
//						} catch (Exception e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
//						System.gc(); 
//					
//
//				}
			}
			}
		}	
		System.out.println("");
		System.out.println("AllActionsAllStates "+totalActionsAllStates);
		System.out.println("");
		//print the objects 
//		for(IndividualFitness ind:listIndividualFitness)
//		{
//			System.out.println("ind "+ind.getIndividual()+" "+ind.getFitness());
//		}
	}
	
	 public void fitnessUnitAction(ArrayList<PlayerAction> paL, StateAction sa, int idScript) {
		int counterFItness=0;
		
		String [] unitActionsStateAction=  unitActionSplitted(sa.getAction());
		
		for(PlayerAction pa:paL)
		{
		String [] unitActionsPlayerAction=  unitActionSplitted(pa.getActions().toString());

		for(String uasa:unitActionsStateAction)
		{
			
			//System.out.println("uasa "+uasa);
			for(String uapa:unitActionsPlayerAction)
			{	//System.out.println("uapa "+uapa);
			
				if(uapa.equals(uasa) && !uasa.contains("wait"))
				{
					
					if(!dataH.containsKey(sa.getNameState()+"_"+uasa))
					{
						List<Integer> CommandsCovering=new ArrayList<Integer>();
						CommandsCovering.add(idScript);
						dataH.put(sa.getNameState()+"_"+uasa, CommandsCovering);
					}
					else
					{
						List<Integer> CommandsCovering=dataH.get(sa.getNameState()+"_"+uasa);
						CommandsCovering.add(idScript);
						dataH.put(sa.getNameState()+"_"+uasa, CommandsCovering);
					}
										
				}
			}
		}
		}

	}
	
	
	static StateAction readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		StateAction sa=new StateAction();
		sa.setNameState(fileName);
		try {
			String line = br.readLine();

			while (line != null) {
				sa.setState(line);
				line = br.readLine();
				sa.setAction(line);
				line = br.readLine();
			}
			return sa;
		} finally {
			br.close();
		}
	}
	
	static String [] unitActionSplitted(String toSplit){
		toSplit= toSplit.replace("[<", "");
		toSplit= toSplit.replace(">]", "");
		String[] parts = toSplit.split(">, <");
		return parts;
	}
	
    private static String getLine(String arquivo) {
        File file = new File(arquivo);
        String linha = "";
        try {
            FileReader arq = new FileReader(file);
            java.io.BufferedReader learArq = new BufferedReader(arq);
            linha = learArq.readLine();

            arq.close();
        } catch (Exception e) {
            System.err.printf("Erro na leitura da linha de configuração");
            System.out.println(e.toString());
        }
        return linha;
    }
}