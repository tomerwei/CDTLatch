package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;


public class IMPIfNode extends AbstractIMPastNode {
	
	public IMPIfNode(IMPastNode parent) 
	{
		super(parent);
	}


	private  IMPastNodeSimplify   boolCondition;
	private  IMPastNode           thenBody;
	private  IMPastNode           elseBody;


	public IMPastNode boolConditionGet()
	{
		return boolCondition;		
	}

	
	public IMPastNode thenBodyGet()
	{
		return thenBody;		
	}

	
	public IMPastNode elseBodyGet()
	{
		return elseBody;		
	}		
	
	
	public void initNode( IASTNode node ) 
	{		
		IASTIfStatement  stmt  =  ( IASTIfStatement) node;		
		this.boolCondition     =  ASTBuilder.IMParseExpr( stmt.getConditionExpression() , this );
		this.thenBody          =  ASTBuilder.IMPParseStmt( stmt.getThenClause() , this );
		this.elseBody          =  ASTBuilder.IMPParseStmt( stmt.getElseClause() , this );
		
		simplifyBoolCondition();
	}

	@Override
	public String toString()
	{
		return "if'{" + boolCondition.toString() +"'," + thenBody.toString() + "," + elseBody.toString() +"}"  ;		
	}		
	
	private void simplifyBoolCondition( ) 	
	{			
		
		if( !boolCondition.isSimple() )
		{
			boolCondition.simplify( this );
		}
		/*		
		if they there is a non-next field reference - create a new variable
		add assignment stmt to the 'if' stmt's parent
		*/		
	}
	

	@Override
	public void nodeChildrenAppend( IMPastNode node ) 
	{
		System.out.println( "Error: should not append to 'if' node " );
	}

}
