package ga.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import util.sqlLite.Log_Facade;


// Modificado para rodar com Script criado atrav�s da interface
//TESTE

public class RunGA {

	private Population population;
	private Instant timeInicial;
	private int generations = 0;
	private ScriptsTable scrTable;
	
	private Population population2;
	private ScriptsTable scrTable2;
	
	private final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
	//private final String pathLogs = System.getProperty("user.dir").concat("/Tracking/");
	private final String pathInitialPopulation = System.getProperty("user.dir").concat("/InitialPopulation/");
	private final String pathUsedCommands = System.getProperty("user.dir").concat("/commandsUsed/");
	
	static int [] frequencyIdsRulesForUCB= new int[ConfigurationsGA.QTD_RULES];
	static int numberCallsUCB11=0;
	//private final String pathTableScripts = "/home/rubens/cluster/TesteNewGASG/Table/";

	
	// O que est� marcado com 4 barras foi o que o Julian tirou para poder testar no notebook
	/**
	 * Este metodo aplicar� todas as fases do processo de um algoritmo Gen�tico
	 * 
	 * @param evalFunction
	 *            Ser� a fun��o de avalia��o que desejamos utilizar
	 */
	
	
	//  A fun��o de avalia��o ir� controlar as chamadas no cluster, ou fazer os c�lculos das simula��es e entregar uma popula��o devidamente avaliada.
	public Population run(RatePopulation evalFunction) {

		// Creating the table of scripts
		scrTable = new ScriptsTable(pathTableScripts, "1");
		scrTable2 = new ScriptsTable(pathTableScripts, "2");
		//do {
			if(!ConfigurationsGA.recoverTable) {  //recoverTable = false
				// Preenche tabela de scripts com o indiv�duo vindo da interface com a key 0
				scrTable = scrTable.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
				scrTable2 = scrTable2.generateScriptsTableMutation(ConfigurationsGA.SIZE_TABLE_SCRIPTS_MUTATION);
			} else {
				scrTable = scrTable.generateScriptsTableRecover();
			}
			
		//}while(scrTable.checkDiversityofTypes());
		scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());

		PrintWriter f0;
		try {
			////f0 = new PrintWriter(new FileWriter(pathLogs+"Tracking.txt")); //trocar
			f0 = new PrintWriter(new FileWriter("Tracking.txt")); // Arquivo onde a primeira popula��o vai ser salva

			do {	//cluster
				// FASE 1 = gerar a popula��o inicial
				if(!ConfigurationsGA.curriculum){ //curriculum = false
					population = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTable);
				} else {
					population = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTable, pathInitialPopulation);
				}		
			

				// FASE 2 = avalia a popula��o
				////population = evalFunction.evalPopulation(population, this.generations, scrTable); //custer (descomentar para teste local que o Rubens ensinou)
				
				//population.printWithValue(f0);
				//System.out.println("sep");
				
				//Get all the used commands
				if(ConfigurationsGA.removeRules==true)
					population.fillAllCommands(pathTableScripts);
				
				/*
			    Iterator it = population.getAllCommandsperGeneration().entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        int id=(Integer)pair.getKey();
			        List<String> scripts= (List<String>) pair.getValue();
			        System.out.println("key "+id+" "+scripts);
			    }
			    */
			    
				//Choose the used commands
				if(ConfigurationsGA.removeRules==true)
					population.chooseusedCommands(pathUsedCommands);
				
				/*
			    Iterator it = population.getUsedCommandsperGeneration().entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        int id=(Integer)pair.getKey();
			        List<String> scripts= (List<String>) pair.getValue();
			        System.out.println("key "+id+" "+scripts);
			        //it.remove(); // avoids a ConcurrentModificationException
			    }
				*/
				
				//Remove used commands from all commands
				if(ConfigurationsGA.removeRules==true)
					population.removeCommands(scrTable);
				/*
			    Iterator it3 = population.getAllCommandsperGeneration().entrySet().iterator();
			    while (it3.hasNext()) {
			        Map.Entry pair = (Map.Entry)it3.next();
			        int id=(Integer)pair.getKey();
			        List<String> scripts= (List<String>) pair.getValue();
			        System.out.println("key "+id+" "+scripts);
			    }
			    */
				
			    // Impress�o dos indiv�duos da popula��o e seus valores de avalia��o ap�s partidas no cluster
				//System.out.println("Log - Generation = " + this.generations);
				//f0.println("Log - Generation = " + this.generations);
				//population.printWithValue(f0);
				
			} while (resetPopulation(population));	//cluster

		resetControls();
		
		// FASE 3 = crit�rio de parada
		while (continueProcess()) {

			// FASE 4 = Sele��o (Aplicar Cruzamento e Muta��o)
			Selection selecao = new Selection();	// sem construtor
			// Retorna a nova popula��o ap�s todos os processos envolvidos na reprodu��o para a pr�xima gera��o
			population = selecao.applySelection(population, scrTable, pathTableScripts);

			// Repete-se Fase 2 = Avalia��oo da popula��o
			////population = evalFunction.evalPopulation(population, this.generations, scrTable);	//cluster
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true)
				population.fillAllCommands(pathTableScripts);
			
			//Remove the unused commands
			if(ConfigurationsGA.removeRules==true)
				population.chooseusedCommands(pathUsedCommands);
//		    Iterator it = population.getUsedCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		        //it.remove(); // avoids a ConcurrentModificationException
//		    }
			//Remove used commands from all commands
			if(ConfigurationsGA.removeRules==true)
				population.removeCommands(scrTable);

			// atualiza a geração
			updateGeneration();

			//System.out.println("Log - Generation = " + this.generations);
			//f0.println("Log - Generation = " + this.generations);
			//population.printWithValue(f0);
			
			if(ConfigurationsGA.UCB1==true)
			{
				Log_Facade.shrinkRewardTable();
				System.out.println("call shrink");
			}
		}
		
		f0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return population;
	}

	private boolean resetPopulation(Population population2) {
		if (ConfigurationsGA.RESET_ENABLED) {
			if (population2.isPopulationValueZero()) {
				//System.out.println("Population reset!");
				return true;
			}
		}
		return false;
	}

	private void updateGeneration() {
		this.generations++;
	}

	private boolean continueProcess() {
		switch (ConfigurationsGA.TYPE_CONTROL) {
		case 0:
			return hasTime();

		case 1:
			return hasGeneration();

		default:
			return false;
		}

	}

	private boolean hasGeneration() {
		if (this.generations < ConfigurationsGA.QTD_GENERATIONS) {
			return true;
		}
		return false;
	}

	/**
	 * Função que inicia o contador de tempo para o critério de parada
	 */
	protected void resetControls() {
		this.timeInicial = Instant.now();
		this.generations = 0;
	}

	protected boolean hasTime() {
		Instant now = Instant.now();

		Duration duracao = Duration.between(timeInicial, now);

		// System.out.println( "Horas " + duracao.toMinutes());

		if (duracao.toHours() < ConfigurationsGA.TIME_GA_EXEC) {
			return true;
		} else {
			return false;
		}

	}
	
}
