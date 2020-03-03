package ga.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;

public class Selection {

	/**
	 * Este m�todo ser� respons�vel por controlar o processo de sele��o. 
	 * Acredito que nele poder�o ser feitas as chamadas para cruzamento e para muta��o.
	 * 
	 * @param populacaoInicial que ser� utilizada para aplicarmos as altera��es.
	 * @return Population com os devidos novos cromossomos.
	 */
	
	static Random rand = new Random();
	
	// populacaInicial, na primeira itera��o, � a popula��o gerada no getInitialPopulation
	// scrTable � a tabela gerada no come�o, com o generateScriptsTable
	// pathTableScripts � o local onde a scrTable est� salva
	public Population applySelection(Population populacaoInicial,ScriptsTable scrTable, String pathTableScripts){

		//System.out.println("printing the initial population");
		//printMap(populacaoInicial.getChromosomes());

		//class preselection have the methods for selecting parents according the tournament
		// Pr�-sele��o com base em torneio e pontua��es obtidas nas avalia��es anteriores
		PreSelection ps = new PreSelection(populacaoInicial);			
		List<Map.Entry<Chromosome, BigDecimal>> parents = ps.Tournament();
		
		//System.out.println("printing the parents selected for reproduction ");
		//printList(parents);

		//Class Reproduction have the methods for getting new population according the parents obtained before using crossover and mutation
		Reproduction rp = new Reproduction(parents, scrTable, pathTableScripts);	// este construtor s� atribui campos
		//Population newPopulation = rp.UniformCrossover();	
		Population newPopulation;
		
		if(ConfigurationsGA.evolvingScript) { // evaluationScript = true
			// Retorna nova popula��o ap�s o Crossover
			newPopulation = rp.CrossoverSingleScript();
		} else {
			newPopulation=rp.Crossover();
		}
		
		//System.out.println("printing the new population after crossover");
		//printMap(newPopulation.getChromosomes());
		
		// Retorna nova popula��o ap�s o processo de muta��o
		newPopulation = rp.mutation(newPopulation);
		
		if(ConfigurationsGA.INCREASING_INDEX == true){ // INCREASING_INDEX = false
			newPopulation=rp.IncreasePopulation(newPopulation);
			newPopulation=rp.DecreasePopulation(newPopulation);
		}
		
		// Coloca invasores na popula��o
		// No TCC, a popula��o menor, baseada no script da interface, n�o deve ter invasores
		newPopulation = rp.invaders(newPopulation);
		
		//System.out.println("printing the new population after mutation");
		//printMap(newPopulation.getChromosomes());

		//in elite is saved the best guys from the last population
		HashMap<Chromosome, BigDecimal> elite = (HashMap<Chromosome, BigDecimal>)ps.sortByValue(populacaoInicial.getChromosomes());
//		System.out.println("printing elite last population");
//		printMap(elite);
		
		//here we mutate copy of the elite individuals and add to the population
		// Retorna nova elite para a nova popula��o ap�s o processo de muta��o
		newPopulation = rp.eliteMutated(newPopulation,elite);

		//joining elite and new sons in chromosomesNewPopulation, 
		HashMap<Chromosome, BigDecimal> chromosomesNewPopulation = new HashMap<Chromosome, BigDecimal>();
		chromosomesNewPopulation.putAll(newPopulation.getChromosomes());
		chromosomesNewPopulation.putAll(elite);
		
		//System.out.println("printing complete new population (elite+new population)");
		//printMap(chromosomesNewPopulation);
		
		newPopulation.setChromosomes(chromosomesNewPopulation);
		
		//if the number of the new pop is less than the initial pop, fill with random elements
		newPopulation = fillWithRandom(newPopulation,scrTable);
		
		//System.out.println("printing complete new population with new random elements If that's the case");
		//printMap(chromosomesNewPopulation);

		newPopulation = rp.RemoveCopies(newPopulation);
		
		return newPopulation;
	}

	public void printMap(HashMap<Chromosome, BigDecimal> m)
	{
		for (Chromosome ch: m.keySet()){

			String key =ch.getGenes().toString();
			String value = m.get(ch).toString();  
			System.out.println(key + " " + value);  


		} 
	}
	public void printList(List<Map.Entry<Chromosome, BigDecimal>> l)
	{
		for (Map.Entry<Chromosome, BigDecimal> it: l){

			String key =it.getKey().getGenes().toString();
			String value = it.getValue().toString(); 
			System.out.println(key + " " + value);

		} 
	}
	public Population fillWithRandom(Population p,ScriptsTable scrTable)
	{
		while(p.getChromosomes().size()<ConfigurationsGA.SIZE_POPULATION)
		{
			Chromosome tChom = new Chromosome();
			int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
			for (int j = 0; j < sizeCh; j++) {
				tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			p.getChromosomes().put(tChom, BigDecimal.ZERO);			
		}
		return p;
	}

}
