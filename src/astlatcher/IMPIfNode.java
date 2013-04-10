package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;


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
	
	private IMPcompoundStmtNode compoundStmtAdd( IASTStatement stmt )
	{
		IMPcompoundStmtNode res = new IMPcompoundStmtNode( this );
		//res.initNode( stmt );
		
		
		IMPastNode child = ASTBuilder.IMPParseStmt( stmt , res );
		
		if( child != null )
		{
			res.nodeChildrenAppend( child );
		}		
		
						
		return res;
	}

	
	public void initNode( IASTNode node ) 
	{		
		IASTIfStatement  stmt  =  ( IASTIfStatement) node;		
		this.boolCondition     =  ASTBuilder.IMParseExpr( stmt.getConditionExpression() , this );

		
		IASTStatement  thenStmt        =  stmt.getThenClause();		
		boolean        isThenCompound  =  thenStmt instanceof IASTCompoundStatement;
		
		System.out.println( "thenStmt: "+ isThenCompound + " " + thenStmt.getClass().getName() );
		
		if( !isThenCompound )
		{
			this.thenBody   =  compoundStmtAdd( thenStmt );
		}
		else
		{
			this.thenBody   =  ASTBuilder.IMPParseStmt( thenStmt , this );
		}
						
		
		IASTStatement  elseStmt        =  stmt.getElseClause();
		boolean        isElseCompound  =  elseStmt instanceof IASTCompoundStatement;
		
		System.out.println( "elseStmt: "+ isElseCompound + " " + elseStmt.getClass().getName() );
		
		if( !isElseCompound )
		{
			
			this.elseBody   =  compoundStmtAdd( elseStmt );			
		}
		else
		{
			this.elseBody   =  ASTBuilder.IMPParseStmt( elseStmt , this );
		}
		
		/*
		boolean isThenCompound =  thenBody instanceof IMPcompoundStmtNode;
		
		if( !isThenCompound )
		{
			this.thenBody = compoundStmtAdd( thenBody );
		}
		
		boolean  isElseCompound = elseBody instanceof IMPcompoundStmtNode;
		
		if( !isElseCompound )
		{
			this.elseBody = compoundStmtAdd( elseBody );
		}
		*/
		
		simplifyBoolCondition();
	}

	@Override
	public String toString()
	{
		return "if'{" + boolCondition.toString() +"'," + thenBody.toString() + "," + elseBody.toString() +"}"  ;		
	}		
	
	private void simplifyBoolCondition( ) 	
	{
		
		boolean isSimp = boolCondition.isSimple();
		
		if( !isSimp )
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
		System.out.println( "Error: should not append to 'if' node " + node.toString() );
	}

}
