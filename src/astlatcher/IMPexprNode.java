package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPexprNode extends IMPastNodeSimplify
{
	private  IMPastNodeSimplify     lhs;
	private  IMPastNodeSimplify     rhs;
	private  int                    op;	
		
	public IMPexprNode(IMPastNode parent) 
	{
		super(parent);
	}

	@Override
	public void initNode( IASTNode node ) 
	{
		IASTBinaryExpression   stmt   =  (IASTBinaryExpression)node;		
		this.op                       =  stmt.getOperator();		

		this.lhs                      =  
				ASTBuilder.IMParseExpr( stmt.getOperand1() , this );
		this.rhs                      =  
			ASTBuilder.IMParseExpr( stmt.getOperand2() , this );							
	}

	
	public IMPastNodeSimplify leftNodeGet()
	{
		return lhs;
	}
	
	
	public void leftNodeSet( IMPastNodeSimplify n )
	{
		this.lhs = n;
	}
	
	
	public IMPastNodeSimplify rightNodeGet()
	{
		return rhs;
	}
	
	
	public void rightNodeSet( IMPastNodeSimplify n )
	{
		this.rhs = n;
	}
	
	
	@Override
	public String toString()
	{
		//CASTLiteralExpression
		return "(" + lhs.toString() + ASTBuilder.impOperandGet( op ) + rhs.toString() + ")";				
	}

	
	@Override
	public boolean isSimple() 
	{	
		return lhs.isSimple() && rhs.isSimple();
	}

	
	private boolean isAtomicExprNode()
	{
		boolean isLhsAtomic  = ( lhs instanceof IMPidNode || lhs instanceof IMPFieldRefNode );
		boolean isRhsAtomic  = ( rhs instanceof IMPidNode || lhs instanceof IMPFieldRefNode );
		
		return isLhsAtomic && isRhsAtomic;
	}

	
	/**
	 * @param node
	 * @param isLeft - indicates whether the left or right side of the expr is the field
	 * 					that we shall extract
	 * @return
	 */
	public static 
	IMPcmdNode assStmtForNonNextFieldCreate( IMPastNode exprTopParent, 
			                                 IMPastNodeSimplify node, boolean isLeft )
	{		
		IMPFieldRefNode  fieldToExtract  =  (IMPFieldRefNode) node;
		String           varName         =  VariableGenerator.nextVarNameGet();
		
		IMPastNode       cmdParent       =  exprTopParent.nodeParentGet();
		IMPcmdNode       res             =  new IMPcmdNode( cmdParent.nodeParentGet() );
		
		IMPidNode        assignedVar     =  new IMPidNode( res );
		
		assignedVar.initNode( varName );

		fieldToExtract.nodeParentSet( res );
		
		res.initNode( assignedVar, fieldToExtract );
				
		return res;
	}
	
	
	private void topParentForAppend( IMPastNode exprTopParent, IMPcmdNode assStmt )
	{
		IMPastNode  grandparent  =  exprTopParent.nodeParentGet();
		
		//TODO deal with case in top node is IF stmt		
		if( exprTopParent instanceof IMPWhileNode )
		{
			exprTopParent.nodeChildrenAppend( assStmt );
		}
		/*
		else if( exprTopParent instanceof IMPIfNode )
		{
			//do something different?
			
		}
		*/
		
		grandparent.nodeChildrenAppend( assStmt );
	}
	
	
	private void 
	simplifyAtomicExprDo( IMPastNode exprTopParent, IMPFieldRefNode field, boolean isLeft )
	{
		IMPcmdNode  assStmt  = assStmtForNonNextFieldCreate( exprTopParent, field , isLeft );
		
		//exprTopParent.nodeParentGet().nodeChildrenAppend( assStmt );
		topParentForAppend( exprTopParent, assStmt );
				
		if( isLeft )
		{
			IMPastNodeSimplify node   = assStmt.leftNodeGet();
			
			node.nodeParentSet( this );
			this.leftNodeSet( node );							
		}
		else
		{
			IMPastNodeSimplify node   = assStmt.rightNodeGet();
			
			node.nodeParentSet( this );
			this.rightNodeSet( node );									
		}		
	}
	
	
	public void simplifyAtomicExpr( IMPastNode exprTopParent ) 
	{
		if( lhs instanceof IMPFieldRefNode )
		{
			simplifyAtomicExprDo( exprTopParent, ( IMPFieldRefNode )lhs, true );			
		}
		
		if( rhs instanceof IMPFieldRefNode )
		{			
			simplifyAtomicExprDo( exprTopParent, ( IMPFieldRefNode )rhs, false );			
		}			
	}

		
	public void simplify( IMPastNode exprTopParent ) 
	{	
		boolean isSimp = isSimple();
		
		if( this instanceof IMPexprNode && !isSimp  )
		{
			boolean isAtomic = isAtomicExprNode();
			
			if( isAtomic )
			{
				simplifyAtomicExpr( exprTopParent );
			}
			else
			{
				this.leftNodeGet().simplify( exprTopParent );
				this.rightNodeGet().simplify( exprTopParent );
			}
		}		
	}

	
	public String prettyPrint( int indent ) {
		
		return "(" + lhs.prettyPrint( 0 ) + ASTBuilder.impOperandGet( op ) + rhs.prettyPrint( 0 ) + ")";				
	}
	
	
	public String prettyPrintAST( int indent ) {
		
		return this.toString();
	}	
}

