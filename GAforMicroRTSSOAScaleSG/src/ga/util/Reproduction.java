package ga.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import ai.ScriptsGenerator.TableGenerator.FunctionsforGrammar;
import ga.ScriptTableGenerator.ChromosomeScript;
import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import util.Cut_Point;
import util.sqlLite.UCB_Facade;

public class Reproduction {

	static Random rand = new Random();

	List<Map.Entry<Chromosome, BigDecimal>> parents;
	ScriptsTable scrTable;
	FunctionsforGrammar functions;
	
	private static String pathTableScripts; 
	
	public Reproduction(List<Map.Entry<Chromosome, BigDecimal>> parents,ScriptsTable scrTable, String pathTableScripts){
		this.parents = parents;
		this.scrTable = scrTable;
		this.pathTableScripts = pathTableScripts;
	}
	
	public Population UniformCrossover() {
		Population newGeneration;
		HashMap<Chromosome, BigDecimal> newChromosomes =new HashMap<Chromosome, BigDecimal>();	
		
		while(newChromosomes.size()<ConfigurationsGA.SIZE_POPULATION-ConfigurationsGA.SIZE_ELITE) {
			//here we shuffle the list of parents in order to select always two different parents to reproduce
			Collections.shuffle(parents);
			Chromosome parent1 = parents.get(0).getKey();
			Chromosome parent2 = parents.get(1).getKey();
			Chromosome child = new Chromosome();

			//The uniform crossover add to the son one of the parents gene for each position (selected randomly)
			// Tamanhos dos genes dos pais e o maior entre os dois escolhidos
			int sizeParent1 = parent1.getGenes().size();
			int sizeParent2 = parent2.getGenes().size();
			int maxSize=Math.max(sizeParent1, sizeParent2);

			for(int i=0;i<maxSize;i++) {
				int newGen=rand.nextInt(2)+1; //valor sorteado 1 ou 2
				//if random was 1, add the parent1 gene, if was 2 add the parent2 gene.
				// Cada gene � um comando usado nos scripts que formam o cromossomo, � mapeado em BasicExpandedConfigurableScript
				if(newGen == 1) {
					if(i<sizeParent1)
						child.addGene(parent1.getGenes().get(i));
				} else {
					if(i<sizeParent2)
						child.addGene(parent2.getGenes().get(i));
				}
			}
			
			//The next method is just for avoiding infinite loops, adding a random element if
			//one with the same key was already added (this can happen because sometimes the resulting
			//element has the same KEY, and produce that the size of the map be always the same) 
			if(newChromosomes.containsKey(child)){
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				
				newChromosomes.put(tChom, BigDecimal.ZERO);
			}

			//here is added the child!
			newChromosomes.put(child, BigDecimal.ZERO);
		}
		
		newGeneration = new Population(newChromosomes);
		return newGeneration;
	}
	
	public Population Crossover()
	{
		Population newGeneration;
		HashMap<Chromosome, BigDecimal> newChromosomes =new HashMap<Chromosome, BigDecimal>();		
		int numberEliteMutated=ConfigurationsGA.SIZE_ELITE;
		while(newChromosomes.size()<(ConfigurationsGA.SIZE_POPULATION-ConfigurationsGA.SIZE_ELITE-ConfigurationsGA.SIZE_INVADERS-numberEliteMutated))
		{
			//here we shuffle the list of parents in order to select always two different parents to reproduce
			Collections.shuffle(parents);
			Chromosome parent1=parents.get(0).getKey();
			Chromosome parent2=parents.get(1).getKey();
			Chromosome child1= new Chromosome();
			Chromosome child2= new Chromosome();

			//The uniform crossover add to the son one of the parents gene for each position (selected randomly)
			int sizeParent1=parent1.getGenes().size();
			int sizeParent2=parent2.getGenes().size();
			
			int breakParent1;
			int breakParent2;
			
			if(sizeParent1>1)
			{
				breakParent1=rand.nextInt(sizeParent1+1);
			}
			else
			{
				breakParent1=0;
			}
			if(sizeParent2>1)
			{
				breakParent2=rand.nextInt(sizeParent2+1);
			}
			else
			{
				breakParent2=0;
			}
			
			ArrayList<Integer> p1sub1= new ArrayList<>();
			ArrayList<Integer> p1sub2= new ArrayList<>();
			ArrayList<Integer> p2sub1= new ArrayList<>();
			ArrayList<Integer> p2sub2= new ArrayList<>();

			
			
			for(int i=0;i<breakParent1;i++)
			{
				p1sub1.add(parent1.getGenes().get(i));
			}
			for(int i=breakParent1;i<sizeParent1;i++)
			{
				p1sub2.add(parent1.getGenes().get(i));
			}
			
			for(int i=0;i<breakParent2;i++)
			{
				p2sub1.add(parent2.getGenes().get(i));
			}
			for(int i=breakParent2;i<sizeParent2;i++)
			{
				p2sub2.add(parent2.getGenes().get(i));
			}	

			child1.getGenes().addAll(p1sub1);
			child1.getGenes().addAll(p2sub2);
			
			child2.getGenes().addAll(p2sub1);
			child2.getGenes().addAll(p1sub2);

			//The next method is just for avoiding infinite loops, adding a random element if
			//one with the same key was already added (this can happen because sometimes the resulting
			//element has the same KEY, and produce that the size of the map be always the same) 
			if(newChromosomes.containsKey(child1))
			{
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				newChromosomes.put(tChom, BigDecimal.ZERO);
			}
			
			if(newChromosomes.containsKey(child2))
			{
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				newChromosomes.put(tChom, BigDecimal.ZERO);
			}

			//here is added the child!
			if(child1.getGenes().size()!=0)
				newChromosomes.put(child1, BigDecimal.ZERO);
			
			if(child2.getGenes().size()!=0)
				newChromosomes.put(child2, BigDecimal.ZERO);
		}
		newGeneration=new Population(newChromosomes);
		return newGeneration;
	}
	
	public Population CrossoverSingleScript(String id){
		Population newGeneration;
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<Chromosome, BigDecimal>();		
		int numberEliteMutated, size_elite, size_population, size_invaders;
		
		if(id == "1") {
			numberEliteMutated = ConfigurationsGA.SIZE_ELITE;
			size_elite = ConfigurationsGA.SIZE_ELITE;
			size_population = ConfigurationsGA.SIZE_POPULATION;
			size_invaders = ConfigurationsGA.SIZE_INVADERS;
		} else {
			numberEliteMutated = ConfigurationsGA.SIZE_ELITE_2;
			size_elite = ConfigurationsGA.SIZE_ELITE_2;
			size_population = ConfigurationsGA.SIZE_POPULATION_2;
			size_invaders = ConfigurationsGA.SIZE_INVADERS_2;
		}
		
		while(newChromosomes.size() < (size_population - size_elite - size_invaders - numberEliteMutated)){
			//here we shuffle the list of parents in order to select always two different parents to reproduce
			Collections.shuffle(parents);
			Chromosome parent1=parents.get(0).getKey();
			Chromosome parent2=parents.get(1).getKey();
//			System.out.println("parent1 "+parent1.getGenes());
//			System.out.println("parent2 "+parent2.getGenes());
			
			String [] parentGenotype1=recoverParentStringParts(parent1.getGenes().get(0));
			String [] parentGenotype2=recoverParentStringParts(parent2.getGenes().get(0));
			
			Chromosome child1= new Chromosome();
			Chromosome child2= new Chromosome();
			ArrayList <String> childGenotype1=new ArrayList<>();
			ArrayList <String> childGenotype2=new ArrayList<>();

			//The uniform crossover add to the son one of the parents gene for each position (selected randomly)
			int sizeParent1=parentGenotype1.length;
			int sizeParent2=parentGenotype2.length;
			
			int breakParent1;
			int breakParent2;
			
			if(sizeParent1>1)
			{
				breakParent1=rand.nextInt(sizeParent1+1);
			}
			else
			{
				breakParent1=0;
			}
			if(sizeParent2>1)
			{
				breakParent2=rand.nextInt(sizeParent2+1);
			}
			else
			{
				breakParent2=0;
			}
			
			ArrayList<String> p1sub1= new ArrayList<>();
			ArrayList<String> p1sub2= new ArrayList<>();
			ArrayList<String> p2sub1= new ArrayList<>();
			ArrayList<String> p2sub2= new ArrayList<>();

			
			
			for(int i=0;i<breakParent1;i++)
			{
				p1sub1.add(parentGenotype1[i]);
			}
			for(int i=breakParent1;i<sizeParent1;i++)
			{
				p1sub2.add(parentGenotype1[i]);
			}
			
			for(int i=0;i<breakParent2;i++)
			{
				p2sub1.add(parentGenotype2[i]);
			}
			for(int i=breakParent2;i<sizeParent2;i++)
			{
				p2sub2.add(parentGenotype2[i]);
			}	
			
//			System.out.println("p1sub1");
//			p1sub1.forEach(System.out::println);
//			
//			System.out.println("p1sub2");
//			p1sub2.forEach(System.out::println);
//			
//			System.out.println("p2sub1");
//			p2sub1.forEach(System.out::println);
//			
//			System.out.println("p2sub2");
//			p2sub2.forEach(System.out::println);

			childGenotype1.addAll(p1sub1);
			childGenotype1.addAll(p2sub2);
			String[] arrchildGenotype1 = childGenotype1.toArray(new String[childGenotype1.size()]);
			
			childGenotype2.addAll(p1sub2);
			childGenotype2.addAll(p2sub1);
			String[] arrchildGenotype2 = childGenotype2.toArray(new String[childGenotype2.size()]);
			
//			System.out.println("childGenotype1");
//			childGenotype1.forEach(System.out::println);
//			
//			System.out.println("childGenotype2");
//			childGenotype2.forEach(System.out::println);

			String childConcatenated1=recoverStringFromArray(arrchildGenotype1);
			childConcatenated1=childConcatenated1.trim();
			String childConcatenated2=recoverStringFromArray(arrchildGenotype2);
			childConcatenated2=childConcatenated2.trim();
			
//			System.out.println("childConcatenated1"+childConcatenated1);
//			System.out.println("childConcatenated2"+childConcatenated2);
			
			int newId;
			if(scrTable.getScriptTable().containsKey(childConcatenated1))
			{
				newId= scrTable.getScriptTable().get(childConcatenated1).intValue();
				//System.out.println("oldId1 "+ newId);
				child1.addGene(newId);
			}
			else if(!scrTable.getScriptTable().containsKey(childConcatenated1) && childConcatenated1.length()>0)
			{
				//System.out.println("beforeMutateScript "+cromScriptOriginal);
				//System.out.println("afterMutateScript "+cromScript);
				newId=scrTable.getScriptTable().size();
				scrTable.getScriptTable().put(childConcatenated1, BigDecimal.valueOf(newId));
				scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());
				addLineFile(newId+" "+childConcatenated1, scrTable.getID());
				//System.out.println("newId1 "+ newId);
				child1.addGene(newId);
			}
			
			
			if(scrTable.getScriptTable().containsKey(childConcatenated2))
			{
				newId= scrTable.getScriptTable().get(childConcatenated2).intValue();
				//System.out.println("oldId2 "+ newId);
				child2.addGene(newId);
			}
			else if(!scrTable.getScriptTable().containsKey(childConcatenated2) && childConcatenated2.length()>0)
			{
				//System.out.println("beforeMutateScript "+cromScriptOriginal);
				//System.out.println("afterMutateScript "+cromScript);
				newId=scrTable.getScriptTable().size();
				scrTable.getScriptTable().put(childConcatenated2, BigDecimal.valueOf(newId));
				scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());
				addLineFile(newId+" "+childConcatenated2, scrTable.getID());
				child2.addGene(newId);
				//System.out.println("newId2 "+ newId);
			}

			//The next method is just for avoiding infinite loops, adding a random element if
			//one with the same key was already added (this can happen because sometimes the resulting
			//element has the same KEY, and produce that the size of the map be always the same) 
			if(newChromosomes.containsKey(child1))
			{
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				newChromosomes.put(tChom, BigDecimal.ZERO);
			}
			
			if(newChromosomes.containsKey(child2))
			{
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				newChromosomes.put(tChom, BigDecimal.ZERO);
			}
			
//			System.out.println("child1 "+child1.getGenes());
//			System.out.println("child2 "+child2.getGenes());

			//here is added the child!
			if(child1.getGenes().size()!=0)
				newChromosomes.put(child1, BigDecimal.ZERO);
			
			if(child2.getGenes().size()!=0)
				newChromosomes.put(child2, BigDecimal.ZERO);
		}
		newGeneration=new Population(newChromosomes);
		return newGeneration;
	}

	private String [] recoverParentStringParts(Integer id) {
		String script = cromosomeById(id);
		ArrayList <String> listFragments = Cut_Point.cut_in_fragments(script);
		String [] parentsSplit = listFragments.toArray(new String[listFragments.size()]);;
		return parentsSplit;
	}

	private int recoverSizeParent(Integer id) {
		String script=cromosomeById(id);
		
		return 0;
	}
	
	// Retorna nova população após o processo de mutação
	@SuppressWarnings("unchecked")
	public Population mutation(Population population, String id) {
		//This method replace each gene with a random script with a probability of 10%
		HashMap<Chromosome, BigDecimal> chromosomesMutated = new HashMap<>();
		
		// Itera sobre todos os cromossomos da população passada como parâmetro
		for(Chromosome chromosome : population.getChromosomes().keySet()){
			Chromosome newCh = new Chromosome();
			newCh.setGenes((ArrayList<Integer>) chromosome.getGenes().clone());
			
			for(int i = 0; i < newCh.getGenes().size(); i++){
				double mutatePercent = ConfigurationsGA.MUTATION_RATE;  // MUTATION_RATE = 30%
				boolean m = rand.nextFloat() <= mutatePercent;
				
				if(ConfigurationsGA.evolvingScript){ // evolvingScript = true
					m=true;
				}

				if(m){
					//newCh.getGenes().set(i, rand.nextInt(scrTable.getCurrentSizeTable()));
					
					//The next line is added in order to keep mutation of rules
					// A mutationScript faz a mutação com manipulação de strings, adiciona o novo cromossomo (script) na scrTable e retorna o ID deste novo cromossomo
					newCh.getGenes().set(i, mutationScript(population, newCh.getGenes().get(i), id));
				}
			}
			
			// Coloca o novo cromossomo, com muta��o sofrida ou n�o, no novo conjunto de cromossomos
			chromosomesMutated.put(newCh, BigDecimal.ZERO);
		}
		
		population.setChromosomes(chromosomesMutated);
		return population;
	}
	
	public Population eliteMutated(Population population, HashMap<Chromosome, BigDecimal> elite, String id){
		//This method replace each gene with a random script with a probability of 10%
		HashMap<Chromosome, BigDecimal> eliteMutated = new HashMap<>();
		
		// Itera sobre todos os scripts da elite
		for(Chromosome chromosome : elite.keySet()){
			//System.out.println("before mutating "+c.getGenes());
			Chromosome newCh=new Chromosome();
			newCh.setGenes((ArrayList<Integer>) chromosome.getGenes().clone());
			
			// Itera sobre genes do cromossomo clone atual da elite
			for(int i=0; i < newCh.getGenes().size(); i++){
				double mutatePercent = ConfigurationsGA.MUTATION_RATE;
				boolean m = rand.nextFloat() <= mutatePercent;
				
				if(ConfigurationsGA.evolvingScript){ // evolvingScript = true
					m=true;
				}

				if(m){
					//newCh.getGenes().set(i, rand.nextInt(scrTable.getCurrentSizeTable()));
					
					//The next line is added in order to keep mutation of rules
					// A mutationScript faz a muta��o com manipula��o de strings, adiciona o novo cromossomo (script) na scrTable e retorna o ID deste novo cromossomo
					newCh.getGenes().set(i, mutationScript(population, newCh.getGenes().get(i), id ));
				}
			}
			
			// Se a elite já possui o novo cromossomo que sofreu ou não mutação
			if(elite.containsKey(newCh) ){
				int scriptToMutate = rand.nextInt(newCh.getGenes().size());
				//
				newCh.getGenes().set(scriptToMutate, mutationScriptMandatory( newCh.getGenes().get(scriptToMutate), id));
			}
			eliteMutated.put(newCh, BigDecimal.ZERO);
			//System.out.println("after mutating "+newCh.getGenes());
		}
		//System.out.println("sizepop before"+p.getChromosomes().size());
		population.getChromosomes().putAll(eliteMutated);
		//System.out.println("sizepop after"+p.getChromosomes().size());
		return population;
		
	}
	
	
	public Population invaders(Population population, String id) {
		HashMap<Chromosome, BigDecimal> newChromosomes = population.getChromosomes();
		Chromosome tChom;
		int numberEliteMutated, size_population, size_elite;
		
		if(id == "1") {
			numberEliteMutated = ConfigurationsGA.SIZE_ELITE;
			size_population = ConfigurationsGA.SIZE_POPULATION;
			size_elite = ConfigurationsGA.SIZE_ELITE;
		} else {
			numberEliteMutated = ConfigurationsGA.SIZE_ELITE_2;
			size_population = ConfigurationsGA.SIZE_POPULATION_2;
			size_elite = ConfigurationsGA.SIZE_ELITE_2;
		}
		
		// tamanho da nova população < tamanho padrão da população - tamanho padrão da elite - quantidade de pertencentes à elite que sofreram mutação
		while (newChromosomes.size() < size_population - size_elite - numberEliteMutated) {
			//gerar o novo cromossomo com base no tamanho
			tChom = new Chromosome();
			int sizeCh = rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME) + 1;
			
			// Adiciona genes aleat�rios ao cromossomo at� preencher o tamanho padr�o
			for (int j = 0; j < sizeCh; j++) {
				tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			
			newChromosomes.put(tChom, BigDecimal.ZERO);
		}
		
		Population pop = new Population(newChromosomes);
		return pop;
	}
	
	//This method will return the new id script for mutate the porfolio o fscripts ????
	// Popula��o (passada como par�metro na mutation); genidScript (clone do cromossomo atual da itera��o na mutation)
	// A mutationScript faz a muta��o com manipula��o de strings, adiciona o novo cromossomo (script) na scrTable e retorna o ID deste novo cromossomo
	public int mutationScript(Population population, int genidScript, String id){
		// Lista as fun��es b�sicas e condicionais
		functions=new FunctionsforGrammar();
		List<FunctionsforGrammar> basicFunctions=functions.getBasicFunctionsForGrammar();
		List<FunctionsforGrammar> conditionalFunctions=functions.getConditionalsForGrammar();
		
		boolean same;
		if(id == "1") same = false;
		else same = true;
		
		String cromScript = cromosomeById(genidScript);
		String cromScriptOriginal = cromosomeById(genidScript);
		
		// Cria um script cromossomo auxiliar e tira os fors
		String cromScriptAux = cromScript;
		cromScriptAux = cromScriptAux.replace("(for(u)", "");
		cromScriptAux = cromScriptAux.replace("for(u)", "");
		
		// Separa o script cromossomo auxiliar em partes
		//cromScriptAux=cromScriptAux.replaceAll("[^0-9!]", " ");
	    String[] parts = cromScriptAux.trim().split("\\s+");
	    
	    String[] news = new String[parts.length];
	       
	    // Itera sobre todas as partes e faz tratamento de strings
	    for(int i = 0; i < parts.length; i++) {
	    	// Retira ifs e parênteses do começo
		    parts[i] = removeFromBeggining(parts[i]);
		    // Retira parênteses do final
		    parts[i] = removeFromLast(parts[i]);
	    }
		   
	    /*
	    for(int i=0;i<parts.length;i++)
	    {
	    	System.out.println(parts[i]);
	    }
	    */
	    
	    // Retorna novo conjunto de scripts após o processo de mutação
	    if(id != "1")
	    	news = chossingFromBag(news, parts, basicFunctions, conditionalFunctions, same);
	    else
	    	news = chossingFromBagLasi(news, parts, basicFunctions, conditionalFunctions, same);
	    
	    // Itera sobre todo o vetor de partes, sorteia a chance de mutação e substitui o novo cromossomo sobre o antigo que sofre mutação
	    for(int i=0; i <parts.length; i++){
	    	double mutatePercent = ConfigurationsGA.MUTATION_RATE_RULE;
	    	boolean m = rand.nextFloat() <= mutatePercent;

	    	if(m) {
	    		//System.out.println("parts[i] = " + parts[i]);
	    		//System.out.println("news[i] = " + news[i]);
	    		cromScript = replaceFromCompleteGrammar(parts[i], news[i], cromScript );
	    	}
	    }
	    
	    // Caso o novo cromossomo gerado já tenha um equivalente na scrTable, o ID usado será o mesmo do já existente
	    cromScript = removingTrashFromGrammar(cromScript);
		if(scrTable.getScriptTable().containsKey(cromScript)){
			return scrTable.getScriptTable().get(cromScript).intValue();			
		} else {
			// Caso o novo cromossomo gerado não tenha equivalente na scrTable, ele é adicionado no final, com ID = tamanho atual da tabela de scripts
			//System.out.println("beforeMutateScript "+cromScriptOriginal);
			//System.out.println("afterMutateScript "+cromScript);
			int newId = scrTable.getScriptTable().size();
			scrTable.getScriptTable().put(cromScript, BigDecimal.valueOf(newId));
			scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());
			addLineFile(newId+" "+cromScript, scrTable.getID());
			return newId;
		}
		
	}
	
	// genidScript � o script que sofreu muta��o na elite
	// A mutationScriptMandatory faz a muta��o com manipula��o de strings, adiciona o novo cromossomo (script) na scrTable e retorna o ID deste novo cromossomo (100%)
	public int mutationScriptMandatory(int genidScript, String id){
		// Lista as fun��es b�sicas e condicionais
		functions = new FunctionsforGrammar();
		List<FunctionsforGrammar> basicFunctions=functions.getBasicFunctionsForGrammar();
		List<FunctionsforGrammar> conditionalFunctions=functions.getConditionalsForGrammar();
		boolean same;
		
		if(id == "1") same = false;
		else same = true;

		String cromScript=cromosomeById(genidScript);
		String cromScriptOriginal=cromosomeById(genidScript);
		
		// Cria um script cromossomo auxiliar e tira os fors
		String cromScriptAux=cromScript;
		cromScriptAux=cromScriptAux.replace("(for(u)", "");
		cromScriptAux=cromScriptAux.replace("for(u)", "");
		
		// Separa o script cromossomo auxiliar em partes
		//cromScriptAux=cromScriptAux.replaceAll("[^0-9!]", " ");
	    String[] parts = cromScriptAux.trim().split("\\s+");
	    String[] news = new String[parts.length];
	       
	    // Itera sobre todas as partes e faz tratamento de strings
	    for(int i=0;i<parts.length;i++){
	    	// Retira ifs e par�nteses do come�o
		    parts[i]=removeFromBeggining(parts[i]);
		    // Retira par�nteses do final
		    parts[i]=removeFromLast(parts[i]);
	    }
		   
//	    for(int i=0;i<parts.length;i++)
//	    {
//	    	System.out.println(parts[i]);
//	    }
	    
	    // Retorna novo conjunto de scripts após o processo de mutação
	    if(id != "1")
	    	news = chossingFromBag(news, parts, basicFunctions, conditionalFunctions, same);
	    else
	    	news = chossingFromBagLasi(news, parts, basicFunctions, conditionalFunctions, same);

	    // Substitui a parte aleat�ria que vai sofrer a mutação para o novo cromossomo
	    int partToMutate = rand.nextInt(parts.length);
	    cromScript = replaceFromCompleteGrammar(parts[partToMutate], news[partToMutate], cromScript );
	    
	    // Caso o novo cromossomo gerado já tenha um equivalente na scrTable, o ID usado será o mesmo do já existente
	    cromScript = removingTrashFromGrammar(cromScript);
		if(scrTable.getScriptTable().containsKey(cromScript)){
			return scrTable.getScriptTable().get(cromScript).intValue();			
		} else {
			//System.out.println("beforeMutateScript "+cromScriptOriginal);
			//System.out.println("afterMutateScript "+cromScript);
			int newId=scrTable.getScriptTable().size();
			scrTable.getScriptTable().put(cromScript, BigDecimal.valueOf(newId));
			scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());
			addLineFile(newId+" "+cromScript, scrTable.getID());
			return newId;
		}
		
	}
	
	public static String removeFromBeggining(String s){
		String cloneS = s;
		  
		try {
			// Retira abre par�nteses (
			while (cloneS.charAt(0)=='(' ){
				cloneS=cloneS.replaceFirst("\\(", "");
			}
		
			// Retira ifs e par�nteses
			if(cloneS.startsWith("if")){
				cloneS=cloneS.replaceFirst("if", "");
				if(cloneS.charAt(0)=='('){
					cloneS=cloneS.replaceFirst("\\(", "");
				}
			}		

			return cloneS;
		} catch(Exception e) {
			System.out.println(e+" String "+cloneS);
		}
		
		return cloneS;
	}
	
	public static String removeFromLast(String s){
		String cloneS = s;
		
		// Retira fechamento de par�nteses )
		while (cloneS.endsWith("))")) {
			cloneS=cloneS.replaceFirst("\\)", "");
		}
		return cloneS;
	}
	
	public static String removingTrashFromGrammar(String originalGrammar){
		originalGrammar=originalGrammar.replace("NEW", "");		
		return originalGrammar;
	}
	
	public static String replaceFromCompleteGrammar(String oldFunction, String newFunction, String originalGrammar){
		originalGrammar = originalGrammar.replace(oldFunction, newFunction+"NEW");
		return originalGrammar;
	}
	
	// Vetor de Strings do mesmo tamanho do parts, Vetor de Strings com cromossomo cortado e tratado, conjuntos de fun��es b�sicas e condicionais
	public static String[]  chossingFromBag(String[] candidates, String[] originals, List<FunctionsforGrammar>basicFunctions, List<FunctionsforGrammar>conditionalFunctions, boolean same){
		// candidates = news
		// originais = parts
		ScriptsTable objScriptTable = new ScriptsTable("", "objScriptTable");
		boolean m;
		
		boolean found=false;
		for (int i = 0; i < originals.length; i++){
			found = false;
			
			// Itera sobre todas as funções do conjunto de funções básicas
			for (FunctionsforGrammar function:basicFunctions){
				
				if(originals[i].startsWith(function.getNameFunction())){
					//change with other basicFunction
					// Se o comando original estava dentro de um for
					if(originals[i].contains(",u,") || originals[i].contains(",u)") || originals[i].contains("(u,")) {

						if(same)
							m = false;
						else
							m = rand.nextFloat() <= 0.5;
						
						if(m){
							// Retorna uma nova função básica totalmente aleatória, respeitando apenas a presença ou não do for
							candidates[i] = objScriptTable.returnBasicFunctionClean(true);
						} else {
							// Retorna uma nova função básica de acordo com a funçãoo antiga, trocando os parâmetros
							candidates[i]=objScriptTable.returnBasicFunctionCleanSame(true, originals[i]);
						}
					// Se o comando original não estava dentro de um for
					} else {
						
						if(same)
							m = false;
						else
							m = rand.nextFloat() <= 0.5;
						
						if(m){
							candidates[i]=objScriptTable.returnBasicFunctionClean(false);
						} else {
							candidates[i]=objScriptTable.returnBasicFunctionCleanSame(false,originals[i]);
						}
					}
					
					found = true;
					break;
				}
			}
			
			if(found == false){
				
				for (FunctionsforGrammar function:conditionalFunctions){
					if(originals[i].startsWith(function.getNameFunction())){
						//change with other basicFunction
						// Se o comando original estava dentro de um for
						if(originals[i].contains(",u,") || originals[i].contains(",u)") || originals[i].contains("(u,")){
							// For�ar este m para o false pro TCC, usar a fun��o que retorna um script com fun��o equivalente ao original da interface
							if(same)
								m = false;
							else
								m = rand.nextFloat() <= 0.5;
							
							if(m){
								// Retorna uma nova fun��o condicional totalmente aleat�ria, respeitando apenas a presen�a ou n�o do for u
								candidates[i]=objScriptTable.returnConditionalClean(true);
							} else {
								// Retorna uma nova fun��o b�sica de acordo com a fun��o antiga, trocando os par�metros
								candidates[i]=objScriptTable.returnConditionalCleanSame(true,originals[i]);
							}
						// Se o comando original n�o estava dentro de um for
						} else {
							
							if(same)
								m = false;
							else
								m = rand.nextFloat() <= 0.5;
							
							if(m) {
								candidates[i]=objScriptTable.returnConditionalClean(false);
							} else {
								candidates[i]=objScriptTable.returnConditionalCleanSame(false,originals[i]);
							}
						}
						break;
					}
				}
				
			}
		}
		
		return candidates;
	}
	
	public static String[]  chossingFromBagLasi(String[] candidates, String[] originals, List<FunctionsforGrammar>basicFunctions, List<FunctionsforGrammar>conditionalFunctions, boolean same){
		// candidates = news
		// originais = parts
		ScriptsTable objScriptTable = new ScriptsTable("", "objScriptTableLasi");
		boolean found=false;
		
		for (int i = 0; i < originals.length; i++){
			found = false;
			
			// Itera sobre todas as funções do conjunto de funções básicas
			for (FunctionsforGrammar function:basicFunctions){
				
				if(originals[i].startsWith(function.getNameFunction())){
					// Faz troca com outra função básica
					
					// Se o comando original estava dentro de um for
					if(originals[i].contains(",u,") || originals[i].contains(",u)") || originals[i].contains("(u,")) {
						// Retorna uma nova função básica totalmente aleatória, respeitando apenas a presça ou não do for(u)
						candidates[i] = objScriptTable.returnBasicFunctionCleanLasi(true);
						//System.out.println("Função báscia sorteada com u: " + candidates[i]);
						
					// Se o comando original não estava dentro de um for
					} else {
						candidates[i] = objScriptTable.returnBasicFunctionCleanLasi(false);
						//System.out.println("Função básica sorteada sem u: " + candidates[i]);
					}
					//System.out.println("Função básica sorteada: " + candidates[i]);
					
					found = true;
					break;
				}
			}
			
			if(found == false){
				
				for (FunctionsforGrammar function:conditionalFunctions){
					if(originals[i].startsWith(function.getNameFunction())){
						// Faz troca com outra função condicional
						
						// Se o comando original estava dentro de um for
						if(originals[i].contains(",u,") || originals[i].contains(",u)") || originals[i].contains("(u,")){
							// Retorna uma nova função condicional totalmente aleatória, respeitando apenas a presça ou não do for(u)
							candidates[i] = objScriptTable.returnConditionalCleanLasi(true);
							//System.out.println("Função condicional sorteada com u: " + candidates[i]);

						// Se o comando original não estava dentro de um for
						} else {
							candidates[i] = objScriptTable.returnConditionalCleanLasi(false);
							//System.out.println("Função condicional sorteada sem u: " + candidates[i]);
						}
						//System.out.println("Função condicional sorteada: " + candidates[i]);
						
						break;
					}
				}
			}
		}
		
		return candidates;
	}
	
	//This method will be expensive if the hashmap its too big
	public String cromosomeById(int genidScript)
	{
        for (Entry<String, BigDecimal> entry : scrTable.getScriptTable().entrySet()) {
            if (entry.getValue().equals(BigDecimal.valueOf(genidScript))) {
                return entry.getKey();
            }
        }
        return null;
	}
	
	public Population IncreasePopulation(Population pop){

		HashMap<Chromosome, BigDecimal> chromosomesMutated = new HashMap<>();
		for(Chromosome c : pop.getChromosomes().keySet()){

			Chromosome newCh=new Chromosome();
			newCh.setGenes((ArrayList<Integer>) c.getGenes().clone());
			Chromosome origCh=new Chromosome();
			origCh.setGenes((ArrayList<Integer>) c.getGenes().clone());

			double IncreasePercent = ConfigurationsGA.INCREASING_RATE;
			boolean m = rand.nextFloat() <= IncreasePercent;

			if(m)
			{
				//newCh.getGenes().set(i, rand.nextInt(ConfigurationsGA.QTD_SCRIPTS));
				newCh.getGenes().add(rand.nextInt(scrTable.getCurrentSizeTable()));
				chromosomesMutated.put(newCh, BigDecimal.ZERO);
			}
			
			chromosomesMutated.put(origCh, BigDecimal.ZERO);
		}
		pop.setChromosomes(chromosomesMutated);
		return pop;
		
	}
	
	public int UCB1(){	
		
		double bestUCB1=Double.NEGATIVE_INFINITY;
		int bestidRule=0;
		for(int i=0;i<ConfigurationsGA.QTD_RULES;i++)
		{
			//weight/reward
			double reward=UCB_Facade.getAverageValueFromRule(i);
			//total of matches
			double ntotalMatches=RunGA.numberCallsUCB11;
			//Number of calls
			double numberCallsRule=RunGA.frequencyIdsRulesForUCB[i];
			
			double UCB1Rule=reward+Math.sqrt((2*Math.log(ntotalMatches))/numberCallsRule);

			if(UCB1Rule>bestUCB1)
			{
				bestUCB1=UCB1Rule;
				bestidRule=i;
			}
		}
		RunGA.frequencyIdsRulesForUCB[bestidRule]=RunGA.frequencyIdsRulesForUCB[bestidRule]+1;
		RunGA.numberCallsUCB11++;
		return bestidRule;
		
	}
	
	public Population DecreasePopulation(Population pop){

		HashMap<Chromosome, BigDecimal> chromosomesMutated = new HashMap<>();
		for(Chromosome c : pop.getChromosomes().keySet()){

			Chromosome newCh=new Chromosome();
			newCh.setGenes((ArrayList<Integer>) c.getGenes().clone());
			Chromosome origCh=new Chromosome();
			origCh.setGenes((ArrayList<Integer>) c.getGenes().clone());

			double decreasePercent = ConfigurationsGA.DECREASING_RATE;
			boolean m = rand.nextFloat() <= decreasePercent;

			if(m && newCh.getGenes().size()>=2)
			{
				//newCh.getGenes().set(i, rand.nextInt(ConfigurationsGA.QTD_SCRIPTS));
				newCh.getGenes().remove(rand.nextInt(newCh.getGenes().size()));
				chromosomesMutated.put(newCh, BigDecimal.ZERO);
			}
			
			chromosomesMutated.put(origCh, BigDecimal.ZERO);
		}
		pop.setChromosomes(chromosomesMutated);
		return pop;
		
	}
	
	public Population RemoveCopies(Population p){ 
		
		//This method replace each gene with a random script with a probability of 10%
		HashMap<Chromosome, BigDecimal> chromosomesMutated = new HashMap<>();
		for(Chromosome c : p.getChromosomes().keySet()){

			Chromosome newCh=new Chromosome();
			newCh.setGenes((ArrayList<Integer>) c.getGenes().clone());
			// The next code block is for removing duplicates in the cromosome.
			//List<String> al = new ArrayList<>();
			// add elements to al, including duplicates
			
			newCh.setGenes(new ArrayList<Integer>(new LinkedHashSet<Integer>(newCh.getGenes())));
//			Set<Integer> hs = new HashSet<>();
//			hs.addAll(newCh.getGenes());
//			newCh.getGenes().clear();
//			newCh.getGenes().addAll(hs);	
			
			//The next method is just for avoiding infinite loops, adding a random element if
			//one with the same key was already added (this can happen because sometimes the resulting
			//element has the same KEY, and produce that the size of the map be always the same) 
			if(chromosomesMutated.containsKey(newCh))
			{
				Chromosome tChom = new Chromosome();
				int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
				for (int j = 0; j < sizeCh; j++) {
					tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
				}
				chromosomesMutated.put(tChom, BigDecimal.ZERO);
			}
			else
			{
				chromosomesMutated.put(newCh, BigDecimal.ZERO);
			}			
			
		}
		p.setChromosomes(chromosomesMutated);
		return p;
		
	}
	
	public void addLineFile(String data, String id) {
	    try{    
	
	        File file = new File(pathTableScripts + "ScriptsTable" + id + ".txt");    
	
	        //if file doesnt exists, then create it    
	        if(!file.exists()){    
	            file.createNewFile();      
	        }    
	
	        //true = append file    
	            FileWriter fileWritter = new FileWriter(file,true);        
	            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	            bufferWritter.write(data);
	            bufferWritter.newLine();
	            bufferWritter.close();
	            fileWritter.close();  
	
	    }catch(Exception e){    
	        e.printStackTrace();    
	    } 
	}
	
	
	public String recoverStringFromArray(String [] parts)
	{
		String newGrammar="";
		for(String part:parts)
		{
			newGrammar=newGrammar+" "+part;
		}
		
		return newGrammar;
	}

}
