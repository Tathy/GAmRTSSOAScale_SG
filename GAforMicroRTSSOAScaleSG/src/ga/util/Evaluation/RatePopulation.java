package ga.util.Evaluation;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.model.Population;

public interface RatePopulation {
	/* 
	*  A fun��o de avalia��o ir� controlar as chamadas no cluster, ou fazer os c�lculos das simula��es e entregar
	*  uma popula��o devidamente avaliada.
	*  Lembrar que na popula��o os cromossomos est�o em um MAP onde 
	*  KEYS   = Cromossomo
	*  VALUES = Valor da avaliação 
	*/
	public Population evalPopulation(Population population, int generation, ScriptsTable scriptsTable);
	public void finishProcess();
}
