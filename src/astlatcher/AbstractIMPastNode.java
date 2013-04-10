package astlatcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;

abstract public class AbstractIMPastNode implements IMPastNode {

	List <IMPastNode>  children;
	IMPastNode         parent;
	
	public AbstractIMPastNode( IMPastNode parent )
	{
		this.children  =  new ArrayList <IMPastNode>();
		this.parent    =  parent;
	}
			
	abstract public void initNode( IASTNode node );
	
	public IMPastNode nodeParentGet( )
	{
		return parent;
	}
	
	public void nodeParentSet( IMPastNode parent )
	{
		this.parent = parent;
	}

	
	public IMPastNode nodeParentGet(IMPastNode node) 
	{		
		return parent;
	}

	
	public List<IMPastNode> nodeChildrenGet(IMPastNode node) 
	{	
		return children;
	}

	
	public void nodeChildrenAppend(IMPastNode node) 
	{
		children.add( node );		
	}
	
	
	

}
