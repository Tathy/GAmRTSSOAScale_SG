package ga.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Population;
import ga.util.Evaluation.RatePopulation;
import util.sqlLite.Log_Facade;

public class RunGA {

	private Population population;
	private Instant timeInicial;
	private int generations = 0;
	private ScriptsTable scrTable;

	private final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");
	private final String pathLogs = System.getProperty("user.dir").concat("/Tracking/");
	private final String pathInitialPopulation = System.getProperty("user.dir").concat("/InitialPopulation/");
	
	static int [] frequencyIdsRulesForUCB= new int[ConfigurationsGA.QTD_RULES];
	static int numberCallsUCB11=0;
	//private final String pathTableScripts = "/home/rubens/cluster/TesteNewGASG/Table/";

	/**
	 * Este metodo aplicará todas as fases do processo de um algoritmo Genético
	 * 
	 * @param evalFunction
	 *            Será a função de avaliação que desejamos utilizar
	 */
	public Population run(RatePopulation evalFunction) {

		// Creating the table of scripts
		scrTable = new ScriptsTable(pathTableScripts);
		//do {
			if(!ConfigurationsGA.recoverTableAG)
			{
				scrTable = scrTable.generateScriptsTable(ConfigurationsGA.SIZE_TABLE_SCRIPTS);
			}
			else
			{
				scrTable = scrTable.generateScriptsTableCurriculumVersion();
			}
		   //}while(scrTable.checkDiversityofTypes());
		scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());

		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathLogs+"Tracking.txt"));

		do {
			// Fase 1 = gerar a população inicial
			if(!ConfigurationsGA.curriculum)
			{
				population = Population.getInitialPopulation(ConfigurationsGA.SIZE_POPULATION, scrTable);
			}
			else
			{
				population = Population.getInitialPopulationCurriculum(ConfigurationsGA.SIZE_POPULATION, scrTable, pathInitialPopulation);
			}

			// Fase 2 = avalia a população
			population = evalFunction.evalPopulation(population, this.generations);
			System.out.println("Log - Generation = " + this.generations);
			f0.println("Log - Generation = " + this.generations);
			population.printWithValue(f0);
		} while (resetPopulation(population));

		resetControls();
		// Fase 3 = critério de parada
		while (continueProcess()) {

			// Fase 4 = Seleção (Aplicar Cruzamento e Mutação)
			Selection selecao = new Selection();
			population = selecao.applySelection(population, scrTable, pathTableScripts);

			// Repete-se Fase 2 = Avaliação da população
			population = evalFunction.evalPopulation(population, this.generations);

			// atualiza a geração
			updateGeneration();

			System.out.println("Log - Generation = " + this.generations);
			f0.println("Log - Generation = " + this.generations);
			population.printWithValue(f0);
			
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
				System.out.println("Population reset!");
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
