package astlatcher;


public abstract class IMPastNodeSimplify extends AbstractIMPastNode{

	
	public IMPastNodeSimplify(IMPastNode parent) {
		super(parent);
	}

	/**returns true if node does not contain an non field data reference
	 * 
	 * @return
	 */
	abstract public boolean isSimple();
	


	abstract public void simplify( IMPastNode exprTopParent );
}
