package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;


public class IMPcmdNode extends AbstractIMPastNode {
	
	private  IMPastNodeSimplify     lhs;
	private  IMPastNodeSimplify     rhs;
	private  int                    op;	

	public IMPcmdNode(IMPastNode parent)
	{
		super(parent);
	}

	
	
	public IMPcmdNode 
	funcReturnValueVarCreate( IMPastNode cmd, IASTFunctionCallExpression funcCall ) 			                         
	{		
		/*
		IMPFieldRefNode  fieldToExtract  =  (IMPFieldRefNode) node;
		String           varName         =  VariableGenerator.nextVarNameGet();
		IMPastNode       cmdParent       =  exprTopParent.nodeParentGet();
		IMPcmdNode       res             =  new IMPcmdNode( cmdParent.nodeParentGet() );	
		IMPidNode        assignedVar     =  new IMPidNode( res );
		
		assignedVar.initNode( varName );
		fieldToExtract.nodeParentSet( res );
		res.initNode( assignedVar, fieldToExtract );
		*/
		
		String      funcName       =  funcCall.getFunctionNameExpression().getRawSignature();		
		IMPcmdNode  assFuncVarCmd  =  new IMPcmdNode( cmd.nodeParentGet() );	
		IMPidNode   returnValueVar =  new IMPidNode( assFuncVarCmd );
		String      funcVarName    =  VariableGenerator.nextVarNameGet( funcName );
		
		returnValueVar.initNode( funcVarName );
			
		IMPliteralNode  assignTypeDefaultValue  =   new IMPliteralNode( cmd );		
		assignTypeDefaultValue.initNode( "null" );
		
		assFuncVarCmd.initNode( returnValueVar,assignTypeDefaultValue );						
		return assFuncVarCmd;
	}	
	
	private IMPastNodeSimplify functionInlineAdd( IASTFunctionCallExpression funcCall )
	{
		IMPastNodeSimplify  res                    =  null;
		IMPastNode          parent                 =  this.nodeParentGet();		
		IMPcmdNode          assignReturnFuncVarCmd =  funcReturnValueVarCreate( this, funcCall );
		
		parent.nodeChildrenAppend( assignReturnFuncVarCmd );
		//create new variable X of the return type of the function. Assign NULL value;
		//copy paste function contents ( should be inside compound stmt )
		//assign return stmt value to var X
		
		//TODO:
		//**********************************************
		//parent.nodeChildrenAppend( "inline function" );
		//funcDecs --> get IASTFunctionDefinition, and process
		//change return function  to assign the return value
		//to the assignReturnFuncVarCmd.
		//
		
		//**********************************************
				
		return assignReturnFuncVarCmd.leftNodeGet();
	}
	
	
	@Override
	public void initNode( IASTNode node ) 
	{
		IASTBinaryExpression   stmt   =  (IASTBinaryExpression)node;		
		this.op                       =  stmt.getOperator();
		
		
		if( op == IASTBinaryExpression.op_assign )
		{
			this.lhs  =  ASTBuilder.IMParseExpr( stmt.getOperand1() , this );
			
			IASTExpression rhsStmt = stmt.getOperand2();
			
			if( rhsStmt instanceof IASTFunctionCallExpression )
			{
				this.rhs  =  functionInlineAdd( ( IASTFunctionCallExpression )rhsStmt );				
			}
			else
			{
				this.rhs  =  ASTBuilder.IMParseExpr( rhsStmt , this );
			}
			
			
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
	
	
	
	public void initNode( IMPSymbol s )
	{
		
		if( s.canInit() )
		{
			IASTNode i  =  s.initNodeGet();
			this.op     =  IASTBinaryExpression.op_assign;
						
			IMPidNode   assignedVar  =  new IMPidNode( this );	
			assignedVar.initNode( s.nameGet() );
			
			this.lhs    =  assignedVar;
			this.rhs    =  ASTBuilder.IMParseExpr( i , this );						
		}
		else
		{
			System.out.println( "Error: Cannot init node " + s.nameGet() );
		}
		
	}
	
	
	
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

	public String prettyPrintAST( int indent ) 
	{
		String   res   = ASTBuilder.indentTabGet( indent ) + toString() + "\n";			
		
		return res;				
	}
	
	
}
