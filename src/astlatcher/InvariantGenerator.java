package astlatcher;

import java.util.HashSet;

public class InvariantGenerator {

	static          int                 nextVarIdx      = 1;		
	private static  String              varPrefix       = "I";
	private static  HashSet < String >  globalInvariantTable  = new HashSet< String >();
		

	public  static String nextVarNameGet()	
	{
		boolean gotNewName = false;
		String nextVarName =  "";
		
		while( !gotNewName )
		{
			nextVarName    =  varPrefix + nextVarIdx;			
			nextVarIdx     =  nextVarIdx + 1;			
			gotNewName     =  nextVarNameSet( nextVarName );		
		}
		
		return nextVarName;		
	}
	
	public  static String nextVarNameGet( String prefix )	
	{
		boolean gotNewName = false;
		String nextVarName =  "";
		
		while( !gotNewName )
		{
			nextVarName    =  prefix + nextVarIdx;			
			nextVarIdx     =  nextVarIdx + 1;			
			gotNewName     =  nextVarNameSet( nextVarName );		
		}
		
		return nextVarName;		
	}	
	
	private static boolean nextVarNameSet( String varName )
	{
		boolean isSet = false;
		
		if( !globalInvariantTable.contains( varName ) )
		{
			globalInvariantTable.add( varName );
			isSet = true;
		}
		
		return isSet;
	}
}
	