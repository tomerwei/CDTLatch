package astlatcher;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class IMPCommentNode extends AbstractIMPastNode {

	public IMPCommentNode(IMPastNode parent) 
	{
		super(parent);
	}


	private String comment;
	
	
	public String commentGet()
	{
		return comment;
	}
	
	@Override
	public void initNode( IASTNode node )
	{
		this.comment = "# " + node.getRawSignature() + "";		
	}
	
	@Override
	public String toString()
	{
		return "";		
	}
	
	
}
