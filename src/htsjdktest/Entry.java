/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htsjdktest;

/**
 *
 * @author asenf
 */
public class Entry implements Comparable<Entry> {
    private String chromosomeName;
    private int start, end;
    
    public Entry(String chromosomeName, int start, int end) {
        this.chromosomeName = chromosomeName;
        this.start = start;
        this.end = end;
    }
    
    public String getChromosomeName() {
        return this.chromosomeName;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public int getEnd() {
        return this.end;                
    }

    @Override
    public String toString() { 
        return String.format(this.getChromosomeName() + ": " + this.start + "-" + this.end); 
    }
    
    /*
     * Sort by chromosome first, then by start within chromosome
     */
    @Override
    public int compareTo(Entry secondEntry) {
        if (this.getChromosomeName().equalsIgnoreCase(secondEntry.getChromosomeName())) {
            return Integer.compare(this.getStart(), secondEntry.getStart());
        } else {
            return this.getChromosomeName().compareTo(secondEntry.getChromosomeName());
        }
    }
	
}
