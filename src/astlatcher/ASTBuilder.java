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
	
	public ASTBuilder( String filename, String [] funcs, String [] nextFlds ) 
	{
		super();
		
		this.pathToFile =  filename;
		this.funcs      =  funcs;
		this.output     =  new StringBuilder();
		this.nextFields =  new HashSet<String>();
		
		AddNextFields( nextFlds );
		ASTinit();	
		ASTOutput();
	}
	
	private void AddNextFields(String[] nextFlds) 
	{
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
			
			
			System.out.println( tu.getDeclarations().length );

			/*
			IASTDeclaration [] dec = tu.getDeclarations();
			
			for( int i = 0; i < dec.length ; ++i )
			{
				System.out.println( "II\n" + dec[i].getSyntax().getOffset() );
				
				System.out.println( "XX\n" + dec[i].getRawSignature() );
			}
			*/
			
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
							printASTNodes( "", dec );							
							output.append( processASTNodes( dec ) );
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
			// TODO Auto-generated catch block
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
			
			temp.append( processASTNodes(((IASTFieldReference) node).getFieldOwner()) );
			
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
				String cur =  processASTNodes( arr[i] );
				
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
			  temp.append( "<SKIP>" );
						
			for( int i = 0 ; i < arr.length ; ++i )
			{
				//temp.append( i + " " + node.getClass().getName() + " \t" + processASTNodes( arr[i] )  + "\n" );
				String cur =  processASTNodes( arr[i] );
				
				temp.append( "" + processASTNodes( arr[i] )  + "" );				
			}
			
			if( node instanceof IASTStatement )
				temp.append( ";" );
			
			return temp.toString();
		}
		
		/* never gets here*/
		//return "";
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

