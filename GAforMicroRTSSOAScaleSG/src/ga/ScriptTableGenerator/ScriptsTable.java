package ga.ScriptTableGenerator;

import java.io.BufferedReader;
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

import ai.ScriptsGenerator.TableGenerator.FunctionsforGrammar;
import ai.ScriptsGenerator.TableGenerator.Parameter;
import ai.ScriptsGenerator.TableGenerator.TableCommandsGenerator;
import ga.config.ConfigurationsGA;
import ga.model.Chromosome;
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

	private String pathTableScripts;

	public ScriptsTable(String pathTableScripts){
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

		HashMap<String, BigDecimal> newChromosomes = new HashMap<>();
		String tChom;
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathTableScripts+"ScriptsTable.txt"));

			int i=0;
			while(i<size)
			{
				//tChom = new ChromosomeScript();				
				//int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME_SCRIPT)+1;
				int sizeCh=rand.nextInt(ConfigurationsGA.MAX_QTD_COMPONENTS)+1;
				tChom=buildScriptGenotype(sizeCh);

				//				for (int j = 0; j < sizeCh; j++) {
				//					int typeSelected=rand.nextInt(numberOfTypes);
				//					int sizeRulesofType=tcg.getBagofTypes().get(typeSelected).size();
				//					int idRuleSelected=tcg.getBagofTypes().get(typeSelected).get(rand.nextInt(sizeRulesofType));
				//					tChom.addGene(idRuleSelected);
				//				}

				if(!newChromosomes.containsKey(tChom))
				{
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
		for (int i = 0; i < size; i++) {

		}
		ScriptsTable st = new ScriptsTable(newChromosomes,pathTableScripts);
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
					for (int i = collectionofIfs.size()-1; i == 0; i-- ) {

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
					for (int i = collectionofIfs.size()-1; i == 0; i-- ) {

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
					for (int i = collectionofIfs.size()-1; i == 0; i-- ) {

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
						for (int i = collectionofIfs.size()-1; i == 0; i-- ) {

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
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.remove(collectionofIfs.size()-1);

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
				int parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
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

	public String returnBasicFunctionClean(Boolean forclausule)
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
				int parametherValueChosen = rand.nextInt(limitSuperior-limitInferior) + limitInferior;
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
		//basicFunction=basicFunction+") ";
		return basicFunction+")";
	}

	public String returnConditionalClean(boolean forClausule)
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
		//conditional="if("+conditional+")) ";
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

	public int getCurrentSizeTable() {
		return currentSizeTable;
	}

	public void setCurrentSizeTable(int currentSizeTabler) {
		currentSizeTable = currentSizeTabler;
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter(pathTableScripts+"SizeTable.txt"));
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

}
