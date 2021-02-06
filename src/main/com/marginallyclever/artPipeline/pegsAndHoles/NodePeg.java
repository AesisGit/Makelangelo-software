package com.marginallyclever.artPipeline.pegsAndHoles;

/**
 * {@code NodePeg<T>} connect to {@code NodeHole<T>}.  Many-to-one relationship.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodePeg<T> extends NodePegAbstract {
	public String name;
	public T defaultValue;
}
