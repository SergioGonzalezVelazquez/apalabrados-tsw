package apalabrados.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface LetterDistribution {
	
	public static final Map<Character, Integer> LETTER_VALUE =
			Collections.unmodifiableMap(new HashMap<Character, Integer>()
			{
			{
			put('A', 1);
			put('B', 3);
			put('C', 3);
			put('D', 2);
			put('E', 1);
			put('F', 4);
			put('G', 2);
			put('H', 4);
			put('I', 1);
			put('J', 8);
			put('L', 1);
			put('M', 3);
			put('N', 1);
			put('Ñ', 8);
			put('O', 1);
			put('P', 3);
			put('Q', 5);
			put('R', 1);
			put('S', 1);
			put('T', 1);
			put('U', 1);
			put('V', 4);
			put('X', 8);
			put('Y', 4);
			put('Z', 10);
			}
			});
	
	public static final Map<Character, Integer> LETTER_QUANTITY =
			Collections.unmodifiableMap(new HashMap<Character, Integer>()
			{
			{
			put('A', 12);
			put('B', 2);
			put('C', 4);
			put('D', 5);
			put('E', 12);
			put('F', 1);
			put('G', 2);
			put('H', 2);
			put('I', 6);
			put('J', 1);
			put('L', 4);
			put('M', 2);
			put('N', 5);
			put('Ñ', 1);
			put('O', 9);
			put('P', 2);
			put('Q', 1);
			put('R', 5);
			put('S', 6);
			put('T', 4);
			put('U', 5);
			put('V', 1);
			put('X', 1);
			put('Y', 1);
			put('Z', 1);
			}
			});
	
}



