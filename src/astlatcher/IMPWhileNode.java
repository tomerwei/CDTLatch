package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

public class IMPWhileNode extends AbstractIMPastNode{
	
	public IMPWhileNode(IMPastNode parent)
	{
		super(parent);
		// TODO Auto-generated constructor stub
	}

	private  IMPastNodeSimplify  boolCondition;
	private  IMPastNode          loopBody;
	
	
	public IMPastNode boolConditionGet()
	{
		return boolCondition;		
	}

	
	public IMPastNode loopBodyGet()
	{
		return loopBody;		
	}
	
		
	public void initNode( IASTNode node ) 
	{
		IASTWhileStatement stmt  =  (IASTWhileStatement)node;			
		this.boolCondition       =  ASTBuilder.IMParseExpr(  stmt.getCondition(), this );
		this.loopBody            =  ASTBuilder.IMPParseStmt( stmt.getBody(), this );					
		
		//IMPastNode expr          =  simplifyBoolCondition( );
		simplifyBoolCondition();
	}

	@Override
	public String toString()
	{
		return "while '{" + boolCondition.toString() +"', I, " + loopBody.toString() +"}" ;		
	}
	
	private void simplifyBoolCondition( ) 	
	{	
		
		if( !boolCondition.isSimple() )
		{
			boolCondition.simplify( this );
		}		
		//System.out.println( "hello world " +  boolCondition.isSimple() );
		//traverse lhs, rhs
		//if they there is a non next field reference - create new variable
		//add assignment stmt to before parent, and in th	
	}
	
	@Override
	public void nodeChildrenAppend( IMPastNode node ) 
	{
		loopBody.nodeChildrenAppend( node );
	}

	private String prettyPrintWhille( int indent ) {
		
		StringBuilder res = new StringBuilder( ASTBuilder.indentTabGet( indent ) + 
				"while $" + boolCondition.toString() + "$\n" );
		
		res.append( loopBody.prettyPrint( indent  ) );
		
		String result = res.toString();
						
		return result;				
	}	
	
	
	public String prettyPrint( int indent ) {
		
		return prettyPrintWhille( indent ) ;				
	}	
}
