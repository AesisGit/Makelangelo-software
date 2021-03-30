package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.Histogram;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.Translator;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;

/**
 * generate crosshatch pattern over an image, with more lines in darker areas.
 * @author Dan Royer
 */
public class Converter_Crosshatch extends ImageConverter {
	// detail of scan.  lower number is more detail.  >0
	private NodeConnectorDouble inputStepSize = new NodeConnectorDouble("Converter_Crosshatch.inputStepSize",2.0);
	
	public Converter_Crosshatch() {
		super();
		inputs.add(inputStepSize);

		inputStepSize.setDescription(Translator.get("Converter_Crosshatch.inputStepSize.tooltip"));
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_Crosshatch.name");
	}

	@Override
	public boolean iterate() {
		outputTurtle.setValue(finish1());
		// outputTurtle.setValue(finish2());
		
		return false;
	}
	
	public Turtle finish2() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		Turtle turtle = new Turtle();
		
		finishPass(turtle,new int[]{15* 2,15* 4},0  ,img);
		finishPass(turtle,new int[]{15* 6,15* 8},90 ,img);
		finishPass(turtle,new int[]{15*10,15*12},45 ,img);
		finishPass(turtle,new int[]{15*14,15*15},135,img);
		
		return turtle;
	}
	
	protected void finishPass(Turtle turtle,int [] passes,double angleDeg,TransformedImage img) {
		double dx = Math.cos(Math.toRadians(angleDeg));
		double dy = Math.sin(Math.toRadians(angleDeg));

		// figure out how many lines we're going to have on this image.
		double stepSize = 2.0;
		if (stepSize < 1) stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).

		// from top to bottom of the margin area...
		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		double height = yTop - yBottom;
		double width = xRight - xLeft;
		double maxLen = Math.sqrt(width*width+height*height);
		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];

		boolean useError=false;
		
		int i=0;
		for(double a = -maxLen;a<maxLen;a+=stepSize) {
			double px = dx * a;
			double py = dy * a;
			double x0 = px - dy * maxLen;
			double y0 = py + dx * maxLen;
			double x1 = px + dy * maxLen;
			double y1 = py - dx * maxLen;
		
			double l2 = passes[(i % passes.length)];
			if ((i % 2) == 0) {
				if(!useError) convertAlongLine(turtle,x0, y0, x1, y1, stepSize, l2, img);
				else convertAlongLineErrorTerms(turtle,x0,y0,x1,y1,stepSize,l2,error0,error1,img);
			} else {
				if(!useError) convertAlongLine(turtle,x1, y1, x0, y0, stepSize, l2, img);
				else convertAlongLineErrorTerms(turtle,x1,y1,x0,y0,stepSize,l2,error0,error1,img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
		}
	}
	
	protected Turtle finish1() {
		if(inputImage==null) return null;
		
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		Turtle turtle = new Turtle();
		
		// if the image were projected on the paper, where would the top left
		// corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		double [] bounds = img.getBounds();		
		double yStart = bounds[TransformedImage.BOTTOM];
		double yEnd   = bounds[TransformedImage.TOP];
		double xStart = bounds[TransformedImage.LEFT];
		double xEnd   = bounds[TransformedImage.RIGHT];

		double stepSize = 2.0 * inputStepSize.getValue();
		double x, y;
		boolean flip = true;

		x = xEnd-xStart;
		y = yEnd-yStart;
		
		int maxLen = (int)Math.sqrt(x*x+y*y);
		
		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];
		Histogram hist = new Histogram();
		
		hist.getGreyHistogramOf(img.getSourceImage());
		//*
		double [] levels = hist.getLevelsMapped( new double[] { 192.0/255.0, 128.0/255.0, 64.0/255.0, 32.0/255.0 } );
		/*/
		double [] levels = {
			4*Math.pow(2,5),
			4*Math.pow(2,4),
			4*Math.pow(2,3),
			4*Math.pow(2,2),
		};//*/
		
		boolean useError=false;
		// vertical
		double level = levels[0];

		for (y = yStart; y <= yEnd; y += stepSize) {
			if (flip) {
				if(!useError) convertAlongLine(turtle,xStart, y, xEnd, y, stepSize, level,img);
				else convertAlongLineErrorTerms(turtle,xStart, y, xEnd, y, stepSize, level,error0,error1, img);
			} else {
				if(!useError) convertAlongLine(turtle,xEnd, y, xStart, y, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,xEnd, y, xStart, y, stepSize, level,error0,error1, img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			flip = !flip;
		}

		level = levels[1];
		for(int j=0;j<error0.length;++j) {
			error0[j]=error1[j]=0;
		}
		
		// horizontal
		for (x = xStart; x <= xEnd; x += stepSize) {
			if (flip) {
				if(!useError) convertAlongLine(turtle,x, yStart, x, yEnd, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x, yStart, x, yEnd, stepSize, level,error0,error1, img);
			} else {
				if(!useError) convertAlongLine(turtle,x, yEnd, x, yStart, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x, yEnd, x, yStart, stepSize, level,error0,error1, img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			flip = !flip;
		}

		level = levels[2];
		for(int j=0;j<error0.length;++j) {
			error0[j]=error1[j]=0;
		}


		// diagonal 1
		double dy = yEnd - yStart;
		double dx = xEnd - xStart;
		double len = dx > dy ? dx : dy;

		double x1 = -len;
		double y1 = -len;

		double x2 = +len;
		double y2 = +len;

		double len2 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double steps;
		if (len2 > 0)
			steps = len2 / stepSize;
		else
			steps = 1;
		double i;

		for (i = 0; i < steps; ++i) {
			double px = x1 + (x2 - x1) * (i / steps);
			double py = y1 + (y2 - y1) * (i / steps);

			double x3 = px - len;
			double y3 = py + len;
			double x4 = px + len;
			double y4 = py - len;

			if (flip) {
				if(!useError) convertAlongLine(turtle,x3, y3, x4, y4, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x3, y3, x4, y4, stepSize, level,error0,error1, img);
			} else {
				if(!useError) convertAlongLine(turtle,x4, y4, x3, y3, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x4, y4, x3, y3, stepSize, level,error0,error1, img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			flip = !flip;
		}

		level = levels[3];
		for(int j=0;j<error0.length;++j) {
			error0[j]=error1[j]=0;
		}

		// diagonal 2

		x1 = +len;
		y1 = -len;

		x2 = -len;
		y2 = +len;

		for (i = 0; i < steps; ++i) {
			double px = x1 + (x2 - x1) * (i / steps);
			double py = y1 + (y2 - y1) * (i / steps);

			double x3 = px + len;
			double y3 = py + len;
			double x4 = px - len;
			double y4 = py - len;

			if (flip) {
				if(!useError) convertAlongLine(turtle,x3, y3, x4, y4, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x3, y3, x4, y4, stepSize, level,error0,error1, img);
			} else {
				if(!useError) convertAlongLine(turtle,x4, y4, x3, y3, stepSize, level, img);
				else convertAlongLineErrorTerms(turtle,x4, y4, x3, y3, stepSize, level,error0,error1, img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			flip = !flip;
		}
		
		return turtle;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
