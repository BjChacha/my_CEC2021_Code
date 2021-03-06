package etmo.metaheuristics.nsgaII;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.HashMap;

import etmo.core.*;
import etmo.operators.crossover.CrossoverFactory;
import etmo.operators.mutation.MutationFactory;
import etmo.operators.selection.SelectionFactory;
import etmo.problems.benchmarks_ETMO.*;
import etmo.qualityIndicator.QualityIndicator;
import etmo.util.JMException;
import etmo.util.Ranking;

public class NSGAII_main {
	public static void main(String args[]) throws IOException, JMException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		ProblemSet problemSet; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection;

		HashMap parameters; // Operator parameters

		int taskStart = 25;
		int taskEnd = 32;

		int times = 21;
		
		DecimalFormat form = new DecimalFormat("#.####E0");

		for (int pCase = taskStart; pCase <= taskEnd; pCase++) {
			problemSet = (ProblemSet) Class
					.forName("etmo.problems.benchmarks_ETMO.ETMOF" + pCase)
					.getMethod("getProblem")
					.invoke(null, null);

			int taskNum = problemSet.size();

			String[] pf = new String[taskNum];
			double[] ave = new double[taskNum];

			String pSName = problemSet.get(0).getName();
			pSName = pSName.substring(0, pSName.length() - 2);
			System.out.println(pSName + "\ttaskNum = " + taskNum + "\tfor " + times + " times.");

			for (int tsk = 0; tsk < taskNum; tsk++) {
				ProblemSet pS = problemSet.getTask(tsk);
				pf[tsk] = "PF/StaticPF/" + problemSet.get(tsk).getHType() + "_" + problemSet.get(tsk).getNumberOfObjectives() + "D.pf";
				for (int t = 1; t <= times; t++) {
					algorithm = new NSGAII(pS);
					algorithm.setInputParameter("populationSize", 100);
					algorithm.setInputParameter("maxEvaluations", 100 * 1000);

					parameters = new HashMap();
					parameters.put("probability", 0.9);
					parameters.put("distributionIndex", 20.0);
					crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

					// Mutation operator
					parameters = new HashMap();
					parameters.put("probability", 1.0 / pS.getMaxDimension());
					parameters.put("distributionIndex", 20.0);
					mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

					// Selection Operator
					parameters = null;
					selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

					// Add the operators to the algorithm
					algorithm.addOperator("crossover", crossover);
					algorithm.addOperator("mutation", mutation);
					algorithm.addOperator("selection", selection);

					QualityIndicator indicator = new QualityIndicator(pS.get(0), pf[tsk]);

					SolutionSet population = algorithm.execute();
					Ranking ranking = new Ranking(population);
					population = ranking.getSubfront(0);
					population.printObjectivesToFile("NSGAII_" + pS.get(0).getNumberOfObjectives() + "Obj_" +
							pS.get(0).getName() + "_" + pS.get(0).getNumberOfVariables() + "D_run" + t + ".txt");
					double igd = indicator.getIGD(population);
					ave[tsk] += igd;
				}
				System.out.println("T" + (tsk + 1) + "\t" + form.format(ave[tsk] / times));
			}
			System.out.println();
		}
	}	

}
