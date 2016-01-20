package com.fgrsnau.diversity;

import net.imglib2.paramaxflow.Parametric;

import java.util.List;

public class ParametricDiversityWrapper {

	protected final DiversitySolver wrapped;
	protected final double lambda;

	public ParametricDiversityWrapper(int nodeNumMax, int edgeNumMax) {
		this(nodeNumMax, edgeNumMax, 0.0d);
	}

	public ParametricDiversityWrapper(int nodeNumMax, int edgeNumMax, double lambda) {
		this.wrapped = new DiversitySolver();
		this.lambda = lambda;
	}

	public int addNode(int num) {
		return wrapped.addNode(num);
	}

	public void addUnaryTerm(int i, double a, double b) {
		wrapped.addUnaryTerm(i, a * lambda + b);
	}

	public void addPairwiseTerm(int i, int j, double e00, double e01, double e10, double e11) {
		wrapped.addPairwiseTerm(i, j, e00, e01, e10, e11);
	}

	public List<boolean[]> solve(int numberOfCuts, double lambda) {
		return wrapped.solve(numberOfCuts, lambda);
	}

}
