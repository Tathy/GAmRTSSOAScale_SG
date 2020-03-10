package ga.model;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ga.ScriptTableGenerator.ScriptsTable;
import ga.config.ConfigurationsGA;

public class Population {
	
	static Random rand = new Random();
	private Map<Integer,List<String>> allCommandsperGeneration;
	private Map<Integer,List<String>> usedCommandsperGeneration;
	private HashMap<BigDecimal, String> scriptsAlternativeTable;
	private String pathTableScripts;
	
	
	
	
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
		allCommandsperGeneration= new HashMap<Integer,List<String>>();
		usedCommandsperGeneration=new HashMap<Integer,List<String>>();
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
	 * Fun��o que zera os valores das avalia��es dos Chromossomos.
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
	public static Population getInitialPopulation(int size, ScriptsTable scrTable, boolean keepIndiv0){
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
		
		if(keepIndiv0) {
			// Garante que o chromossomo com key = 0 esteja dentro da população
			Chromosome tChom0 = new Chromosome();
			tChom0.addGene(0);
			newChromosomes.put(tChom0, BigDecimal.ZERO);
		}
		
		Chromosome tChom;
		while (newChromosomes.size()<size) {
			//gerar o novo cromossomo com base no tamanho
			tChom = new Chromosome();
			int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
			
			for (int j = 0; j < sizeCh; j++) {
				tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			
			newChromosomes.put(tChom, BigDecimal.ZERO);
		}
		/*
		System.out.println("Lista do newChromosomes:");
		for(Chromosome k : newChromosomes.keySet() ) {
			BigDecimal value = newChromosomes.get(k);
			System.out.print(k.getGenes() + " = " + value + "; ");
		}
		System.out.println();
		*/
		
		Population pop = new Population(newChromosomes);
		//System.out.println("Population dentro da getInitialPopulation: " + pop.getChromosomes().size());
		return pop;
	}
	
	public static Population getInitialPopulationMutation(int size, ScriptsTable scrTable){
		HashMap<Chromosome, BigDecimal> newChromosomes = new HashMap<>();
		
		// Garante que o chromossomo com key = 0, vindo da interface, esteja dentro da popula��o
		Chromosome tChom0 = new Chromosome();
		tChom0.addGene(0);
		newChromosomes.put(tChom0, BigDecimal.ZERO);
		
		Chromosome tChom;
		while (newChromosomes.size() < size) {
			// Gerar novo cromossomo com muta��o
			tChom = new Chromosome();
			int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
			
			for (int j = 0; j < sizeCh; j++) {
				tChom.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			
			newChromosomes.put(tChom, BigDecimal.ZERO);
		}
		
		System.out.println("Lista do newChromosome popula��o 2:");
		for(Chromosome k : newChromosomes.keySet() ) {
			BigDecimal value = newChromosomes.get(k);
			System.out.print(k.getGenes() + " = " + value + "; ");
		}
		System.out.println();
		
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
	public static Population getInitialPopulation(Integer gene, String id){
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
	
	public void fillAllCommands(String pathscrTable, String idTable) {
		allCommandsperGeneration.clear();
		this.pathTableScripts = pathscrTable;
		buildScriptsAlternativeTable(idTable);
	    Iterator it = Chromosomes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        int id = ((BigDecimal)pair.getValue()).intValue();
	        ArrayList<Integer> scriptsId = ((Chromosome)pair.getKey()).getGenes();
	        String completeGrammars;
	        
	        for(Integer idScript:scriptsId) {
	        	//System.out.println(scriptsAlternativeTable);
	        	completeGrammars = scriptsAlternativeTable.get(BigDecimal.valueOf(idScript));
	        	getCommandsFromFullScript(idScript,completeGrammars);
	        }
	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	}
	
	public void getCommandsFromFullScript(int id,String script)
	{
		//System.out.println("script "+script);
		int counterIdsCimmands=0;
		String[] splited = script.split("\\s+");
		for(String s : splited)
		{
			if(!s.contains("for") && !s.contains("if") && s.length()>1)
			{
				if(s.charAt(0) =='(')
				{
					s=s.replaceFirst("\\(", "");
				}
				//System.out.println("part "+s);
				while(s.charAt(s.length()-1)==')' && s.charAt(s.length()-2)==')')
				{
					s=s.substring(0, s.length() - 1);
				}
				
			
			if(allCommandsperGeneration.containsKey(id))
			{
				List<String> allCommandsStored=allCommandsperGeneration.get(id);
				if(!allCommandsStored.contains(String.valueOf(counterIdsCimmands)))
				{
					allCommandsStored.add(String.valueOf(counterIdsCimmands));
					allCommandsperGeneration.put(id, allCommandsStored);
				}
				
			}
			else
			{	List<String> allCommandsStored=new ArrayList<String>();
				allCommandsStored.add(String.valueOf(counterIdsCimmands));
				allCommandsperGeneration.put(id, allCommandsStored);
			}
			counterIdsCimmands++;
		}
			
		}
		
	}


	/**
	 * @return the allCommandsperGeneration
	 */
	public Map<Integer,List<String>> getAllCommandsperGeneration() {
		return allCommandsperGeneration;
	}


	/**
	 * @param allCommandsperGeneration the allCommandsperGeneration to set
	 */
	public void setAllCommandsperGeneration(Map<Integer,List<String>> allCommandsperGeneration) {
		this.allCommandsperGeneration = allCommandsperGeneration;
	}
	
    public void buildScriptsAlternativeTable(String id) {
        scriptsAlternativeTable = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathTableScripts + "/ScriptsTable" + id + ".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
            	//System.out.println(line);
                String code = line.substring(line.indexOf(" "), line.length());
                String[] strArray = line.split(" ");
                int idScript = Integer.decode(strArray[0]);
                scriptsAlternativeTable.put(BigDecimal.valueOf(idScript), code);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


	public void chooseusedCommands(String pathUsedCommands, String id) {
		// TODO Auto-generated method stub
		
		readUsedCommands(pathUsedCommands, id);
	}
	
	public void removeCommands(ScriptsTable scrTable) {
		// TODO Auto-generated method stub
		
	    Iterator it = getUsedCommandsperGeneration().entrySet().iterator();
	    while (it.hasNext()) {
	    	
	        Map.Entry pair = (Map.Entry)it.next();
	        int id=(Integer)pair.getKey();
	        
	        List<String> commandsUsed= (List<String>) pair.getValue();
	        
	        if(getAllCommandsperGeneration().get(id)!=null)
	        {
	        	List<String> commandsAll=getAllCommandsperGeneration().get(id);
		        commandsAll.removeAll(commandsUsed);
		        		        
	        }
	        	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	    changeGrammars(scrTable);
	}
	
	public void changeGrammars(ScriptsTable scrTable)
	{

		HashMap<Chromosome, BigDecimal> ChromosomesNew=new HashMap<>();
	    Iterator it = Chromosomes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        int value=((BigDecimal)pair.getValue()).intValue();
	        Chromosome chOriginal=(Chromosome)pair.getKey();
	        ArrayList<Integer> scriptsId= ((Chromosome)pair.getKey()).getGenes();
	        
	        Chromosome newCh=new Chromosome();
			newCh.setGenes((ArrayList<Integer>) scriptsId.clone());
			
	        ArrayList<Integer> scriptsToDelete=new ArrayList<Integer>();
	        String originalcompleteGrammars;
	        for(int i=0;i<newCh.getGenes().size();i++) 
	        {
	        	if(allCommandsperGeneration.containsKey(newCh.getGenes().get(i)))
	        	{
	        		//System.out.println(scriptsAlternativeTable);
	        		originalcompleteGrammars=scriptsAlternativeTable.get(BigDecimal.valueOf(newCh.getGenes().get(i)));
	        		
	        		String newGrammar=replaceCommandsinGrammarAccordingIdScripts(originalcompleteGrammars,newCh.getGenes().get(i));
	        		
	        		String newTempGrammar= newGrammar.replaceAll("\\s","");
	        		newGrammar=newGrammar.trim();
	        	
	        		if(newTempGrammar.length()>0 && newTempGrammar.matches(".*[a-zA-Z]+.*"))
	        		{
	        			if(!originalcompleteGrammars.equals(newGrammar))
	        			{
	        				if(scrTable.getScriptTable().containsKey(newGrammar))
	        				{
	        					//System.out.println("before replace Rules "+originalcompleteGrammars);
	        					//System.out.println("After replace Rules "+newGrammar);
	        					newCh.getGenes().set(i, scrTable.getScriptTable().get(newGrammar).intValue());
	        				}
	        				else
	        				{   
	        					//System.out.println("before replace Rules "+originalcompleteGrammars);
	        					//System.out.println("After replace Rules "+newGrammar);
	        					
	        					int newId=scrTable.getScriptTable().size();
	        					scrTable.getScriptTable().put(newGrammar, BigDecimal.valueOf(newId));
	        					scrTable.setCurrentSizeTable(scrTable.getScriptTable().size());
	        					//addLineFile(newId+" old "+scriptsId.get(i)+" "+newGrammar);
	        					addLineFile(newId+" "+newGrammar);
	        					newCh.getGenes().set(i, newId);
	        				}
	        			}
	        		}
	        		else
	        		{
	        			scriptsToDelete.add(newCh.getGenes().get(i));
	        			
	        		}
	        	}

	        }
	        newCh.getGenes().removeAll(scriptsToDelete);
	        
	        if(newCh.getGenes().size()>0) 
	        {
	        	ChromosomesNew.put(newCh, BigDecimal.valueOf(value));
	        }	        
	        
	    	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    while(ChromosomesNew.size()<ConfigurationsGA.SIZE_PARENTSFORCROSSOVER)
	    {
        	Chromosome newCh = new Chromosome();
			int sizeCh=rand.nextInt(ConfigurationsGA.SIZE_CHROMOSOME)+1;
			for (int j = 0; j < sizeCh; j++) {
				newCh.addGene(rand.nextInt(scrTable.getCurrentSizeTable()));
			}
			ChromosomesNew.put(newCh, BigDecimal.valueOf(0));
	    }
	    setChromosomes(ChromosomesNew);;
	}
	
	public void addLineFile(String data) {
	    try{    

	        File file =new File(pathTableScripts+"ScriptsTable.txt");    

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

	
	public String replaceCommandsinGrammar(String originalcompleteGrammars,int id)
	{
		String newGrammar=originalcompleteGrammars;
		for(String command:allCommandsperGeneration.get(id))
		{
			if(newGrammar.contains(command))
			{
				newGrammar=newGrammar.replace(command, "");
			}
		}
		newGrammar=validateUnusefulIfsImproved(newGrammar);
		newGrammar=removePaddings(newGrammar);
		newGrammar=newGrammar.replace("#", "");
		newGrammar=removeTrashBracketsFromString(newGrammar);
		newGrammar=newGrammar.replaceFirst("\\s+", "");
		newGrammar=removingRemainingElses(newGrammar);
		return newGrammar;
	}
	
	public String replaceCommandsinGrammarAccordingIdScripts(String originalcompleteGrammars,int id)
	{		
		String newGrammar=originalcompleteGrammars;
		String[] splited = newGrammar.split("\\s+");
		for(String command:allCommandsperGeneration.get(id))
		{
			int counterIdsCimmands=0;
						
			for( int i=0;i<splited.length; i++)
			{
				String s=splited[i];
				if(!s.contains("for") && !s.contains("if") && s.length()>1)
				{
					if(s.charAt(0) =='(')
					{
						s=s.replaceFirst("\\(", "");
					}
					while(s.charAt(s.length()-1)==')' && s.charAt(s.length()-2)==')')
					{
						s=s.substring(0, s.length() - 1);
					}
					if(counterIdsCimmands==Integer.valueOf(command))
					{
						splited[i]=splited[i].replace(s, "TOREMOVE");						
						
					}
					counterIdsCimmands++;
				}
				
			}
			
		}
		newGrammar=recoverStringFromArray(splited);
		//System.out.println("First stage "+newGrammar);
		newGrammar=newGrammar.replace("TOREMOVE", "");
		//System.out.println("second stage "+newGrammar);
		newGrammar=validateUnusefulIfsImproved(newGrammar);
		//System.out.println("third stage "+newGrammar);
		newGrammar=removePaddings(newGrammar);
		//System.out.println("Fourth stage "+newGrammar);
		newGrammar=newGrammar.replace("#", "");
		//System.out.println("fifth stage "+newGrammar);
		newGrammar=removeTrashBracketsFromString(newGrammar);
		//System.out.println("six stage "+newGrammar);
		newGrammar=newGrammar.replaceFirst("\\s+", "");
		//System.out.println("seven stage "+newGrammar);
		newGrammar=removingRemainingElses(newGrammar);
		//System.out.println("FInal stage "+newGrammar);
		return newGrammar;
		
	}
	
	public String validateUnusefulIfsImproved(String newGrammar)
	{
		String parts[]=newGrammar.split("\\s+");

		for(int i=parts.length-1; i>=0;i--)
		{
			
			if(parts[i].contains("if") || parts[i].contains("for"))
				{	
				
					boolean letter=false;
					boolean closed=false;
					boolean open=false;
					int pointOpen=0;
					int pointClosed=0;
					int countOpen=0;
					String removedExcess=removeExcessBrackets(parts[i]);
					int pos=newGrammar.lastIndexOf(removedExcess)+removedExcess.length();
					
					for(int j=pos; j<newGrammar.length();j++)
					{
						if(newGrammar.charAt(j) =='(')
						{	
						    if(open==false)
						    {
						    	pointOpen=j;
						    }
						    open=true;
							countOpen++;
						}
						else if(newGrammar.charAt(j) ==')')
						{
							pointClosed=j;
							closed=true;
							countOpen--;
						}
						else if(Character.isLetter(newGrammar.charAt(j)) && newGrammar.charAt(j) !='?' && newGrammar.charAt(j) !='�')
						{
							letter=true;
						}
						
						if(closed==true && letter==false && countOpen==0)
						{
							
							newGrammar=changeCharInPosition(pointClosed,'?',newGrammar);
							newGrammar=changeCharInPosition(pointOpen,'?',newGrammar);
							//newGrammar=newGrammar.replace("Z", "");
							
							int start = newGrammar.lastIndexOf(removedExcess);
							StringBuilder builder = new StringBuilder();
							
							builder.append(newGrammar.substring(0, start));
							builder.append("�");
							builder.append(newGrammar.substring(start + removedExcess.length()));
							newGrammar=builder.toString();
							
							break;

						}
						else if(closed==false && letter==true)
						{
							int start = newGrammar.lastIndexOf(removedExcess);
							if(removedExcess.contains("for"))
							{
							newGrammar=changeCharInPosition(start, '@', newGrammar);
							}
							else
							{
								newGrammar=changeCharInPosition(start, '$', newGrammar);	
							}
							break;
						}
					}
					}
		}
		return newGrammar;
	}
	
	public String [] validateUnusefulIfs(String newGrammar)
	{
		String parts[]=newGrammar.split("\\s+");
		for(int i=parts.length-1; i>=0;i--)
		{
			if(parts[i].contains("if") || parts[i].contains("for"))
				{	
					boolean found=false;
					
					int k=i+1;
					int t=i+2;
					if(parts[i+1].equals(""))
					{
						String firstComparing=parts[k];
						while(firstComparing.equals(""))
						{
							k++;
							firstComparing=parts[k];
						}
						if(k+1<parts.length)
						{
							String secondComparing=parts[k+1];
							t=k+1;
							while(secondComparing.equals(""))
							{
								t++;
								secondComparing=parts[t];
							}
						}
					}
					else {
						
						if(t<parts.length)
						{
							if(parts[t].equals(""))
							{
								String secondComparing=parts[t];
								while(secondComparing.equals(""))
								{
									t++;
									secondComparing=parts[t];
								}	
							}
						}
					}
					if(!parts[k].matches(".*[a-zA-Z]+.*"))
					{

						for(int j=0; j<parts[k].length();j++)
						{
							if(parts[k].charAt(j) ==')')
							{	
								parts[i]=parts[i].replace(removeExcessBrackets(parts[i]), "");
								found=true;
								break;
							}
						}
						if(found==false && !parts[t].matches(".*[a-zA-Z]+.*"))
						{
							for(int j=0; j<parts[t].length();j++)
							{
								if(parts[t].charAt(j) ==')')
								{
									parts[i]=parts[i].replace(removeExcessBrackets(parts[i]), "");
									found=true;
									break;
								}
							}
						}
						
					}
				
				}
		}
		return parts;
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
	
	public String validateUnusefulFor(String newGrammar)
	{
		
		return newGrammar;
	}
	
	public void readUsedCommands(String pathUsedCommands, String id) {
		usedCommandsperGeneration.clear();
		List <String> usedCommands;

		File COMMFolder = new File(pathUsedCommands);
		if (COMMFolder != null) {

			for (File folder : COMMFolder.listFiles()) {
				File arq = new File(folder+"/LogsGrammars" + id + ".txt");
				if(arq.exists()) {
					try (BufferedReader br = new BufferedReader(new FileReader(folder+"/LogsGrammars" + id + ".txt"))) {
						String line;
						while ((line = br.readLine()) != null) {
							String parts[]=line.split(" ");

							if(usedCommandsperGeneration.containsKey(Integer.valueOf(parts[0])))
							{
								usedCommands=usedCommandsperGeneration.get(Integer.valueOf(parts[0]));
							}
							else
							{
								usedCommands=new ArrayList<String>();
								usedCommandsperGeneration.put(Integer.valueOf(parts[0]), usedCommands);
							}
							for(int i=1; i<parts.length;i++)
							{
								if(!usedCommands.contains(cleaned(parts[i])))
									usedCommands.add(cleaned(parts[i]));
							}				    	

						}
						File toDelete=new File(folder+"/LogsGrammars" + id + ".txt");
						toDelete.delete();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

			
	}


	private String cleaned(String command) {
		
		if(command.charAt(0) =='(')
		{
			command=command.replaceFirst("\\(", "");
		}
		
		while(command.charAt(command.length()-1)==')' && command.charAt(command.length()-2)==')')
		{
			command=command.substring(0, command.length() - 1);
		}
		return command;
	}


	/**
	 * @return the usedCommandsperGeneration
	 */
	public Map<Integer, List<String>> getUsedCommandsperGeneration() {
		return usedCommandsperGeneration;
	}


	/**
	 * @param usedCommandsperGeneration the usedCommandsperGeneration to set
	 */
	public void setUsedCommandsperGeneration(Map<Integer, List<String>> usedCommandsperGeneration) {
		this.usedCommandsperGeneration = usedCommandsperGeneration;
	}
	
	public String removeExcessBrackets(String part)
	{
		
		String tem=part;
		while(tem.charAt(0)=='(')
		{
			tem=tem.replaceFirst("\\(", "");
		}
		while(tem.charAt(tem.length()-1)==')' && tem.charAt(tem.length()-2)==')' && tem.contains("for"))
		{
			tem=tem.substring(0, tem.length() - 1);
		}
		while(tem.charAt(tem.length()-1)==')' && tem.charAt(tem.length()-2)==')' && tem.charAt(tem.length()-3)==')' && tem.contains("if"))
		{
			tem=tem.substring(0, tem.length() - 1);
		}
		return tem;
	}
	
	public String removePaddings(String part)
	{
		//For open brackets
		for(int i=0; i<part.length();i++)
		{
			int j=i+1;
			if(j<part.length())
			{
				
				while(part.charAt(i) =='(' && (part.charAt(j) ==' ' || part.charAt(j) =='#'))
				{
					part=changeCharInPosition(j,'#',part );
					j++;
					if(j==part.length())
					{
						break;
					}
				}				
			}

		}
		//For closed brackets
		for(int i=part.length()-1; i>0;i--)
		{
			int j=i-1;
			if(j>=0)
			{
				while(part.charAt(i) ==')' && (part.charAt(j) ==' ' || part.charAt(j) =='#'))
				{
					part=changeCharInPosition(j,'#',part );
					j--;
					if(j<0)
					{
						break;
					}
				}				
			}

		}		
		
		return part;
	}
	
	public String changeCharInPosition(int position, char ch, String str){
	    char[] charArray = str.toCharArray();
	    charArray[position] = ch;
	    return new String(charArray);
	}
	
	public String removeTrashBracketsFromString(String str)
	{

		String grammar=str;
		grammar=grammar.replace("?", "");
		grammar=grammar.replace("�", "");

		boolean atLeastOne=true;
		while(atLeastOne)
		{
		atLeastOne=false;
		for(int i=0;i<grammar.length();i++)
		{
			if(grammar.charAt(i)=='(')
			{
				for(int j=i+1;j<grammar.length();j++)
				{
					if(grammar.charAt(j)!=')' && grammar.charAt(j)!=' ')
					{
						break;
					}
					else if(grammar.charAt(j)==')')
					{
						grammar=changeCharInPosition(i, '?', grammar);
						grammar=changeCharInPosition(j, '?', grammar);
						atLeastOne=true;
						break;
					}
					
				}
			}
		}
		grammar=grammar.replace("?", "");
		
		grammar=removePaddings(grammar);
		grammar=grammar.replace("#", "");
		}
		
		
		grammar=grammar.replace("$", "i");
		grammar=grammar.replace("@", "f");
		return grammar;
	}
	
	public String removingRemainingElses(String str)
	{
		
		String grammar=str;
		String parts[]=grammar.split("\\s+");
		for(int i=0; i<parts.length;i++)
		{
			if(parts[i].matches(".*[a-zA-Z]+.*") )
			{
				for(int j=0;j<parts[i].length();j++)
				{
					if(Character.isLetter(parts[i].charAt(j)) && (!parts[i].contains("if") && !parts[i].contains("for")))
					{
						if(j>0)
						{
							int k=j-1;
							while(parts[i].charAt(k) =='(' )
							{
								if(i>1)
								{
									if((!parts[i-2].contains("if") && (!parts[i-1].contains("if") && !parts[i-1].contains("for"))) || k>0)
									{
										parts[i]=changeCharInPosition(k, '*', parts[i]);

									}
									k--;

									if(k<0)
										break;
								}
								else {
									if((i==0 || (i==1 && (!parts[i-1].contains("if") && !parts[i-1].contains("for")))) || k>0)
									{
										parts[i]=changeCharInPosition(k, '*', parts[i]);
									}
									k--;

									if(k<0)
										break;
								}
							}
						}
						break;
					}
				}
			}
		}
		
		grammar=recoverStringFromArray(parts);
		grammar=balancingParentes(grammar);
		grammar=grammar.replace("#", "");
		return grammar;
	}
	
	public String balancingParentes(String grammar)
	{
		while(grammar.contains("*"))
		{
//			System.out.println("gram "+grammar);
		boolean open=false;
		int countOpen=0;
		for(int i=0; i<grammar.length();i++)
		{
			if(grammar.charAt(i) =='*' && open==false)
			{	
			    open=true;
			    countOpen++;
				grammar=changeCharInPosition(i, '#', grammar);
			}
			else if(grammar.charAt(i) =='(' && open)
			{
				countOpen++;
			}
			else if(grammar.charAt(i) ==')' && open)
			{
				countOpen--;
				if(countOpen==0)
				{
					grammar=changeCharInPosition(i, '#', grammar);
					break;
				}
			}
		}
		}
		return grammar;
	}
	
	

}
