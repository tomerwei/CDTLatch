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
	
	
	public void simplifyAtomicExpr( IMPastNode exprTopParent ) 
	{
		if( lhs instanceof IMPFieldRefNode )
		{			
			IMPcmdNode         assStmt     = 
					assStmtForNonNextFieldCreate( exprTopParent, lhs , true );
			
			exprTopParent.nodeParentGet().nodeChildrenAppend( assStmt );
			
			IMPastNodeSimplify leftSide    = assStmt.leftNodeGet();
			
			leftSide.nodeParentSet( this );
			this.leftNodeSet( leftSide );			
		}
		
		if( rhs instanceof IMPFieldRefNode )
		{		
			IMPcmdNode         assStmt     = 
					assStmtForNonNextFieldCreate( exprTopParent, rhs , false );
			
			exprTopParent.nodeParentGet().nodeChildrenAppend( assStmt );
			
			IMPastNodeSimplify rightSide    = assStmt.rightNodeGet();
			
			rightSide.nodeParentSet( this );
			this.rightNodeSet( rightSide );							
		}			
	}

		
	public void simplify( IMPastNode exprTopParent ) 
	{					
		
		if( this instanceof IMPexprNode && !isSimple()  )
		{
			
			if( isAtomicExprNode() )
			{
				simplifyAtomicExpr( exprTopParent );
			}
			else
			{
				this.leftNodeGet().simplify( exprTopParent );
				this.leftNodeGet().simplify( exprTopParent );
			}
		}		
	}
	

}

