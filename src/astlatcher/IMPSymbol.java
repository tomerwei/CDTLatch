package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;

public class IMPSymbol {
	
	public   static  String  impBoolType =  "bool";
	public   static  String  cBoolType   =  "bool";
	public   static  String  impVarType  =  "v";
	
	private  String          impType;
	private  String          cType;
	private  String          name;
	private  String          originalDec;
	private  IASTInitializer initNode;
	
	public IMPSymbol()
	{
		this.impType      =  "";
		this.cType        =  "";
		this.name         =  "";
		this.originalDec  =  "";
		this.initNode     =  null;
	}
	
	public String nameGet()
	{
		return name;			
	}
		
	public void nameSet( String name )
	{
		this.name = name;

	}
	
	public String cTypeGet( )
	{
		return cType;
	}

	
	private void fillFields( IASTNode node )
	{
		if( node instanceof ICASTDeclSpecifier )
		{
			String cType = node.getRawSignature();

			cTypeSet( cType );
		}
		else if( node instanceof IASTDeclarator )
		{
			IASTDeclarator n = (IASTDeclarator)node;
			
			this.name      = n.getName().toString();
			this.initNode  = n.getInitializer();
		}
		else
		{
			IASTNode [] arr = node.getChildren();
			
			if( arr.length == 0)
			{
				//??
			}
						
			for( int i = 0 ; i < arr.length ; ++i )
			{			
				fillFields( arr[i] );			
			}			
		}		
	}
	
	
	public IASTInitializer initNodeGet()
	{
		return  initNode;
	}	
	

	/**Checks If variable is of type 'v', and has a initializer command.
	 * @return
	 */
	public boolean needsInitCmd()
	{
		return  impType.equals( impVarType ) && initNode != null;
	}
	
	
	/**Checks if we can create initilizing cmd
	 * 
	 * @return
	 */
	public boolean canInit()
	{
		boolean          res  =  false;
		IASTInitializer  i    =  initNode;
		
		if( i != null )
		{
			res = i instanceof IASTEqualsInitializer;

		}
		else
		{
			System.out.println( "Cannot init. initializer is of type " + i.getClass().toString() );
		}
			
		return res;		
	}
	
	
	//node is of type IASTParameterDeclaration or IASTDeclarationStatement
	public void initSymbol( IASTNode node )
	{
		this.originalDec = node.getRawSignature();		

		fillFields( node );
	}
	
	public void cTypeSet( String type )
	{
		this.cType = type;
		
		if( type.equals( cBoolType ) )
		{
			impTypeSet( impBoolType );
		}
		else 
		{
			impTypeSet( impVarType );
		}
	}

	public String impTypeGet( )
	{
		return impType;
	}
	
	public void impTypeSet( String type )
	{
		this.impType = type;
	}
	
	@Override
	public String toString()
	{
		return impType + ",  " + cType + ",  " + name + ",  " + originalDec;	
	}
	
	public String[] toStringArray()
	{
		String []result = new String[]{impType, cType,name,originalDec };
		return result;
	}
	
	public static int numOfFieldsGet()
	{
		return fieldNamesGet().length;
	}
	
	public static String[] fieldNamesGet()
	{
		return new String[]{"IMP Type", "C Type", "Variable name", "Original Decleration"};
	}	
	
}
