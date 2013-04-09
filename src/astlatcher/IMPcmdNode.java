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
		
	
	/*
	public static String cmdStmtStrGet( String lhs, String rhs, int op )
	{
		String stmtStr= "";
		
		if( rhs.equals( CNullConstant ) )
		{
			stmtStr = "x:=null{" + rhs + "}";
		}
		else
		{
			stmtStr = "x:=y{" + lhs + "," + rhs + "}";
		}

		return stmtStr;
	}
	*/
	
	
	@Override
	public String toString()
	{
		String  res       =  "";
		String  x         =  "";
		String  y         =  "";
		String  lExp      =  "";
		String  rExp      =  "";
		String  midComma  = ",";
		
		if( lhs instanceof IMPFieldRefNode )
		{
			x       =  "x.n";
			lExp    =  (( IMPFieldRefNode )lhs).ownerGet() + ",";
			lExp    =  (( IMPFieldRefNode )lhs).ownerGet() + "," + ASTBuilder.nextFieldStrGet();
		}
		else
		{
			x       =  "x";
			lExp    =  lhs.toString();
		}

		
		if( rhs instanceof IMPFieldRefNode )
		{
			y       =  "y.n";
			rExp    =  (( IMPFieldRefNode )rhs).ownerGet() + "," + ASTBuilder.nextFieldStrGet();
		}
		else if( rhs.toString().equals( ASTBuilder.nullStrGet() ) )
		{
			y        =  "null";			
			midComma =  "";
		}
		else
		{
			y     =  "y";
			rExp  =  rhs.toString();
		}
		
		res = x + ":=" + y + "{" + lExp + midComma + rExp  + "}";
		
		//ASTBuilder.cmdStmtStrGet(  lhs.toString(), rhs.toString(), op );	
		return res; 
		//was previously:
		//return lhs.toString() + ASTBuilder.impOperandGet( op ) + rhs.toString();
	}

}
