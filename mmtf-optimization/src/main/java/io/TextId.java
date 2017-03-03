package io;

import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class TextId {
	public TextId(String s) {
		
	}	
	public static void main(String[] args) {
		
		char [] a = {'c', 'l'};
		char [] b = {'f', 'e'};
		String sa = "cld";
		String sb = "fed";
		
		long n = 100000000L;
		Timer.start();
		for (long i = 0 ; i < n; i++) {
			a.equals(a);
			a.equals(b);
		}
		Timer.stop();
		System.out.println(Timer.get());
		
		Timer.start();
		for (long i = 0 ; i < n; i++) {			
			sa.equalsIgnoreCase(sa);
			sa.equalsIgnoreCase(sb);		
		}
		Timer.stop();
		System.out.println(Timer.get());
		
		Timer.start();
		for (long i = 0 ; i < n; i++) {			
			sa.equals(sa);
			sa.equals(sb);		
		}
		Timer.stop();
		System.out.println(Timer.get());
		
		System.out.println(a.equals(a));
	}
}
