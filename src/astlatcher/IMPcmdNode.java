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
		String  midComma  =  ",";
		
		if( lhs instanceof IMPFieldRefNode )
		{
			IMPFieldRefNode lNode = (IMPFieldRefNode)lhs;
			
			x       =  "x." + lNode.fieldNameGet();
			lExp    =  lNode.ownerGet() + ",";
			lExp    =  lNode.ownerGet() + "," + lNode.fieldNameGet();
		}
		else
		{
			x       =  "x";
			lExp    =  lhs.toString();
		}

		
		if( rhs instanceof IMPFieldRefNode )
		{
			IMPFieldRefNode rNode = (IMPFieldRefNode)rhs;
			
			y       =  "y." + rNode.fieldNameGet();
			rExp    =  rNode.ownerGet() + "," + rNode.fieldNameGet();
		}
		else if( rhs.toString().equals( ASTBuilder.nullStrGet() ) )
		{
			y        =  "null";			
			midComma =  "";
		}
		else
		{
			y        =  "y";
			rExp     =  rhs.toString();
		}
		
		res = x + ":=" + y + "{" + lExp + midComma + rExp  + "}";
		
		return res; 
		//was previously:
		//return lhs.toString() + ASTBuilder.impOperandGet( op ) + rhs.toString();
	}

	
	public String prettyPrintCommand( int indent ) {
		
		String res = lhs.prettyPrint( 0 ) + " := " + rhs.prettyPrint( 0 );
		
		return res; 
	}	
	
	
	
	public String prettyPrint( int indent ) 
	{
		//debug
		boolean  astPrettyPrint  =  false;
		String   res             =  "";
		
		if( astPrettyPrint )
		{
			res = ASTBuilder.indentTabGet( indent ) + toString() + "\n";
		}
		else
		{
			res = ASTBuilder.indentTabGet( indent ) + prettyPrintCommand( indent ) + "\n";
		}
		
		return res;				
	}

}
