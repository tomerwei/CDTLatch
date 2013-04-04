package astlatcher;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.core.runtime.CoreException;

public class ASTBuilder extends org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage {
		
	private String            pathToFile;
	private String []         funcs;
	private StringBuilder     output;
	private VariableGenerator varGen;
	HashSet <String>          nextFields;
	HashSet <String>          symbolTablePointers;
	
	public ASTBuilder( String filename, String [] funcs, String [] nextFlds, String [] ptrs ) 
	{
		super();
		
		this.pathToFile           =  filename;
		this.funcs                =  funcs;
		this.output               =  new StringBuilder();
		this.varGen               =  new VariableGenerator();
		
		initPointers( ptrs );
		initNextFields( nextFlds );
		ASTinit();	
		ASTOutput();
		
		//CASTSimpleDeclSpecifier
	}
	
	private void initPointers( String [] ptrs )
	{
		this.symbolTablePointers  =  new HashSet<String>();
		
		symbolTablePointers.add( "NULL" );
		
		for( String p : ptrs )
		{
			symbolTablePointers.add( p );
		}
		//TODO to replace later with relevant code
		/*
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration ]
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier ]
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTName ]
		[ * 	org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator ]
		[ * org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer ]
		[ h org.eclipse.cdt.internal.core.dom.parser.c.CASTName ]

		or:
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement ]
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration ]
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier ]
		[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTName ]
		[ * org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator ]
		[ * org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer ]
		[ j org.eclipse.cdt.internal.core.dom.parser.c.CASTName ]
		*/			
	}
	
	private void initNextFields( String[] nextFlds ) 
	{		
		this.nextFields           =  new HashSet<String>();

		for( String s : nextFlds )
			nextFields.add( s );
	}
	
	private boolean isNextField( String fldName )
	{
		return nextFields.contains( fldName );
	}
	
	private boolean isImpCompatiblePointer( String varName )
	{
		return symbolTablePointers.contains( varName );
	}	

	private void ASTOutput() 
	{		
		System.out.println( output.toString() + "\n" );		
	}
	
	private void ASTinit()
	{
		FileContent fc = FileContent.createForExternalFileLocation( pathToFile );
		
		System.out.println( fc.getFileLocation() );

		try 
		{
			IASTTranslationUnit tu =  
			this.getASTTranslationUnit( fc, 
					new ScannerInfo( 
						new HashMap<String, String>(),new String[0]  )		/*scanInfo*/, 
						IncludeFileContentProvider.getEmptyFilesProvider()	/*fileCreator*/, 
						null												/*index*/, 
						0, 
						new DefaultLogService()								/*logger*/ );
					
			ASTVisitor visitor = new ASTVisitor() {
				
				@Override 
				public int visit( IASTName name ) 
				{
					IBinding b = name.resolveBinding();
					
					//CFunction c;
					
					System.out.print( b.getName() +" " + b.getClass().getName()  +"\n" );
					
					IType type = ( b instanceof IFunction ) ? 
									( (IFunction) b).getType() : null;
					
					if( type != null )
					{
						//check if funcsNames contain this function						
						if( name.toString().equals( funcs[0] ) )
						{							
							System.out.print( "Referencing " + name + ", type " + ASTTypeUtil.getType(type) + "\n" );
						}					
					}
					
					return ASTVisitor.PROCESS_CONTINUE;
				}
				
				@Override 
				public int visit( IASTDeclaration dec ) 
				{					
					if( dec instanceof IASTFunctionDefinition )
					{
						
						String funcName = ((IASTFunctionDefinition) dec).getDeclarator().getName().toString();
						
						boolean toProcess = isFuncIntersting( funcName );
						
						if( toProcess )
						{
							//System.out.println( ((IASTFunctionDefinition) dec).getDeclarator().getName() );
							output.append( "# " + funcName + "():\n");
							//printASTNodes( "", dec );							
							//output.append( processASTNodes( dec ) + "\n" );
							output.append( printFuncNodes( dec, 0 ) + "\n" );
							
							//System.out.print( dec.getFileLocation().getStartingLineNumber() + ":\t" + dec.getRawSignature() + "\n");							
						}
					}				
					
					return ASTVisitor.PROCESS_CONTINUE;
				}
			};

			//visitor.shouldVisitNames = true;
			visitor.shouldVisitDeclarations = true;
			tu.accept( visitor );
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}	
	}
	
	private boolean isFuncIntersting( String name )
	{
		return funcs[0].equals( name );
	}
	
	public void prettyPrintNode( IASTNode node )
	{
		System.out.println( node.getRawSignature()) ;		
	}
	
	public void printASTNodes( String prefix, IASTNode node )
	{		
					
		try 
		{
			System.out.println( prefix + node.getFileLocation().getStartingLineNumber() + " " +   node.getSyntax() + " " + node.getClass().getName() );
		} 
		catch (UnsupportedOperationException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ExpansionOverlapsBoundaryException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		IASTNode [] arr = node.getChildren();
		
		for( int i = 0 ; i < arr.length ; ++i )
		{
			printASTNodes( prefix + " ", arr[i]  );
		}
	}
	
	public String processASTNodes( IASTNode node )
	{
	
		if( node instanceof IASTIdExpression )
		{
			return ((IASTIdExpression) node).getName().toString();
		}
		else if( node instanceof IASTFieldReference )
		{
			StringBuilder temp = new StringBuilder();
			
			String fldName = ((IASTFieldReference) node).getFieldName().toString();
			
			//temp.append( processASTNodes(((IASTFieldReference) node).getFieldOwner()) );
			
			temp.append( printFuncNodes(((IASTFieldReference) node).getFieldOwner(), 0 ) );
			
			temp.append( "." );
			
			if( isNextField( fldName ) )
			{
				temp.append( "n" );
			}
			else
			{
				temp.append( fldName );
			}
			
			return temp.toString();
		}
		else if( node instanceof IASTIfStatement )
		{
			IASTIfStatement stmt = (IASTIfStatement)node;
					
			StringBuilder temp = new StringBuilder();
						
			temp.append( "if $" );
			
			boolean isImpCond = isImpCondition( stmt.getConditionExpression() );
			
			if( isImpCond )
			{
				temp.append( processASTNodes( stmt.getConditionExpression() ) +  "$ "  );				
			}
			else
			{
				temp.append( "CIF(I)$ "  );
				//need to extract this differently
			}
			
			temp.append( "then " + processASTNodes( stmt.getThenClause() ) + " " );
			temp.append( "else " + processASTNodes( stmt.getElseClause() ) + " " );
			
			return temp.toString();
		}		
		else if( node instanceof IASTWhileStatement )
		{
			IASTWhileStatement stmt = (IASTWhileStatement)node;
			
			StringBuilder temp = new StringBuilder();
						
			temp.append( "while $" );
			
			boolean isImpCond = isImpCondition( stmt.getCondition() );
			
			if( isImpCond )
			{
				temp.append( processASTNodes( stmt.getCondition() ) +  "$ "  );				
			}
			else
			{
				temp.append( "CWhile(I)$ "  );
				//need to extract this differently
			}
			
			temp.append( " ( " + processASTNodes( stmt.getBody() ) + " ) " );
			
			return temp.toString();		
		}		
		/*
		else if( node instanceof IASTStatement )
		{
			
			return processASTNodes( node ) + "\n"; 
		}
		*/
		else if( node instanceof IASTBinaryExpression )
		{
			
			IASTBinaryExpression  b  = ( IASTBinaryExpression )node;			
			int                  op  = b.getOperator();
			String opStr             = "";
			
			
			switch( op )
			{

			case( IASTBinaryExpression.op_assign ):				
				opStr = " := ";
				break;
			case( IASTBinaryExpression.op_notequals ):
				opStr = " != ";
				break;
			case( IASTBinaryExpression.op_equals ):
				opStr = " == ";
				break;
			case( IASTBinaryExpression.op_logicalAnd ):				
				opStr = " && ";
				break;
				
			default:
				opStr = " ? ";					
			}
			
			return ( processASTNodes( b.getOperand1() ) + opStr +
					  processASTNodes( b.getOperand2() ) .toString() + "" );		
		}
		if( node instanceof IASTCompoundStatement )
		{
			IASTNode [] arr = node.getChildren();
			
			StringBuilder temp = new StringBuilder();
			
			temp.append( " ( " );

			if( arr.length == 0)
			  temp.append( "<SKIP>" );
			
			for( int i = 0 ; i < arr.length ; ++i )
			{
			
				temp.append( " " + processASTNodes( arr[i] )  + "" );				
			}
			
			temp.append( " ) " );
			
			return temp.toString();
		}
		else
		{
			IASTNode [] arr = node.getChildren();
			
			StringBuilder temp = new StringBuilder();
			
			if( arr.length == 0)
			{
				try 
				{
					temp.append( "< " + node.getSyntax() +  " SKIP>" );
				} 
				catch (ExpansionOverlapsBoundaryException e) 
				{
					temp.append( "< " + " SKIP>" );					
					e.printStackTrace();
				}
			}
						
			for( int i = 0 ; i < arr.length ; ++i )
			{
				String cur =  processASTNodes( arr[i] );
				
				temp.append( "" + processASTNodes( arr[i] )  + "" );				
			}
			
			if( node instanceof IASTStatement )
				temp.append( ";" );
			
			return temp.toString();
		}
	}
	
	private String indentTabGet( int indent )
	{
		String result = "";
		
		if( indent > 0 )
		{
		
			StringBuilder temp = new StringBuilder();
		
			temp.append("");
		
			for( int i = 0 ; i < indent ; ++i )
				temp.append("\t");
		
			result = temp.toString();
		}
		
		return result;
	}
	
	private String impOperandGet( int op )
	{
		String opStr = "";
		
		switch( op )
		{

		case( IASTBinaryExpression.op_assign ):				
			opStr = " := ";
			break;
		case( IASTBinaryExpression.op_notequals ):
			opStr = " != ";
			break;
		case( IASTBinaryExpression.op_equals ):
			opStr = " = ";
			break;
		case( IASTBinaryExpression.op_logicalAnd ):				
			opStr = " & ";
			break;				
		default:
			opStr = " ? ";					
		}	
		return opStr;
	}
	
	
	//CASTBinaryExpression c;
	//CASTIdExpression c;

	public String[] impConditionGet( IASTNode node )
	{
		String [] res         =  { "", "" }; //to add to parent statemtn prefix, "new statement";
		StringBuilder prefix  =  new StringBuilder();
		StringBuilder suffix  =  new StringBuilder();
		
		if( node instanceof IASTFieldReference )
		{
			String fldName = ( ( IASTFieldReference ) node).getFieldName().toString();
			
			if( ! isNextField( fldName ) )
			{
				IASTNode  parent   =  node.getParent();
				String    varName  =  varGen.nextVarNameGet();
				
				//CASTDeclarationStatement dec = new CASTDeclarationStatement( varName );
				
				//CASTIdExpression var = new CASTIdExpression( varName );
				//CASTSimpleDeclaration c;
				//CASTDeclorator c;
				
							
				
				
				/*
				String fieldRef     =  visitStmt( ( IASTFieldReference )node, 0 );				
				
				String prefix       =  "int " + varName + 
						               impOperandGet( IASTBinaryExpression.op_assign );
						               
						               				
				prefix.append( )
				*/
			}
		}
		else if( node instanceof IASTIdExpression )
		{
			String nodeName = ((IASTIdExpression)node).getName().toString();
			
			//res				= isImpCompatiblePointer( nodeName );			
		}		
		else
		{
			IASTNode [] arr = node.getChildren();
						
			/*
			for( int i = 0 ; i < arr.length && res ; ++i )
			{			
				res = isImpCondition( arr[i] );				
			}
			*/
		}
		
		res = new String[] { prefix.toString(), suffix.toString() };

		return res;
	}
	
	
	
	private String[] impBinExpGet( IASTBinaryExpression node )
	{
		String [] res  =  { "newCreatedVars", "binOp" };		
		int    op      =  node.getOperator();
		
		if( op != IASTBinaryExpression.op_assign )
		{
			IASTExpression l_op = node.getOperand1();
			
			IASTExpression r_op = node.getOperand2();
		}
		
		return res;
	}
	
	
	private String impExpStmtGet( IASTExpressionStatement node )
	{
		String res = "";
					
		return res;
	}
	

	
	private String impExpGet( IASTNode node )
	{
		String res = "";
		
		StringBuilder temp = new StringBuilder();
		
		IASTNode [] arr = node.getChildren();
		
		for( int i = 0; i < arr.length; ++i )
		{
			IASTNode curr = arr[i];
			
		}
					
		res = temp.toString();
		
		return res;
	}	

	
	
	public String printFuncNodes( IASTNode node, int indent )
	{
		//CASTPointer c;
		//CASTExpressionStatement c;
		
		try 
		{
			System.out.println( "[ " + node.getChildren().length + " " + node.getSyntax().toString() + " " + node.getClass().getName() + " ]" );
		} 
		catch (ExpansionOverlapsBoundaryException e1) 
		{		
			System.out.println( "[ noSyntax " + node.getClass().getName() + " ]" );
		}
		
		if( node instanceof IASTIdExpression )
		{
			return indentTabGet( 0 ) + 
					((IASTIdExpression) node).getName().toString();
		}
		else if( node instanceof IASTFieldReference )
		{
			return visitStmt( ( IASTFieldReference )node, indent );
		}
		else if( node instanceof IASTIfStatement )
		{
			return visitStmt( ( IASTIfStatement )node, indent );
		}		
		else if( node instanceof IASTWhileStatement )
		{
			IASTWhileStatement stmt = (IASTWhileStatement)node;
			
			StringBuilder temp = new StringBuilder();									
			
			temp.append( "while $" );
			
			boolean isImpCond = isImpCondition( stmt.getCondition() );
			
			if( isImpCond )
			{
				temp.append(  printFuncNodes( stmt.getCondition(), 0 ) +  "$ "  );				
			}
			else
			{
				temp.append( "CWhile(I)$ "  );
				//need to extract this differently
			}			
			
			temp.append( printFuncNodes( stmt.getBody(), indent ) 
				+ indentTabGet( indent ) + "" );
			
			return temp.toString();		
		}		
		else if( node instanceof IASTBinaryExpression )
		{
			
			IASTBinaryExpression  b  = ( IASTBinaryExpression )node;			
			int                  op  = b.getOperator();
			String opStr             = impOperandGet( op );
			
			return ( printFuncNodes( b.getOperand1(), 0 ) + opStr +
					printFuncNodes( b.getOperand2(), 0 ) );		
		}
		else if( node instanceof IASTParameterDeclaration )
		{
			return "# " + node.getRawSignature() + "\n" + indentTabGet( indent ) ;
		}		
		else if( node instanceof IASTDeclarationStatement )
		{
			return "# " + node.getRawSignature() + "\n" + indentTabGet( indent ) ;
		}
		else if( node instanceof IASTReturnStatement )
		{
			return "# " + node.getRawSignature() + "\n" + indentTabGet( indent ) ;						
		}		
		/*
		else if( node instanceof IASTFunctionDefinition )
		{
			return "";
		}		
		*/
		else if( node instanceof IASTCompoundStatement )
		{
			IASTNode [] arr = node.getChildren();
			
			StringBuilder temp = new StringBuilder();
			
			temp.append( "\n" + indentTabGet( indent ) +  "(\n" + indentTabGet( indent + 1 ) );

			if( arr.length == 0)
			  temp.append( "<SKIP>" );
			
			for( int i = 0 ; i < arr.length ; ++i )
			{
				String cur =  processASTNodes( arr[i] );
				
				temp.append( "" + printFuncNodes( arr[i], indent + 1 )  + "" );				
			}
			
			temp.append( "\n" + indentTabGet( indent ) +  ")\n" );
			
			return temp.toString();
		}
		/*
		else if( node instanceof IASTExpressionStatement )
		{
			IASTNode [] arr = node.getChildren();
			
			//visit all children till we find a illegit imp field reference
			//create a new var for field x
			//replace field expression statement with assignment of type
			 * Every op except assign: h.data == key --> int x01 = h.data; and relpace h.data with x01;
			 * With assign: leve the same
			
		}				
		*/
		else
		{
			IASTNode [] arr = node.getChildren();
			
			StringBuilder temp = new StringBuilder();
			
			if( arr.length == 0)
			{
				try 
				{
					temp.append( "#< " + node.getSyntax() +  " " + node.getClass().getName() + " SKIP>" + "\n" + indentTabGet( indent ) );
				} 
				catch (ExpansionOverlapsBoundaryException e) 
				{
					temp.append( "#< " + " SKIP>"  + "\n" + indentTabGet( indent ) );					
					e.printStackTrace();
				}
			}
						
			for( int i = 0 ; i < arr.length ; ++i )
			{			
				temp.append( "" + printFuncNodes( arr[i], indent )  + "" );				
			}
			
			if( node instanceof IASTStatement )
				temp.append( ";\n"  + indentTabGet( indent ) );
			
			return temp.toString();
		}
	}

	public String visitStmt( IASTBinaryExpression b, int indent )
	{				
		int         op     =  b.getOperator();
		String      opStr  =  impOperandGet( op );
	
		return ( printFuncNodes( b.getOperand1(), 0 ) + opStr +
				printFuncNodes( b.getOperand2(), 0 ) );
	}
	
	public String visitStmt( IASTFieldReference node, int indent )
	{
		StringBuilder temp =  new StringBuilder();		
		String fldName     =  node.getFieldName().toString();
		
		temp.append( printFuncNodes( ( (IASTFieldReference) node ).getFieldOwner() , 0 ) );
		
		temp.append( "." );
		
		if( isNextField( fldName ) )
		{
			temp.append( "n" );
		}
		else
		{
			temp.append( fldName );
		}
		
		temp.append( indentTabGet( indent ) );
		
		return temp.toString();		
	}
	
	
	public String visitStmt( IASTIfStatement stmt, int indent )
	{
		
		StringBuilder temp       =  new StringBuilder();															
		boolean       isImpCond  =  isImpCondition( stmt.getConditionExpression() );
		
		if( isImpCond )
		{
			
			temp.append( "if $" );
			temp.append( printFuncNodes( stmt.getConditionExpression(), 0 ) +  "$ "  );				
		}
		else
		{
			
			
			temp.append( "#$" + printFuncNodes( stmt.getConditionExpression(), 0 ) +  "$\n" +indentTabGet(indent) );
			
			temp.append( "if $" );
			temp.append( "CIF(I)$ "  );
			//need to extract this differently
		}			
		
		temp.append( "then " + printFuncNodes( stmt.getThenClause(), indent ) + " " );
		temp.append( indentTabGet( indent ) + "else " + printFuncNodes( stmt.getElseClause(), indent ) + " " );
		
		return temp.toString();
				
		//returns boolean condition that contains only linked list nodes or referenced 'next' fields
		//does not contain referenced data fields or data comparisions
		
		//calls impBooleanConditionGet()
	}
	
	
	public void analyazeWhileStmt()
	{
		//returns boolean condition that contains only linked list nodes or referenced 'next' fields
		//does not contain referenced data fields or data comparisions
		
		//calls impBooleanConditionGet()
	}

	public String impBooleanConditionGet( IASTNode node )
	{
		String result = "";

		//if expression includes a not 'next' field or number condition --> create $C(i)$ condition
		//else return regular parsing result
				
		return result;
	}
	
	public boolean isImpCondition( IASTNode node )
	{
		boolean res = true;
		
		//CASTIdExpression c;
		//need to check if CASTIdExpression is of a recognized Pointer or null field		
		
		if( node != null && node instanceof IASTFieldReference )
		{
			String fldName = ((IASTFieldReference) node).getFieldName().toString();
			
			if( ! isNextField( fldName ) )
			{
				res = false;
			}
		}
		else if( node instanceof IASTIdExpression )
		{
			String nodeName = ((IASTIdExpression)node).getName().toString();
			
			res = isImpCompatiblePointer( nodeName );			
			//System.out.println( "impBooleanConditionGet: " + nodeName );
			// need to check other stuff like constants and such
		}		
		else
		{
			IASTNode [] arr = node.getChildren();
						
			for( int i = 0 ; i < arr.length && res ; ++i )
			{			
				res = isImpCondition( arr[i] );				
			}
		}		

		return res;
	}
	
	public static void main( String [] args )
	{				
		String [] interestingFuncs = { "my_find" };
		String [] nextFields       = { "next" };
		String [] ptrs             = { "t", "j", "i" };

		
		//ASTBuilder temp = new ASTBuilder( "/home/tomerwei/workspace/CDTLatch/workfiles/thttpd-2.25b/thttpd.c" );
		ASTBuilder temp = new ASTBuilder( 
				"/home/tomerwei/workspace/CDTLatch/workfiles/thttpd-2.25b/stupid.c",
				interestingFuncs,
				nextFields,
				ptrs );		
	}
}

