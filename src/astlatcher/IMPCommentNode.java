package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPCommentNode extends AbstractIMPastNode {

	public IMPCommentNode(IMPastNode parent) 
	{
		super(parent);
	}


	private String comment;
	
	
	public String commentGet()
	{
		return comment;
	}
	
	@Override
	public void initNode( IASTNode node )
	{
		this.comment = "#{" + node.getRawSignature() + "}";		
	}
	
	/** initializes a command node with lhs := rhs
	 * @param lhs
	 * @param rhs
	 */	
	public void initNode( String lhs, String rhs )
	{
		this.comment = "#{" + lhs + " := " + rhs + "}";		
	}
	
	public void initNode( IMPastNode node )
	{
		this.comment = "#{" + node.toString() + "}";		
	}

	
	@Override
	public String toString()
	{
		return comment;
		//return "";
	}
	
	public String prettyPrint( int indent ) {
		
		return ASTBuilder.indentTabGet( indent ) + toString() + "\n";				
	}
	
	public String prettyPrintAST( int indent ) {
		
		return ASTBuilder.indentTabGet( indent ) + toString() + "\n";				
	}	
}
