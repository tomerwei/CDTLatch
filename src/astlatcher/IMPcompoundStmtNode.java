package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPcompoundStmtNode extends AbstractIMPastNode {

	
	public IMPcompoundStmtNode(IMPastNode parent) 
	{	
		super(parent);
	}

	
	@Override
	public void initNode( IASTNode node ) 
	{
		
		if( node != null )
		{
		
			IASTNode [] arr = node.getChildren();
		
			if( arr.length == 0 )
			{
			//	?? should not get here
			}
					
			for( int i = 0 ; i < arr.length ; ++i )
			{			
				IMPastNode child = ASTBuilder.IMPParseStmt( arr[i] , this );
			
				if( child != null )
				{
					this.nodeChildrenAppend( child );
				}
			}		
		}
	}

	
	public String toString()
	{
		StringBuilder res           = new StringBuilder( ";{" );
		boolean       appendedComma = false;
		
		int numOfChildren = this.nodeChildrenGet( this ).size();
		
		if( numOfChildren > 0 )
		{
		
			for( IMPastNode child : this.nodeChildrenGet( this ) )
			{
				String childStr = child.toString();
			
				if( childStr.length() > 0 )				
				{
					res.append( child.toString() + ",");
					appendedComma = true;
				}
				else
				{				
					appendedComma = false;
				}
			}	
		}
		else
		{
			res.append(ASTBuilder.IMPSkipCmd);
		}
		
		/* removes last ',' char */
		String result = res.toString();

		if( appendedComma ) 
		{
			result  = result.substring(0, res.length()-1 ) +  "}";
		}
		else
		{
			result  = result + "}";
		}
						
		return result;
	}
	
	public String prettyPrintCompoundChildren( int indent ) {
		
		StringBuilder res = new StringBuilder( ASTBuilder.indentTabGet( indent ) + "{\n" );
		
		for( IMPastNode child : this.nodeChildrenGet( this ) )
		{
			String childStr = child.toString();
			
			if( childStr.length() > 0 )				
			{
				res.append( child.prettyPrint( indent + 1 ) );
			}
		}
		
		String result = res.toString() + ASTBuilder.indentTabGet( indent ) +  "}";
						
		return result;				
	}
	
	

	public String prettyPrint( int indent ) {
		
		return prettyPrintCompoundChildren( indent ) + "\n";				
	}
	
	
	public String prettyPrintCompoundChildrenAST( int indent ) {
		
		StringBuilder res = new StringBuilder( ASTBuilder.indentTabGet( indent ) + ";{\n" );
		
		for( IMPastNode child : this.nodeChildrenGet( this ) )
		{
			String childStr = child.toString();
			
			if( childStr.length() > 0 )				
			{
				res.append( child.prettyPrintAST( indent + 1 ) );
			}
		}
		
		String result = res.toString() + ASTBuilder.indentTabGet( indent ) +  "}";
						
		return result;				
	}
	
	
	public String prettyPrintAST( int indent ) {
		
		return prettyPrintCompoundChildrenAST( indent ) + "\n";				
	}	
}
