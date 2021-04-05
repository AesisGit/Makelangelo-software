package com.marginallyclever.makelangelo.nodes;

import java.security.SecureRandom;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorAngle;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;

/**
 * L System fractal
 * @author Dan Royer
 */
public class Generator_LSystemTree extends TurtleGenerator {
	// random seed
	private NodeConnectorInteger inputSeed = new NodeConnectorInteger(Translator.get("Generator_LSystemTree.inputSeed"),0xDEADBEEF);
	// resursion depth
	private NodeConnectorInteger inputOrder = new NodeConnectorBoundedInt(Translator.get("inputOrder.inputOrder"),10,1,4);
	// resursion width
	private NodeConnectorInteger inputBranches = new NodeConnectorBoundedInt(Translator.get("Generator_LSystemTree.inputBranches"),10,1,3);
	// variation
	private NodeConnectorInteger inputNoise = new NodeConnectorInteger(Translator.get("Generator_LSystemTree.inputNoise"),0);
	// how far branches can spread
	private NodeConnectorDouble inputAngleSpan = new NodeConnectorAngle(Translator.get("Generator_LSystemTree.inputAngleSpan"),120.0);
	// how far branches can spread
	private NodeConnectorDouble inputOrderScale = new NodeConnectorDouble(Translator.get("Generator_LSystemTree.inputOrderScale"),0.76);
	
	private int order;
	private int numBranches;
	private int noise;
	private double angleSpan;
	private double orderScale;
	private SecureRandom random;
	
	public Generator_LSystemTree() {
		super();
		inputs.add(inputSeed		);
		inputs.add(inputOrder		);
		inputs.add(inputBranches	);
		inputs.add(inputNoise		);
		inputs.add(inputAngleSpan	);
		inputs.add(inputOrderScale	);
		inputSeed		.setDescription(Translator.get("Generator_LSystemTree.inputSeed.tooltip"));
		inputOrder		.setDescription(Translator.get("inputOrder.inputOrder.tooltip"));
		inputBranches	.setDescription(Translator.get("Generator_LSystemTree.inputBranches.tooltip"));
		inputNoise		.setDescription(Translator.get("Generator_LSystemTree.inputNoise.tooltip"));
		inputAngleSpan	.setDescription(Translator.get("Generator_LSystemTree.inputAngleSpan.tooltip"));
		inputOrderScale	.setDescription(Translator.get("Generator_LSystemTree.inputOrderScale.tooltip"));
	}

	@Override
	public String getName() {
		return Translator.get("Generator_LSystemTree.name");
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		random = new SecureRandom();
		random.setSeed(inputSeed.getValue());
		
		order = inputOrder.getValue();
		numBranches = inputBranches.getValue();
		noise = inputNoise.getValue();
		angleSpan = inputAngleSpan.getValue();
		orderScale = inputOrderScale.getValue();
				
		// move to starting position
		turtle.moveTo(0,-100);
		turtle.turn(90);
		turtle.penDown();
		// do the curve
		lSystemTree(turtle,order, 10);

		outputTurtle.setValue(turtle);
	    return false;
	}


	// recursive L System tree fractal
	private void lSystemTree(Turtle turtle,int n, double distance) {
		if (n == 0) return;

		turtle.forward(distance);
		if(n>1) {
			double angleStep = angleSpan / (float)(numBranches-1);

			double oldAngle = turtle.getAngle();
			double len = distance*orderScale;
			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(turtle,n-1,len - len*random.nextDouble()*(noise/100.0f) );
				if(noise>0) {
					turtle.turn(angleStep + (random.nextDouble()-0.5)*(noise/100.0f)*angleStep);
				} else {
					turtle.turn(angleStep);
				}
			}
			turtle.setAngle(oldAngle);
		}
		turtle.forward(-distance);
	}


	public void setOrder(int value) {
		order=value;	
	}
	public int getOrder() {
		return order;
	}

	public void setScale(double value) {
		orderScale = value;
	}
	public double getScale() {
		return orderScale;
	}

	public void setAngle(double value) {
		angleSpan = value;
	}
	public double getAngle() {
		return angleSpan;
	}

	public void setBranches(int value) {
		numBranches = value;
	}
	public int getBranches() {
		return numBranches;
	}

	public void setNoise(int value) {
		noise = value;		
	}

	public int getNoise() {
		return noise;		
	}
}
