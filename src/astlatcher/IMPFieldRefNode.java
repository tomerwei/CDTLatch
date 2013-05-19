package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPFieldRefNode extends IMPastNodeSimplify{

	private String   name;
	private String   fieldName;
	private String   owner;
	
	
	
	public IMPFieldRefNode( IMPastNode parent ) 
	{
		super(parent);
	}

	
	@Override
	public void initNode( IASTNode node ) 
	{
		//TODO: checkout h->next->next;
		IASTFieldReference stmt =  ( IASTFieldReference )node;		
		this.fieldName          =  stmt.getFieldName().toString();
		this.owner              =  stmt.getFieldOwner().getRawSignature();				
				            
		//this.isNextField      =  ASTBuilder.isNextField( fieldName );
				
		if( isNextField() )
		{
			this.fieldName      = ASTBuilder.nextField;
		}
		else
		{
			ASTBuilder.valueFieldNameAdd( fieldName );
		}
		
		this.name               =  owner + "." + fieldName;
	}
	
	
	public String ownerGet()
	{
		return "" + owner + "";
	}
	
	
	@Override 
	public String toString() 
	{
		return name;
	}
	
	
	public boolean isNextField()
	{
		return ASTBuilder.isNextField( fieldName );
	}


	@Override
	public boolean isSimple() 
	{	
		boolean isNextField = isNextField();
		
		return isNextField;
	}


	public String fieldNameGet()
	{
		return fieldName;
	}
	
	
	@Override
	public void simplify(IMPastNode exprTopParent) 
	{
		//do nothing.
	}

	
	public String prettyPrint( int indent ) 
	{		
		return owner + "." + fieldName; 				
	}
	
	
	public String prettyPrintAST( int indent ) 
	{
		
		return this.toString(); 				
	}
	
}


