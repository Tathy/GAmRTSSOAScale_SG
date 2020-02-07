package ga.ScriptTableGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.ScriptsGenerator.TableGenerator.FunctionsforGrammar;
import ga.config.ConfigurationsGA;

public class Sketch {
	static Random rand = new Random();
	private FunctionsforGrammar functions;
	ScriptsTable st=new ScriptsTable();
	public Sketch()
	{
		functions=new FunctionsforGrammar();
	}
	public String sketchA(String genotypeScript,int numberComponentsAdded) {
		//basic function
		if(rand.nextInt(2)>0 || numberComponentsAdded>=ConfigurationsGA.MAX_QTD_COMPONENTS)
		{
			genotypeScript=genotypeScript+st.returnBasicFunction(false);

		}
		else
		{
			genotypeScript=genotypeScript+st.returnBasicFunction(false);
			genotypeScript=sketchA(genotypeScript,numberComponentsAdded+1);
		}
		return genotypeScript;
	}
	
	public String sketchB(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;
		boolean isOpenFor=false;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		do
		{

			//basic function
			int coin=rand.nextInt(2);
			if(coin==0)
			{
				genotypeScript=genotypeScript+st.returnBasicFunction(isOpenFor);
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
			else if(coin==1 && collectionofIfs.size()==0)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+st.returnConditional(isOpenFor);
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



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				//int coinOpenClose=;
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
				{
					genotypeScript=genotypeScript.substring(0, genotypeScript.length() - 1);
					genotypeScript=genotypeScript+") ";
					collectionofIfs.get(collectionofIfs.size()-1).setLastOpen(false);
					
					
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
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
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

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1);
		
		//ensure close open parenthesis

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

		//

		return genotypeScript.trim();
	}
	
	public int counterIfsOpen(List<itemIf> collectionofIfs)
	{
		//List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int counterOpensIfs=0;
		for(itemIf item: collectionofIfs)
		{
			if(item.getType().equals("if"))
			{
				counterOpensIfs++;
			}
		}
		//System.out.println("counter "+counterOpensIfs);
		return counterOpensIfs;
	}
	
	public String sketchC(String genotypeScript,int numberComponentsAdded) {
		
		boolean canCloseParenthesisIf=false;
		boolean canOpenParenthesisIf=false;

		List<itemIf> collectionofIfs= new ArrayList<itemIf>();
		int continueCoin=0;
		boolean isOpenFor=false;
		do
		{
			int coin=rand.nextInt(3);
			//for
			if(coin==0 && isOpenFor==false && collectionofIfs.size()==0)
			{
				collectionofIfs.add(new itemIf(0,true,"for"));
				genotypeScript=genotypeScript+st.returnForFunction();
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
			
			else if(coin==1)
			{
				genotypeScript=genotypeScript+st.returnBasicFunction(isOpenFor);
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
			
			else if(coin==2 && counterIfsOpen(collectionofIfs)==0)
			{
				collectionofIfs.add(new itemIf(1,true,"if"));
				genotypeScript=genotypeScript+st.returnConditional(isOpenFor);
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



			//close parenthesis if
			if(collectionofIfs.size()>0)
			{
				int coinOpenClose=rand.nextInt(2);
				//close parenthesis if
				if(rand.nextInt(2)==0  && canCloseParenthesisIf && collectionofIfs.get(collectionofIfs.size()-1).isLastOpen())
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
				
				else if(rand.nextInt(2)==0 && canOpenParenthesisIf==true && collectionofIfs.get(collectionofIfs.size()-1).getMaxOpens()>0 && !collectionofIfs.get(collectionofIfs.size()-1).isLastOpen() )
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

			continueCoin=rand.nextInt(2);
		}while(collectionofIfs.size()>0 || continueCoin==1);
		
		//ensure close open parenthesis

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

		//

		return genotypeScript.trim();
	}
}
