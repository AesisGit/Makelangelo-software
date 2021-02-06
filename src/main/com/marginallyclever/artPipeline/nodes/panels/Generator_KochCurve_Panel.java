package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.Generator_HilbertCurve;
import com.marginallyclever.artPipeline.nodes.Generator_KochCurve;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_KochCurve}
 * @author Dan Royer
 *
 */
public class Generator_KochCurve_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_KochCurve generator;
	
	public Generator_KochCurve_Panel(Generator_KochCurve generator) {
		super();
		
		this.generator = generator;

		add(fieldOrder = new SelectSlider(Translator.get("HilbertCurveOrder"),7,1,Generator_HilbertCurve.getOrder()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Koch_curve'>Learn more</a>"));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		Generator_KochCurve.setOrder(fieldOrder.getValue());
		generator.restart();
	}
}
