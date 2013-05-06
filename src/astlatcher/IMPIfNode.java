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
		
		IMPastNode child = ASTBuilder.IMPParseStmt( stmt , res );
		
		if( child != null )
		{
			res.nodeChildrenAppend( child );
		}		
		
						
		return res;
	}

	
	private IMPastNodeSimplify boolExpressionGet( IASTIfStatement stmt )	
	{
		IMPastNodeSimplify res =  ASTBuilder.IMParseExpr
								( stmt.getConditionExpression() , this );
		
		boolean isSinglePredicate =  res instanceof IMPidNode;
		
		/*
		if( !isSinglePredicate )
		{
			IMPastNode      parent            =  this.nodeParentGet();			
			IMPCommentNode  assignBoolCmd     =  new IMPCommentNode( parent );			
			String          boolPredicateName =  VariableGenerator.nextVarNameGet( "k" );
			
			assignBoolCmd.initNode( boolPredicateName , res.toString() );
					
			parent.nodeChildrenAppend( assignBoolCmd );
						
			IMPidNode boolPredicate = new IMPidNode( this );			
			boolPredicate.initNode( boolPredicateName );

			res = boolPredicate;
			
			//checks that the condition is a single boolean operator
			//if not --> creates a new bool variable, value it according to the conditionExpression
			//and replace the conditionExpression with the new variable 
		}
		*/
		
		return res;
	}
	
	
	public void initNode( IASTNode node ) 
	{		
		IASTIfStatement  stmt  =  ( IASTIfStatement) node;		
		this.boolCondition     =  boolExpressionGet( stmt );
		
		IASTStatement  thenStmt        =  stmt.getThenClause();		
		boolean        isThenCompound  =  thenStmt instanceof IASTCompoundStatement;
		
		//System.out.println( "thenStmt: "+ isThenCompound + " " + thenStmt.getClass().getName() );
		
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
		
		//System.out.println( "elseStmt: "+ isElseCompound + " " + elseStmt.getClass().getName() );
		
		if( !isElseCompound )
		{			
			this.elseBody   =  compoundStmtAdd( elseStmt );			
		}
		else
		{
			this.elseBody   =  ASTBuilder.IMPParseStmt( elseStmt , this );
		}
		
		simplifyBoolCondition();
	}

	
	@Override
	public String toString()
	{
		//return "if{C(" + boolCondition.toString() +")," + thenBody.toString() + "," + elseBody.toString() +"}"  ;
		return "if{ " + boolCondition.toString() +"," + thenBody.toString() + "," + elseBody.toString() +"}"  ;
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
	
	private String prettyPrintIf( int indent ) 
	{	
		StringBuilder res  =  new StringBuilder( ASTBuilder.indentTabGet( indent ) + 
				"if $" + boolCondition.toString() + "$ then\n" );			
		
		res.append( thenBody.prettyPrint( indent  ) );
						
		res.append(  ASTBuilder.indentTabGet( indent ) + "else\n" );
						
		res.append( elseBody.prettyPrint( indent ) );			
		
		String result = res.toString();
						
		return result;				
	}	
	
	
	public String prettyPrint( int indent ) 
	{	
		return prettyPrintIf( indent );				
	}

	
	private String prettyPrintIfAST( int indent ) 
	{	
		StringBuilder res = new StringBuilder( ASTBuilder.indentTabGet( indent ) + 
				"if{" + boolCondition.toString() + ",\n" );			
		
		res.append( thenBody.prettyPrintAST( indent ) );						
		res.append( ASTBuilder.indentTabGet( indent ) + ",\n");						
		res.append( elseBody.prettyPrintAST( indent ) );					
		res.append( ASTBuilder.indentTabGet( indent ) + "}\n" );
		
		String result = res.toString();
						
		return result;				
	}		
	
	public String prettyPrintAST( int indent ) 
	{
		
		return prettyPrintIfAST( indent );		
	}	


}
