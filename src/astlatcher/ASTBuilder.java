package astlatcher;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
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
		
	private String         pathToFile;
	private String []      funcs;
	private StringBuilder  output;
	HashSet <String>        nextFields;
	HashSet <String>        symbolTablePointers;
	
	public ASTBuilder( String filename, String [] funcs, String [] nextFlds ) 
	{
		super();
		
		this.pathToFile           =  filename;
		this.funcs                =  funcs;
		this.output               =  new StringBuilder();
		
		initPointers();
		initNextFields( nextFlds );
		ASTinit();	
		ASTOutput();
	}
	
	private void initPointers()
	{
		this.symbolTablePointers  =  new HashSet<String>();
		
		symbolTablePointers.add( "NULL" );
		
		symbolTablePointers.add( "t" );
		symbolTablePointers.add( "j" );
		symbolTablePointers.add( "i" );
		//need to replace later with relevant code
		/*
[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration ]
[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier ]
[ node org.eclipse.cdt.internal.core.dom.parser.c.CASTName ]
[ * org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator ]
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
	
	private void initNextFields(String[] nextFlds) 
	{		
		this.nextFields           =  new HashSet<String>();

		for( String s : nextFlds )
			nextFields.add( s );
	}
	
	private boolean isNextField( String fldName )
	{
		return nextFields.contains( fldName );
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
							new HashMap<String, String>(),new String[0]  )/*scanInfo*/, 
					IncludeFileContentProvider.getEmptyFilesProvider()/*fileCreator*/, 
					null/*index*/, 
					0, 
					new DefaultLogService()/*logger*/ );
			
			//System.out.println( tu.getDeclarations().length );
			
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
		CASTBinaryExpression tomer;
					
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
			//System.out.println( prefix + "Node no. " + i + ":\n" ) ;
			//System.out.println( prefix + func.getRawSignature()) ;
			
			//if( arr[i] instanceof IASTStatement )
			//{
				printASTNodes( prefix + " ", arr[i]  );
			//}
			//else
			//{
			//	System.out.println( prefix + "<SKIP>" );
			//}
		}
	}
	
	public String processASTNodes( IASTNode node )
	{
		/*
		try 
		{
			System.out.println( node.getFileLocation().getStartingLineNumber() + " " +   
									node.getSyntax() + " " + node.getClass().getName() );
		}
		*/
		
		//CASTCompoundStatement c;
		
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
						
			temp.append( "if $"  + processASTNodes( stmt.getConditionExpression() ) +  "$ "  );	
			temp.append( "then " + processASTNodes( stmt.getThenClause() ) + " " );
			temp.append( "else " + processASTNodes( stmt.getElseClause() ) + " " );
			
			return temp.toString();
		}		
		else if( node instanceof IASTWhileStatement )
		{
			IASTWhileStatement stmt = (IASTWhileStatement)node;
			
			StringBuilder temp = new StringBuilder();
						
			temp.append( "while $"  + processASTNodes( stmt.getCondition() ) +  "$ "  );				
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
	
	
	public String printFuncNodes( IASTNode node, int indent )
	{
		
		//CASTPointer c;
		
		try {
			System.out.println( "[ " + node.getSyntax().toString() + " " + node.getClass().getName() + " ]" );
		} catch (ExpansionOverlapsBoundaryException e1) {
			// TODO Auto-generated catch block
			System.out.println( "[ noSyntax " + node.getClass().getName() + " ]" );
		}
		
		if( node instanceof IASTIdExpression )
		{
			return indentTabGet( 0 ) + 
					((IASTIdExpression) node).getName().toString();
		}
		else if( node instanceof IASTFieldReference )
		{
			StringBuilder temp = new StringBuilder();
			
			String fldName = ((IASTFieldReference) node).getFieldName().toString();
			
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
			
			return indentTabGet( indent ) + temp.toString();
		}
		else if( node instanceof IASTIfStatement )
		{
			IASTIfStatement stmt = (IASTIfStatement)node;
					
			StringBuilder temp = new StringBuilder();
						
			temp.append( "if $"  + printFuncNodes( stmt.getConditionExpression(), indent) +  "$ "  );	
			temp.append( "then " + printFuncNodes( stmt.getThenClause(), indent ) + " " );
			temp.append( indentTabGet( indent ) + "else " + printFuncNodes( stmt.getElseClause(), indent ) + " " );
			
			return temp.toString();
		}		
		else if( node instanceof IASTWhileStatement )
		{
			IASTWhileStatement stmt = (IASTWhileStatement)node;
			
			StringBuilder temp = new StringBuilder();
						
			temp.append( "while $"  + printFuncNodes( stmt.getCondition(), indent ) +  "$ "  );				
			temp.append( printFuncNodes( stmt.getBody(), indent ) 
				+ indentTabGet( indent ) + "" );
			
			return temp.toString();		
		}		
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
			
			return ( printFuncNodes( b.getOperand1(), 0 ) + opStr +
					printFuncNodes( b.getOperand2(), 0 ) );		
		}
		if( node instanceof IASTCompoundStatement )
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
		else
		{
			IASTNode [] arr = node.getChildren();
			
			StringBuilder temp = new StringBuilder();
			
			if( arr.length == 0)
			{
				try 
				{
					temp.append( "< " + node.getSyntax() +  " " + node.getClass().getName() + " SKIP>" );
				} 
				catch (ExpansionOverlapsBoundaryException e) 
				{
					temp.append( "< " + " SKIP>" );					
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
	
	
	public void analyazeIfStmt()
	{
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
	
	public boolean isImpBooleanCondition( IASTNode node )
	{
		boolean res = true;
		
		//CASTIdExpression c;
		//need to check if CASTIdExpression is of a recognized Pointer or null field
		
		
		if( node instanceof IASTFieldReference )
		{
			String fldName = ((IASTFieldReference) node).getFieldName().toString();
			
			if( ! isNextField( fldName ) )
			{
				res = false;
			}
		}
		else
		{
			IASTNode [] arr = node.getChildren();
						
			for( int i = 0 ; i < arr.length && res ; ++i )
			{			
				res = isImpBooleanCondition( arr[i] );				
			}
		}		

		return res;
	}
	
	public static void main( String [] args )
	{				
		String [] interestingFuncs = { "my_find" };
		String [] nextFields = { "next" };

		
		//ASTBuilder temp = new ASTBuilder( "/home/tomerwei/workspace/CDTLatch/workfiles/thttpd-2.25b/thttpd.c" );
		ASTBuilder temp = new ASTBuilder( 
				"/home/tomerwei/workspace/CDTLatch/workfiles/thttpd-2.25b/demo_linked_lists.c",
				interestingFuncs,
				nextFields );		
	}
}

