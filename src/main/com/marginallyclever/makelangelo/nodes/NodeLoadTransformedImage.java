package com.marginallyclever.makelangelo.nodes;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorExistingFile;

public class NodeLoadTransformedImage extends Node {
	private NodeConnectorExistingFile inputFile;
	
	public NodeLoadTransformedImage() {
		super();
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
		inputFile = new NodeConnectorExistingFile("NodeLoadImage.inputFile",filter,"");
		
		inputs.add(inputFile);
		inputFile.setDescription(Translator.get("NodeLoadImage.inputFile.tooltip"));
	}
	
	@Override
	public String getName() {
		return Translator.get("NodeLoadTransformedImage.name");
	}

}
