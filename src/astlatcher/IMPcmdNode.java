package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;


public class IMPcmdNode extends AbstractIMPastNode {
	
	private  IMPastNodeSimplify     lhs;
	private  IMPastNodeSimplify     rhs;
	private  int                    op;	

	public IMPcmdNode(IMPastNode parent)
	{
		super(parent);
	}

	@Override
	public void initNode( IASTNode node ) 
	{
		IASTBinaryExpression   stmt   =  (IASTBinaryExpression)node;		
		this.op                       =  stmt.getOperator();
		
		if( op == IASTBinaryExpression.op_assign )
		{
			this.lhs  =  ASTBuilder.IMParseExpr( stmt.getOperand1() , this );
			this.rhs  =  ASTBuilder.IMParseExpr( stmt.getOperand2() , this );										
		}
		else
		{
			this.lhs =  null;
			this.rhs =  null;										
			
			System.out.println( "Operator " + op + " not supported " + node.getClass().getName() );
		}			
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
	
	public void initNode( IMPastNodeSimplify lhs, IMPastNodeSimplify rhs ) 
	{			
		this.op                       =  IASTBinaryExpression.op_assign;		
		this.lhs                      =  lhs;
		this.rhs                      =  rhs;
	}	
		
	@Override
	public String toString()
	{
		return lhs.toString() + ASTBuilder.impOperandGet( op ) + rhs.toString();
	}

}
