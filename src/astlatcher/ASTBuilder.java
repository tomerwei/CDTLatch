package astlatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;

public class ASTBuilder extends org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage {
		
	private String                      pathToFile;
	private HashSet<String>             funcs;
	private StringBuilder               output;
	private VariableGenerator           varGen;
	private static HashSet <String>     nextFields;
	private static HashMap <String, IASTFunctionDefinition > funcDecs;
	public static String                CNullConstant          = "null";
	public static String                IMPSkipCmd             = "skip";
	public static String                nextField              = "n";
	public static SymbolTable           symbolTable;
	public static HashSet<String>       valueFieldNames;                  
	 
	
	
	public ASTBuilder( String filename, String [] funcs, String [] nextFlds, String [] ptrs ) 
	{
		super();
		
		this.pathToFile           =  filename;		
		this.output               =  new StringBuilder();
		this.varGen               =  new VariableGenerator();
		
		initPointers( ptrs, funcs );
		initNextFields( nextFlds );
		ASTinit();
		interestingFuncsProcess();
		ASTOutput();		
	}
	
	
	private void initPointers( String [] ptrs, String [] funcsArr )
	{		
		symbolTable         =  new SymbolTable();
		funcDecs            =  new HashMap <String, IASTFunctionDefinition >();
		valueFieldNames     =  new HashSet <String>();
		this.funcs          =  new HashSet <String>();

		for( String f : funcsArr )
		{
			funcs.add( f );
		}
		
		/*
		symbolTableVars     =  new HashSet<String>();		
		symbolTableVars.add( CNullConstant );
		
		for( String p : ptrs )
		{
			symbolTableVars.add( p );			
		}
		*/
	}
	
	
	public static void valueFieldNameAdd( String fieldName )
	{
		valueFieldNames.add( fieldName );
	}

	
	
	/*	
	"data: V * V -> bool\n" +
	"[wp x.data:=y](x, _ , y, Q) := x!=null & dr( data(u,v):= (u = x & v = y ) | u != x & data(u,v) , Q)\n"+
	"[wp x:=y.data](x, y, _, Q)  := y!=null & forall z (data(y,z) -> dr(x:=z, Q)) &\n"+ 
	                               "(forall z (~data(y,z) ) -> dr(x:=null, Q ) )\n"+

	"forall x y z (data(x,y) & data(x,z) -> y=z)\n"+
	"forall z (~data(null,z));\n";
	*/		
	public String valueFieldsDefGet()
	{
		
		StringBuilder sb = new StringBuilder();
		
		for( String s : valueFieldNames )
		{
			sb.append(  s + ": V * V -> bool\n" );
			sb.append( "[wp x."+ s + ":=y](x, _ , y, Q) := x!=null & dr( "+ s + "(u,v):= " +			
					"(u = x & v = y ) | u != x & "+ s +"(u,v) , Q)\n" );
			
			sb.append("[wp x:=y."+s+"](x, y, _, Q)  := y!=null & forall z ("+s+"(y,z) -> dr(x:=z, Q)) &\n"+ 
                      "                              (forall z (~"+s+"(y,z) ) -> dr(x:=null, Q ) )\n" );
						
			sb.append( "forall x y z ( "+ s + "(x,y) & "+s+"(x,z) -> y=z)\n" );
			
			sb.append( "forall z (~"+ s +"(null,z))\n" );
		}
		
		return sb.toString();
	}
	
	
	private String variableDefsGet()
	{
		StringBuilder sb = new StringBuilder();
		
		for( String var : symbolTable.symbolsIteratorGet() )
		{
			sb.append( var + " : V\n" );
		}
				
		return sb.toString();
	}
	
	public static void symbolTableAdd( String varName )		
	{
		IMPSymbol s = new IMPSymbol();
		
		s.nameSet( varName );
		s.impTypeSet( IMPSymbol.impVarType );
		
		symbolTable.symbolAdd( s);
	}
	
	
	public static void symbolTableAdd( IMPSymbol s )
	{
		symbolTable.symbolAdd( s);
	}

	
	public static String nullStrGet()
	{
		return  CNullConstant ;
	}	

	
	private void interestingFuncsProcess()
	{		
		Set <String> funcs =  funcDecs.keySet();
		
		for( String funcName : funcs )
		{
			
			boolean toProcess = isFuncIntersting( funcName );
			
			if( toProcess )
			{
				
				IASTFunctionDefinition dec = funcDecs.get( funcName );
				
				output.append( "# " + funcName + "():\n");
				
				System.out.println( "AST output" );
				System.out.println( "----------\n" );
				
				IMPcompoundStmtNode  par      =  new IMPcompoundStmtNode( null );
				IMPastNode           check    =  IMPParseStmt( dec, par );
				
				System.out.println( variableDefsGet() );
				
				System.out.println( valueFieldsDefGet() );
						
				visitIMPastNode( par );				
				//System.out.println( par.prettyPrint( 0 ) );
				
				System.out.println( "" );
				
				System.out.println( "Symbol Table" );
				System.out.println( "------------\n" );
				
				symbolTable.toString();
				
				//System.out.println( "PrettyPrint AST:");
				//System.out.println( "----------------");
				
				//System.out.println( "ast[(" + par.prettyPrintAST( 0 ) + ")]");
			}
		}
	}
	
	
	public static IASTFunctionDefinition funcDeclerationGet( String name )
	{
		return funcDecs.get( name );
	}
	
	
	private void initNextFields( String[] nextFlds ) 
	{		
		nextFields           =  new HashSet<String>();

		for( String s : nextFlds )
			nextFields.add( s );
	}
	
	public static boolean isNextField( String fldName )
	{
		return nextFields.contains( fldName );
	}
	
	/*
	private static boolean isImpCompatiblePointer( String varName )
	{
		return symbolTableVars.contains( varName );
	}	
	*/

	private void ASTOutput() 
	{		
		//System.out.println( output.toString() + "\n" );		
	}
	
	private void ASTinit()
	{
		FileContent fc = FileContent.createForExternalFileLocation( pathToFile );
		
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
					
					//System.out.print( b.getName() +" " + b.getClass().getName()  +"\n" );
					
					IType type = ( b instanceof IFunction ) ? 
									( (IFunction) b).getType() : null;
					
					if( type != null )
					{
						//check if funcsNames contain this function						
						//if( name.toString().equals( funcs[0] ) )
						if( funcs.contains( name ) )							
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
						String funcName = ( (IASTFunctionDefinition) dec).getDeclarator().getName().toString();
						
						funcDecs.put( funcName ,(IASTFunctionDefinition) dec );						
					}				
					
					return ASTVisitor.PROCESS_CONTINUE;
				}
			};
			//visitor.shouldVisitNames = true;
			visitor.shouldVisitDeclarations = true;
			tu.accept( visitor );
		} 
		catch( CoreException e ) 
		{
			e.printStackTrace();
		}	
	}
	
	
	public static void visitIMPastNode( IMPastNode node )
	{
		System.out.println( "ast([" + node + "])" );
		
		/*
		for( IMPastNode n : node.nodeChildrenGet(node) )
			visitIMPastNode( n );
		*/
	}
	
	
	public static 
	IASTFunctionDefinition funcDeclerationGet( IASTFunctionCallExpression funcCall )
	{
		IASTFunctionDefinition res = null;
		
		//TODO?
		
		return null;			
	}
	
	
	private boolean isFuncIntersting( String name )
	{
		boolean res = funcs.contains( name );
		
		return res;
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
		
	
	public static String indentTabGet( int indent )
	{
		String result = "";
		
		if( indent > 0 )
		{
		
			StringBuilder temp = new StringBuilder();
		
			temp.append("");
		
			for( int i = 0 ; i < indent ; ++i )
				temp.append("  ");
		
			result = temp.toString();
		}
		
		return result;
	}
	
	public static String impOperandGet( int op )
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

	
	public static IMPastNodeSimplify IMParseExpr( IASTNode node, IMPastNode parent )
	{
		IMPastNodeSimplify result = null;
		
		//System.out.println( "Simplify: " + node.getRawSignature() + " " + node.getClass().getName() );
		
		if( node instanceof IASTIdExpression )
		{
			IMPidNode n = new IMPidNode( parent );
			n.initNode(node );
			
			result = n;							
		}
		else if( node instanceof IASTLiteralExpression )
		{
			IMPliteralNode n = new IMPliteralNode( parent );
			n.initNode(node );
			
			result = n;					
		}
		else if( node instanceof IASTFieldReference )
		{
			IMPFieldRefNode n = new IMPFieldRefNode( parent );
			n.initNode(node );
			
			result = n;							
		}		
		else if( node instanceof IASTBinaryExpression )
		{
			//we need to simplify the binary expression...
			IMPexprNode n = new IMPexprNode( parent );
			n.initNode(node );
			
			result = n;							
		}
		else if( node instanceof IASTInitializer )
		{
			IASTInitializer n    =  (IASTInitializer)node;
			IASTNode      []arr  =  n.getChildren();
			
			if( arr.length == 1 )
			{
				return IMParseExpr( arr[0], parent );
			}
			else
			{
				System.out.println( "Not expected, more than 1 child in initializer " 
			             + node.getRawSignature() + " " + node.getClass().getName() );
			}
		}
		//TODO:
		//should be removed later --> processing function calls should be different
		//e.g. by calling the function for inlining.
		else if( node instanceof IASTFunctionCallExpression )
		{
			IASTFunctionCallExpression fCall = (IASTFunctionCallExpression)node;
			
			IMPidNode n = new IMPidNode( parent );
			n.initNode( fCall.getRawSignature()  );
			
			result = n;							
		}		
		//IASTFunctionCallExpression
		else 
		{
			System.out.println( "Not expected: " + node.getRawSignature() + " " + node.getClass().getName() );			
		}

		return result;
	}
	
	
	public static IMPastNode IMPParseStmt( IASTNode node, IMPastNode parent )
	{
		IMPastNode result = null;
	
		//debug
		/*
		try 
		{
			System.out.println( "[ " + node.getChildren().length + " " + 
					node.getSyntax().toString() + " " + node.getClass().getName() + " ]" );
		} 
		catch( ExpansionOverlapsBoundaryException e1 ) 
		{		
			System.out.println( "[ noSyntax " + node.getClass().getName() + " ]" );
		}
		*/
					
		if( node instanceof IASTDeclarationStatement ||
			node instanceof IASTParameterDeclaration )
		{
			/**
			 * Variables are declared in statements in the function or function input
			 * Otherwise they are global or static variables
			 */
			IMPSymbol s = new IMPSymbol();
			s.initSymbol(node);
			
			symbolTable.symbolAdd( s );
			
			if( s.needsInitCmd() && s.canInit() )
			{
				
				IMPcmdNode n = new IMPcmdNode( parent );
				n.initNode( s );
				
				result = n;								
			}
		}		
		
		if( node instanceof IASTBinaryExpression )
		{
			IMPcmdNode n = new IMPcmdNode( parent );
			n.initNode(node );
			
			result = n;							
		}
		else if( node instanceof IASTIfStatement )
		{
			IMPIfNode n = new IMPIfNode( parent );
			n.initNode(node );
			
			result = n;				
		}		
		else if( node instanceof IASTWhileStatement )
		{					
			IMPWhileNode n = new IMPWhileNode( parent );
			n.initNode(node );
			
			result = n;				
		}		
		else if( node instanceof IASTBinaryExpression )
		{			
			IMPcmdNode n = new IMPcmdNode( parent );
			n.initNode(node );
			
			result = n;			
		}
		/*
		else if( result == null //TODO fix this later, we want a comment node
		         node instanceof IASTReturnStatement      ||
				 node instanceof IASTDeclarationStatement ||
				 node instanceof IASTParameterDeclaration )
		{				
			
			IMPCommentNode n = new IMPCommentNode( parent );
			n.initNode(node );
			
			result = n;			
		}		
		*/
		else if( node instanceof IASTCompoundStatement )
		{
			IMPcompoundStmtNode n = new IMPcompoundStmtNode( parent );
			n.initNode(node );
			
			result = n;						
		}
		else// what to do here
		{
			
			IASTNode [] arr = node.getChildren();
			
			if( arr.length == 0)
			{
				//??
			}
						
			for( int i = 0 ; i < arr.length ; ++i )
			{			
				IMPastNode child = IMPParseStmt( arr[i] , parent );
				
				if( child != null )
				{
					parent.nodeChildrenAppend( child );
				}
			}			
		}
		
		return result;
	}
		
	
	public static void main( String [] args )
	{
		String [] interestingFuncs = { "my_find" , "my_delete" };
		String [] nextFields       = { "next" };
		String [] ptrs             = { "t", "j", "i" };
		
		//ASTBuilder temp = new ASTBuilder( "/home/tomerwei/workspace/CDTLatch/workfiles/thttpd-2.25b/thttpd.c" );
		ASTBuilder temp = new ASTBuilder( 
				"/home/tomerwei/workspace/CDTLatch/workfiles/examples/delete.c",
				interestingFuncs,
				nextFields,
				ptrs );		
	}
}

