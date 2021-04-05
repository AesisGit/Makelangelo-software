package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.Translator;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.node.NodeConnectorOneOfMany;
import com.marginallyclever.core.turtle.Turtle;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Sandy extends ImageConverter {
	private String [] centerChoices = new String[] { 
		Translator.get("Converter_Sandy.inputCenter.topRight"),
		Translator.get("Converter_Sandy.inputCenter.topLeft"), 
		Translator.get("Converter_Sandy.inputCenter.bottomLeft"), 
		Translator.get("Converter_Sandy.inputCenter.bottomRight"), 
		Translator.get("Converter_Sandy.inputCenter.center")
	};

	private NodeConnectorInteger inputBlockScale = new NodeConnectorInteger(Translator.get("Converter_Sandy.inputBlockScale"),150);
	private NodeConnectorOneOfMany inputCenter = new NodeConnectorOneOfMany(Translator.get("Converter_Sandy.inputCenter"),centerChoices,0);
	// only consider intensity above the low pass value.
	protected NodeConnectorBoundedInt inputLowPass = new NodeConnectorBoundedInt(Translator.get("ImageConverter.inputLowPass"),255,0,0);
	// only consider intensity below the high pass value.
	protected NodeConnectorBoundedInt inputHighPass = new NodeConnectorBoundedInt(Translator.get("ImageConverter.inputHighPass"),255,0,255);
	
	public Converter_Sandy() {
		super();
		inputs.add(inputBlockScale);
		inputs.add(inputCenter);
		inputs.add(inputLowPass);
		inputs.add(inputHighPass);
		inputBlockScale.setDescription(Translator.get("Converter_Sandy.inputBlockScale.tooltip"));
		inputCenter.setDescription(Translator.get("Converter_Sandy.inputCenter.tooltip"));
		inputLowPass.setDescription(Translator.get("ImageConverter.inputLowPass.tooltip"));
		inputHighPass.setDescription(Translator.get("ImageConverter.inputHighPass.tooltip"));
	}
		
	@Override
	public String getName() {
		return Translator.get("Converter_Sandy.name");
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];

		double pBottom = bounds[TransformedImage.BOTTOM]+1;
		double pTop    = bounds[TransformedImage.TOP]   -1;
		double pLeft   = bounds[TransformedImage.LEFT]  +1;
		double pRight  = bounds[TransformedImage.RIGHT] -1;
		
		double cx,cy;
		double last_x=0,last_y=0;

		boolean wasDrawing=false;
		
		switch(inputCenter.getValue()) {
		case 0:		cx = xRight;	cy = yTop;		last_x = pRight; 	last_y = pTop;		break;
		case 1:		cx = xLeft;		cy = yTop;		last_x = pLeft; 	last_y = pTop;		break;
		case 2:		cx = xLeft;		cy = yBottom;	last_x = pLeft; 	last_y = pBottom;	break;
		case 3:		cx = xRight;	cy = yBottom;	last_x = pRight; 	last_y = pBottom;	break;
		default:	cx = 0;			cy = 0;			last_x = 0;      	last_y = 0;			break;
		}

		double blockScale = inputBlockScale.getValue();
		double lowPass = inputLowPass.getValue(); 
		double highPass = inputHighPass.getValue();
		
		double x, y, z, scaleZ;

		double dx = xRight - xLeft; 
		double dy = yTop - yBottom;
		double rMax = Math.sqrt(dx*dx+dy*dy);
		double rMin = 0;

		double radius = 1.0;
		double rStep = (rMax-rMin)/(double)blockScale;
		double r;
		double t_dir=1;
		double pulseFlip=1;
		double t,t_step;
		double flipSum;
		double pulseSize = rStep*0.5 - radius;//r_step * 0.6 * scale_z;

		turtle = new Turtle();
		turtle.lock();
		Log.message("Sandy started.");
		//Thread.dumpStack();
		
		try {
			// make concentric circles that get bigger and bigger.
			for(r=rMin;r<rMax;r+=rStep) {
				// go around in a circle
				t=0;
				t_step = radius/r;
				flipSum=0;
				// go around the circle
				for(t=0;t<Math.PI*2;t+=t_step) {
					dx = Math.cos(t_dir *t);
					dy = Math.sin(t_dir *t);
					x = cx + dx * r;
					y = cy + dy * r;
					if(x<xLeft || x >=xRight || y <yBottom || y>=yTop) {
						if(wasDrawing) {
							turtle.jumpTo(last_x,last_y);
							wasDrawing=false;
						}
						continue;
					}
	
					last_x=x;
					last_y=y;
					// read a block of the image and find the average intensity in this block
					z = img.sample( x-pulseSize/2.0, y-pulseSize/2.0,x+pulseSize/2.0,y +pulseSize/2.0 );
					// low & high pass
					z = Math.max(lowPass,z);
					z = Math.min(highPass,z);
					// invert
					z = 255.0-z;
					// scale to 0...1
					scaleZ= (z-lowPass) / (highPass-lowPass);
	
					if(wasDrawing == false) {
						turtle.jumpTo(last_x,last_y);
						wasDrawing=true;
					}
	
					turtle.moveTo(	x + dx * pulseSize*pulseFlip,
									y + dy * pulseSize*pulseFlip);
					
					flipSum+=scaleZ;
					if(flipSum >= 1) {
						flipSum-=1;
						pulseFlip = -pulseFlip;
						turtle.moveTo(	x + dx * pulseSize*pulseFlip,
										y + dy * pulseSize*pulseFlip);
					}
				}
				t_dir=-t_dir;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			turtle.unlock();
			Log.message("Sandy finished.");
		}

		outputTurtle.setValue(turtle);
		return false;
	}
}


/**
 * This file is part of Makelangelo.
 *
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */