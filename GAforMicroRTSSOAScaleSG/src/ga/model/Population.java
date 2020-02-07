package ga.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;

public class Population {
	
	static Random rand = new Random();
	/**
	 * A população será representada como um Map visando podermos armazenar como Value 
	 * o valor dado pela avaliação a cada cromossomo da população e como Key o Cromossomo.
	 */
	private HashMap<Chromosome, BigDecimal> Chromosomes ;

	
	
	public Population(){
		this.Chromosomes = new HashMap<>();
	}
	

	public Population(HashMap<Chromosome, BigDecimal> chromosomes) {
		super();
		Chromosomes = chromosomes;
	}



	public HashMap<Chromosome, BigDecimal> getChromosomes() {
		return Chromosomes;
	}

	public void setChromosomes(HashMap<Chromosome, BigDecimal> chromosomes) {
		Chromosomes = chromosomes;
	}
	
	public void addChromosome(Chromosome chromosome){
		this.Chromosomes.put(chromosome, BigDecimal.ZERO);
	}	
	
//	public void print(){
//		System.out.println("-- Population --");
//		for(Chromosome c : Chromosomes.keySet()){
//			c.print();
//		}
//		System.out.println("-- Population --");
//	}
	
	public void printWithValue(PrintWriter f0){
		System.out.println("-- Population --");
		f0.println("-- Population --");
		for(Chromosome c : Chromosomes.keySet()){
			c.print(f0);
			System.out.println("Value = "+ this.Chromosomes.get(c));
			f0.println("Value = "+ this.Chromosomes.get(c));
		}
		System.out.println("-- Population --");
		f0.println("-- Population --");
	}
	
	/**
	 * Função que zera os valores das avaliações dos Chromossomos.
	 */
	public void clearValueChromosomes(){
		for(Chromosome chromo : this.Chromosomes.keySet()){
			this.Chromosomes.put(chromo, BigDecimal.ZERO);
		}
	}
	
	//static methods
	
	/**
	 * Cria uma população inicial gerada randomicamente.
	 * @param size Tamanho limite da população
	 * @return uma população com Key = Chromosome e Values = 0
	 */
	public static Population getInitialPopulation(int size, ScriptsTable scrTable){
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
		
		Chromosome tChom;
		for (int i = 0; i < size; i++) {
			//gerar o novo cromossomo com base no tamanho
			tChom = new Chromosome();
			int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
			
			///// individuo 0
			// sortear de 1 a size
			for (int j = 0; j < sizeCh; j++) {
				tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			newChromosomes.put(tChom, BigDecimal.ZERO);
		}
		Population pop = new Population(newChromosomes);
		return pop;
	}
	
	public static Population getInitialPopulationCurriculum(int size, ScriptsTable scrTable, String pathInitialPopulation){
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathInitialPopulation + "/population.txt"))) {
            String line;
            Chromosome tChom;
            while ((line = br.readLine()) != null) {
            	if(line.startsWith("Value"))
            	{
            		continue;
            	}
                String[] strArray = line.split(" ");
                int[] intArray = new int[strArray.length-1];
                for (int i = 0; i < strArray.length-1; i++) {
                    intArray[i] = Integer.parseInt(strArray[i+1]);
                }
                //int[] idsScripts = Arrays.copyOfRange(intArray, 0, intArray.length);

                tChom = new Chromosome();
                for (int i : intArray) {
                	tChom.addGene(i);
                }
                
                newChromosomes.put(tChom, BigDecimal.ZERO);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Population pop = new Population(newChromosomes);
        return pop;
	}
	
	
	/**
	 * Cria uma população inicial com os genes dos cromossomos iguais ao passado por parametros
	 * @param gene Integer que será utilizado como gene dos cromossomos
	 * @return uma população com Key = Chromosome e Values = 0
	 */
	public static Population getInitialPopulation(Integer gene){
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
		
		Chromosome tChom;
		for (int i = 0; i < ConfigurationsGA.SIZE_POPULATION; i++) {
			//gerar o novo cromossomo com base no tamanho
			tChom = new Chromosome();
			for (int j = 0; j < ConfigurationsGA.SIZE_CHROMOSOME; j++) {
				tChom.addGene(gene);
			}
			newChromosomes.put(tChom, BigDecimal.ZERO);
		}
		
		Population pop = new Population(newChromosomes);
		return pop;
	}
	
	public boolean isPopulationValueZero(){
		
		for (BigDecimal value : Chromosomes.values()) {
			if(value.compareTo(BigDecimal.ZERO) == 1 ){
				return false;
			}
		}
		return true;
	}
}
