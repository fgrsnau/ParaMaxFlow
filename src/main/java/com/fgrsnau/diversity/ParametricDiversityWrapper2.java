package com.fgrsnau.diversity;

import net.imglib2.paramaxflow.Parametric;
import net.imglib2.paramaxflow.Region;

import java.util.ArrayList;
import java.util.List;

public class ParametricDiversityWrapper2 {

	protected class UnaryTerm {

		public final int node;
		public final double a;
		public final double b;

		public UnaryTerm(int node, double a, double b) {
			this.node = node;
			this.a = a;
			this.b = b;
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

	protected final Parametric parametric;
	protected final int nodeNumMax;
	protected final int edgeNumMax;
	protected int nodeNum;
	protected final List<UnaryTerm> unaryTerms;
	protected final List<PairwiseTerm> pairwiseTerms;

	public ParametricDiversityWrapper2(int nodeNumMax, int edgeNumMax) {
		this.nodeNum = 0;
		this.parametric = new Parametric(nodeNumMax, edgeNumMax);
		this.nodeNumMax = nodeNumMax;
		this.edgeNumMax = edgeNumMax;
		this.unaryTerms = new ArrayList<>();
		this.pairwiseTerms = new ArrayList<>();
	}

	public int addNode(int num) {
		int node =  parametric.addNode(num);
		nodeNum = node + num;
		return node;
	}

	public void addUnaryTerm(int i, double a, double b) {
		unaryTerms.add(new UnaryTerm(i, a, b));
		parametric.addUnaryTerm(i, a, b);
	}

	public void addPairwiseTerm(int i, int j, double e00, double e01, double e10, double e11) {
		pairwiseTerms.add(new PairwiseTerm(i, j, e00, e01, e10, e11));
		parametric.addPairwiseTerm(i, j, e00, e01, e10, e11);
	}

	public int solve(double lambda1, double lambda2) {
		return solve(lambda1, lambda2, 101);
	}

	public int solve(double lambda1, double lambda2, int numberOfCuts) {
		int regionNum = parametric.solve(lambda1, lambda2);
		int count = 0;

		Region first = parametric.getFirstRegion();
		Region last = parametric.getLastRegion();

		for (Region r = first; !last.equals(r); r = parametric.getNextRegion(r)) {
			double lambda = parametric.getRegionLambda(r);
			System.out.print("region lambda ");
			System.out.print(count);
			System.out.print(" = ");
			System.out.println(lambda);

			ParametricDiversityWrapper solver = buildModel(lambda);
			solver.solve(numberOfCuts, 0.005);

			++count;
		}

		return count;
	}

	private ParametricDiversityWrapper buildModel(double lambda) {
		ParametricDiversityWrapper result = new ParametricDiversityWrapper(nodeNumMax, edgeNumMax, lambda);
		result.addNode(nodeNum);

		for (UnaryTerm pot : unaryTerms) {
			result.addUnaryTerm(pot.node, pot.a, pot.b);
		}

		for (PairwiseTerm pot : pairwiseTerms) {
			result.addPairwiseTerm(pot.i, pot.j, pot.e00, pot.e01, pot.e10, pot.e11);
		}

		return result;
	}

}