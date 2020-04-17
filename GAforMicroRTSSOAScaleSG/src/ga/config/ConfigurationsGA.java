package ga.config;

public final class ConfigurationsGA {
	public static final int SIZE_CHROMOSOME = 1;
	//public static final int SIZE_POPULATION = 25;//
	public static final int NUMBER_JOBS = 39;
	//public static final int SIZE_ELITE = 2;//
	//public static final int SIZE_INVADERS = 2;//
	//public static final int K_TOURNMENT = 6; //
	//public static final int SIZE_PARENTSFORCROSSOVER = 10;//
	public static final double MUTATION_RATE = 0.3;
	public static final double MUTATION_RATE_RULE = 0.3;
	public static final double MUTATION_ORDER_RATE = 0.1;
	public static final boolean INCREASING_INDEX = false;
	public static final double INCREASING_RATE = 0.2;
	public static final double DECREASING_RATE = 0.2;
	public static final int QTD_ENEMIES_SAMPLE_RANDOM = 3; //
	public static final int QTD_ENEMIES_SAMPLE_ELITE = 10; //
	public static final int QTD_RULES = 60088;
	public static final int SIZE_CHROMOSOME_SCRIPT = 10;
	//public static final int SIZE_TABLE_SCRIPTS = 1000;
	public static final int TYPE_CONTROL = 1;
	public static final int TIME_GA_EXEC = 13;
	public static final int deltaForMutation = 1;
	public static final boolean RESET_ENABLED = true;
	public static final boolean MUTATION_ORDER_ENABLED = true;
	public static final boolean curriculum = false;
	public final static boolean UCB1=false;
	
	public static final int QTD_GENERATIONS = 100;
	
	// Configurações da População 2
	public static final int SIZE_TABLE_SCRIPTS_2 = 200;
	public static final int SIZE_POPULATION_2 = 20;
	public static final int SIZE_PARENTSFORCROSSOVER_2 = 8;
	public static final int K_TOURNMENT_2 = 5;
	public static final int SIZE_ELITE_2 = 2;
	public static final int SIZE_INVADERS_2 = 2;
	
	// Configurações da População 1
	public static final int SIZE_TABLE_SCRIPTS = 500;
	public static final int SIZE_POPULATION = 50;
	public static final int SIZE_PARENTSFORCROSSOVER = 20;
	public static final int K_TOURNMENT = 13;
	public static final int SIZE_ELITE = 5;
	public static final int SIZE_INVADERS = 5;
	
    public static final int QTD_RULES_CONDITIONAL=115;
    public static final int QTD_RULES_BASIC_FUNCTIONS=518;
    public static final int MAX_QTD_COMPONENTS=10;
    public static boolean recoverTable=false;
    
    public final static boolean removeRules = true;
    public static final boolean evolvingScript = true;
	
    public static final boolean sketch=true;
    public static final String idSketch="C";
	
}
