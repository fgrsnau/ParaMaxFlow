package com.fgrsnau.diversity;

import com.github.fgrsnau.maxflow.GraphCut;
import weka.Run;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DiversitySolver {

	protected class UnaryTerm {

		public final int node;
		public final double e1;

		public UnaryTerm(int node, double e1) {
			this.node = node;
			this.e1 = e1;
		}

	}

	protected class PairwiseTerm {

		public final int i;
		public final int j;
		public final double e00;
		public final double e01;
		public final double e10;
		public final double e11;

		public PairwiseTerm(int i, int j, double e00, double e01, double e10, double e11) {
			this.i = i;
			this.j = j;
			this.e00 = e00;
			this.e01 = e01;
			this.e10 = e10;
			this.e11 = e11;
		}

	}

	protected final List<UnaryTerm> unaryTerms;
	protected final List<PairwiseTerm> pairwiseTerms;

	public DiversitySolver() {
		unaryTerms = new ArrayList<>();
		pairwiseTerms = new ArrayList<>();
	}

	public int addNode(int num) {
		int index = unaryTerms.size();

		for (int i = 0; i < num; ++i) {
			unaryTerms.add(new UnaryTerm(i, 0));
		}

		return index;
	}

	public void addUnaryTerm(int i, double e1) {
		unaryTerms.set(i, new UnaryTerm(i, e1));
	}

	public void addPairwiseTerm(int i, int j, double e00, double e01, double e10, double e11) {
		pairwiseTerms.add(new PairwiseTerm(i, j, e00, e01, e10, e11));
	}

	public List<boolean[]> solve(int numberOfCuts, double lambda) {
		List<Future<boolean[]>> futures = new ArrayList<>();

		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		try (GraphCut graphCut = new GraphCut(unaryTerms.size(), pairwiseTerms.size())) {
			for (int i = 1; i <= numberOfCuts; ++i) {
				// TODO: Should current_parameter get negated?
				final double current_parameter = 2 * lambda * (2 * i - numberOfCuts - 1);

				Future<boolean[]> future = threadPool.submit(new Callable<boolean[]>() {
					public boolean[] call() {
						try (GraphCut graphCut = buildGraphCut(current_parameter)) {
							return graphCut.solve();
						}
					}
				});

				futures.add(future);
			}
		}

		threadPool.shutdown();
		List<boolean[]> solutions = new ArrayList<>();
		for (Future<boolean[]> f : futures) {
			try {
				solutions.add(f.get());

				int count0 = 0;
				int count1 = 0;
				for (boolean x : f.get()) {
					count0 += x ? 0 : 1;
					count1 += x ? 1 : 0;
				}
				System.out.print("count0 = ");
				System.out.print(count0);
				System.out.print(" / count1 = ");
				System.out.println(count1);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		return solutions;
	}

	private GraphCut buildGraphCut(double parameter) {
		GraphCut graphCut = new GraphCut(unaryTerms.size(), pairwiseTerms.size());
		graphCut.addNode(unaryTerms.size());

		for (UnaryTerm u : unaryTerms) {
			graphCut.addUnaryTerm(u.node, 0, u.e1 + parameter);
		}

		for (PairwiseTerm pw : pairwiseTerms) {
			graphCut.addPairwiseTerm(pw.i, pw.j, pw.e00, pw.e01, pw.e10, pw.e11);
		}

		return graphCut;
	}

}
