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
		StringBuilder res = new StringBuilder( ";{" );
		
		for( IMPastNode child : this.nodeChildrenGet( this ) )
		{
			String childStr = child.toString();
			
			if( childStr.length() > 0 )				
			{
				res.append( child.toString() + ",");
			}
		}
		
		/* removes last ',' char */
		String result = res.toString().substring(0, res.length()-1 ) +  "}";
						
		return result;
	}

}
