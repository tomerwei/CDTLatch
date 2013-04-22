package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPliteralNode extends IMPidNode {

	public IMPliteralNode(IMPastNode parent) 
	{
		super(parent);	
	}
	
	@Override
	public void initNode( IASTNode node ) 
	{				
		this.name = "Literal{" + ((IASTLiteralExpression) node).getRawSignature() + "}";		
	}
	
	
	public void initNode( String varName ) 
	{
		this.name = varName;	
	}	
}
