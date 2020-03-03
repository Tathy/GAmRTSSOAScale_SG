package ga.ScriptTableGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import ai.ScriptsGenerator.TableGenerator.FunctionsforGrammar;
import ai.ScriptsGenerator.TableGenerator.Parameter;
import ai.ScriptsGenerator.TableGenerator.TableCommandsGenerator;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
import ga.model.Population;
import rts.units.UnitTypeTable;


public class ScriptsTable {

	static Random rand = new Random();
	private int currentSizeTable;

	/**
	 * @return the currentSizeTable
	 */


	private HashMap<String, BigDecimal> scriptsTable ;
	private int numberOfTypes;
	private TableCommandsGenerator tcg;
	private FunctionsforGrammar functions;
	private String id;

	private String pathTableScripts;
	
	//Tathy
	private final String pathScriptFromInterface = System.getProperty("user.dir").concat("/Table/ScriptFromInterface.txt");
	
	public ScriptsTable(){
		functions=new FunctionsforGrammar();
	}

	public ScriptsTable(String pathTableScripts, String id){
		this.id = id;
		this.scriptsTable = new HashMap<>();
		this.pathTableScripts=pathTableScripts;
		this.tcg=TableCommandsGenerator.getInstance(new UnitTypeTable());
		this.numberOfTypes=tcg.getNumberTypes();
		functions=new FunctionsforGrammar();
	}


	public ScriptsTable(HashMap<String, BigDecimal> scriptsTable,String pathTableScripts) {
		super();
		this.scriptsTable = scriptsTable;
		this.pathTableScripts=pathTableScripts;
		this.tcg=TableCommandsGenerator.getInstance(new UnitTypeTable());
		this.numberOfTypes=tcg.getNumberTypes();
		functions=new FunctionsforGrammar();
	}



	public HashMap<String, BigDecimal> getScriptTable() {
		return scriptsTable;
	}


	public void addScript(String chromosomeScript){
		this.scriptsTable.put(chromosomeScript, BigDecimal.ZERO);
	}	

	public void print(){
		System.out.println("-- Table Scripts --");
		for(String c : scriptsTable.keySet()){
			//c.print();
			System.out.print(c);
		}
		System.out.println("-- Table Scripts --");
	}

	public void printWithValue(){
		System.out.println("-- Table Script --");
		for(String c : scriptsTable.keySet()){
			System.out.println(c);
			System.out.println("Value = "+ this.scriptsTable.get(c));
		}
		System.out.println("-- Table Scripts --");
	}


	//static methods

	public ScriptsTable generateScriptsTable(int size){
		
		// Script gerado com a interface é lido no arquivo e colocado no newChromosomes com key = 0
		
		HashMap<String, BigDecimal> newChromosomes = new HashMap<>();
		String tChom;
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathTableScripts+"ScriptsTable.txt"));

			int i=0;
			while(i<size){
				//tChom = new ChromosomeScript();				
				//int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME_SCRIPT)+1;
				int sizeCh=rand.nextInt(ConfigurationsGA.MAX_QTD_COMPONENTS)+1;
				if(ConfigurationsGA.sketch){ // sketch = true
					
					if(i == 0) {
						// Faz leitura do arquivo na primeira iteração para garantir a key = 0
						tChom = "";
						try {
							BufferedReader br = new BufferedReader(new FileReader(pathScriptFromInterface));
							while(br.ready()){
								String linha = br.readLine();
								//System.out.println("Script do arquivo: " + linha);
								tChom = tChom + linha;
							}
							br.close();
							tChom = tChom.substring(0, tChom.length()-1);
						}catch(IOException ioe){
							ioe.printStackTrace();
						}
						
					} else {
						tChom=buildScriptGenotypeSketch();
					}

				} else {
					tChom=buildScriptGenotype(sizeCh);
				}

				/*
				for (int j = 0; j < sizeCh; j++) {
					int typeSelected=rand.nextInt(numberOfTypes);
					int sizeRulesofType=tcg.getBagofTypes().get(typeSelected).size();
					int idRuleSelected=tcg.getBagofTypes().get(typeSelected).get(rand.nextInt(sizeRulesofType));
					tChom.addGene(idRuleSelected);
				}
				*/

				// Não aceita cromossomos repetidos
				if(!newChromosomes.containsKey(tChom)) {
					newChromosomes.put(tChom, BigDecimal.valueOf(i));
					f0.println(i+" "+tChom);
					i++;
				}

			}
			f0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		ScriptsTable st = new ScriptsTable(newChromosomes,pathTableScripts);
		return st;
	}
	
public ScriptsTable generateScriptsTableMutation(int size){
		
		// Script gerado com a interface é lido no arquivo e colocado no newChromosomes com key = 0
		
		HashMap<String, BigDecimal> newChromosomes = new HashMap<>();
		String tChom = null, tChom0 = null;
		PrintWriter f2;
		
		try {
			f2 = new PrintWriter(new FileWriter(pathTableScripts+"ScriptsTable2.txt"));

			int i=0;
			while(i<size){
				int sizeCh=rand.nextInt(ConfigurationsGA.MAX_QTD_COMPONENTS)+1;
				
				if(ConfigurationsGA.sketch){ // sketch = true
					if(i == 0) {
						// Faz leitura do arquivo na primeira iteração para garantir a key = 0
						tChom0 = "";
						try {
							BufferedReader br = new BufferedReader(new FileReader(pathScriptFromInterface));
							while(br.ready()){
								String linha = br.readLine();
								//System.out.println("Script do arquivo: " + linha);
								tChom0 = tChom0 + linha;
							}
							tChom = tChom0;
							br.close();
							//tChom0 = tChom0.substring(0, tChom0.length()-1);
						}catch(IOException ioe){
							ioe.printStackTrace();
						}
						
					} else {
						tChom = buildScriptMutation(tChom0);
						System.out.println("TESTE: i = " + i + " size = " + size);
						System.out.println("Novo script na mutação: " + tChom);
					}

				} else {
					tChom=buildScriptGenotype(sizeCh);
				}

				// Não aceita cromossomos repetidos
				if(!newChromosomes.containsKey(tChom)) {
					newChromosomes.put(tChom, BigDecimal.valueOf(i));
					f2.println(i+" "+tChom);
					i++;
				}

			}
			f2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		ScriptsTable st = new ScriptsTable(newChromosomes, pathTableScripts);
		return st;
	}

	public String buildScriptGenotype(int sizeGenotypeScript )
	{
		String genotypeScript = "";
		int numberComponentsAdded=0;

		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;


		boolean isOpenFor=false;



		List<itemIf> collectionofIfs= new ArrayList<itemIf>();

		while(numberComponentsAdded<sizeGenotypeScript)
		{


			//for
			if(rand.nextInt(2)>0 && numberComponentsAdded<sizeGenotypeScript-1 && isOpenFor==false)
			{
				collectionofIfs.add(new itemIf(0,true,"for"));
				genotypeScript=genotypeScript+returnForFunction();
				isOpenFor=true;
				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}
				
			}


			//basic function
			if(rand.nextInt(2)>0)
			{
				genotypeScript=genotypeScript+returnBasicFunction(isOpenFor);
				numberComponentsAdded++;
				canCloseParenthesisIf=true;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}


			}
			//conditional
			else if(rand.nextInt(2)>0 && numberComponentsAdded<sizeGenotypeScript-1)
			{

				collectionofIfs.add(new itemIf(1,true,"if"));

				genotypeScript=genotypeScript+returnConditional(isOpenFor);
				genotypeScript=genotypeScript+"(";

				numberComponentsAdded++;
				canCloseParenthesisIf=false;
				canOpenParenthesisIf=false;

				if(collectionofIfs.size()>0)
				{
					for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

						if(collectionofIfs.get(i).isLastOpen()==false)
						{
							collectionofIfs.remove(i);

						}
						else
						{
							break;
						}
					}
				}

			}



			//open parenthesis if
			if(collectionofIfs.size()>0)
			{
				//close parenthesis if
				if(rand.nextInt(2)>0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getType()=="for")
					{
						isOpenFor=false;
					}
					
					if(collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()==0)
					{
						for (int i = collectionofIfs.size()-1; i >= 0; i-- ) {

							if(collectionofIfs.get(i).isLastOpen()==false) 
							{

								collectionofIfs.remove(i);

							}
							else
							{
								break;
							}
						}
					}
					canOpenParenthesisIf=true;

				}
				
			}
				
			if(collectionofIfs.size()>0)
			{
				if(rand.nextInt(2)>0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() && numberComponentsAdded<sizeGenotypeScript)
				{
					genotypeScript=genotypeScript+"(";

					int counterLastIf=collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens();
					counterLastIf--;
					collectionofIfs.get(collectionofIfs.size()-1).setMaxOpens(counterLastIf);
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

					canOpenParenthesisIf=false;
					canCloseParenthesisIf=false;

					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(true);

				}
			}

			

			//ensure close open parenthesis if
			//ensure close open parenthesis
			if(numberComponentsAdded==sizeGenotypeScript)
			{
				while(collectionofIfs.size()>0)
				{
					if(collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
					{
						genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
						genotypeScript=genotypeScript+") ";
						collectionofIfs.remove(collectionofIfs.size()-1);
					}
					else
					{
						collectionofIfs.remove(collectionofIfs.size()-1);
					}

				}

			}

			//			//close parenthesis for
			//			if(rand.nextInt(2)>0 && isOpenFor  && canCloseParenthesisFor==true && isOpenIf==false)
			//			{
			//				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
			//				genotypeScript=genotypeScript+") ";
			//				isOpenFor=false;
			//			}


			//			if(numberComponentsAdded==sizeGenotypeScript && isOpenFor)
			//			{			
			//				genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
			//				genotypeScript=genotypeScript+") ";		
			//			
			//			}
			//System.out.println("actual "+genotypeScript+ "collec "+collectionofIfs.size());
		}
		//

		return genotypeScript;

	}
	
	public String buildScriptGenotypeSketch()
	{
		String genotypeScript = "";
		int numberComponentsAdded=0;
		Sketch sk=new Sketch();
		if(ConfigurationsGA.idSketch=="A")
		{
			genotypeScript=sk.sketchA(genotypeScript,numberComponentsAdded);
			genotypeScript=genotypeScript.trim();
			//basicFunction=basicFunction+") ";
			
		}
		
		if(ConfigurationsGA.idSketch=="B")
		{
			genotypeScript=sk.sketchB(genotypeScript,numberComponentsAdded);
			genotypeScript=genotypeScript.trim();
			//basicFunction=basicFunction+") ";
			
		}
		
		if(ConfigurationsGA.idSketch=="C")
		{
			genotypeScript=sk.sketchC(genotypeScript,numberComponentsAdded);
			genotypeScript=genotypeScript.trim();
			//basicFunction=basicFunction+") ";
			
		}

		return genotypeScript;
	}

	public String returnBasicFunction(Boolean forclausule)
	{
		String basicFunction="";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		FunctionsforGrammar functionChosen;
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		if(forclausule==false)
		{
			int idBasicActionSelected=rand.nextInt(functions.getBasicFunctionsForGrammar().size());
			functionChosen=functions.getBasicFunctionsForGrammar().get(idBasicActionSelected);
		}
		else
		{
			int idBasicActionSelected=rand.nextInt(functions.getBasicFunctionsForGrammarUnit().size());
			functionChosen=functions.getBasicFunctionsForGrammarUnit().get(idBasicActionSelected);
		}

		basicFunction=basicFunction+functionChosen.getNameFunction()+"(";
		for(Parameter parameter:functionChosen.getParameters())
		{
			if(parameter.getParameterName()=="u")
			{				
				basicFunction=basicFunction+"u,";
			}
			else if(parameter.getDiscreteSpecificValues()==null)
			{
				limitInferior=(int)parameter.getInferiorLimit();
				limitSuperior=(int)parameter.getSuperiorLimit();
				int parametherValueChosen;
				if(limitSuperior!=limitInferior)
				{
					parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
				}
				else
				{
					parametherValueChosen=limitSuperior;
				}
				basicFunction=basicFunction+parametherValueChosen+",";
			}
			else
			{
				int idChosen=rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue=parameter.getDiscreteSpecificValues().get(idChosen);
				basicFunction=basicFunction+discreteValue+",";
			}
		}
		basicFunction=basicFunction.substring(0, basicFunction.length() - 1);
		basicFunction=basicFunction+") ";
		return basicFunction;
	}

	public String returnConditional(boolean forClausule)
	{

		String conditional="";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		FunctionsforGrammar functionChosen;
		if(forClausule==false)		
		{
			int idconditionalSelected=rand.nextInt(functions.getConditionalsForGrammar().size());
			functionChosen=functions.getConditionalsForGrammar().get(idconditionalSelected);
		}
		else
		{
			int idconditionalSelected=rand.nextInt(functions.getConditionalsForGrammarUnit().size());
			functionChosen=functions.getConditionalsForGrammarUnit().get(idconditionalSelected);
		}

		conditional=conditional+functionChosen.getNameFunction()+"(";
		for(Parameter parameter:functionChosen.getParameters())
		{
			if(parameter.getParameterName()=="u")
			{

				conditional=conditional+"u,";
			}
			else if(parameter.getDiscreteSpecificValues()==null)
			{

				limitInferior=(int)parameter.getInferiorLimit();
				limitSuperior=(int)parameter.getSuperiorLimit();
				int parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
				conditional=conditional+parametherValueChosen+",";
			}
			else
			{
				int idChosen=rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue=parameter.getDiscreteSpecificValues().get(idChosen);
				conditional=conditional+discreteValue+",";
			}
		}
		conditional=conditional.substring(0, conditional.length() - 1);
		conditional="if("+conditional+")) ";
		return conditional;
	}

	public String returnForFunction()
	{
		String forClausule="";
		forClausule="for(u) (";
		return forClausule;
	}

	// Retorna uma nova função básica totalmente aleatória, respeitando apenas a presenção ou não do for u
	public String returnBasicFunctionClean(Boolean forclausule){
		String basicFunction = "";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		FunctionsforGrammar functionChosen;
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		
		// Sorteia nova função básica (com ou ser for u)
		if(forclausule==false){
			int idBasicActionSelected=rand.nextInt(functions.getBasicFunctionsForGrammar().size());
			functionChosen = functions.getBasicFunctionsForGrammar().get(idBasicActionSelected);
		} else {
			int idBasicActionSelected=rand.nextInt(functions.getBasicFunctionsForGrammarUnit().size());
			functionChosen = functions.getBasicFunctionsForGrammarUnit().get(idBasicActionSelected);
		}

		basicFunction = basicFunction + functionChosen.getNameFunction()+"(";
		
		// Itera sobre todos os parâmetros da função sorteada
		for(Parameter parameter:functionChosen.getParameters()){
			// Adiciona u caso a função o tenha como parâmetro
			if(parameter.getParameterName()=="u"){				
				basicFunction = basicFunction + "u,";
			// Adiciona parâmetro inteiro de acordo com seus limites inferior e superior
			} else if(parameter.getDiscreteSpecificValues() == null) {
				limitInferior=(int)parameter.getInferiorLimit();
				limitSuperior=(int)parameter.getSuperiorLimit();
				int parametherValueChosen;
				
				if(limitSuperior!=limitInferior){
					parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
				}else{
					parametherValueChosen=limitSuperior;
				}
				
				basicFunction=basicFunction+parametherValueChosen+",";
			
			// Os outros parâmetros são sorteados a partir de uma distribuição discreta equivalente a ele
			} else {
				int idChosen = rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue = parameter.getDiscreteSpecificValues().get(idChosen);
				basicFunction = basicFunction + discreteValue+",";
			}
			
		}
		
		basicFunction = basicFunction.substring(0, basicFunction.length() - 1);
		//basicFunction=basicFunction+") ";
		return basicFunction + ")";
	}
	
	// Retorna uma nova função básica de acordo com a função antiga, trocando os parâmetros
	public String returnBasicFunctionCleanSame(Boolean forclausule, String oldFunction){
		
		String basicFunction="";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		FunctionsforGrammar functionChosen = new FunctionsforGrammar();
		String parts[] = oldFunction.split("[\\W]");
		List<Integer> parametersDiscrete = new ArrayList<Integer>();
		
		// Itera em cada parte da função antiga, procura parâmetros inteiros (de 0 a 9) e adiciona na lista de parêmtros discretos
		for(String part: parts){
			if(Pattern.compile( "[0-9]" ).matcher(part).find()){
				parametersDiscrete.add(Integer.valueOf(part));
			}
		}
		
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		// Escolhe função equivalente à original com ou sem for u
		if(forclausule == false){
			// Itera sobre toda a lista de funções básicas sem parâmetro u e escolhe a equivalente
			for(FunctionsforGrammar lis: functions.getBasicFunctionsForGrammar()){
				if(oldFunction.startsWith(lis.getNameFunction())){
					functionChosen=lis;
				}
			}
		} else {
			// Itera sobre toda a lista de funções básicas com parâmetro u e escolhe a equivalente
			for(FunctionsforGrammar lis: functions.getBasicFunctionsForGrammarUnit()){
				if(oldFunction.startsWith(lis.getNameFunction())){
					functionChosen=lis;
				}
			}
		}

		basicFunction=basicFunction+functionChosen.getNameFunction()+"(";
		
		// Itera sobre todos os parâmetros da função escolhida
		for(Parameter parameter:functionChosen.getParameters()){
			// Adiciona u caso a função o tenha como parâmetro
			if(parameter.getParameterName()=="u"){				
				basicFunction=basicFunction+"u,";
			// Adiciona parâmetro inteiro de acordo com seus limites inferior e superior
			}else if(parameter.getDiscreteSpecificValues() == null){
				int currentValueParameter = parametersDiscrete.get(0);
				parametersDiscrete.remove(0);
				
				// A tendência a ir para valores mais altos ou mais baixos é aleatória
				boolean m = rand.nextFloat() <= 0.5;
				limitInferior=(int)parameter.getInferiorLimit();
				limitSuperior=(int)parameter.getSuperiorLimit();
				
				if(m){
					
					// Testa se o valor atual do parâmetro discreto + passo da mutação (delta) ultrapassa o limite superior
					if(!(currentValueParameter + ConfigurationsGA.deltaForMutation >= limitSuperior)){
						if(limitSuperior!=limitInferior){ // Dá o passo
							currentValueParameter = currentValueParameter + ConfigurationsGA.deltaForMutation;
						} else {						  // Não dá o passo
							currentValueParameter = limitSuperior;
						}
						
					// Testa se o valor atual do parâmetro discreto - passo da mutação (delta) ultrapassa o limite inferior
					}else if(!(currentValueParameter - ConfigurationsGA.deltaForMutation<=limitInferior)){
						if(limitSuperior != limitInferior){ // Dá o passo
							currentValueParameter = currentValueParameter - ConfigurationsGA.deltaForMutation;
						} else { 							// Não dá o passo
							currentValueParameter=limitInferior;
						}
					}	
					
				} else {
					// Testa se o valor atual do parâmetro discreto - passo da mutação (delta) ultrapassa o limite inferior
					if(!(currentValueParameter - ConfigurationsGA.deltaForMutation<=limitInferior)){
						if(limitSuperior!=limitInferior){
							currentValueParameter = currentValueParameter - ConfigurationsGA.deltaForMutation;
						} else {
							currentValueParameter=limitInferior;
						}
					// Testa se o valor atual do parâmetro discreto + passo da mutação (delta) ultrapassa o limite superior
					} else if(!(currentValueParameter+ ConfigurationsGA.deltaForMutation>=limitSuperior)) {
						if(limitSuperior!=limitInferior){
							currentValueParameter = currentValueParameter + ConfigurationsGA.deltaForMutation;
						} else {
							currentValueParameter=limitSuperior;
						}
					}
				}

				basicFunction=basicFunction+currentValueParameter+",";
			// Os outros parâmetros são sorteados a partir de uma distribuição discreta equivalente a ele
			} else {
				int idChosen=rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue=parameter.getDiscreteSpecificValues().get(idChosen);
				basicFunction=basicFunction+discreteValue+",";
			}
		}
		
		basicFunction=basicFunction.substring(0, basicFunction.length() - 1);
		//basicFunction=basicFunction+") ";
		return basicFunction+")";
	}

	public String returnConditionalClean(boolean forClausule) {
		String conditional="";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		FunctionsforGrammar functionChosen;
		
		if(forClausule == false) {
			// Sorteia um novo condicional pelo ID fora de for
			int idconditionalSelected = rand.nextInt(functions.getConditionalsForGrammar().size());
			functionChosen = functions.getConditionalsForGrammar().get(idconditionalSelected);
		} else {
			// Sorteia um novo condicional pelo ID dentro de for
			int idconditionalSelected=rand.nextInt(functions.getConditionalsForGrammarUnit().size());
			functionChosen=functions.getConditionalsForGrammarUnit().get(idconditionalSelected);
		}

		conditional=conditional+functionChosen.getNameFunction()+"(";
		
		for(Parameter parameter:functionChosen.getParameters()) {
			// Coloca o u caso este seja o parâmetro atual
			if(parameter.getParameterName()=="u"){
				conditional=conditional+"u,";
			// Adiciona parâmetro inteiro de acordo com seus limites inferior e superior
			} else if(parameter.getDiscreteSpecificValues()==null){
				limitInferior=(int)parameter.getInferiorLimit();
				limitSuperior=(int)parameter.getSuperiorLimit();
				int parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
				conditional=conditional+parametherValueChosen+",";
			// Os outros parâmetros são sorteados a partir de uma distribuição discreta equivalente a ele
			} else {
				int idChosen = rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue=parameter.getDiscreteSpecificValues().get(idChosen);
				conditional=conditional+discreteValue+",";
			}
		}
		
		conditional=conditional.substring(0, conditional.length() - 1);
		//conditional="if("+conditional+")) ";
		return conditional+")";
	}
	
	public String returnConditionalCleanSame(boolean forClausule, String oldFunction){
		String conditional="";
		int limitInferior;
		int limitSuperior;
		String discreteValue;
		FunctionsforGrammar functionChosen=new FunctionsforGrammar();
		String parts[]=oldFunction.split("[\\W]");
		List<Integer> parametersDiscrete=new ArrayList<Integer>();
		
		// Itera em cada parte da função antiga, procura parâmetros inteiros (de 0 a 9) e adiciona na lista de parêmtros discretos
		for(String part: parts){
			if(Pattern.compile( "[0-9]" ).matcher(part).find()){
					parametersDiscrete.add(Integer.valueOf(part));
				}
		}
		
		//int id=rand.nextInt(ConfigurationsGA.QTD_RULES_BASIC_FUNCTIONS);
		// Escolhe função equivalente à original com ou sem for u
		if(forClausule==false){
			// Itera sobre toda a lista de funções condicionais sem o u e escolhe uma equivalente
			for(FunctionsforGrammar lis: functions.getConditionalsForGrammar()){
				if(oldFunction.startsWith(lis.getNameFunction())){
					functionChosen=lis;
				}
			}
		} else {
			// Itera sobre toda a lista de funções condicionais com o u e escolhe uma equivalente
			for(FunctionsforGrammar lis: functions.getConditionalsForGrammarUnit()){
				if(oldFunction.startsWith(lis.getNameFunction())){
					functionChosen=lis;
				}
			}
		}

		conditional=conditional+functionChosen.getNameFunction()+"(";
		
		// Itera sobre todos os parâmetros da função escolhida
		for(Parameter parameter:functionChosen.getParameters()){
			// Adiciona u caso a função o tenha como parâmetro
			if(parameter.getParameterName()=="u"){				
				conditional=conditional+"u,";
			// Adiciona parâmetro inteiro de acordo com seus limites inferior e superior
			}else if(parameter.getDiscreteSpecificValues() == null){
				int currentValueParameter=parametersDiscrete.get(0);
				parametersDiscrete.remove(0);
				
				// A tendência a ir para valores mais altos ou mais baixos é aleatória
				boolean m = rand.nextFloat() <= 0.5;
				limitInferior = (int)parameter.getInferiorLimit();
				limitSuperior = (int)parameter.getSuperiorLimit();
				
				if(m){
					
					// Testa se o valor atual do parâmetro discreto + passo da mutação (delta) ultrapassa o limite superior
					if(!(currentValueParameter + ConfigurationsGA.deltaForMutation >= limitSuperior)){
						if(limitSuperior!=limitInferior){ // Dá o passo
							currentValueParameter = currentValueParameter + ConfigurationsGA.deltaForMutation;
						} else {						  // Não dá o passo
							currentValueParameter = limitSuperior;
						}
						
					// Testa se o valor atual do parâmetro discreto - passo da mutação (delta) ultrapassa o limite inferior
					}else if(!(currentValueParameter - ConfigurationsGA.deltaForMutation<=limitInferior)){
						if(limitSuperior != limitInferior){ // Dá o passo
							currentValueParameter = currentValueParameter - ConfigurationsGA.deltaForMutation;
						} else { 							// Não dá o passo
							currentValueParameter=limitInferior;
						}
					}	
					
				} else {
					
					// Testa se o valor atual do parâmetro discreto - passo da mutação (delta) ultrapassa o limite inferior
					if(!(currentValueParameter - ConfigurationsGA.deltaForMutation<=limitInferior)){
						if(limitSuperior!=limitInferior){
							currentValueParameter = currentValueParameter - ConfigurationsGA.deltaForMutation;
						} else {
							currentValueParameter=limitInferior;
						}
					// Testa se o valor atual do parâmetro discreto + passo da mutação (delta) ultrapassa o limite superior
					} else if(!(currentValueParameter+ ConfigurationsGA.deltaForMutation>=limitSuperior)) {
						if(limitSuperior!=limitInferior){
							currentValueParameter = currentValueParameter + ConfigurationsGA.deltaForMutation;
						} else {
							currentValueParameter=limitSuperior;
						}
					}
				}

				conditional=conditional+currentValueParameter+",";
				
			// Os outros parâmetros são sorteados a partir de uma distribuição discreta equivalente a ele
			} else {
				int idChosen=rand.nextInt(parameter.getDiscreteSpecificValues().size());
				discreteValue=parameter.getDiscreteSpecificValues().get(idChosen);
				conditional=conditional+discreteValue+",";
			}
		}
		
		conditional=conditional.substring(0, conditional.length() - 1);
		//basicFunction=basicFunction+") ";
		return conditional+")";
	}

	//THis method uses a preexistent table of scripts instead of create a new one
	public ScriptsTable generateScriptsTableCurriculumVersion(){

		HashMap<String, BigDecimal> newChromosomes = new HashMap<>();
		ChromosomeScript tChom;
		try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable.txt"))) {
			String line;            
			while ((line = br.readLine()) != null) {
				String[] strArray = line.split(" ");
				int[] intArray = new int[strArray.length];
				for (int i = 0; i < strArray.length; i++) {
					intArray[i] = Integer.parseInt(strArray[i]);
				}
				int idScript = intArray[0];
				int[] rules = Arrays.copyOfRange(intArray, 1, intArray.length);

				tChom = new ChromosomeScript();
				for (int i : rules) {
					tChom.addGene(i);
				}
				newChromosomes.put("", BigDecimal.valueOf(idScript));;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScriptsTable st = new ScriptsTable(newChromosomes,pathTableScripts);
		//st.print();
		return st;
	}
	
	//THis method uses a preexistent table of scripts instead of create a new one
	public ScriptsTable generateScriptsTableRecover(){

		HashMap<String, BigDecimal> newChromosomes = new HashMap<>();
		ChromosomeScript tChom;
		try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable.txt"))) {
			String line;            
			while ((line = br.readLine()) != null) {
				String[] strArray = line.split(" ");

				int idScript = Integer.parseInt(strArray[0]);
				String rules = line.replaceFirst(strArray[0]+" ", "");

				tChom = new ChromosomeScript();
//				for (int i : rules) {
//					tChom.addGene(i);
//				}
				newChromosomes.put(rules, BigDecimal.valueOf(idScript));;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScriptsTable st = new ScriptsTable(newChromosomes,pathTableScripts);
		//st.print();
		return st;
	}


	public int getCurrentSizeTable() {
		return currentSizeTable;
	}

	public void setCurrentSizeTable(int currentSizeTabler) {
		currentSizeTable = currentSizeTabler;
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathTableScripts+"SizeTable" + this.id +  ".txt"));
			f0.println(currentSizeTable);
			f0.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	//	public boolean checkDiversityofTypes() {
	//		
	//		HashSet<Integer> diferentTypes =  new HashSet<Integer>();
	//		for(String c : scriptsTable.keySet()){
	//			for (Integer gene : c.getGenes()) {
	//				
	//				diferentTypes.add(tcg.getCorrespondenceofTypes().get(gene));
	//			}
	//		}
	//		if(diferentTypes.size()==numberOfTypes) {
	//			return false;
	//		}
	//		else {
	//			return true;
	//		}		
	//	}
	
	public String buildScriptMutation(String tChom){
		// Lista as funções básicas e condicionais
		functions=new FunctionsforGrammar();
		List<FunctionsforGrammar> basicFunctions = functions.getBasicFunctionsForGrammar();
		List<FunctionsforGrammar> conditionalFunctions = functions.getConditionalsForGrammar();
		
		// Cria um script cromossomo auxiliar e tira os fors
		String tChomAux = tChom;
		tChomAux = tChomAux.replace("(for(u)", "");
		tChomAux = tChomAux.replace("for(u)", "");
		
		// Separa o script cromossomo auxiliar em partes
	    String[] parts = tChomAux.trim().split("\\s+");
	    
	    String[] news = new String[parts.length];
	       
	    // Itera sobre todas as partes e faz tratamento de strings
	    for(int i = 0; i < parts.length; i++) {
	    	// Retira ifs e parênteses do começo
		    parts[i] = removeFromBeggining(parts[i]);
		    // Retira parênteses do final
		    parts[i] = removeFromLast(parts[i]);
	    }
	    
	    // Retorna novo conjunto de scripts após o processo de mutação
	    news = chossingFromBag(news, parts, basicFunctions, conditionalFunctions);
	    
	    // Itera sobre todo o vetor de partes, sorteia a chance de mutação e substitui o novo cromossomo sobre o antigo que sofre mutação
	    for(int i=0; i <parts.length; i++){
	    	double mutatePercent = ConfigurationsGA.MUTATION_RATE_RULE;
	    	boolean m = rand.nextFloat() <= mutatePercent;

	    	if(m)
	    		tChom = replaceFromCompleteGrammar(parts[i], news[i], tChom );
	    }
	    
	    // Caso o novo cromossomo gerado já tenha um equivalente na scrTable, o ID usado será o mesmo do já existente
	    tChom = removingTrashFromGrammar(tChom);
		if(scriptsTable.containsKey(tChom)){
			return tChom;			
		} else {
			// Caso o novo cromossomo gerado não tenha equivalente na scrTable, ele é adicionado no final, com ID = tamanho atual da tabela de scripts
			//System.out.println("beforeMutateScript "+cromScriptOriginal);
			//System.out.println("afterMutateScript "+cromScript);
			int newId = scriptsTable.size();
			scriptsTable.put(tChom, BigDecimal.valueOf(newId));
			setCurrentSizeTable(scriptsTable.size());
			addLineFile(newId + " " + tChom);
			return tChom;
		}
		
	}
	
	public static String removeFromBeggining(String s){
		String cloneS = s;
		  
		try {
			// Retira abre parênteses (
			while (cloneS.charAt(0)=='(' ){
				cloneS=cloneS.replaceFirst("\\(", "");
			}
		
			// Retira ifs e parênteses
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

	public String removeFromLast(String s){
		String cloneS = s;
		
		// Retira fechamento de parênteses )
		while (cloneS.endsWith("))")) {
			cloneS=cloneS.replaceFirst("\\)", "");
		}
		return cloneS;
	}

	public String[]  chossingFromBag(String[] news, String[] parts, List<FunctionsforGrammar>basicFunctions, List<FunctionsforGrammar>conditionalFunctions){
		ScriptsTable objScriptTable = new ScriptsTable("", this.id);
		
		boolean found=false;
		for (int i = 0; i < parts.length; i++){
			found = false;
			
			// Itera sobre todas as funções do conjunto de funções básicas
			for (FunctionsforGrammar function:basicFunctions){
				
				if(parts[i].startsWith(function.getNameFunction())){
					//change with other basicFunction
					// Se o comando original estava dentro de um for
					if(parts[i].contains(",u,") || parts[i].contains(",u)") || parts[i].contains("(u,")) {
						// Forçar este m para o false pro TCC, usar a função que retorna um script com função equivalente ao original da interface
						boolean m = rand.nextFloat() <= 0.5;
						// VALOR DO M VERIFICAR
						m = false;
						if(m){
							// Retorna uma nova função básica totalmente aleatória, respeitando apenas a presença ou não do for u
							news[i] = objScriptTable.returnBasicFunctionClean(true);
						} else {
							// Retorna uma nova função básica de acordo com a função antiga, trocando os parâmetros
							news[i]=objScriptTable.returnBasicFunctionCleanSame(true, parts[i]);
						}
					// Se o comando original não estava dentro de um for
					} else {
						boolean m = rand.nextFloat() <= 0.5;
						// VALOR DO M VERIFICAR
						m = false;
						if(m){
							news[i]=objScriptTable.returnBasicFunctionClean(false);
						} else {
							news[i]=objScriptTable.returnBasicFunctionCleanSame(false, parts[i]);
						}
					}
					
					found = true;
					break;
				}
			}
			
			if(found == false){
				
				for (FunctionsforGrammar function:conditionalFunctions){
					if(parts[i].startsWith(function.getNameFunction())){
						//change with other basicFunction
						// Se o comando original estava dentro de um for
						if(parts[i].contains(",u,") || parts[i].contains(",u)") || parts[i].contains("(u,")){
							// Forçar este m para o false pro TCC, usar a função que retorna um script com função equivalente ao original da interface
							boolean m = rand.nextFloat() <= 0.5;
							// VALOR DO M VERIFICAR
							m = false;
							if(m){
								// Retorna uma nova função condicional totalmente aleatória, respeitando apenas a presença ou não do for u
								news[i]=objScriptTable.returnConditionalClean(true);
							} else {
								// Retorna uma nova função básica de acordo com a função antiga, trocando os parâmetros
								news[i]=objScriptTable.returnConditionalCleanSame(true, parts[i]);
							}
						// Se o comando original não estava dentro de um for
						} else {
							boolean m = rand.nextFloat() <= 0.5;
							// VALOR DO M VERIFICAR
							m = false;
							if(m) {
								news[i]=objScriptTable.returnConditionalClean(false);
							} else {
								news[i]=objScriptTable.returnConditionalCleanSame(false, parts[i]);
							}
						}
						
						break;
					}
				}
				
			}
		}
		
		return news;
	}
	
	public String replaceFromCompleteGrammar(String oldFunction, String newFunction, String originalGrammar){
		originalGrammar = originalGrammar.replace(oldFunction, newFunction+"NEW");
		return originalGrammar;
	}
	
	public String removingTrashFromGrammar(String originalGrammar){
		originalGrammar=originalGrammar.replace("NEW", "");		
		return originalGrammar;
	}
	
	public void addLineFile(String data) {
	    
		try{    
	
	        File file = new File(pathTableScripts+"ScriptsTable2.txt");    
	
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
	
}
