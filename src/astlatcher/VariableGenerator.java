package astlatcher;

import java.util.HashSet;

public class VariableGenerator {
	
	static          int                 nextVarIdx      = 1;		
	private static  String              varPrefix       = "x_";
	private static  HashSet < String >  globalVarTable  = new HashSet< String >();
		
	/*
	VariableGenerator( )
	{
		this.globalVarTable = new HashSet< String >();
		this.nextVarIdx     = 1;
	}
	*/
	
	public  static String nextVarNameGet( )	
	{
		boolean gotNewName = false;
		String nextVarName =  "";
		
		while( !gotNewName )
		{
			nextVarName    =  varPrefix + nextVarIdx;			
			nextVarIdx     =  nextVarIdx + 1;			
			gotNewName     =  nextVarNameSet( nextVarName );		
		}
		
		ASTBuilder.symbolTableAdd( nextVarName );
		
		return nextVarName;		
	}
	
	
	public  static String nextVarNameGet( String prefix )	
	{
		boolean gotNewName = nextVarNameSet( prefix );
		String nextVarName = gotNewName? prefix : "";
		
		while( !gotNewName )
		{
			nextVarName    =  prefix + nextVarIdx;			
			nextVarIdx     =  nextVarIdx + 1;			
			gotNewName     =  nextVarNameSet( nextVarName );		
		}
		
		ASTBuilder.symbolTableAdd( nextVarName );
		
		return nextVarName;		
	}	
	
	
	private static boolean nextVarNameSet( String varName )
	{
		boolean isSet = false;
		
		if( varName.length() > 0 && !globalVarTable.contains( varName ) )
		{
			globalVarTable.add( varName );
			isSet = true;
		}
		
		return isSet;
	}	

}
