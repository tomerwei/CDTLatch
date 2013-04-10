package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPidNode extends IMPastNodeSimplify{

	String name;
	
	public IMPidNode( IMPastNode parent ) 
	{
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initNode( IASTNode node ) 
	{				
		this.name = "'" + ( (IASTIdExpression)node ).getName().toString() + "'";		
	}
	
	public void initNode( String varName ) 
	{
		this.name = "'" + varName + "'";
	}	
	
	
	@Override 
	public String toString() 
	{
		return name;
	}

	@Override
	public boolean isSimple() 
	{
		return true;
	}

	@Override
	public void simplify( IMPastNode exprTopParent ) 
	{
		// TODO Auto-generated method stub	
	}
	
	public String prettyPrint( int indent ) {
		
		return toString();				
	}	

}
