package ga.util.Evaluation;

import java.util.ArrayList;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.model.Population;

public interface RatePopulations {
	/* 
	*  A função de avaliação irá controlar as chamadas no cluster, ou fazer os cálculos das simulações e entregar
	*  uma população devidamente avaliada.
	*  Lembrar que na população os cromossomos estão em um MAP onde 
	*  KEYS   = Cromossomo
	*  VALUES = Valor da avaliaÃ§Ã£o 
	*/
	public ArrayList<Population> evalPopulation(ArrayList<Population> populations, int generation, ScriptsTable scriptsTable1, ScriptsTable scriptsTable2, String id1, String id2);
	public void finishProcess();
}