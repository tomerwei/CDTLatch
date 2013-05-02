package astlatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SymbolTable {
	
	private List<IMPSymbol>      symbols;
	private HashSet <String>     symbolTableVars;
	
	
	public SymbolTable()
	{
		this.symbols          =  new ArrayList<IMPSymbol> ();
		this.symbolTableVars  =  new HashSet <String>(); 
	}
	
	
	public void symbolAdd( IMPSymbol s )
	{
		symbols.add( s );
		symbolTableVars.add( s.nameGet() );
	}
	
	
	//TODO
	public boolean containsSymbol( IMPSymbol s )
	{
		return false;
	}
	
	
	private String[][] to2dTable()	
	{
		int rows     =  symbols.size()+1;
		int columns  =  IMPSymbol.numOfFieldsGet();
		
		String [][]result = new String[rows][columns];
		
		result[0] = IMPSymbol.fieldNamesGet();
		
		int i = 1;
		for( IMPSymbol s : symbols )
		{
			result[i] = s.toStringArray();
			i++;			
		}		
				
		return result;
	}
	
	//DUMMY implementation
	@Override
	public String toString()
	{
		String [][]table = to2dTable();
		
		final SymbolTablePrettyPrinter printer = new SymbolTablePrettyPrinter(System.out);
		printer.print(table);		
		return "";		
	}

}
