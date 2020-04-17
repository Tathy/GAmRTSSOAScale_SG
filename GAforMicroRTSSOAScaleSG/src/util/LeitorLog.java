package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import model.EvalResult;

public class LeitorLog {
	
	private String pathStruture;
	
	
	
	public LeitorLog(){
		pathStruture = System.getProperty("user.dir").concat("/logs");
		//pathStruture = "/home/rubens/cluster/TesteNewGASG/logs";
	}

	public ArrayList<EvalResult> processar() {
		ArrayList<String> tempCaminhos = new ArrayList<String>();
		File diretorio = new File(pathStruture);
		buscarParcial(diretorio, ".txt", tempCaminhos);
		
		/*
		System.out.println("Arquivos encontrados:");
		for (String string : tempCaminhos) {
			System.out.println(string);
		}
		*/
		ArrayList<EvalResult> choices = lerArquivos(tempCaminhos); 
		
		//remover arquivos
		removerArquivos(tempCaminhos);
		
		return choices;
		
	}
	
	public ArrayList<EvalResult> processarIterative() {
		ArrayList<String> tempCaminhos = new ArrayList<String>();
		File diretorio = new File(pathStruture);
		// Lista arquivos da pasta logs
		buscarParcial(diretorio, ".txt", tempCaminhos);
		
		/*
		System.out.println("Arquivos encontrados:");
		for (String string : tempCaminhos) {
			System.out.println(string);
		}
		*/
		
		//remove files without winner
		// itera sobre todos os arquivos da pasta logs, verifica quais arquivos não possuem vencedores, coloca só partidas com vencedores na matchsToProcessPath
		ArrayList<String> matchsToProcessPath = new ArrayList<String>();
		for (String path : tempCaminhos) {
			if(hasResult(path)){
				matchsToProcessPath.add(path);
			}
		}
		
		// Monta lista de resultados com IA1 e IA2 com String ID do script, e Evaluation com inteiro do resultado (0, 1 ou -1)
		ArrayList<EvalResult> choices = lerArquivos(matchsToProcessPath);
		
		//remover arquivos
		removerArquivos(matchsToProcessPath);
		
		return choices;
		
	}
	
	private void removerArquivos(ArrayList<String> tempCaminhos){
		File file;
		for (String string : tempCaminhos) {
			file = new File(string);
			file.delete();
		}
	}
	
	// Recebe lista de arquivos que possuem vencedor na pasta logs
	private ArrayList<EvalResult> lerArquivos(ArrayList<String> tempCaminhos) {
		ArrayList<EvalResult> results = new ArrayList<>();
		String linha;
		
		// itera sobre todos os caminhos de arquivo
		for(String caminhoArquivo : tempCaminhos){
			
			//cria um EvalResult auxiliar
			EvalResult tResult = new EvalResult();
			
			File arqTour = new File(caminhoArquivo);

			try {
				FileReader arq = new FileReader(arqTour);
				BufferedReader learArq = new BufferedReader(arq);

				//leitura do arquivo
				linha = learArq.readLine();
				
				while(linha != null){
					
					if(linha.startsWith("Tupla A1 =")){
						String item = linha.replace("Tupla A1 =", "");
						tResult.setIA1(item.trim());
						System.out.println("Jogador 1: " + tResult.getIA1());
					}
					if(linha.startsWith("Tupla A2 =")){
						String item = linha.replace("Tupla A2 =", "");
						tResult.setIA2(item.trim());
						System.out.println("Jogador 2: " + tResult.getIA2());
					}
					if(linha.startsWith("Winner")){
						String item = linha.replace("Winner", "").trim();
						tResult.setEvaluation(Integer.decode(item));
						System.out.println("Resultado da partida: " + tResult.getEvaluation());
					}
										
					linha = learArq.readLine();
				}
				arq.close();
				results.add(tResult);

			} catch (Exception e) {
				System.err.printf("Erro na leitura dos arquivos");
				System.out.println(e.toString());
			}
		}

		return results;
	}
	
	public void removeNoResults(){
		ArrayList<String> tempCaminhos = new ArrayList<String>();
		File diretorio = new File(pathStruture);
		buscarParcial(diretorio, ".txt", tempCaminhos);
		
		for (String path : tempCaminhos) {
			if(!hasResult(path)){
				File rem = new File(path);
				rem.deleteOnExit();
			}
		}
	}


	private boolean hasResult(String path) {
		boolean winnerFounded = false;
		String linha;
		File arqTour = new File(path);

		try {
			FileReader arq = new FileReader(arqTour);
			BufferedReader learArq = new BufferedReader(arq);

			linha = learArq.readLine();
			
			while(linha != null){
				
				if(linha.contains("Game Over")){
					winnerFounded = true;
				}
				
									
				linha = learArq.readLine();
			}
			arq.close();

		} catch (Exception e) {
			System.err.printf("Erro na leitura dos arquivos");
			System.out.println(e.toString());
		}
		 
		return winnerFounded;
	}


	/**
	 * Realiza a busca (recursiva) de todos os arquivos com o nome informado
	 * @param arquivo = File contendo o caminho que se deseja procurar.
	 * @param palavra = String com o nome que se deseja buscar
	 * @param lista = ArrayList<String> que retornará os arquivos encontrados
	 * @return Lista de Strings com todos os caminhos absolutos dos arquivos com o nome encontrado
	 */
	public ArrayList<String> buscar(File arquivo, String palavra, ArrayList<String> lista) {
        if (arquivo.isDirectory()) {
            File[] subPastas = arquivo.listFiles();
            for (int i = 0; i < subPastas.length; i++) {
                lista = buscar(subPastas[i], palavra, lista);
                if (arquivo.getName().equalsIgnoreCase(palavra)) lista.add(arquivo.getAbsolutePath());
                else if (arquivo.getName().indexOf(palavra) > -1) lista.add(arquivo.getAbsolutePath());
            }
        }
        else if (arquivo.getName().equalsIgnoreCase(palavra)) lista.add(arquivo.getAbsolutePath());
        //else if (arquivo.getName().indexOf(palavra) > -1) lista.add(arquivo.getAbsolutePath());
        return lista;
    }
	
	// caminho da pasta logs, string .txt, lista vazia
	public static ArrayList<String> buscarParcial(File arquivo, String palavra, ArrayList<String> lista) {
        if (arquivo.isDirectory()) {
            File[] subPastas = arquivo.listFiles();
            for (int i = 0; i < subPastas.length; i++) {
                lista = buscarParcial(subPastas[i], palavra, lista);
                if (arquivo.getName().equalsIgnoreCase(palavra)) lista.add(arquivo.getAbsolutePath());
                else if (arquivo.getName().contains(palavra)) lista.add(arquivo.getAbsolutePath());
            }
        }
        else if (arquivo.getName().equalsIgnoreCase(palavra)) lista.add(arquivo.getAbsolutePath());
        else if (arquivo.getName().contains(palavra)) lista.add(arquivo.getAbsolutePath());
        
        return lista;
    }

}
