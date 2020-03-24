package ga.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import ga.util.Evaluation.RatePopulations;
import util.sqlLite.Log_Facade;


public class RunGA {

	private Population population1;
	private Instant timeInicial;
	private int generations = 0;
	private ScriptsTable scrTable1;
	
	private Population population2;
	private ScriptsTable scrTable2;
	
	private final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
	private final String pathLogs = System.getProperty("user.dir").concat("/Tracking/");
	private final String pathInitialPopulation = System.getProperty("user.dir").concat("/InitialPopulation/");
	private final String pathUsedCommands = System.getProperty("user.dir").concat("/commandsUsed/");
	
	static int [] frequencyIdsRulesForUCB= new int[ConfigurationsGA.QTD_RULES];
	static int numberCallsUCB11=0;
	//private final String pathTableScripts = "/home/rubens/cluster/TesteNewGASG/Table/";

	
	// O que está marcado com 4 barras foi o que o Julian tirou para poder testar no notebook
	/**
	 * Este metodo aplicará todas as fases do processo de um algoritmo Genético
	 * 
	 * @param evalFunction
	 *            Será a função de avaliação que desejamos utilizar
	 */
	
	
	//  A função de avaliação irá controlar as chamadas no cluster, ou fazer os cálculos das simulações e entregar uma população devidamente avaliada.
	public Population run(RatePopulations evalFunction) {

		// Creating the table of scripts
		scrTable1 = new ScriptsTable(pathTableScripts, "1");
		scrTable2 = new ScriptsTable(pathTableScripts, "2");
		//do {
			if(!ConfigurationsGA.recoverTable) {  //recoverTable = false
				// Preenche tabela com scripts aleatórios
				scrTable1 = scrTable1.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
				// Preenche tabela de scripts com o indivíduo vindo da interface com a key 0 e o resto com mutações dele
				scrTable2 = scrTable2.generateScriptsTableMutation(ConfigurationsGA.SIZE_TABLE_SCRIPTS_2);
			} else {
				scrTable1 = scrTable1.generateScriptsTableRecover();
			}
		//}while(scrTable.checkDiversityofTypes());
		scrTable1.setCurrentSizeTable(scrTable1.getScriptTable().size());
		scrTable2.setCurrentSizeTable(scrTable2.getScriptTable().size());

		PrintWriter f1;
		PrintWriter f2;
		try {
			f1 = new PrintWriter(new FileWriter(pathLogs+"Tracking1.txt")); // Arquivo onde a primeira população vai ser salva
			f2 = new PrintWriter(new FileWriter(pathLogs+"Tracking2.txt")); // Arquivo onde a segunda população vai ser salva

			//do {	//cluster
				// FASE 1 = gerar a população inicial
				if(!ConfigurationsGA.curriculum){ //curriculum = false
					population1 = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTable1, false);
					population2 = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION_2, scrTable2, true);
				} else {
					population1 = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTable1, pathInitialPopulation);
				}	
				
				
				System.out.println("--------- POPULAÇÃO 1 inicial " +  "-------------");
				for(Chromosome k : population1.getChromosomes().keySet() ) {
					BigDecimal value = population1.getChromosomes().get(k);
					System.out.print(k.getGenes() + " = " + value + "; ");
				}
				System.out.println();
				System.out.println("--------- POPULAÇÃO 2 inicial " +  "-------------");
				for(Chromosome k : population2.getChromosomes().keySet() ) {
					BigDecimal value = population2.getChromosomes().get(k);
					System.out.print(k.getGenes() + " = " + value + "; ");
				}
				System.out.println();
				

				// FASE 2 = avalia a população
				//population1 = evalFunction.evalPopulation(population1, population2, this.generations, scrTable1, scrTable2, "1", "2"); //custer (descomentar para teste local que o Rubens ensinou)
				//population2 = evalFunction.evalPopulation(population2, population1, this.generations, scrTable2, scrTable1, "2", "1");
				
				population1.printWithValue(f1);
				population2.printWithValue(f2);
				//System.out.println("sep");
				
				//Get all the used commands
				if(ConfigurationsGA.removeRules == true) {
					population1.fillAllCommands(pathTableScripts, "1");
					population2.fillAllCommands(pathTableScripts, "2");
				}
				/*	
				System.out.println("--------- POPULAÇÃO 1 inicial -------------");
				for(Chromosome k : population1.getChromosomes().keySet() ) {
					BigDecimal value = population1.getChromosomes().get(k);
					System.out.print(k.getGenes() + " = " + value + "; ");
				}
				System.out.println();
				System.out.println("--------- POPULAÇÃO 2 inicial -------------");
				for(Chromosome k : population2.getChromosomes().keySet() ) {
					BigDecimal value = population2.getChromosomes().get(k);
					System.out.print(k.getGenes() + " = " + value + "; ");
				}
				System.out.println();
			    */
				//Choose the used commands
				if(ConfigurationsGA.removeRules==true) {
					population1.chooseusedCommands(pathUsedCommands, "1");
					population2.chooseusedCommands(pathUsedCommands, "2");
				}
				
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
				if(ConfigurationsGA.removeRules==true) {
					population1.removeCommands(scrTable1);
					population2.removeCommands(scrTable2);
				}
				
				/*
			    Iterator it3 = population2.getAllCommandsperGeneration().entrySet().iterator();
			    while (it3.hasNext()) {
			        Map.Entry pair = (Map.Entry)it3.next();
			        int id=(Integer)pair.getKey();
			        List<String> scripts= (List<String>) pair.getValue();
			        System.out.println("key "+id+" "+scripts);
			    }
			    */
				
			    // Impressão dos indivíduos da população e seus valores de avaliação após partidas no cluster
				//System.out.println("Log - Generation = " + this.generations);
				f1.println("Log - Generation = " + this.generations);
				f2.println("Log - Generation = " + this.generations);
				population1.printWithValue(f1);
				population1.printWithValue(f2);
				
			//} while (resetPopulation(population1) && resetPopulation(population2));	//cluster

		resetControls();
		
		// FASE 3 = critério de parada
		while (continueProcess()) {

			// FASE 4 = Seleção (Aplicar Cruzamento e Mutação)
			Selection selecao1 = new Selection();
			Selection selecao2 = new Selection(); 
			// Retorna a nova população após todos os processos envolvidos na reprodução para a próxima geração
			if(generations != 0) {
				population1 = selecao1.applySelection(population1, scrTable1, pathTableScripts);
				population2 = selecao2.applySelection(population2, scrTable2, pathTableScripts);
			}

			// Repete-se Fase 2 = Avaliaçãoo da população
			ArrayList<Population> populations = new ArrayList<>();
			populations.add(population1);
			populations.add(population2);
			populations = evalFunction.evalPopulation(populations, this.generations, scrTable1, scrTable2, "1", "2");
			population1 = populations.get(0);
			population2 = populations.get(1);
			
			System.out.println("--------- POPULAÇÃO 1 após avaliação " + generations +  "-------------");
			for(Chromosome k : population1.getChromosomes().keySet() ) {
				BigDecimal value = population1.getChromosomes().get(k);
				System.out.print(k.getGenes() + " = " + value + "; ");
			}
			System.out.println();
			System.out.println("--------- POPULAÇÃO 2 após avaliação " + generations +  "-------------");
			for(Chromosome k : population2.getChromosomes().keySet() ) {
				BigDecimal value = population2.getChromosomes().get(k);
				System.out.print(k.getGenes() + " = " + value + "; ");
			}
			System.out.println();
			
			//Get all the used commands
			if(ConfigurationsGA.removeRules==true) {
				population1.fillAllCommands(pathTableScripts, "1");
				population2.fillAllCommands(pathTableScripts, "2");
			}
			
			//Remove the unused commands
			if(ConfigurationsGA.removeRules==true) {
				population1.chooseusedCommands(pathUsedCommands, "1");
				population2.chooseusedCommands(pathUsedCommands, "2");
			}
//		    Iterator it = population.getUsedCommandsperGeneration().entrySet().iterator();
//		    while (it.hasNext()) {
//		        Map.Entry pair = (Map.Entry)it.next();
//		        int id=(Integer)pair.getKey();
//		        List<String> scripts= (List<String>) pair.getValue();
//		        System.out.println("key "+id+" "+scripts);
//		        //it.remove(); // avoids a ConcurrentModificationException
//		    }
			//Remove used commands from all commands
			if(ConfigurationsGA.removeRules==true) {
				population1.removeCommands(scrTable1);
				population2.removeCommands(scrTable2);
			}

			// atualiza a geração
			updateGeneration();

			//System.out.println("Log - Generation = " + this.generations);
			f1.println("Log - Generation = " + this.generations);
			population1.printWithValue(f1);
			f2.println("Log - Generation = " + this.generations);
			population2.printWithValue(f2);
			
			if(ConfigurationsGA.UCB1==true)
			{
				Log_Facade.shrinkRewardTable();
				System.out.println("call shrink");
			}
			
		}
		
		f1.close();
		f2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		System.out.println("--------- POPULAÇÃO 2 final -------------");
		for(Chromosome k : population2.getChromosomes().keySet() ) {
			BigDecimal value = population2.getChromosomes().get(k);
			System.out.print(k.getGenes() + " = " + value + "; ");
		}
	    
		return population2;
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
	 * FunÃ§Ã£o que inicia o contador de tempo para o critÃ©rio de parada
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
