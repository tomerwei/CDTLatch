package astlatcher;

import java.util.HashSet;

public class VariableGenerator {
	
	private HashSet< String >  globalVarTable;
	int                         nextVarIdx;
	private static  String    varPrefix = "x_";
		
	VariableGenerator( )
	{
		this.globalVarTable = new HashSet< String >();
		this.nextVarIdx     = 1;
	}
	
	public  String nextVarNameGet()	
	{
		boolean gotNewName = false;
		String nextVarName =  "";
		
		while( gotNewName )
		{
			nextVarName       = varPrefix + nextVarIdx;
			
			this.nextVarIdx   = nextVarIdx + 1;
			
			gotNewName        = nextVarNameSet( nextVarName );		
		}
		
		return nextVarName;		
	}
	
	private  boolean nextVarNameSet( String varName )
	{
		boolean isSet = false;
		
		if( !globalVarTable.contains( varName ) )
		{
			globalVarTable.add( varName );
			isSet = true;
		}
		
		return isSet;
	}
	

}
