package ga.util.Evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import ai.core.AI;

import java.util.Random;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import ga.util.PreSelection;
import model.EvalResult;
import rts.units.UnitTypeTable;
import util.LeitorLog;

public class RoundRobinEliteandSampleIterativeEval implements RatePopulations {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 1;

	//private static final String pathSOA = "/home/rubens/cluster/TesteNewGASG/configSOA/";
	private static final String configSOA = System.getProperty("user.dir").concat("/configSOA/");

	//private static final String pathCentral = "/home/rubens/cluster/TesteNewGASG/centralSOA";
	private static final String centralSOA = System.getProperty("user.dir").concat("/centralSOA/");
	
	private static final String pathLogsGrammars = System.getProperty("user.dir").concat("/LogsGrammars/");
	
	private static final String pathTableScripts = System.getProperty("user.dir").concat("/Table/");

	// Classes de informaÃ§Ã£o
	private int atualGeneration = 0;

	// Atributos locais
	ArrayList<String> SOA_Folders = new ArrayList<>();
	ArrayList<String> SOA_arqs = new ArrayList<>();

	ArrayList<Chromosome> ChromosomeSample = new ArrayList<>();
	
	private HashMap<BigDecimal, String> scriptsTable1;
	private HashMap<BigDecimal, String> scriptsTable2;
	int maxLinesLogsGrammar=100000;
	int counterLinesLogsGrammar=0;

	public RoundRobinEliteandSampleIterativeEval() {
		super();
	}

	@Override
	public ArrayList<Population> evalPopulation(ArrayList<Population> populations, int generation, ScriptsTable scriptsTable1, ScriptsTable scriptsTable2, String id1, String id2) { // tirar ids, usar scrTable.getID
		//recordMarkNewGeneration();
		// Constroi tabela com scripts e seus IDs, lê o arquivo com ID da scriptsTable
		buildScriptsTable(id1);
		buildScriptsTable(id2);
		
		// A geração atual é atualizada em cada iteração do RunGA
		this.atualGeneration = generation;
		
		SOA_Folders.clear();
		// Zera os valores das avaliações dos Chromossomos.
		populations.get(0).clearValueChromosomes();
		populations.get(1).clearValueChromosomes();

		// executa os confrontos (avalia a primeira)
		runBattles(populations.get(0), populations.get(1), id1, id2);
		//runBattles(population2, population1, id2, id1);

		// Só permite continuar a execução após terminar os JOBS.
		//controllExecute();
		System.out.println("Aguardando JOBs...");
		iterativeControll(populations, id1, id2);
		System.out.println("Terminaram os JOBs!");

		// remove qualquer aquivo que não possua um vencedor
		removeLogsEmpty();

		// ler resultados
		ArrayList<EvalResult> resultados = lerResultados();
		//ArrayList<EvalResult> resultados2 = lerResultados(id2);
		
		// atualizar valores das populacoes
		if(resultados.size() > 0){
			populations = updatePopulationValue(resultados, populations, id1, id2);
		}
		//if(resultados2.size() > 0){
		//	updatePopulationValue(resultados2, population2, id2);
		//}

		return populations;
	}
	
	public HashMap<BigDecimal, String> buildScriptsTable(String id) {
		HashMap<BigDecimal, String> scriptsTable = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "ScriptsTable" + id + ".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.substring(line.indexOf(" "), line.length());
                String[] strArray = line.split(" ");
                int idScript = Integer.decode(strArray[0]);
                scriptsTable.put(BigDecimal.valueOf(idScript), code);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if(id == "1")
        	scriptsTable1 = scriptsTable;
        else
        	scriptsTable2 = scriptsTable;

        return scriptsTable;
    }

	private void removeLogsEmpty() {
		LeitorLog log = new LeitorLog();
		log.removeNoResults();
	}

	//											resultados vindos da leitura, população 1 e 2, "1", "2"
	public ArrayList<Population> updatePopulationValue(ArrayList<EvalResult> results, ArrayList<Population> pop, String id1, String id2) {
		//ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		ArrayList<EvalResult> resultsNoDraw = results;
		/*
		 * System.out.println("AvaliaÃ§Ãµes sem Draw"); for (EvalResult evalResult
		 * : resultsNoDraw) { evalResult.print(); }
		 */
		
		// Itera sobre todos os resultados da results
		for (EvalResult evalResult : resultsNoDraw) {
			// evalResult (único), população 1, id da ScriptTable 1
			updateChomoPopulation(evalResult, pop.get(0), id1);
		}
		
		for (EvalResult evalResult : resultsNoDraw) {
			updateChomoPopulation(evalResult, pop.get(1), id2);
		}

		return pop;
	}

	private void updateChomoPopulation(EvalResult evalResult, Population pop, String id) {
		// Na população 1, o vencedor não possui parênteses
		String AIWinnerTableID = null;

		// Identifica as IAs para usa a tabela correta
		if(evalResult.getEvaluation() == 0 && evalResult.getIA1().contains("(")) {
			AIWinnerTableID = "2";
		} else if(evalResult.getEvaluation() == 0 && evalResult.getIA2().contains("(")) {
			AIWinnerTableID = "1";
		} else if(evalResult.getEvaluation() == 1 && evalResult.getIA2().contains("(")) {
			AIWinnerTableID = "2";
		} else if(evalResult.getEvaluation() == 1 && evalResult.getIA1().contains("(")) {
			AIWinnerTableID = "1";
		} else
			AIWinnerTableID = "0";
		
		if (evalResult.getEvaluation() == 0) { 		// 0 indica que o vencedor é o evalResult.getIA1()
			if(AIWinnerTableID == id) {
				updateChromo(pop, evalResult.getIA1(), BigDecimal.ONE);
				System.out.println("O vencedor é da população " + id);
				System.out.println("---------------------------");
			}
        } else if (evalResult.getEvaluation() == 1) {	// 1 indica que o vencedor é o evalResult.getIA2()
            if(AIWinnerTableID == id) {
				updateChromo(pop, evalResult.getIA2(), BigDecimal.ONE);
				System.out.println("O vencedor é da população " + id);
				System.out.println("---------------------------");
			}
        } else if(evalResult.getEvaluation() == -1) {				// -1 indica empate (a outra população é atualizada depois)
        	if(id == "2" && evalResult.getIA1().contains("("))
        		updateChromo(pop, evalResult.getIA1(), new BigDecimal(0.5));
        	else if(id == "2" && evalResult.getIA2().contains("("))
        		updateChromo(pop, evalResult.getIA2(), new BigDecimal(0.5));
        	else if(id == "1" && !evalResult.getIA1().contains("("))
        		updateChromo(pop, evalResult.getIA1(), new BigDecimal(0.5));
        	else if(id == "1" && !evalResult.getIA2().contains("("))
        		updateChromo(pop, evalResult.getIA2(), new BigDecimal(0.5));
        	if(id == "1") {
	        	System.out.println("Empate!");
	        	System.out.println("---------------------------");
        	}
        }
		
		if(counterLinesLogsGrammar < maxLinesLogsGrammar){
			String portfolioGrammar0=buildCompleteGrammar(convertBasicTupleToInteger(evalResult.getIA1()), id);
			//System.out.println("portfolio0 "+portfolioGrammar0);
	     
			String portfolioGrammar1=buildCompleteGrammar(convertBasicTupleToInteger(evalResult.getIA2()), id);
			//System.out.println("portfolio1 "+portfolioGrammar1);
	     
			counterLinesLogsGrammar++;
	     
			portfolioGrammar0=portfolioGrammar0.substring(0, portfolioGrammar0.length() - 1);
			portfolioGrammar1=portfolioGrammar1.substring(0, portfolioGrammar1.length() - 1);
	    
			recordGrammars(Integer.toString(evalResult.getEvaluation()), portfolioGrammar0, portfolioGrammar1);
		}
        
    }
	
    private void recordGrammars(String winner, String portfolioGrammar0, String portfolioGrammar1) {
		
    	try(FileWriter fw = new FileWriter(pathLogsGrammars+"LogsGrammars.txt", true);
    		    BufferedWriter bw = new BufferedWriter(fw);
    		    PrintWriter out = new PrintWriter(bw))
    		{
    		    out.println(portfolioGrammar0+"/"+portfolioGrammar1+"="+winner);
    		} catch (IOException e) {
    		    //exception handling left as an exercise for the reader
    		}
		
	}
    
    private void recordMarkNewGeneration() {
		
    	try(FileWriter fw = new FileWriter(pathLogsGrammars+"LogsGrammars.txt", true);
    		    BufferedWriter bw = new BufferedWriter(fw);
    		    PrintWriter out = new PrintWriter(bw))
    		{
    		    out.println("New Generation!");
    		} catch (IOException e) {
    		    //exception handling left as an exercise for the reader
    		}
		
	}
	
    public String buildCompleteGrammar(ArrayList<Integer> iScripts, String id) {
        List<AI> scriptsAI = new ArrayList<>();
        String portfolioGrammar="";

        if(id == "1") {
	        for (Integer idSc : iScripts) {
	        	portfolioGrammar = portfolioGrammar + scriptsTable1.get(BigDecimal.valueOf(idSc)) + ";";
	        }
        } else {
        	for (Integer idSc : iScripts) {
	        	portfolioGrammar = portfolioGrammar + scriptsTable2.get(BigDecimal.valueOf(idSc)) + ";";
	        }
        }

        return portfolioGrammar;
    }

    // população x, script da IA x salvo do array de resultados, valor da pontuação
    private void updateChromo(Population pop, String IAWinner, BigDecimal value) {
    	IAWinner = IAWinner.replace("(", "").replace(")", "");
        // buscar na populacao o script compatível.
    	Chromosome chrUpdate = null; // script que vai ganhar pontos (value)
    	for (Chromosome ch : pop.getChromosomes().keySet()) {
    		//System.out.println("candidato: " + convertBasicTuple(ch) + " vencedor: " + IAWinner);
    		if (convertBasicTuple(ch).equals(IAWinner) ) {
    			chrUpdate = ch;
    			//System.out.println("Encontrou vencedor ou empate! " + convertBasicTuple(chrUpdate));
    		}
    	}
                
    	if (chrUpdate != null) {
    		// atualizar valores.
    		BigDecimal toUpdate = pop.getChromosomes().get(chrUpdate);
    		if (toUpdate != null) {
    			toUpdate = toUpdate.add(value);
    			HashMap<Chromosome, BigDecimal> chrTemp = pop.getChromosomes();
    			chrTemp.put(chrUpdate, toUpdate);
    		}
    	}
    	
	}

	private ArrayList<EvalResult> removeDraw(ArrayList<EvalResult> results) {
		ArrayList<EvalResult> rTemp = new ArrayList<>();

		for (EvalResult evalResult : results) {
			if (evalResult.getEvaluation() != -1) {
				rTemp.add(evalResult);
			}
		}

		return rTemp;
	}

	public ArrayList<EvalResult> lerResultados() {
		LeitorLog leitor = new LeitorLog();
		ArrayList<EvalResult> resultados = leitor.processar();
		/*
		 * for (EvalResult evalResult : resultados) { evalResult.print(); }
		 */
		return resultados;
	}

	
	private ArrayList<Population> iterativeControll(ArrayList<Population> populations, String id1, String id2) {
		// look for clients and share the data.
		// É verdadeiro enquanto ainda há arquivos na centralSOA (arquivos com definição dos scripts e das partidas, ex: (3;)#98;#0#0
		while (hasSOACentralFile()) {
			// update the quantity of SOA Clients.
			// Atualiza pastas presentes na configSOA para a ArrayList SOA_Folders
			updateSOAClients();
			
			// update the file to process
			// Atualiza lista de arquivos da centralSOA para a ArrayList SOA_arqs
			updateFiles();
			
			// share the files between SOA Clients
			// Divide os arquivos de partidas do centralSOA para 
			shareFiles();

			// run iterative process
			populations = iterativeEvaluation(populations, id1, id2);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while (hasSOAArq()) {
			try {
				// run iterative process
				populations = iterativeEvaluation(populations, id1, id2);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return populations;
	}
	
	
	private ArrayList<Population> iterativeEvaluation(ArrayList<Population> populations, String id1, String id2) {
		// ler resultados
		ArrayList<EvalResult> resultados = lerResultadosIterative();
		
		// atualizar valores das populacoes
		if(resultados.size() > 0 ){
			 populations = updatePopulationValue(resultados, populations, id1, id2);		// TATHY VOCE ESTA AQUI
		}
		
		return populations;
	}
	
	public ArrayList<EvalResult> lerResultadosIterative() {
		LeitorLog leitor = new LeitorLog();
		// Monta lista de resultados com IA1 e IA2 com String ID do script, e Evaluation com inteiro do resultado (0, 1 ou -1)
		ArrayList<EvalResult> resultados = leitor.processarIterative();
		/*
		 * for (EvalResult evalResult : resultados) { evalResult.print(); }
		 */
		
		return resultados;
	}
	
	/**
	 * Verifica se os jobs jÃ¡ foram encerrados no cluster.
	 */
	private void controllExecute() {

		// look for clients and share the data.
		while (hasSOACentralFile()) {
			// update the quantity of SOA Clients.
			updateSOAClients();
			// update the file to process
			updateFiles();
			// share the files between SOA Clients
			shareFiles();

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while (hasSOAArq()) {
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void shareFiles() {
		for (String folder : this.SOA_Folders) {
			for (int i = 0; i < BATCH_SIZE; i++) {

				if (SOA_arqs.size() == 0) {
					return;
				}
				
				String nFile = SOA_arqs.get(0);
				File f = new File(nFile);
				try {
					copyFileUsingStream(f, new File(folder + "/" + f.getName()));
					SOA_arqs.remove(nFile);
					f.delete();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	private void updateFiles() {
		// SOA_arqs é uma ArrayList de Strings
		this.SOA_arqs.clear();
		File CentralFolder = new File(centralSOA + "/");
		for (File file : CentralFolder.listFiles()) {
			SOA_arqs.add(file.getAbsolutePath());
		}
	}

	private void updateSOAClients() {
		// SOA_Folders é um ArrayList de String
		this.SOA_Folders.clear();
		File configSOAFolder = new File(configSOA);
		if (configSOAFolder != null) {
			for (File folder : configSOAFolder.listFiles()) {
				if (folder.listFiles().length == 0) {
					SOA_Folders.add(folder.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * irÃ¡ verificar se todas as pastas SOA estÃ£o vazias
	 * 
	 * @return True se estiver vazias
	 */
	private boolean hasSOAArq() {
		updateSOACLientFull();
		for (String soaFolder : this.SOA_Folders) {
			String strConfig = soaFolder;
			File f = new File(strConfig);
			String[] children = f.list();
			if (children.length > 0) {
				return true;
			}

		}

		return false;
	}

	private void updateSOACLientFull() {
		this.SOA_Folders.clear();
		File configSOAFolder = new File(configSOA);
		for (File folder : configSOAFolder.listFiles()) {
			SOA_Folders.add(folder.getAbsolutePath());
		}

	}

	/**
	 * Irá verificar se a pasta central não tem mais arquivos.
	 * 
	 * @return
	 */
	private boolean hasSOACentralFile() {
		File centralF = new File(centralSOA);
		if (centralF.list().length > 0) {
			return true;
		}
		return false;
	}

	/**
	 * MetÃ³do para enviar todas as batalhas ao cluster.
	 * 
	 * @param population
	 *            Que contÃ©m as configuracoes para a IA
	 */
	private void runBattles(Population population1, Population population2, String id1, String id2) {
		int numberSOA = 1;
		// montar a lista de batalhas que irão ocorrer
		
		//defineChromosomeSample(population);
		//defineRandomSet(population);

		for (int i = 0; i < TOTAL_PARTIDAS_ROUND; i++) {

			for (Chromosome cIA1 : population1.getChromosomes().keySet()) {

				//for (Chromosome cIA2 : this.ChromosomeSample) {
				for (Chromosome cIA2 : population2.getChromosomes().keySet()) {

					//if (!cIA1.equals(cIA2)) {
						// System.out.println("IA1 = "+ convertTuple(cIA1)+ "
						// IA2 = "+ convertTuple(cIA2));

						// first position
						// Ordem das Script Tables no começo do nome do arquivo
						String strConfig = centralSOA + "/" + convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2)
								+ ")#" + i + "#" + atualGeneration + ".txt";
						File arqConfig = new File(strConfig);
						if (!arqConfig.exists()) {
							try {
								arqConfig.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						// escreve a configuração de teste
						try {
							FileWriter arq = new FileWriter(arqConfig, false);
							PrintWriter gravarArq = new PrintWriter(arq);
							
							// Ordem das Scripts Tables dentro do arquivo
							gravarArq.println(convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2) + ")#" + i + "#"
									+ atualGeneration);

							gravarArq.flush();
							gravarArq.close();
							arq.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// second position
						strConfig = centralSOA + "/" + "(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#"
								+ i + "#" + atualGeneration + ".txt";
						arqConfig = new File(strConfig);
						if (!arqConfig.exists()) {
							try {
								arqConfig.createNewFile();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							FileWriter arq = new FileWriter(arqConfig, false);
							PrintWriter gravarArq = new PrintWriter(arq);

							gravarArq.println("(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#" + i
									+ "#" + atualGeneration);

							gravarArq.flush();
							gravarArq.close();
							arq.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					//}

				}
			}
		}
	}
	
	private void defineRandomSet(Population population) {
		
		
		int totalPop = population.getChromosomes().size();
		Random rand = new Random();
		HashSet<Chromosome> samples = new HashSet<>();
		ArrayList<Chromosome> temp = new ArrayList<>(population.getChromosomes().keySet());
		
		while (samples.size() < ConfigurationsGA.QTD_ENEMIES_SAMPLE_RANDOM) {
			
			Chromosome cTemp;
			do {
				cTemp = temp.get(rand.nextInt(totalPop));
			}while(ChromosomeSample.contains(cTemp));
			
			samples.add(cTemp);
		}
		
		this.ChromosomeSample.addAll(samples);

	}

	private void defineChromosomeSample(Population population, String id) {
		this.ChromosomeSample.clear();
		
		PreSelection ps = new PreSelection(population);	
		HashMap<Chromosome, BigDecimal> elite = (HashMap<Chromosome, BigDecimal>)ps.sortByValue(population.getChromosomes(), id);
		
		ArrayList<Entry<Chromosome, BigDecimal>> arrayElite = new ArrayList<>();
		arrayElite.addAll(elite.entrySet());
		
		HashSet<Chromosome> eliteH = new HashSet<>();
		for(int i = 0; i < arrayElite.size(); i++) {
			eliteH.add(arrayElite.get(i).getKey());
		}
		
		this.ChromosomeSample.addAll(eliteH);
	}

	private String convertTuple(Chromosome cromo) {
		String tuple = "'";

		for (Integer integer : cromo.getGenes()) {
			tuple += integer + ";";
		}

		return tuple += "'";
	}

	private String convertBasicTuple(Chromosome cromo) {
		String tuple = "";

		for (Integer integer : cromo.getGenes()) {
			tuple += integer + ";";
		}

		return tuple;
	}
	
	private ArrayList<Integer> convertBasicTupleToInteger(String cromo) {
		ArrayList<Integer> gens = new ArrayList<>();;
		
		cromo=cromo.replace("(", "");
		cromo=cromo.replace(")", "");
		String[] arr = cromo.split(";");
		
		
		for (int i=0; i<arr.length;i++) {
			gens.add(Integer.parseInt(arr[i]));
		}

		return gens;
	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	/**
	 * Envia o sinal de exit para todos os SOA clientes
	 */
	@Override
	public void finishProcess() {
		for (String soaFolder : this.SOA_Folders) {
			String strConfig = soaFolder;
			File f = new File(strConfig + "/exit");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}