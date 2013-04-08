package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPFieldRefNode extends IMPastNodeSimplify{

	private String   name;
	private String   fieldName;
	private String   owner;
	private boolean  isNextField;
	
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
		this.name               =  owner + "." + fieldName;		            
		this.isNextField        =  ASTBuilder.isNextField( fieldName );	
	}
	
	
	@Override 
	public String toString() 
	{
		return name;
	}
	
	public boolean isNextField()
	{
		return isNextField;
	}


	@Override
	public boolean isSimple() {		
		return isNextField();
	}


	@Override
	public void simplify(IMPastNode exprTopParent) {
		
	}


}