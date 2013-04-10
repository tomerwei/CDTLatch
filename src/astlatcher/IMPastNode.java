package astlatcher;

import java.util.List;

public interface IMPastNode {
	
	/**Returns parent node
	 * Will return null if parent is root
	 * @param node
	 * @return
	 */
	public IMPastNode nodeParentGet( );
	
	
	public void nodeParentSet( IMPastNode parent );

	
	/**Returns node's children
	 * 
	 * @param node
	 * @return
	 */
	public List<IMPastNode> nodeChildrenGet( IMPastNode node );
	
	
	
	/**Adds child to node's list
	 * 
	 * @param node
	 * @return
	 */	
	public void nodeChildrenAppend( IMPastNode node );

	
	
	public String prettyPrint( int indent );
}
