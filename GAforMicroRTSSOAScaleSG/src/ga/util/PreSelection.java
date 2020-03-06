package ga.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;

public class PreSelection {

	Population population;
	
	public PreSelection(Population p){
		this.population=p;
	}
	
	public List<Map.Entry<Chromosome, BigDecimal>> Tournament(String id){
		//we want to select some parents (list parents) from all the population (listCandidates)
		// Controle de pais adicionados para seleção, lista de pais, lista de candidatos com populção passada no construtor da classe (populacaoInicial)
		int parentsAdded=0, size_parentsForCrossover, k_tournment;
		List<Map.Entry<Chromosome, BigDecimal>> listParents= new ArrayList();
		List<Map.Entry<Chromosome, BigDecimal>> listCandidates = new ArrayList<Map.Entry<Chromosome, BigDecimal>>(population.getChromosomes().entrySet());
		
		if(id == "1") {
			size_parentsForCrossover = ConfigurationsGA.SIZE_PARENTSFORCROSSOVER;
			k_tournment = ConfigurationsGA.K_TOURNMENT;
		} else {
			size_parentsForCrossover = ConfigurationsGA.SIZE_PARENTSFORCROSSOVER_2;
			k_tournment = ConfigurationsGA.K_TOURNMENT_2;
		}
		
		while(parentsAdded < size_parentsForCrossover) {
			//here we randomize the list in order to select k random elements for the tournament
			// Embaralha itens da lista de candidatos
			Collections.shuffle(listCandidates);
			
			// Melhor indivíduo do torneio
			Map.Entry<Chromosome, BigDecimal> best = null;

			// Compara K_TOURNMENT aleatórios (primeiros da lista embaralhada) e escolhe o que obteve melhor resultado nas avalia��es anteriores
			for(int i=0; i < k_tournment; i++) {
				if( best==null || listCandidates.get(i).getValue().intValue() > best.getValue().intValue()){
					best = listCandidates.get(i);
				}
			}
			//here we add the champion as a parent
			// O melhor indivíduo é colocado na lista de pais que servirá de base para a próxima geração
			listParents.add(best);
			parentsAdded++;
		}
		
		return listParents;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List list = new LinkedList(map.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
			if(sortedHashMap.size()==ConfigurationsGA.SIZE_ELITE)
			{
				break;
			}
		} 
		return sortedHashMap;

	}
	
	public  <K, V extends Comparable<? super V>> Map<K, V> sortByValueEliteFItnessFunction(Map<K, V> map) {
		List list = new LinkedList(map.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
			if(sortedHashMap.size()==ConfigurationsGA.QTD_ENEMIES_SAMPLE_ELITE)
			{
				break;
			}
		} 
		return sortedHashMap;

	}

}
