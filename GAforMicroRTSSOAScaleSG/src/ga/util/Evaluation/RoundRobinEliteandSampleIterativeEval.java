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
import util.LeitorLogID;

public class RoundRobinEliteandSampleIterativeEval implements RatePopulations {
	// CONSTANTES
	private static final int TOTAL_PARTIDAS_ROUND = 1;
	private static final int BATCH_SIZE = 1;

	//private static final String pathSOA = "/home/rubens/cluster/TesteNewGASG/configSOA/";
	private static final String pathSOA = System.getProperty("user.dir").concat("/configSOA/");

	//private static final String pathCentral = "/home/rubens/cluster/TesteNewGASG/centralSOA";
	private static final String pathCentral = System.getProperty("user.dir").concat("/centralSOA");
	
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
	public Population evalPopulation(Population population1, Population population2, int generation, ScriptsTable scriptsTable1, ScriptsTable scriptsTable2, String id1, String id2) {
		//recordMarkNewGeneration();
		// Constroi tabela com scripts e seus IDs, lê o arquivo com ID da scriptsTable
		buildScriptsTable(id1);
		
		// A geração atual é atualizada em cada iteração do RunGA
		this.atualGeneration = generation;
		
		SOA_Folders.clear();
		// Zera os valores das avaliações dos Chromossomos.
		population1.clearValueChromosomes();
		population2.clearValueChromosomes();

		// executa os confrontos (avalia a primeira)
		runBattles(population1, population2, id1, id2);
		//runBattles(population2, population1, id2, id1);

		// Só permite continuar a execução após terminar os JOBS.
		//controllExecute();
		iterativeControll(population1, id1);
		//iterativeControll(population2, id2);

		// remove qualquer aquivo que não possua um vencedor
		removeLogsEmpty(id1);
		//removeLogsEmpty(id2);

		// ler resultados
		ArrayList<EvalResult> resultados1 = lerResultados(id1);
		//ArrayList<EvalResult> resultados2 = lerResultados(id2);
		
		// atualizar valores das populacoes
		if(resultados1.size() > 0){
			updatePopulationValue(resultados1, population1, id1);
		}
		//if(resultados2.size() > 0){
		//	updatePopulationValue(resultados2, population2, id2);
		//}

		return population1;
	}
	
    public HashMap<BigDecimal, String> buildScriptsTable(String scrTableID) {
    	HashMap<BigDecimal, String> scriptsTable = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable" + scrTableID + ".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
            	// script
                String code = line.substring(line.indexOf(" "), line.length());
                String[] strArray = line.split(" ");
                // id do script
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
        
        if(scrTableID == "1") {
        	scriptsTable1.clear();
        	scriptsTable1 = scriptsTable;
        } else {
        	scriptsTable2.clear();
        	scriptsTable2 = scriptsTable;
        }

        return scriptsTable;
    }

	private void removeLogsEmpty(String id) {
		LeitorLogID log = new LeitorLogID(id);
		log.removeNoResults();
	}

	public Population updatePopulationValue(ArrayList<EvalResult> results, Population pop, String id) {
		//ArrayList<EvalResult> resultsNoDraw = removeDraw(results);
		ArrayList<EvalResult> resultsNoDraw = results;
		/*
		 * System.out.println("AvaliaÃ§Ãµes sem Draw"); for (EvalResult evalResult
		 * : resultsNoDraw) { evalResult.print(); }
		 */
		
		for (EvalResult evalResult : resultsNoDraw) {
			updateChomoPopulation(evalResult, pop, id);
		}

		return pop;
	}

	private void updateChomoPopulation(EvalResult evalResult, Population pop, String id) {
		
		if (evalResult.getEvaluation() == 0) {
            //IAWinner = evalResult.getIA1();
            updateChromo(pop, evalResult.getIA1(), BigDecimal.ONE);
        } else if (evalResult.getEvaluation() == 1){
            updateChromo(pop, evalResult.getIA2(), BigDecimal.ONE);
        }else{
            updateChromo(pop, evalResult.getIA1(), new BigDecimal(0.5));
            updateChromo(pop, evalResult.getIA2(), new BigDecimal(0.5));
        }
		
		if(counterLinesLogsGrammar<maxLinesLogsGrammar)
		{
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
	        	portfolioGrammar=portfolioGrammar+scriptsTable1.get(BigDecimal.valueOf(idSc))+";";
	        }
        } else {
        	for (Integer idSc : iScripts) {
	        	portfolioGrammar=portfolioGrammar+scriptsTable2.get(BigDecimal.valueOf(idSc))+";";
	        }
        }

        return portfolioGrammar;
    }

    private void updateChromo(Population pop, String IAWinner, BigDecimal value) {
        // buscar na populacao a IA compatavel.
                Chromosome chrUpdate = null;
                for (Chromosome ch : pop.getChromosomes().keySet()) {
                    if (convertBasicTuple(ch).equals(IAWinner)) {
                        chrUpdate = ch;
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

	public ArrayList<EvalResult> lerResultados(String id) {
		LeitorLogID leitor = new LeitorLogID(id);
		ArrayList<EvalResult> resultados = leitor.processar();
		/*
		 * for (EvalResult evalResult : resultados) { evalResult.print(); }
		 */
		return resultados;
	}

	
	private Population iterativeControll(Population population, String id) {
		// look for clients and share the data.
		while (hasSOACentralFile()) {
			// update the quantity of SOA Clients.
			updateSOAClients();
			// update the file to process
			updateFiles();
			// share the files between SOA Clients
			shareFiles();

			// run iterative process
			population = iterativeEvaluation(population, id);
			
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
				population = iterativeEvaluation(population, id);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return population;
	}
	
	
	private Population iterativeEvaluation(Population population, String id) {
		// ler resultados
		ArrayList<EvalResult> resultados = lerResultadosIterative(id);
		
		// atualizar valores das populacoes
		if(resultados.size() > 0 ){
			 population = updatePopulationValue(resultados, population, id);
		}
		
		return population;
	}
	
	public ArrayList<EvalResult> lerResultadosIterative(String id) {
		LeitorLogID leitor = new LeitorLogID(id);
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
		this.SOA_arqs.clear();
		File CentralFolder = new File(pathCentral + "/");
		for (File file : CentralFolder.listFiles()) {
			SOA_arqs.add(file.getAbsolutePath());
		}
	}

	private void updateSOAClients() {
		this.SOA_Folders.clear();
		File configSOAFolder = new File(pathSOA);
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
		File configSOAFolder = new File(pathSOA);
		for (File folder : configSOAFolder.listFiles()) {
			SOA_Folders.add(folder.getAbsolutePath());
		}

	}

	/**
	 * IrÃ¡ verificar a pasta central nÃ£o tem mais arquivos.
	 * 
	 * @return
	 */
	private boolean hasSOACentralFile() {
		File centralF = new File(pathCentral);
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
		String ordemTablesFirst = id1 + "_" + id2;
		String ordemTablesSecond = id2 + "_" + id1;
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
						String strConfig = pathCentral + "/" + ordemTablesFirst + "#" + convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2)
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
							gravarArq.println(ordemTablesFirst + "#" + convertBasicTuple(cIA1) + "#(" + convertBasicTuple(cIA2) + ")#" + i + "#"
									+ atualGeneration);

							gravarArq.flush();
							gravarArq.close();
							arq.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// second position
						strConfig = pathCentral + "/" + ordemTablesSecond + "#" + "(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#"
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

							gravarArq.println(ordemTablesSecond + "#" + "(" + convertBasicTuple(cIA2) + ")#" + convertBasicTuple(cIA1) + "#" + i
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

	private void defineChromosomeSample(Population population) {
		this.ChromosomeSample.clear();
		
		PreSelection ps = new PreSelection(population);	
		HashMap<Chromosome, BigDecimal> elite = (HashMap<Chromosome, BigDecimal>)ps.sortByValue(population.getChromosomes());
		
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