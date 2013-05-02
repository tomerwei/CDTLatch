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
		String literalStr = ((IASTLiteralExpression) node).getRawSignature();
		
		if( literalStr.equalsIgnoreCase( ASTBuilder.nullStrGet() ) )
		{
			this.name = ASTBuilder.nullStrGet();
		}
		else
		{
			this.name = "Literal{" + literalStr + "}";
		}
	}
	
	
	public void initNode( String varName ) 
	{
		this.name = varName;	
	}	
}
